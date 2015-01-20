package gov.nist.healthcare.ttt.direct.smtpMdns;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import gov.nist.healthcare.ttt.direct.messageGenerator.MDNGenerator;
import gov.nist.healthcare.ttt.direct.messageGenerator.SMTPAddress;
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender;

public class SmtpMDNMessageGenerator {

	public static void sendSmtpMDN(InputStream originalMessage, String from, String to, String type, String failure) throws Exception {

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
		generator.setText("Your message was successfully processed.");
		generator.setToAddress(to);
		generator.setFailure(failure);
		
		MimeMessage mdnToSend = new MimeMessage(session);
		mdnToSend.setFrom(new InternetAddress(new SMTPAddress().properEmailAddr(from)));
		mdnToSend.setRecipient(Message.RecipientType.TO, new InternetAddress(new SMTPAddress().properEmailAddr(to)));
		mdnToSend.setSentDate(new Date());
		mdnToSend.setContent(generator.create());
		mdnToSend.setSubject("Automatic MDN");
		mdnToSend.saveChanges();
		
		DirectMessageSender sender = new DirectMessageSender();
		
		String targetDomain = sender.getTargetDomain(to);
		
		// Send mdn
		sender.send(25, targetDomain, mdnToSend, from, to);
		
	}

}
