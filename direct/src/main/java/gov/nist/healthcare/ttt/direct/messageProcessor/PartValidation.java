package gov.nist.healthcare.ttt.direct.messageProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.util.Store;

import gov.nist.healthcare.ttt.database.log.DetailInterface.Status;
import gov.nist.healthcare.ttt.database.log.PartInterface;
import gov.nist.healthcare.ttt.direct.directValidator.DirectMessageValidator;
import gov.nist.healthcare.ttt.direct.directValidator.DirectMimeEntityValidator;
import gov.nist.healthcare.ttt.direct.directValidator.DirectSignatureValidator;
import gov.nist.healthcare.ttt.model.logging.DetailModel;
import gov.nist.healthcare.ttt.model.logging.PartModel;

public class PartValidation {
	
	private Logger logger = Logger.getLogger(PartValidation.class.getName());
	
	private DetailedPartValidation detailedpartValidation = new DetailedPartValidation();
	private DirectMessageValidator directMessageValidator = new DirectMessageValidator();
	private DirectMimeEntityValidator mimeEntityValidator = new DirectMimeEntityValidator();
	private DirectSignatureValidator signatureValidator = new DirectSignatureValidator();
	
	private boolean wrapped;
	private boolean hasError;
	
	public PartValidation(boolean wrapped) {
		this.wrapped = wrapped;
		this.hasError = false;
	}
	
	public void processMainPart(PartModel part) throws Exception {
		Part p = part.getContent();
		
		
		// MIME Entity Validation
		detailedpartValidation.validateMimeEntity(part);
		
		// If this is a message part (outer envelope or message/rfc822)
		if(p instanceof MimeMessage) {
			this.processEnvelope(part, this.wrapped);
		}
		
		if(part.hasParent()) {
			if(p.isMimeType("multipart/signed")) {    // If this is multipart
				convertContentToBlob(part, p);
				this.validateMultipartSigned(part);
			} else if(p.isMimeType("multipart/*")) {
				this.processMultipart(part);
			} else if(p.isMimeType("message/rfc822")) {
				// DTS 151, Validate First MIME Part Body
				part.addNewDetailLine(directMessageValidator.validateFirstMIMEPartBody(true));
			} else if(p.isMimeType("application/pkcs7-signature") || p.isMimeType("application/x-pkcs7-signature")) {
				// Validate signature Part
				detailedpartValidation.validateSignaturePart(part);
				this.processLeafPart(part);
			} else {
				this.processLeafPart(part);
				convertLeafContentToBlob(part, p);
			}
		}
		
		if(!part.isStatus()) {
			this.hasError = true;
		}
		
		if(part.hasChild()) {
			Iterator<PartInterface> it = part.getChildren().iterator();
			while(it.hasNext()) {
				this.processMainPart((PartModel) it.next());
			}
		}
	}
	
	/**
	 * Validates the envelope of the message
	 * @throws Exception 
	 * */
	public void processEnvelope(PartModel part, boolean wrapped) throws Exception {
		Part p = part.getContent();
		
		try {
			// Validation Message Headers
			detailedpartValidation.validateMessageHeader(part, wrapped);

		} catch (Exception e) {
			logger.error(e.getMessage());
			part.addNewDetailLine(new DetailModel("No DTS", "Message file", "Problem parsing message file", "", "", Status.ERROR));
			throw e;
		}

		if(p.isMimeType("application/pkcs7-mime") || p.isMimeType("application/x-pkcs7-mime")) {

			// DTS 133a, Content-Type
			part.addNewDetailLine(directMessageValidator.validateMessageContentTypeA(part.getContentType()));

			// DTS 201, Content-Type Name
			part.addNewDetailLine(directMessageValidator.validateContentTypeNameOptional(part.getContentType()));

			// DTS 202, Content-Type S/MIME Type
			part.addNewDetailLine(directMessageValidator.validateContentTypeSMIMETypeOptional(part.getContentType()));

			// DTS 203, Content-Disposition
			part.addNewDetailLine(directMessageValidator.validateContentDispositionOptional(part.getContentDisposition()));

			// DTS 161-194 Validate Content-Disposition Filename
			part.addNewDetailLine(mimeEntityValidator.validateContentDispositionFilename(getFilename(part.getContentDisposition())));
			
			// Write the blob in the part
			convertContentToBlob(part, p);
		}
	}
	
	public void processMultipart(PartModel part) throws Exception {
		// Part p = part.getContent();
		
		// DTS 151, Validate First MIME Part Body
		part.addNewDetailLine(directMessageValidator.validateFirstMIMEPart(true));
		
		// DTS 152, Validate Second MIME Part
		part.addNewDetailLine(directMessageValidator.validateSecondMIMEPart(true));
		
		// DTS 155, Validate Content-Type
		// part.addNewDetailLine(directMessageValidator.validateContentType2(p.getContentType()));
	}

