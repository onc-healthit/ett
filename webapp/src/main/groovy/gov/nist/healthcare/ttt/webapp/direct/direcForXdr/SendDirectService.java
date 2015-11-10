package gov.nist.healthcare.ttt.webapp.direct.direcForXdr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import gov.nist.healthcare.ttt.direct.messageGenerator.DirectMessageGenerator;
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender;
import gov.nist.healthcare.ttt.model.sendDirect.SendDirectMessage;
import gov.nist.healthcare.ttt.webapp.direct.listener.ListenerProcessor;

@Component
public class SendDirectService {

	private static Logger logger = Logger.getLogger(SendDirectService.class.getName());

	private SendDirectMessage messageInfo;
	private ListenerProcessor listener = new ListenerProcessor();
	private DirectMessageSender sender = new DirectMessageSender();

	@Value("${direct.certificates.repository.path}")
	String certificatesPath;

	@Value("${direct.certificates.password}")
	String certPassword;

	public SendDirectService(SendDirectMessage msgInfo) {
		this.messageInfo = msgInfo;
	}

	public SendDirectMessage getMessageInfo() {
		return messageInfo;
	}

	public void setMessageInfo(SendDirectMessage messageInfo) {
		this.messageInfo = messageInfo;
	}

	public boolean sendDirect() throws Exception {
		// Set certificates values
		listener.setCertificatesPath(this.certificatesPath);
		listener.setCertPassword(this.certPassword);

		if (messageInfo.isValidSendEmail()) {
			InputStream attachmentFile = null;
			if (messageInfo.getOwnCcdaAttachment() != null && !messageInfo.getOwnCcdaAttachment().equals("")) {
				File ownCcda = new File(messageInfo.getOwnCcdaAttachment());
				messageInfo.setAttachmentFile(ownCcda.getName());
				attachmentFile = new FileInputStream(ownCcda);
			} else if (!messageInfo.getAttachmentFile().equals("")) {
				attachmentFile = getClass().getResourceAsStream("/cda-samples/" + messageInfo.getAttachmentFile());
			}
			if (messageInfo.getSigningCert().toLowerCase().equals("")) {
				messageInfo.setSigningCert("good");
			}
			InputStream signingCert = listener.getSigningPrivateCert(messageInfo.getSigningCert().toLowerCase());

			DirectMessageGenerator messageGenerator = new DirectMessageGenerator(messageInfo.getTextMessage(),
					messageInfo.getSubject(), messageInfo.getFromAddress(), messageInfo.getToAddress(), attachmentFile,
					messageInfo.getAttachmentFile(), signingCert, messageInfo.getSigningCertPassword(), null,
					messageInfo.isWrapped());

			// Get encryption cert
			InputStream encryptionCert = null;
			if (!messageInfo.getEncryptionCert().equals("")) {
				encryptionCert = new FileInputStream(new File(messageInfo.getEncryptionCert()));
			} else {
				logger.debug("Trying to fetch encryption cert for " + messageInfo.getToAddress());
				encryptionCert = messageGenerator.getEncryptionCertByDnsLookup(messageInfo.getToAddress());
			}

			messageGenerator.setEncryptionCert(encryptionCert);

			// Check if we want invalid digest
			MimeMessage msg;
			if (messageInfo.isInvalidDigest()) {
				msg = messageGenerator.generateAlteredDirectMessage();
			} else {
				msg = messageGenerator.generateMessage();
			}

			return sender.send(25, messageGenerator.getTargetDomain(messageInfo.getToAddress()), msg,
					messageInfo.getFromAddress(), messageInfo.getToAddress());
		}
		return false;
	}
}
