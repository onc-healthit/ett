package gov.nist.healthcare.ttt.smtp.util;

import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

public class DeleteInbox {

	public static void main(String args[])
	{
		Store store;
		Properties props = System.getProperties();
		try {
			Session session = Session.getDefaultInstance(props, null);

			store = session.getStore("imap");

			store.connect("hit-testing2.nist.gov",993,"failure15@hit-testing2.nist.gov","smtptesting123");
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);


			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message messages[] = inbox.search(unseenFlagTerm);
			
			for (Message message : messages){
				
				message.setFlag(Flags.Flag.DELETED, true);
			}


			inbox.close(true);

			System.out.println("Delete sequence completed succesfully.");

		} catch (MessagingException e) {

			
			System.out.println("Error in delete sequence!");
			
			System.out.println(e.getLocalizedMessage());
		}

	}
}
