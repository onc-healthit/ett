package gov.nist.healthcare.ttt.smtp.testcases;

import gov.nist.healthcare.ttt.smtp.TestInput;
import gov.nist.healthcare.ttt.smtp.TestResult;
import gov.nist.healthcare.ttt.smtp.TestResult.CriteriaStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.mail.util.MailSSLSocketFactory;

public class TTTSenderTests {

	public static Logger log = Logger.getLogger("TTTSenderTests");
	Properties config;

	/**
	 * Implements Testcase #9. Sends a mail from TTT James to SUT.
	 * 
	 * @return
	 */

	public TestResult testSendMail(TestInput ti) {

		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE); 
		HashMap<String, String> result = tr.getTestRequestResponses();


		// Create a mail session
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", ti.useTLS ? "true" : "false");
		properties.put("mail.smtp.quitwait", "false");
		properties.put("mail.smtp.userset", "true");
		properties.put("mail.smtp.ssl.trust", "*");
		try {
			Session session = Session.getInstance(properties, null);

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));

			message.setSubject("Email from TTT (Test Case 9)");
			message.setText("This is a mail from JAMES Server");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			Multipart multipart = new MimeMultipart();
			String aName = "";
			for (Map.Entry<String, byte[]> e : ti.getAttachments().entrySet()) {

				DataSource source = new ByteArrayDataSource(e.getValue(),
						"text/html");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(e.getKey());
				aName += e.getKey();
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);
			}
			Transport transport = session.getTransport("smtp");
			transport.connect (ti.tttSmtpAddress, ti.useTLS ? ti.startTlsPort : ti.tttSmtpPort, ti.tttUserName, ti.tttPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			log.info("SENDING FIRST EMAIL");
			result.put("1","SENDING FIRST EMAIL TO " + ti.sutEmailAddress + " FROM " + ti.tttEmailAddress + " WITH ATTACHMENT " + aName);
			result.put("2","Email sent Successfully");
			System.out.println("Email sent successfully");

		} catch (MessagingException e) {
			e.printStackTrace();
			log.info("Error in Testcase 9" );
			result.put("1", "Error Sending Email " +  e.getLocalizedMessage() + new String(e.getMessage()));
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	

	/**
	 * Implements Testcase #16. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 */
	public TestResult testStarttls(TestInput ti) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		try{
			
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.setProperty("mail.smtp.ssl.trust", "*");
	  
		Session session = Session.getInstance(props, null);

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing STARTTLS & PLAIN SASL AUTHENTICATION (Test Case 9,16,20)!");
			message.setText("This is a message to test STARTTLS Security!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart();
			
			// Adding attachments
			for (Map.Entry<String, byte[]> e : ti.getAttachments().entrySet()) {

				DataSource source = new ByteArrayDataSource(e.getValue(),
						"text/html");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(e.getKey());
				aName += e.getKey();
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);
			}

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING STARTTLS & PLAIN SASL AUTHENTICATION EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENT " + aName);
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		} catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		return tr;
	}
	
	/**
	 * Implements Testcase to send Text and CCDA. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 * @throws IOException 
	 */

	public TestResult testStarttlsCCDAandText(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing CCDA and Text!");
			message.setText("This is a message to test Text+CCDA!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/CCDA_Ambulatory.xml")),
						"application/xml");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("CCDA_Ambulatory.xml");
				multipart.addBodyPart(messageBodyPart);
				
				DataSource source1 = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/Text.txt")),
						"text/plain");
				messageBodyPart1.setDataHandler(new DataHandler(source1));
				messageBodyPart1.setFileName("Text.txt");
				multipart.addBodyPart(messageBodyPart1);
				

			
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENTS " + "Text.txt and CCDA_Ambulatory.xml");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	/**
	 * Implements Testcase to send Pdf and CCDA. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 * @throws IOException 
	 */

	public TestResult testStarttlsCCDAandPdf(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing CCDA and Pdf!");
			message.setText("This is a message to test Pdf+CCDA!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source1 = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/CCDA_Ambulatory.xml")),
						"application/xml");
				messageBodyPart1.setDataHandler(new DataHandler(source1));
				messageBodyPart1.setFileName("CCDA_Ambulatory.xml");
				multipart.addBodyPart(messageBodyPart1);
				
				DataSource source = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/Sample.pdf")),
						"application/pdf");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("Sample.pdf");
				multipart.addBodyPart(messageBodyPart);

			
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENTS " + "Sample.pdf and CCDA_Ambulatory.xml ");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	/**
	 * Implements Testcase to send Text and XDM. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 * @throws IOException 
	 */

	public TestResult testStarttlsXDMandText(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing XDM and Text!");
			message.setText("This is a message to test Text+XDM!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source1 =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/CCDA_Ambulatory_in_XDM.zip")),
						"application/zip");
				messageBodyPart1.setDataHandler(new DataHandler(source1));
				messageBodyPart1.setFileName("CCDA_Ambulatory_in_XDM.zip");
				multipart.addBodyPart(messageBodyPart1);
				
				DataSource source =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/Text.txt")),
						"text/plain");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("Text.txt");
				multipart.addBodyPart(messageBodyPart);
				
				

			
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENTS " + "CCDA_Ambulatory_in_XDM.zip and Text.txt");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	
	/**
	 * Implements Testcase to send Text and CCDA. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 * @throws IOException 
	 */

	public TestResult testStarttlsTextandCCDA(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		
		try {
			MailSSLSocketFactory socketFactory= new MailSSLSocketFactory();
			socketFactory.setTrustAllHosts(true);
			
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true"); 
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);
		

		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing Text and CCDA!");
			message.setText("This is a message to test Text+CCDA!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/Text.txt")),
						"text/plain");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("Text.txt");
				multipart.addBodyPart(messageBodyPart);
				
				DataSource source1 = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/CCDA_Ambulatory.xml")),
						"application/xml");
				messageBodyPart1.setDataHandler(new DataHandler(source1));
				messageBodyPart1.setFileName("CCDA_Ambulatory.xml");
				multipart.addBodyPart(messageBodyPart1);
				

			
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENTS " + "CCDA_Ambulatory.xml and Text.txt");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	/**
	 * Implements Testcase to send Pdf and CCDA. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 * @throws IOException 
	 */

	public TestResult testStarttlsPdfandCCDA(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing Pdf and CCDA!");
			message.setText("This is a message to test Pdf+CCDA!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/Sample.pdf")),
						"application/pdf");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("Sample.pdf");
				multipart.addBodyPart(messageBodyPart);
				
				DataSource source1 = new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/CCDA_Ambulatory.xml")),
						"application/xml");
				messageBodyPart1.setDataHandler(new DataHandler(source1));
				messageBodyPart1.setFileName("CCDA_Ambulatory.xml");
				multipart.addBodyPart(messageBodyPart1);
				

			
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENTS " + "CCDA_Ambulatory.xml and Sample.pdf");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	/**
	 * Implements Testcase to send Text and XDM. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 * @throws IOException 
	 */

	public TestResult testStarttlsTextandXDM(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing Text and XDM!");
			message.setText("This is a message to test Text+XDM!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/Text.txt")),
						"text/plain");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("Text.txt");
				multipart.addBodyPart(messageBodyPart);
				
				DataSource source1 =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/CCDA_Ambulatory_in_XDM.zip")),
						"application/zip");
				messageBodyPart1.setDataHandler(new DataHandler(source1));
				messageBodyPart1.setFileName("CCDA_Ambulatory_in_XDM.zip");
				multipart.addBodyPart(messageBodyPart1);
				

			
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENTS " + "Text.txt and CCDA_Ambulatory_in_XDM.zip");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	public TestResult testStarttlsXDMBadHtml(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing XDM with bad XHTML!");
			message.setText("This is a message to test XDM with bad XHTML!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source1 =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream("/cda-samples/BadXHTML.zip")),
						"application/zip");
				messageBodyPart1.setDataHandler(new DataHandler(source1));
				messageBodyPart1.setFileName("BadXHTML.zip");
				multipart.addBodyPart(messageBodyPart1);
				
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENT " + "BadXHTML.zip");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		return tr;
	}
	/**
	 * Implements Testcase #20 and #22. Authenticates with SUT(good/bad password) and sends a mail from SUT Server to a user on SUT.
	 * 
	 * @return
	 * @throws MessagingException, NullPointerException 
	 * @throws AddressException 
	 */
	public TestResult testPlainSasl(TestInput ti, boolean useBadPassWord) {

		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE);
		HashMap<String, String> result = tr.getTestRequestResponses();
		Properties props = new Properties();
		System.setProperty("java.net.preferIPv4Stack", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", ti.useTLS ? "true" : "false");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");

		Session session = Session.getInstance(props, null);
		Transport transport = null;
		
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing PLAIN SASL (Test Case 20)");
			message.setText("This is a message to test PLAIN SASL!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			Multipart multipart = new MimeMultipart();
			for (Map.Entry<String, byte[]> e : ti.getAttachments().entrySet()) {

				DataSource source = new ByteArrayDataSource(e.getValue(),
						"text/html");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(e.getKey());
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);
			}
			
			log.info("Authenticating....");
			transport = session.getTransport("smtp");

			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName,
					useBadPassWord ? "badpassword" : ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			log.info("Authenticated Succefully");
			result.put("\n1","SENDING STARTTLS EMAIL TO " + ti.sutEmailAddress);
			result.put("\n2","Email sent Successfully");

			System.out.println("Email Sent.");

		} catch (MessagingException e) {
			if (e instanceof AuthenticationFailedException) {
				log.info("Authentication Failed. SUT rejects user/pass");
				result.put("SUCCESS", "Vendor rejects bad Username/Password combination :" + e.getLocalizedMessage());
				if(useBadPassWord){
					tr.setCriteriamet(CriteriaStatus.TRUE);
				}
				else {
					tr.setCriteriamet(CriteriaStatus.FALSE);
				}
			} else {
				tr.setCriteriamet(CriteriaStatus.FALSE);
				e.printStackTrace();
				log.info("error in PLAIN SASL");
				result.put("ERROR", e.getLocalizedMessage());
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			log.info("error in PLAIN SASL");
			result.put("ERROR", e.getLocalizedMessage());
		}
		finally {
				if (transport != null)
					try {
						transport.close();
					} catch (MessagingException e) {
						log.error("Error when closing transport");
						e.printStackTrace();
					}
		}

		return tr;
	}

	public TestResult testSendBadCCDA(TestInput ti, String filename) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing bad CCDA!");
			message.setText("This is a message to test Text+XDM!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream(filename)),
						"text/html");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("ToC_Ambulatory.xml");
				multipart.addBodyPart(messageBodyPart);
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENT " + "ToC_Ambulatory.xml");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	public TestResult testSendXDMApplicationOctect(TestInput ti, String filename) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing XDM with application/octect MIME type!");
			message.setText("This is a message to test Text+XDM!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream(filename)),
						"application/octet-stream");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("CCDA_Ambulatory_XDM.zip");
				multipart.addBodyPart(messageBodyPart);
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENT " + "CCDA_Ambulatory_XDM.zip");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	
	public TestResult testSendXDMApplicationXml(TestInput ti, String filename) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing XDM with applicaton/xml MIME type!");
			message.setText("This is a message to test Text+XDM!");

			BodyPart messageBodyPart = new MimeBodyPart();
			BodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart("mixed");
			
			// Adding attachments

				DataSource source =  new ByteArrayDataSource(IOUtils.toByteArray(getClass().getResourceAsStream(filename)),
						"application/zip");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("CCDA_Ambulatory_XDM.zip");
				multipart.addBodyPart(messageBodyPart);
			
			// Send the complete message parts
			message.setContent(multipart);
			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENT " + "CCDA_Ambulatory_XDM.zip");
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe were not able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (AuthenticationFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + " Authentication Failed");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE); 
		}catch (Exception e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}
	public void testDigestMd5(TestInput ti) {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", ti.useTLS);
		props.put("mail.smtp.host", ti.sutSmtpAddress);
		props.put("mail.smtp.port", ti.startTlsPort);
		props.put("mail.smtp.auth.mechanisms", "DIGEST-MD5");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));

			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing DIGEST-MD5");
			message.setText("This is a message to test DIGEST-MD5!");

			log.info("Sending Message");
			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.sutSmtpPort,
					ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			System.out.println("Done");

		} catch (MessagingException e) {
			log.info("Error in DigestMD5");
			throw new RuntimeException(e);
		}
	}


}