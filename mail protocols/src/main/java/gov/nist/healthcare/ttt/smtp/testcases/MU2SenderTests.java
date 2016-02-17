package gov.nist.healthcare.ttt.smtp.testcases;

import java.io.FileInputStream;
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
	static public String MessageId;
	static public String fetch;
	static public String type = "";
	public String getMessageId(){
		return MessageId;
	}
	public String getfetch(){
		return fetch;
	}
	public String gettype(){
		return type;
	}
	public TestResult testBadAddress(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		//	String dsn = "SUCCESS,FAILURE,DELAY,ORCPT=rfc1891";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable",true);
	//	props.put("mail.smtp.starttls.required",true);
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.dsn.ret", "HDRS");
		//	props.put("mail.smtp.notify", dsn);
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "imap";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("badaddress@gfail.com"));
		//	InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			String mId = message.getHeader("Message-ID")[0];
		//	tr.setMessageId(message.getHeader("Message-ID")[0]);
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);


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
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "imap";
		type = "pass";

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
				//	InternetAddress.parse("provider1@direct2.sitenv.org"));
					InternetAddress.parse("badaddress@gfail.com"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);
			


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

	public TestResult testMu2Three(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "imap";
		type = "pass";

		Session session = Session.getInstance(props, null);
	

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("failure15@hit-testing2.nist.gov"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);
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

	public TestResult testMu2Four(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "imap";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("nomdn8@hit-dev.nist.gov"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

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

	public TestResult testBadAddressPop(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		//	String dsn = "SUCCESS,FAILURE,DELAY,ORCPT=rfc1891";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable",true);
	//	props.put("mail.smtp.starttls.required",true);
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.dsn.ret", "HDRS");
		//	props.put("mail.smtp.notify", dsn);
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "pop";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("badaddress@gfail.com"));
		//	InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);


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


	public TestResult testMu2TwoPop(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "pop";
		type = "pass";

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("badaddress@gmffail.com"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);
			


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

	public TestResult testMu2ThreePop(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "pop";
		type = "pass";

		Session session = Session.getInstance(props, null);
	

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("failure15@hit-testing2.nist.gov"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);
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

	public TestResult testMu2FourPop(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "pop";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("nomdn8@hit-dev.nist.gov"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

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
	public TestResult testBadAddressSmtp(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		//	String dsn = "SUCCESS,FAILURE,DELAY,ORCPT=rfc1891";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable",true);
	//	props.put("mail.smtp.starttls.required",true);
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.dsn.ret", "HDRS");
		//	props.put("mail.smtp.notify", dsn);
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "smtp";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("badaddress@gmffail.com"));
		//	InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);


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


	public TestResult testMu2TwoSmtp(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable","true");
//		props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "smtp";
		type = "pass";

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("badaddress@gmffail.com"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);
			


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

	public TestResult testMu2ThreeSmtp(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "smtp";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("failure15@hit-testing2.nist.gov"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);
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

	public TestResult testMu2FourSmtp(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "smtp";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("nomdn8@hit-dev.nist.gov"));
			message.setSubject("Testing sending mail to BadAddress!");
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO BAD ADDRESS\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

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


	public TestResult testMu2TwoEight(TestInput ti, String Address) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "imap";
		type = "fail";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(Address));
			message.setSubject("Testing sending mail to " + Address);
			message.setText("This is a message to "+ Address);

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO  "+ Address + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in MU2 -27/28");
			result.put("ERROR", "Cannot send message to " + Address + ": " +  e.getLocalizedMessage());
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	
	public TestResult testMu2TwoEightSmtp(TestInput ti, String Address) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "smtp";
		type = "fail";

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(Address));
			message.setSubject("Testing sending mail to " + Address);
			message.setText("This is a message to "+ Address);

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO  "+ Address + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in MU2 -27/28");
			result.put("ERROR", "Cannot send message to " + Address + ": " +  e.getLocalizedMessage());
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	
	public TestResult testMu2TwoEightPop(TestInput ti, String Address) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
	//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "pop";
		type = "fail";

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(Address));
			message.setSubject("Testing sending mail to " + Address);
			message.setText("This is a message to "+ Address);

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO  "+ Address + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in MU2 -27/28");
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
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "imap";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH MESSAGE DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	public TestResult testDispositionNotificationPop(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "pop";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH MESSAGE DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	public TestResult testDispositionNotificationSmtp(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "smtp";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
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
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH MESSAGE DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	
	public TestResult testDispositionNotificationSutReceiver(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.from", "failure15@hit-testing2.nist.gov");
		fetch = "imap1";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {
			
			Properties prop = new Properties();
			String path = "./application.properties";
			FileInputStream file = new FileInputStream(path);
			prop.load(file);
			file.close();

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(prop.getProperty("dir.username")));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail with Disposition Notification Header!");
			message.setText("This is a message to a SUT");
			message.addHeader("Disposition-Notification-Options", "X-DIRECT-FINAL-DESTINATION-DELIVERY=optional,true");

			
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect("hit-testing.nist.gov", ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort,prop.getProperty("dir.username"), prop.getProperty("dir.password"));
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH MESSAGE DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}
	
	public TestResult testBadDispositionNotification(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		fetch = "imap";
		type = "pass";
		Session session = Session.getInstance(props, null);
		try {
			
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
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
			MessageId = message.getHeader("Message-ID")[0];
			
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH BAD DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (Exception e) {
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}
	
	public TestResult testBadDispositionNotificationSutReceiver(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		fetch = "imap1";
		type = "pass";
		Session session = Session.getInstance(props, null);
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("sandeep@hit-testing.nist.gov"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing sending mail with Bad Disposition Notification Header");
			message.setText("This is a message to a SUT!");
			message.addHeader("Disposition-Notification-Options", "X-XXXX-FINAL-X-DELXXXX=optioXXX,tXX");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect("hit-testing.nist.gov", ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort,"sandeep@hit-testing.nist.gov", "sandeeppassword");
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH BAD DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}
	
	
	public TestResult testBadAddressSutReceiver(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		fetch = "imap1";
		type = "fail";
		Session session = Session.getInstance(props, null);
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("sandeep@hit-testing.nist.gov"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Mail to receivng HISP");
			message.setText("This is a message to a SUT!");

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect("hit-testing.nist.gov", ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort,"sandeep@hit-testing.nist.gov", "sandeeppassword");
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","Sending email\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}
	public TestResult testBadDispositionNotificationPop(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		fetch = "pop";
		type = "pass";
		Session session = Session.getInstance(props, null);
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
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
			MessageId = message.getHeader("Message-ID")[0];
			
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH BAD DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error" + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}
	
	public TestResult testBadDispositionNotificationSmtp(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		fetch = "smtp";
		type = "pass";

		Session session = Session.getInstance(props, null);
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
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
			MessageId = message.getHeader("Message-ID")[0];
			
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL WITH BAD DISPOSITION NOTIFICATION HEADER\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST MESSAGE BAD DISPOSITION NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}
		

		return tr;
	}
	
	public TestResult testPositiveDeliveryNotification(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.dsn.ret", "HDRS");
		//	props.put("mail.smtp.notify", dsn);
		fetch = "imap";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
			message.setSubject("Testing sending mail to receive Positive Delivery Notification (Test Case MU2-29)!");
			message.setText("This is a message to a badAddress!");
			
			//	message.addHeader("Disposition-Notification-To", "failure15@hit-testing2.nist.gov");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");
			

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO ADDRESS  " + ti.sutEmailAddress + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST POSITIVE DELIVERY NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}

	public TestResult testPositiveDeliveryNotificationPop(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.dsn.ret", "HDRS");
		//	props.put("mail.smtp.notify", dsn);
		fetch = "pop";
		type = "pass";
		Session session = Session.getInstance(props, null);

		try {
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
			message.setSubject("Testing sending mail to receive Positive Delivery Notification (Test Case MU2-29)!");
			message.setText("This is a message to a badAddress!");
			
			//	message.addHeader("Disposition-Notification-To", "failure15@hit-testing2.nist.gov");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");
			

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO ADDRESS  " + ti.sutEmailAddress + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST POSITIVE DELIVERY NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}
	
	public TestResult testPositiveDeliveryNotificationSmtp(TestInput ti) {
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.STEP2);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
	//	props.put("mail.smtp.starttls.enable","true");
	//	props.put("mail.smtp.starttls.required","true");
		props.put("mail.smtp.ssl.trust", "*");
		//	props.put("mail.smtp.dsn.ret", "HDRS");
		//	props.put("mail.smtp.notify", dsn);
		Session session = Session.getInstance(props, null);
		fetch = "smtp";
		type = "pass";

		try {
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("processeddispatched6@hit-dev.nist.gov"));
			message.setSubject("Testing sending mail to receive Positive Delivery Notification (Test Case MU2-29)!");
			message.setText("This is a message to a badAddress!");
			
			//	message.addHeader("Disposition-Notification-To", "failure15@hit-testing2.nist.gov");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");
			

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			MessageId = message.getHeader("Message-ID")[0];
			System.out.println("Done");
			log.info("Message Sent");
			result.put("1","SENDING EMAIL TO ADDRESS  " + ti.sutEmailAddress + "\n");
			result.put("2","Email sent Successfully\n");
			result.put("3", "Message-ID of the email sent: " + MessageId);

		} catch (MessagingException e) {
			log.info("Error in testBadAddress");
			result.put("1", "Error in TEST POSITIVE DELIVERY NOTIFICATION: " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			result.put("1", "Error " + e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
			
		}

		return tr;
	}
}