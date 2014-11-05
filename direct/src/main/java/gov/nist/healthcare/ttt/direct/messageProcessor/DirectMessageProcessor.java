package gov.nist.healthcare.ttt.direct.messageProcessor;

import gov.nist.healthcare.ttt.database.log.LogInterface.Status;
import gov.nist.healthcare.ttt.direct.certificates.PrivateCertificateLoader;
import gov.nist.healthcare.ttt.direct.utils.ValidationUtils;
import gov.nist.healthcare.ttt.model.logging.DetailModel;
import gov.nist.healthcare.ttt.model.logging.LogModel;
import gov.nist.healthcare.ttt.model.logging.PartModel;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMEUtil;

public class DirectMessageProcessor {
	
	private Logger logger = Logger.getLogger(DirectMessageProcessor.class.getName());
	
	private InputStream directMessage;
	private InputStream certificate;
	private String certificatePassword;
	// Log object to fill with message validation
	private LogModel logModel;
	private PartModel mainPart;
	private boolean wrapped;
	private boolean isMdn;
	private String originalMessageId;
	
	public DirectMessageProcessor() {
		super();
		this.wrapped = false;
		this.isMdn = false;
		this.logModel = new LogModel();
		this.mainPart = new PartModel();
	}
	
	public DirectMessageProcessor(InputStream directMessage, InputStream certificate, String certificatePassword) {
		this.directMessage = directMessage;
		this.certificate = certificate;
		this.certificatePassword = certificatePassword;
		this.wrapped = false;
		this.isMdn = false;
		this.logModel = new LogModel();
		this.mainPart = new PartModel();
	}

	public void processDirectMessage() throws Exception {

		// Get the session variable
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props, null);

		// Get the MimeMessage object
		MimeMessage msg = new MimeMessage(session, this.directMessage);

		// Fill the log information
		fillLogModel(msg);

		// Process the message
		this.mainPart = processPart(msg, null);

		PartValidation validationPart = new PartValidation(this.wrapped);
		validationPart.processMainPart(this.mainPart);

