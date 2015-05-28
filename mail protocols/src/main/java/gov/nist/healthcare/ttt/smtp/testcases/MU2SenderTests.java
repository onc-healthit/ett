package gov.nist.healthcare.ttt.smtp.testcases;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import gov.nist.healthcare.ttt.smtp.TestInput;
import gov.nist.healthcare.ttt.smtp.TestResult;
import gov.nist.healthcare.ttt.smtp.TestResult.CriteriaStatus;

public class MU2SenderTests {

	public static Logger log = Logger.getLogger("MU2SenderTests");

	public TestResult testBadAddress(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		//	String dsn = "SUCCESS,FAILURE,DELAY,ORCPT=rfc1891";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable",true);
		props.put("mail.smtp.starttls.required",true);
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.dsn.ret", "HDRS");
		//	props.put("mail.smtp.notify", dsn);
		props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail to BadAddress (Test Case MU2-1)!");
			message.setText("This is a message to a badAddress!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			message.saveChanges();
			System.out.println(message.getHeader("Message-ID")[0]);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);


		} 
		
		catch (NullPointerException e) {
			log.info("Error in testBadAddress");
			result.put("ERROR :", "Address cannot be null");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		
		catch (Exception e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST BAD ADDRESS: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	
	public TestResult testMu2Two(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
				message.setSubject("Testing sending mail to BadAddress (Test Case MU2-2)!");
				message.setText("This is a message to a badAddress!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);

		} catch (MessagingException e) {
			log.info("Error in MU2 - 2");
			result.put("1", "Error in TEST MU2 - 2: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	public TestResult testMu2Three(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail to BadAddress (Test Case MU2-3)!");
			message.setText("This is a message to a badAddress!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);

		} catch (MessagingException e) {
			log.info("Error in MU2 -3");
			result.put("1", "Error in TEST MU2 - 3: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	public TestResult testMu2Four(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail to BadAddress (Test Case MU2-4)!");
			message.setText("This is a message to a badAddress!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);

		} catch (MessagingException e) {
			log.info("Error in MU2-4");
			result.put("1", "Error in TEST MU2 - 4: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}




	public TestResult testMu2TwoEight(TestInput ti, String Address) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(Address));
			//	message.setSubject("Testing sending mail to BadAddress (Test Case MU2-28)!");
			//	message.setText("This is a message to a badAddress!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO  "+ Address + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);

		} catch (MessagingException e) {
			log.info("Error in MU2 -27");
			result.put("ERROR", "Cannot send message to " + Address + ": " +  e.getLocalizedMessage());
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	// to add custom message-id
	static class MyMessage extends MimeMessage {
		MyMessage(Session session) { super(session); }

		@Override
		protected void updateMessageID() throws MessagingException {
			setHeader("Message-ID", "<123456>");		
		}
	}


	public TestResult testDispositionNotification(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail with Disposition Notification Header (Test Case MU2-21)!");
			message.setText("This is a message to a Address 6!");
			message.addHeader("Disposition-Notification-Options", "X-DIRECT-FINAL-DESTINATION-DELIVERY=optional,true");


			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH MESSAGE DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	public TestResult testBadDispositionNotification(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail with Bad Disposition Notification Header (Test Case MU2-22)!");
			message.setText("This is a message to a Address 6!");
			message.addHeader("Disposition-Notification-Options", "X-XXXX-FINAL-X-DELXXXX=optioXXX,tXX");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH BAD DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	public TestResult testPositiveDeliveryNotification(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE);
		HashMap<String, String> result = tr.getTestRequestResponses();
		String dsn = "SUCCESS,FAILURE,DELAY,ORCPT=rfc1891";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.smtp.dsn.ret", "HDRS");
		props.put("mail.smtp.notify", dsn);

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail to receive Positive Delivery Notification (Test Case MU2-29)!");
			message.setText("This is a message to a badAddress!");
			message.addHeader("Disposition-Notification-To", "failure15@hit-testing2.nist.gov");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO ADDRESS  " + ti.sutEmailAddress + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + message.getHeader("Message-ID")[0]);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST POSITIVE DELIVERY NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
}