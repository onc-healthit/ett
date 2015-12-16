package gov.nist.healthcare.ttt.smtp.testcases;

import gov.nist.healthcare.ttt.smtp.TestInput;
import gov.nist.healthcare.ttt.smtp.TestResult;
import gov.nist.healthcare.ttt.smtp.TestResult.CriteriaStatus;
import gov.nist.healthcare.ttt.smtp.testcases.MU2SenderTests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class MU2ReceiverTests {
	public static Logger log = Logger.getLogger("MU2ReceiverTests");
    MU2SenderTests st = new MU2SenderTests();
    String id = st.getMessageId();
    String fetch = st.getfetch();
    
	public TestResult fetchMail1(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		HashMap<String, String> result = tr.getTestRequestResponses();
		HashMap<String, String> bodyparts = tr.getAttachments();
		//int j = 0;
		Properties props = System.getProperties();
		try {
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imap");
			store.connect(ti.tttSmtpAddress,993,"failure15@hit-testing2.nist.gov","smtptesting123");

			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);


			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message messages[] = inbox.search(unseenFlagTerm);

			for (Message message : messages) {

				
				Address[] froms = message.getFrom();
				String sender_ = froms == null ? ""
						: ((InternetAddress) froms[0]).getAddress();

				String sender = ti.sutEmailAddress;
				if (sender_.equals(sender)) {
					//j++;
					// Store all the headers in a map
					Enumeration headers = message.getAllHeaders();
					while (headers.hasMoreElements()) {
						Header h = (Header) headers.nextElement();
					//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
						result.put("\n"+h.getName(), h.getValue()+"\n");




					}
					Multipart multipart = (Multipart) message.getContent();
					for (int i = 0; i < multipart.getCount(); i++) {
						BodyPart bodyPart = multipart.getBodyPart(i);
						InputStream stream = bodyPart.getInputStream();

						byte[] targetArray = IOUtils.toByteArray(stream);
						System.out.println(new String(targetArray));
						int m = i+1;
						 bodyparts.put("bodyPart" + " " + "[" +m +"]", new String(targetArray));

					}
				}

			}
			
			/*for (Message message : messages){
				Enumeration headers = message.getAllHeaders();
				while(headers.hasMoreElements()) {
					Header h = (Header) headers.nextElement();
					if (ti.equals(h.getValue())){
						
						Enumeration headers1 = message.getAllHeaders();
						while (headers.hasMoreElements()) {
							Header h1 = (Header) headers.nextElement();
						//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
							result.put("\n"+h1.getName(), h1.getValue()+"\n");




						}
						Multipart multipart = (Multipart) message.getContent();
						for (int i = 0; i < multipart.getCount(); i++) {
							BodyPart bodyPart = multipart.getBodyPart(i);
							InputStream stream = bodyPart.getInputStream();

							byte[] targetArray = IOUtils.toByteArray(stream);
							System.out.println(new String(targetArray));
							int m = i+1;
							 bodyparts.put("bodyPart" + " " + "[" +m +"]", new String(targetArray));

						}
					}
				}
			}*/

			if (result.size() == 0) {
				tr.setCriteriamet(CriteriaStatus.STEP2);
				tr.getTestRequestResponses().put("ERROR","No messages found! Send a message and try again." +" id");
			}
			else {
				tr.setCriteriamet(CriteriaStatus.TRUE);
			}
		} catch (MessagingException e) {
			tr.setCriteriamet(CriteriaStatus.FALSE);
			e.printStackTrace();
			log.info("Error fetching email " + e.getLocalizedMessage());
			tr.getTestRequestResponses().put("1","Error fetching email :" + e.getLocalizedMessage());
		}

		return tr;
	}
	
	public TestResult fetchMail(TestInput ti) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		TestResult tr = new TestResult();
		HashMap<String, String> result = tr.getTestRequestResponses();
		HashMap<String, String> bodyparts = tr.getAttachments();
		//int j = 0;
		 Store store;
		Properties props = System.getProperties();
		try {
			Session session = Session.getDefaultInstance(props, null);
			
			store = session.getStore("imap");
			
			if (fetch.equals("smtp")){
				store.connect(ti.tttSmtpAddress,993,"failure15@hit-testing2.nist.gov","smtptesting123");
			}
			else if (fetch.equals("imap")) {
			store.connect(ti.sutSmtpAddress,143,ti.sutUserName,ti.sutPassword);
			}
			
			else if (fetch.equals("imap1")) {
				store.connect("hit-testing.nist.gov",143,"sandeep@hit-testing.nist.gov","sandeeppassword");
				}
			
			else {
				store = session.getStore("pop3");
				store.connect(ti.sutSmtpAddress,110,ti.sutUserName,ti.sutPassword);
			}
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);


			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message messages[] = inbox.search(unseenFlagTerm);

			
			for (Message message : messages){
				Enumeration headers = message.getAllHeaders();
				while(headers.hasMoreElements()) {
					Header h = (Header) headers.nextElement();
					String x = h.getValue();
					if (id.equals(x)){
						
						Enumeration headers1 = message.getAllHeaders();
						while (headers1.hasMoreElements()) {
							Header h1 = (Header) headers1.nextElement();
						//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
							result.put("\n"+h1.getName(), h1.getValue()+"\n");

						}
						Multipart multipart = (Multipart) message.getContent();
						for (int i = 0; i < multipart.getCount(); i++) {
							BodyPart bodyPart = multipart.getBodyPart(i);
							InputStream stream = bodyPart.getInputStream();

							byte[] targetArray = IOUtils.toByteArray(stream);
							System.out.println(new String(targetArray));
							int m = i+1;
							 bodyparts.put("bodyPart" + " " + "[" +m +"]", new String(targetArray));

						}
					}
				}
				
			//	message.isMimeType(mimeType)
			}

			if (result.size() == 0) {
				tr.setCriteriamet(CriteriaStatus.STEP2);
				tr.getTestRequestResponses().put("ERROR","No messages found with Message ID: " + id);
			}
			else {
				tr.setCriteriamet(CriteriaStatus.TRUE);
			}
		} catch (MessagingException e) {
			tr.setCriteriamet(CriteriaStatus.FALSE);
			e.printStackTrace();
			log.info("Error fetching email " + e.getLocalizedMessage());
			tr.getTestRequestResponses().put("1","Error fetching email :" + e.getLocalizedMessage());
		}

		return tr;
	}
}