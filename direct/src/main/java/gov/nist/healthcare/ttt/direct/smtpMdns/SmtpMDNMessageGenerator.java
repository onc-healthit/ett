package gov.nist.healthcare.ttt.direct.smtpMdns;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import gov.nist.healthcare.ttt.direct.messageGenerator.MDNGenerator;
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender;

public class SmtpMDNMessageGenerator {

	public static void sendSmtpMDN(InputStream originalMessage, String from, String to, String type, String failure, InputStream signingCert, String signingCertPassword) throws Exception {

		// Get the session variable
		Properties props = System.getProperties();
		Session session = Session.getDefaultInstance(props, null);

		// Get the MimeMessage object
		MimeMessage msg = new MimeMessage(session, originalMessage);

		MDNGenerator generator = new MDNGenerator();
		generator.setDisposition("automatic-action/MDN-sent-automatically;" + type);
		generator.setFinal_recipient(to);
		generator.setFromAddress(from);
		generator.setOriginal_message_id(msg.getMessageID());
		generator.setOriginal_recipient(from);
		generator.setReporting_UA_name("smtp.nist.gov");
		generator.setReporting_UA_product("Security Agent");
		generator.setSubject("Automatic MDN");
		if(type.equals("dispatched")) {
			generator.setText("Your message was successfully dispatched.");
		} else {
			generator.setText("Your message was successfully processed.");
		}
		generator.setToAddress(to);
		generator.setFailure(failure);
		// Certificates 
		generator.setSigningCert(signingCert);
		generator.setSigningCertPassword(signingCertPassword);
		generator.setEncryptionCert(generator.getEncryptionCertByDnsLookup(to));
		
		MimeMessage mdnToSend = generator.generateMDN();
		
		DirectMessageSender sender = new DirectMessageSender();
		
		String targetDomain = sender.getTargetDomain(to);
		
		// Send mdn
		sender.send(25, targetDomain, mdnToSend, from, to);
		
	}

}