		// Set status to error if the part contains error
		if (validationPart.isHasError()) {
			this.logModel.setStatus(Status.ERROR);
		}
	}

	public PartModel processPart(Part p, PartModel parent) throws Exception {
		if (p == null) {
			logger.info("Part is null");
			return null;
		}

		// Decode if quoted printable
		String encoding = "";
		encoding = ValidationUtils.getSingleHeader(p,
				"content-transfer-encoding");
		if (encoding.equals("quoted-printable")) {
			p = decodeQP(p.getInputStream());
		}

		// Add the child except if it is the first Part
		PartModel currentlyProcessedPart = fillPartModel(p, parent);
		if (parent != null) {
			parent.addChild(currentlyProcessedPart);
		}

		try {

			// If the Part is a Message then first validate the Envelope
			if (p instanceof MimeMessage) {
				logger.debug("Processing enveloppe of the message");

			}

			// Check if message/rfc822 (wrapped message)
			if (p.isMimeType("message/rfc822")) {
				logger.debug("Processing message/rfc822 part");
				this.wrapped = true;

				Object o = p.getContent();
				if (o instanceof Part) {
					logger.debug("message/rfc822 contains part");
					this.processPart((Part) o, currentlyProcessedPart);
				} else if (o instanceof MimeMultipart) {

					logger.debug("message/rfc822 is multipart");

					MimeMultipart mp = (MimeMultipart) p.getContent();

					int count = mp.getCount();
					for (int i = 0; i < count; i++) {
						this.processPart(mp.getBodyPart(i),
								currentlyProcessedPart);
					}
				}

			} else if (p.isMimeType("application/pkcs7-mime")
					|| p.isMimeType("application/x-pkcs7-mime")) {
				logger.debug("Processing application/pkcs7-mime");
				this.processPart(
						processSMIMEEnvelope(p, certificate,
								certificatePassword), currentlyProcessedPart);

			} else if (p.isMimeType("multipart/*")) {
				logger.debug("Processing part " + p.getContentType());

				MimeMultipart mp = (MimeMultipart) p.getContent();
				int count = mp.getCount();
				for (int i = 0; i < count; i++) {
					this.processPart(mp.getBodyPart(i), currentlyProcessedPart);
				}

			} else if (p.isMimeType("message/disposition-notification")) {
				this.isMdn = true;
				this.logModel.setMdn(true);
				ProcessMDN mdnProcessor = new ProcessMDN(p);
				mdnProcessor.validate(currentlyProcessedPart);
				this.setOriginalMessageId(mdnProcessor.getOriginalMessageId());
				logger.debug("Processing part " + p.getContentType());
			} else {

				// This is a leaf part
				logger.debug("Processing part " + p.getContentType());
			}
			
			
		} catch (Exception e) {
			currentlyProcessedPart
					.addNewDetailLine(new DetailModel(
							"No DTS",
							"Unexpected Error",
							e.getMessage(),
							"",
							"-",
							gov.nist.healthcare.ttt.database.log.DetailInterface.Status.ERROR));
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		
		return currentlyProcessedPart;
	}
	
public Part processSMIMEEnvelope(Part p, InputStream certificate, String password) throws Exception {
		
		PrivateCertificateLoader certLoader = null;
		RecipientId     recId = null;		
		
		try {
			certLoader = new PrivateCertificateLoader(certificate, password);
			recId = new JceKeyTransRecipientId(certLoader.getX509Certificate());
		} catch (KeyStoreException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (NoSuchProviderException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (NoSuchAlgorithmException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (CertificateException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (Exception e1) {
			logger.error("Probably wrong format file or wrong certificate " + e1.getMessage());
			throw new Exception("Probably wrong format file or wrong certificate " + e1.getMessage());
		}


		SMIMEEnveloped m = null;
		try {
			m = new SMIMEEnveloped((MimeMessage)p);
		} catch (MessagingException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (CMSException e1) {
			logger.error(e1.getMessage());
			throw e1;
		}
		
		RecipientInformationStore   recipients = m.getRecipientInfos();
		RecipientInformation        recipient = recipients.get(recId);

		MimeBodyPart res = null;
		try {
			res = SMIMEUtil.toMimeBodyPart(recipient.getContent(new JceKeyTransEnvelopedRecipient(certLoader.getPrivateKey()).setProvider("BC")));
		} catch (SMIMEException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (CMSException e1) {
			logger.error(e1.getMessage());
			throw e1;
		} catch (Exception e1) {
			logger.error("Encryption certificate was probably wrong file " + e1.getMessage());
			throw new Exception("Encryption certificate was probably wrong file " + e1.getMessage());
		}

		return res;
	}
	
	public void fillLogModel(MimeMessage msg) throws MessagingException {
		this.logModel.setContentType(msg.getContentType());
		this.logModel.setContentDisposition(ValidationUtils.getSingleHeader(msg, "content-disposition"));
		this.logModel.setFromLine(ValidationUtils.fillArrayLog(msg.getFrom()));
		this.logModel.setIncoming(true);
		this.logModel.setMdn(false);
		this.logModel.setStatus(Status.SUCCESS);
		this.logModel.setMimeVersion(ValidationUtils.getSingleHeader(msg, "mime-version"));
		this.logModel.setMessageId(msg.getMessageID());
		this.logModel.setOrigDate(ValidationUtils.getSingleHeader(msg, "date"));
		this.logModel.setReceived(ValidationUtils.fillArrayLog(msg.getHeader("received")));
		this.logModel.setReplyTo(ValidationUtils.fillArrayLog(msg.getReplyTo()));
		this.logModel.setSubject(msg.getSubject());
		this.logModel.setToLine(ValidationUtils.fillArrayLog(msg.getRecipients(Message.RecipientType.TO)));
	}
	
	public PartModel fillPartModel(Part p, PartModel parent) {
		PartModel partModel = new PartModel();
		try {
			partModel.setContent(p);
			partModel.setContentDisposition(ValidationUtils.getSingleHeader(p, "content-disposition"));
			partModel.setContentTransferEncoding(ValidationUtils.getSingleHeader(p, "content-transfer-encoding"));
			partModel.setContentType(p.getContentType());
			partModel.setStatus(true);
			partModel.setParent(parent);
		} catch(Exception e) {
			partModel.addNewDetailLine(new DetailModel("No DTS", "Unexpected Error", e.getMessage(), "", "-", gov.nist.healthcare.ttt.database.log.DetailInterface.Status.ERROR));
		}
		return partModel;
	}
	
	public MimeBodyPart decodeQP(InputStream encodedQP) throws MessagingException {
		InputStream res = MimeUtility.decode(encodedQP, "quoted-printable");
		return new MimeBodyPart(res);
	}

	public LogModel getLogModel() {
		return logModel;
	}

	public void setLogModel(LogModel logModel) {
		this.logModel = logModel;
	}

	public PartModel getMainPart() {
		return mainPart;
	}

	public void setMainPart(PartModel mainPart) {
		this.mainPart = mainPart;
	}

	public InputStream getDirectMessage() {
		return directMessage;
	}

	public void setDirectMessage(InputStream directMessage) {
		this.directMessage = directMessage;
	}

	public InputStream getCertificate() {
		return certificate;
	}

	public void setCertificate(InputStream certificate) {
		this.certificate = certificate;
	}

	public String getCertificatePassword() {
		return certificatePassword;
	}

	public void setCertificatePassword(String certificatePassword) {
		this.certificatePassword = certificatePassword;
	}

	public boolean isWrapped() {
		return wrapped;
	}

	public void setWrapped(boolean wrapped) {
		this.wrapped = wrapped;
	}

	public boolean isMdn() {
		return isMdn;
	}

	public void setMdn(boolean isMdn) {
		this.isMdn = isMdn;
	}

	public String getOriginalMessageId() {
		return originalMessageId;
	}

	public void setOriginalMessageId(String originalMessageId) {
		this.originalMessageId = originalMessageId;
	}
	
}
