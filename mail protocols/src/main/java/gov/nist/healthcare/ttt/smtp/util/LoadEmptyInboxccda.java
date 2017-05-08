package gov.nist.healthcare.ttt.smtp.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;

public class LoadEmptyInboxccda {

	public static void sendmessage(String Address) throws Exception {

		System.out.println("Sending message");

		Properties props1 = new Properties();
		props1.put("mail.smtp.auth", "true");
		props1.put("mail.smtp.starttls.enable","true");
		props1.put("mail.smtp.starttls.required", "true");
		props1.put("mail.smtp.auth.mechanisms", "PLAIN");
		props1.setProperty("mail.smtp.ssl.trust", "*");
		Session session1 = Session.getInstance(props1, null);
		Message message = new MimeMessage(session1);
		message.setFrom(new InternetAddress("sut.example@gmail.com"));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(Address));
		message.setSubject("test");
		message.setText("This is a message to test!");

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText("This is message body");

		
		int index = Address.indexOf('@');
		String foldername = Address.substring(0,index);
			File folder = new File("./data/"+foldername);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {

				
				message.setSubject(listOfFiles[i].getName());
				message.setText("This is a message to test!");

				
				messageBodyPart.setText("This is message body");
				Multipart multipart = new MimeMultipart();

				// Adding attachments
				FileInputStream input;
				input = new FileInputStream(listOfFiles[i]);
				byte[] b =  IOUtils.toByteArray(input);

				DataSource source = new ByteArrayDataSource(b,"application/xml");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(listOfFiles[i].getName());
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);
				Transport transport = session1.getTransport("smtp");
				transport.connect("smtp.gmail.com","sut.example@gmail.com","***");
				transport.sendMessage(message, message.getAllRecipients());
				transport.close();
				System.out.println("Message Sent");


			}
			

		
		
		
	}

	public static void main(String args[]) throws Exception{
		List<String> addresses = Arrays.asList("b1-ambulatory@ttpds.sitenv.org","b1-inpatient@ttpds.sitenv.org",
				"b2-ambulatory@ttpds.sitenv.org","b2-inpatient@ttpds.sitenv.org",
				"b5-ambulatory@ttpds.sitenv.org","b5-inpatient@ttpds.sitenv.org",
				"b9-ambulatory@ttpds.sitenv.org","b9-inpatient@ttpds.sitenv.org",
				"negativetestingcareplan@ttpds.sitenv.org","negativetestingccds@ttpds.sitenv.org");
		
		Store store;
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		store = session.getStore("imap");
		for(String s : addresses){
			store.connect("ttpds.sitenv.org",143,s,"smtptesting123");
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			int mcount = inbox.getMessageCount();
			System.out.println(s+" "+mcount);
			store.close();
			if(mcount == 0){
				MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
				mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
				mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
				mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
				mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
				mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
				sendmessage(s);

			}
		}
	}
}
