package gov.nist.healthcare.ttt.webapp.direct.model.messageStatus;

import java.util.Collection;

public class MessageStatusList {
	
	private String type;
	private String directAddress;
	private Collection<MessageStatusDetail> logList;
	
	
	public MessageStatusList(String type, String directAddress,
			Collection<MessageStatusDetail> logList) {
		super();
		this.type = type;
		this.directAddress = directAddress;
		this.logList = logList;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getDirectAddress() {
		return directAddress;
	}


	public void setDirectAddress(String directAddress) {
		this.directAddress = directAddress;
	}


	public Collection<MessageStatusDetail> getLogList() {
		return logList;
	}


	public void setLogList(Collection<MessageStatusDetail> logList) {
		this.logList = logList;
	}

}
