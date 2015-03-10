package gov.nist.healthcare.ttt.webapp.direct.controller;

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import gov.nist.healthcare.ttt.direct.certificates.PublicCertLoader;
import gov.nist.healthcare.ttt.direct.messageGenerator.DirectMessageGenerator;
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender;
import gov.nist.healthcare.ttt.webapp.direct.listener.ListenerProcessor;
import gov.nist.healthcare.ttt.webapp.common.model.ObjectWrapper.ObjWrapper;
import gov.nist.healthcare.ttt.model.logging.LogModel;
import gov.nist.healthcare.ttt.model.sendDirect.SendDirectMessage;

import org.apache.log4j.Logger;
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
	
	private static Logger logger = Logger.getLogger(SendDirectController.class.getName());
	
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
	public @ResponseBody ObjWrapper<Boolean> sendDirectMessage(@RequestBody SendDirectMessage messageInfo) throws Exception {
		
		// Set certificates values
		listener.setCertificatesPath(this.certificatesPath)
		listener.setCertPassword(this.certPassword)
		
		if (messageInfo.isValidSendEmail()) {
			InputStream attachmentFile = null;
			if(messageInfo.getOwnCcdaAttachment() != null && !messageInfo.getOwnCcdaAttachment().equals("")) {
				File ownCcda = new File(messageInfo.getOwnCcdaAttachment());
				messageInfo.setAttachmentFile(ownCcda.getName());
				attachmentFile = new FileInputStream(ownCcda);
			} else if(!messageInfo.getAttachmentFile().equals("")) {
				attachmentFile = getClass().getResourceAsStream("/cda-samples/" + messageInfo.getAttachmentFile());
			}
			InputStream signingCert = listener.getSigningPrivateCert(messageInfo.getSigningCert().toLowerCase());
			
			DirectMessageGenerator messageGenerator = new DirectMessageGenerator(
					messageInfo.getTextMessage(), messageInfo.getSubject(),
					messageInfo.getFromAddress(), messageInfo.getToAddress(),
					attachmentFile, messageInfo.getAttachmentFile(),
					signingCert, messageInfo.getSigningCertPassword(),
					null, messageInfo.isWrapped());
			
			// Get encryption cert
			InputStream encryptionCert = null;
			if(!messageInfo.getEncryptionCert().equals("")) {
				encryptionCert = new FileInputStream(new File(messageInfo.getEncryptionCert()));
			} else {
				logger.debug("Trying to fetch encryption cert for " + messageInfo.getToAddress());
				encryptionCert = messageGenerator.getEncryptionCertByDnsLookup(messageInfo.getToAddress());
			}
			
			messageGenerator.setEncryptionCert(encryptionCert);

			MimeMessage msg = messageGenerator.generateMessage();
			
			// Log the outgoing message in the database
			LogModel outgoingMessage = new LogModel(msg);
			this.db.getLogFacade().addNewLog(outgoingMessage);
			
			return new ObjWrapper<Boolean>(sender.send(25, messageGenerator.getTargetDomain(messageInfo.getToAddress()), msg, messageInfo.getFromAddress(), messageInfo.getToAddress()));
		}
		return new ObjWrapper<Boolean>(false);
	}

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody SendDirectMessage generate() throws IOException {

		return new SendDirectMessage();
	}

}
