package gov.nist.healthcare.ttt.webapp.direct.direcForXdr;

import java.util.Date;

public class DirectMessageInfoForXdr {
	
	private String messageId;
	private String from;
	private String to;
	private Date date;
	private String attachmentName;
	
	public DirectMessageInfoForXdr(String messageId, String from, String to,
			Date date, String attachmentName) {
		super();
		this.messageId = messageId;
		this.from = from;
		this.to = to;
		this.date = date;
		this.attachmentName = attachmentName;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

}
