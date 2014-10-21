package gov.nist.healthcare.ttt.webapp.direct.model.messageStatus;

public class MessageStatusDetail {
	
	private String a_address;
	private String b_messageID;
	private String c_time;
	private String d_status;
	
	public MessageStatusDetail(String a_address, String b_messageID,
			String c_time, String d_status) {
		super();
		this.a_address = a_address;
		this.b_messageID = b_messageID;
		this.c_time = c_time;
		this.d_status = d_status;
	}

	public String getA_from() {
		return a_address;
	}

	public void setA_from(String a_from) {
		this.a_address = a_from;
	}

	public String getB_messageID() {
		return b_messageID;
	}

	public void setB_messageID(String b_messageID) {
		this.b_messageID = b_messageID;
	}

	public String getC_time() {
		return c_time;
	}

	public void setC_time(String c_time) {
		this.c_time = c_time;
	}

	public String getD_status() {
		return d_status;
	}

	public void setD_status(String d_status) {
		this.d_status = d_status;
	}	

}
