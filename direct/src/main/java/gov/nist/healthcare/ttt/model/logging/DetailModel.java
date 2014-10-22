package gov.nist.healthcare.ttt.model.logging;

import gov.nist.healthcare.ttt.database.log.DetailImpl;
import gov.nist.healthcare.ttt.database.log.DetailInterface;

public class DetailModel extends DetailImpl implements DetailInterface {
	
	private String name;
	private String dts;
	private String found;
	private String expected;
	private String rfc;
	private Status status;
	
	public DetailModel(String dts, String name, String found, String expected,
			String rfc, Status status) {
		super();
		this.dts = dts;
		this.name = name;
		this.found = found;
		this.expected = expected;
		this.rfc = rfc;
		this.status = status;
	}

	public String getDts() {
		return dts;
	}

	public void setDts(String dts) {
		this.dts = dts;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFound() {
		return found;
	}

	public void setFound(String found) {
		this.found = found;
	}

	public String getExpected() {
		return expected;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public boolean isSuccess() {
		if(this.status.equals(DetailInterface.Status.ERROR)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "["
//				+ "name=" + name 
				+ "dts=" + dts 
//				+ ", found=" + found 
//				+ ", expected=" + expected 
//				+ ", rfc=" + rfc
//				+ ", status=" + status 
				+ "]\n";
	}

}
