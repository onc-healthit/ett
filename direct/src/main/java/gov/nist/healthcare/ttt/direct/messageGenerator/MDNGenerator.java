package gov.nist.healthcare.ttt.direct.messageGenerator;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import com.sun.mail.dsn.DispositionNotification;
import com.sun.mail.dsn.MultipartReport;

public class MDNGenerator extends DirectMessageGenerator {

	protected String reporting_UA_name;
	protected String reporting_UA_product;
	protected String original_recipient;
	protected String final_recipient;
	protected String original_message_id;
	protected String disposition;
	protected String failure;
	protected String text;

	public MDNGenerator() {
		this.reporting_UA_name = "";
		this.reporting_UA_product = "";
		this.original_recipient = "";
		this.original_message_id = "";
		this.final_recipient = "";
		this.failure = "";
		this.disposition = "";
	}

	public MDNGenerator(String reporting_UA_name, String reporting_UA_product,
			String original_recipient, String final_recipient,
			String original_message_id, String disposition, String failure, String text) {
		super();
		this.reporting_UA_name = reporting_UA_name;
		this.reporting_UA_product = reporting_UA_product;
		this.original_recipient = original_recipient;
		this.final_recipient = final_recipient;
		this.original_message_id = original_message_id;
		this.disposition = disposition;
		this.failure = failure;
		this.text = text;
	}

	public MultipartReport create() throws MessagingException {

		// Create InterHeaders
		InternetHeaders notification = new InternetHeaders();
		if(!isNullorEmpty(reporting_UA_name) || !isNullorEmpty(reporting_UA_product)) {
			notification.addHeader("Reporting-UA", reporting_UA_name + "; " + reporting_UA_product);			
		}
		if(!isNullorEmpty(final_recipient))
			notification.addHeader("Final-Recipient", "rfc822; " + final_recipient);
		if(!isNullorEmpty(original_recipient))
			notification.addHeader("Original-Recipient", "rfc822; " + original_recipient);
		if(!isNullorEmpty(original_message_id))
			notification.addHeader("Original-Message-ID", original_message_id);
		if(!isNullorEmpty(disposition))
			notification.addHeader("Disposition", disposition);
		if(!isNullorEmpty(failure))
			notification.addHeader("Failure", failure);
		
		
		// Create disposition/notification
		DispositionNotification dispositionNotification = new DispositionNotification();
		dispositionNotification.setNotifications(notification);
		
		// Create the message parts. According to RFC 2298, there are two
		// compulsory parts and one optional part...
		MultipartReport multiPartReport = new MultipartReport(text, dispositionNotification);

		// Part 3: The optional third part, the original message is omitted.
		// We don't want to propogate over-sized, virus infected or
		// other undesirable mail!
		// There is the option of adding a Text/RFC822-Headers part, which
		// includes only the RFC 822 headers of the failed message. This is
		// described in RFC 1892. It would be a useful addition!
		return multiPartReport;
	}

	public MimeBodyPart generateBodyReport() throws MessagingException {
			MimeBodyPart m = new MimeBodyPart();
			m.setContent(create());
			return m;
	}
	
	public MimeBodyPart signMDN(MimeBodyPart mdnPart) throws MessagingException, Exception {
		return generateMultipartSigned(mdnPart);
	}
	
	public MimeMessage encryptMDN(MimeBodyPart body) throws Exception {
		return generateEncryptedMessage(body);
	}
	
	public MimeMessage generateMDN() throws MessagingException, Exception {
		return encryptMDN(signMDN(generateBodyReport()));
	}
	
	/**
	 * 
	 * Faulty MDN generation
	 * 
	 */
	
	// Null Envelope Sender
	public MimeMessage generateNullEnvelopeSenderMDN() throws MessagingException, Exception {
		return generateEncryptedMessageWithNullSender(signMDN(generateBodyReport()), true, false);
	}
	
	// Different Sender
	public MimeMessage generateDifferentSenderMDN() throws MessagingException, Exception {
		return generateEncryptedMessageWithNullSender(signMDN(generateBodyReport()), false, true);
	}
	
	public boolean isNullorEmpty(String value) {
		if(value == null) {
			return true;
		}
		if(value.equals("")) {
			return true;
		}
		return false;
	}

	public String getReporting_UA_name() {
		return reporting_UA_name;
	}

	public void setReporting_UA_name(String reporting_UA_name) {
		this.reporting_UA_name = reporting_UA_name;
	}

	public String getReporting_UA_product() {
		return reporting_UA_product;
	}

	public void setReporting_UA_product(String reporting_UA_product) {
		this.reporting_UA_product = reporting_UA_product;
	}

	public String getOriginal_recipient() {
		return original_recipient;
	}

	public void setOriginal_recipient(String original_recipient) {
		this.original_recipient = original_recipient;
	}

	public String getFinal_recipient() {
		return final_recipient;
	}

	public void setFinal_recipient(String final_recipient) {
		this.final_recipient = final_recipient;
	}

	public String getOriginal_message_id() {
		return original_message_id;
	}

	public void setOriginal_message_id(String original_message_id) {
		this.original_message_id = original_message_id;
	}

	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}

	public String getFailure() {
		return failure;
	}

	public void setFailure(String failure) {
		this.failure = failure;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
