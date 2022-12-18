package gov.nist.healthcare.ttt.webapp.direct.controller;

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import gov.nist.healthcare.ttt.direct.certificates.PublicCertLoader;
import gov.nist.healthcare.ttt.direct.messageGenerator.DirectMessageGenerator;
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender;
import gov.nist.healthcare.ttt.webapp.direct.listener.ListenerProcessor;
import gov.nist.healthcare.ttt.webapp.common.model.ObjectWrapper.ObjWrapper;
import gov.nist.healthcare.ttt.model.logging.LogModel;
import gov.nist.healthcare.ttt.model.sendDirect.SendDirectMessage;

import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.internet.MimeMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

@Controller
@RequestMapping("/api/sendDirect")
public class SendDirectController {
	
	private static Logger logger = LogManager.getLogger(SendDirectController.class.getName());
	
	@Value('${direct.certificates.repository.path}')
	String certificatesPath
	
	@Value('${direct.certificates.password}')
	String certPassword
	
	// Used to get the ressources
	private ListenerProcessor listener = new ListenerProcessor();
	DirectMessageSender sender = new DirectMessageSender();
	
	@Autowired
	private DatabaseInstance db;

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody ObjWrapper<Boolean> sendDirectMessage(@RequestBody SendDirectMessage messageInfo) {
		try  
        {  
		// Set certificates values
		listener.setCertificatesPath(this.certificatesPath)
		listener.setCertPassword(this.certPassword)

		// Get digest algo
		String digestAlgo = "SHA1withRSA";
		if(messageInfo.getDigestAlgo().equals("sha256")) {
			digestAlgo = "SHA256withRSA";
		}
		
		if(messageInfo.getDigestAlgo().equals("sha384")) {
			digestAlgo = "SHA384withRSA";
		}
		
		if(messageInfo.getDigestAlgo().equals("sha512")) {
			digestAlgo = "SHA512withRSA";
		}

		if(messageInfo.getDigestAlgo().equals("edsasha256")) {
			digestAlgo = "SHA256withPLAIN-ECDSA";
		}

		if(messageInfo.getDigestAlgo().equals("edsasha384")) {
			digestAlgo = "SHA384withPLAIN-ECDSA";
		}
                
                if(messageInfo.getDigestAlgo().equals("edsap256")) {
                        digestAlgo = "SHA256withECDSA";
                }
                
                if(messageInfo.getDigestAlgo().equals("edsap384")) {
                        digestAlgo = "SHA384withECDSA";
                }

		if (messageInfo.isValidSendEmail()) {
			InputStream attachmentFile = null;
			if(messageInfo.getOwnCcdaAttachment() != null && !messageInfo.getOwnCcdaAttachment().equals("")) {
			    
				String[] parts = messageInfo.getOwnCcdaAttachment().split("/");
				String fileName = parts[ parts.length - 1 ]
				
				File ownCcda = new File("/tmp/" + fileName);
				messageInfo.setAttachmentFile(ownCcda.getName());
				attachmentFile = new FileInputStream(ownCcda);
			} else if(!messageInfo.getAttachmentFile().equals("")) {
				attachmentFile = getClass().getResourceAsStream("/cda-samples/" + messageInfo.getAttachmentFile());
			}
			if(messageInfo.getSigningCert().toLowerCase().equals("")) {
				messageInfo.setSigningCert("good")
			}
			InputStream signingCert = listener.getSigningPrivateCert(messageInfo.getSigningCert().toLowerCase());
			
			String fname = messageInfo.getAttachmentFile();
			
			if (fname != null && fname.endsWith("_ett")) {
				fname = fname.substring(0, fname.lastIndexOf("-ett_"));
			}

			DirectMessageGenerator messageGenerator = new DirectMessageGenerator(
					messageInfo.getTextMessage(), messageInfo.getSubject(),
					messageInfo.getFromAddress(), messageInfo.getToAddress(),
					attachmentFile, fname,
					signingCert, messageInfo.getSigningCertPassword(),
					null, messageInfo.isWrapped(), digestAlgo);
			
			// Get encryption cert
			InputStream encryptionCert = null;
			if(!messageInfo.getEncryptionCert().equals("")) {
				
				String[] parts = messageInfo.getEncryptionCert().split("/");
				String fileName = parts[ parts.length - 1 ]
				File ownEncryptionCert = new File("/tmp/" + fileName);
				encryptionCert = new FileInputStream(ownEncryptionCert);
			} else {
				logger.debug("Trying to fetch encryption cert for " + messageInfo.getToAddress());
				encryptionCert = messageGenerator.getEncryptionCertByDnsLookup(messageInfo.getToAddress());
			}
			
			messageGenerator.setEncryptionCert(encryptionCert);

			// Check if we want invalid digest
			MimeMessage msg;
			if(messageInfo.invalidDigest) {
				msg = messageGenerator.generateAlteredDirectMessage()
			} else {
				msg = messageGenerator.generateMessage();
			}
			
			// Log the outgoing message in the database
			LogModel outgoingMessage = new LogModel(msg);
			this.db.getLogFacade().addNewLog(outgoingMessage);
			
			return new ObjWrapper<Boolean>(sender.send(25, messageGenerator.getTargetDomain(messageInfo.getToAddress()), msg, messageInfo.getFromAddress(), messageInfo.getToAddress()));
		}
		return new ObjWrapper<Boolean>(false);
		}
		
		
		catch (FileNotFoundException e){
		          println(e);
		        //  throw new Exception("Error: " + "Cannot send message", e);
		      }
		catch (Exception e){
		          println(e);
		          throw new Exception("Error: " + e.getMessage(), e);
		      }

	}
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody SendDirectMessage generate() throws IOException {

		return new SendDirectMessage();
	}

}