	public void validateMultipartSigned(PartModel part) throws Exception {
		Part p = part.getContent();
		
		// DTS 129, Message Body
		part.addNewDetailLine(directMessageValidator.validateMessageBody(true));

		// Validate inner message
		detailedpartValidation.validateDirectMessageInnerDecryptedMessage(part);

		SMIMESigned s = new SMIMESigned((MimeMultipart)p.getContent());

		// Find micalg
		String micalg = p.getContentType().split("micalg=")[1];
		if(micalg.contains(";")) {
			micalg = micalg.split(";")[0];
		}

		// verify signature
		this.verifySignature(part, s, micalg);
	}
	
	public void processLeafPart(PartModel part) {
		
	}
	
	/**
	 * verify the signature (assuming the cert is contained in the message)
	 */
	@SuppressWarnings("rawtypes")
	private void verifySignature(PartModel part, SMIMESigned s, String contentTypeMicalg) throws Exception{

		// DTS-164, SignedData exists for the message
		part.addNewDetailLine(signatureValidator.validateSignedData(s.getSignedContent()));

		//
		// extract the information to verify the signatures.
		//

		//
		// certificates and crls passed in the signature
		//
		Store certs = s.getCertificates();

		//
		// SignerInfo blocks which contain the signatures
		//
		SignerInformationStore  signers = s.getSignerInfos();

		Collection c = signers.getSigners();
		Iterator it = c.iterator();

		String digestAlgOID = "";

		// DTS 167, SignedData.certificates must contain at least one certificate
		part.addNewDetailLine(signatureValidator.validateSignedDataAtLeastOneCertificate(c));

		//
		// check each signer
		//
		while (it.hasNext())
		{
			SignerInformation   signer = (SignerInformation)it.next();
			// Get digest Algorihm OID
			digestAlgOID = signer.getDigestAlgOID();
			Collection certCollection = certs.getMatches(signer.getSID());

			Iterator certIt = certCollection.iterator();
			X509Certificate cert = null;
			try {
				cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate((X509CertificateHolder)certIt.next());
			} catch (Exception e) {
				part.addNewDetailLine(new DetailModel("No DTS", "Certificate File", "Cannot extract the signing certificate", "", "-", Status.ERROR));
				throw e;
			}

			// DTS 158, Second MIME Part Body
			part.addNewDetailLine(directMessageValidator.validateSecondMIMEPartBody(""));

			// DTS 165, AlgorithmIdentifier.algorithm
			part.addNewDetailLine(signatureValidator.validateDigestAlgorithmDirectMessage(digestAlgOID, contentTypeMicalg));

			// DTS 166, SignedData.encapContentInfo
			part.addNewDetailLine(signatureValidator.validateSignedDataEncapContentInfo(new String(cert.getSignature())));

			// DTS 222, tbsCertificate.signature.algorithm
			part.addNewDetailLine(signatureValidator.validateTbsCertificateSA(cert.getSigAlgName()));
			// needs signer.getDigestAlgorithmID(); and compare the two (needs to be the same)

			// DTS 225, tbsCertificate.subject
			part.addNewDetailLine(signatureValidator.validateTbsCertificateSubject(cert.getSubjectDN().toString()));

			// DTS 240, Extensions.subjectAltName
			// C-4 - cert/subjectAltName must contain either rfc822Name or dNSName extension
			// C-5 cert/subjectAltName/rfc822Name must be an email address - Conditional
			part.addNewDetailLine(signatureValidator.validateExtensionsSubjectAltName(cert.getSubjectAlternativeNames()));

			// C-2 - Key size <=2048
			//msgValidator.validateKeySize(er, new String(cert.getPublicKey()));


			// -------how to get other extension fields:
			//-------  cert.getExtensionValue("2.5.29.17")

			// verify that the sig is valid and that it was generated
			// when the certificate was current
			part.addNewDetailLine(signatureValidator.validateSignature(cert, signer, BouncyCastleProvider.PROVIDER_NAME));

		}
	}
	
	public void convertContentToBlob(PartModel part, Part p) throws IOException, MessagingException {
		// Convert content
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		p.writeTo(out);
		String contentString = "";
		contentString = out.toString();
		part.setRawMessage(contentString);
	}
	
	public void convertLeafContentToBlob(PartModel part, Part p) throws IOException, MessagingException {
		if(p.getContent() instanceof String) {
			String content = (String) p.getContent();
			part.setRawMessage(content);
		} else {
			convertContentToBlob(part, p);
		}
	}
	
	public String getFilename(String contentDisposition) {
		String res = contentDisposition;
		if(contentDisposition.contains("filename=")) {
			res = contentDisposition.split("filename=")[1];
			res = res.split(";")[0];
		}
		return res;
	}

	public boolean isHasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

}
