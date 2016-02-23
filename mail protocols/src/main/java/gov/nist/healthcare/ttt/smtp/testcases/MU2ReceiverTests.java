package gov.nist.healthcare.ttt.smtp.testcases;

import gov.nist.healthcare.ttt.smtp.TestInput;
import gov.nist.healthcare.ttt.smtp.TestResult;
import gov.nist.healthcare.ttt.smtp.TestResult.CriteriaStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
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

import com.sun.mail.dsn.DispositionNotification;


public class MU2ReceiverTests {
	public static Logger log = Logger.getLogger("MU2ReceiverTests");

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
		HashMap<String, String> buffer = new HashMap<String, String>();
		//int j = 0;
		Store store;
		Properties props = new Properties();

		TestResult t = ti.tr;

		String id = t.getMessageId();
		String fetch = t.getFetchType();
		String type = t.getSearchType();
		String startTime = t.getStartTime();
		Duration duration = null;
		Duration timeout = null;
		final long  timeoutConstant = 65; // 1 hour 5 mins (or) 65 minutes to get back the failure MDN


		try {


			Properties prop = new Properties();
			String path = "./application.properties";
			FileInputStream file = new FileInputStream(path);
			prop.load(file);
			file.close();

			Session session = Session.getDefaultInstance(props, null);

			store = session.getStore("imap");

			if (fetch.equals("smtp")){
				store.connect(ti.tttSmtpAddress,993,"failure15@hit-testing2.nist.gov",prop.getProperty("ett.password"));
			}
			else if (fetch.equals("imap")) {
				store.connect(ti.sutSmtpAddress,143,ti.sutUserName,ti.sutPassword);
			}

			else if (fetch.equals("imap1")) {
				store.connect("hit-testing.nist.gov",143,prop.getProperty("dir.username"), prop.getProperty("dir.password"));
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

			if(type.equals("fail")){
				System.out.println("Search in-reply-to");
				for (Message message : messages){
					Enumeration headers = message.getAllHeaders();
					while(headers.hasMoreElements()) {
						Header h = (Header) headers.nextElement();
						String x = h.getValue();
						if (id.equals(x)){
							ZonedDateTime endTime = ZonedDateTime.now();
							Enumeration headers1 = message.getAllHeaders();
							while (headers1.hasMoreElements()) {
								Header h1 = (Header) headers1.nextElement();
								//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
								result.put("\n"+h1.getName(), h1.getValue()+"\n");
								duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
								result.put("\nElapsed Time", duration.toString().substring(3)+"\n");

							}
							Multipart multipart = (Multipart) message.getContent();
							for (int i = 0; i < multipart.getCount(); i++) {
								BodyPart bodyPart = multipart.getBodyPart(i);
								InputStream stream = bodyPart.getInputStream();

								byte[] targetArray = IOUtils.toByteArray(stream);
								System.out.println(new String(targetArray));
								int m = i+1;
								//	bodyparts.put("bodyPart" + " " + "[" +m +"]", new String(targetArray));

							}
						}
					}
				}
			}

			else if (type.equals("timeout")){
				System.out.println("Search in-reply-to on Timeout");
				for (Message message : messages){
					Enumeration headers = message.getAllHeaders();
					while(headers.hasMoreElements()) {
						Header h = (Header) headers.nextElement();
						String x = h.getValue();
						if (id.equals(x)){
							ZonedDateTime endTime = ZonedDateTime.now();
							//	if (ti.MessageId.equals(x)){
							Enumeration headers1 = message.getAllHeaders();
							while (headers1.hasMoreElements()) {
								Header h1 = (Header) headers1.nextElement();
								//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
								result.put("\n"+h1.getName(), h1.getValue()+"\n");
								timeout = Duration.between(endTime, ZonedDateTime.parse(startTime));
								result.put("\nElapsed Time", timeout.toString().substring(3)+"\n");

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
				}

			}

			else {
				System.out.println("Search Original-Message-Id in DSN or in-reply-to in Headers");
				for (Message message : messages){
					Enumeration headers = message.getAllHeaders();
					while(headers.hasMoreElements()) {
						Header h = (Header) headers.nextElement();
						String x = h.getValue();
						if (id.equals(x)){
							ZonedDateTime endTime = ZonedDateTime.now();
							Enumeration headers1 = message.getAllHeaders();
							while (headers1.hasMoreElements()) {
								Header h1 = (Header) headers1.nextElement();
								//	result.put(h.getName() + " " +  "[" + j +"]", h.getValue());
								result.put("\n"+h1.getName(), h1.getValue()+"\n");
								duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
								result.put("\nElapsed Time", duration.toString().substring(3)+"\n");

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
				}

				if (bodyparts.size() == 0){
					// DSN Search for processed/dispatched MDN
					String s = "";
					for (Message message : messages){
						Object m =  message.getContent();
						if (m instanceof Multipart){
							Multipart multipart = (Multipart) message.getContent();
							for (int i = 0; i < ((Multipart) m).getCount(); i++){
								BodyPart bodyPart = multipart.getBodyPart(i);
								if (!(bodyPart.isMimeType("text/*"))){
									Object d =   bodyPart.getContent();
									//d.getNotifications();
									if (d instanceof DispositionNotification){
										Enumeration headers2 = ((DispositionNotification) d).getNotifications().getAllHeaders();
										while (headers2.hasMoreElements()) {
											Header h1 = (Header) headers2.nextElement();
											buffer.put("\n"+h1.getName(), h1.getValue()+"\n");
											s = h1.getValue();
											if (id.equals(s)){
												ZonedDateTime endTime = ZonedDateTime.now();
												result.put("\n"+h1.getName(), h1.getValue()+"\n");
												result.putAll(buffer);
												duration = Duration.between(endTime, ZonedDateTime.parse(startTime));
												result.put("\nElapsed Time", duration.toString().substring(3)+"\n");
												System.out.println("\n"+h1.getName() + ":" + h1.getValue()+"\n");
											}
											

										}

									}

								}

							}

						}
					}

				}

			}
			
			if (result.size() == 0) {
				tr.setCriteriamet(CriteriaStatus.STEP2);
				tr.getTestRequestResponses().put("ERROR","No messages found with Message ID: " + id);
			}

			else if(timeout!=null && (timeout.toMinutes() > timeoutConstant)){

				tr.setCriteriamet(CriteriaStatus.FALSE);
				tr.getTestRequestResponses().put("ERROR","MDN received after timeout");

			}

			else {
				tr.setCriteriamet(CriteriaStatus.TRUE);
			}

		} catch (Exception e) {
			tr.setCriteriamet(CriteriaStatus.FALSE);
			e.printStackTrace();
			log.info("Error fetching email " + e.getLocalizedMessage());
			tr.getTestRequestResponses().put("1","Error fetching email :" + e.getLocalizedMessage());
		}

		tr.setMessageId(id);
		tr.setFetchType(fetch);
		tr.setSearchType(type);
		tr.setStartTime(startTime);
		return tr;
	}
}