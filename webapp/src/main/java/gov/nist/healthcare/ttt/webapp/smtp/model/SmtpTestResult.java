package gov.nist.healthcare.ttt.webapp.smtp.model;

import gov.nist.healthcare.ttt.smtp.ITestResult;
import gov.nist.healthcare.ttt.smtp.TestResult;

import java.util.LinkedHashMap;

public class SmtpTestResult {

	private boolean testSuccess;
	private boolean criteriaMet;
	private int testCaseId;
	private LinkedHashMap<String, String> testRequestResponses;
	private String testCaseDesc;
	private String lastTestResponse;
	private int lastTestResultStatus;

	public SmtpTestResult() {

	}

	public SmtpTestResult(boolean testSuccess, boolean criteriaMet,
			int testCaseId, LinkedHashMap<String, String> testRequestResponses,
			String testCaseDesc, String lastTestResponse,
			int lastTestResultStatus) {
		super();
		this.testSuccess = testSuccess;
		this.criteriaMet = criteriaMet;
		this.testCaseId = testCaseId;
		this.testRequestResponses = testRequestResponses;
		this.testCaseDesc = testCaseDesc;
		this.lastTestResponse = lastTestResponse;
		this.lastTestResultStatus = lastTestResultStatus;
	}

	public SmtpTestResult(ITestResult trs) {
//		this.testSuccess = ((TestResult) trs).isTestSuccess();
		this.criteriaMet = trs.isCriteriaMet();
		this.testCaseId = trs.getTestCaseId();
		this.testRequestResponses = trs.getTestRequestResponses();
		this.testCaseDesc = trs.getTestCaseDesc();
		this.lastTestResponse = ((TestResult) trs).getLastTestResponse();
		this.lastTestResultStatus = ((TestResult) trs).getLastTestResultStatus();
	}

	public boolean isTestSuccess() {
		return testSuccess;
	}

	public void setTestSuccess(boolean testSuccess) {
		this.testSuccess = testSuccess;
	}

	public boolean isCriteriaMet() {
		return criteriaMet;
	}

	public void setCriteriaMet(boolean criteriaMet) {
		this.criteriaMet = criteriaMet;
	}

	public int getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(int testCaseId) {
		this.testCaseId = testCaseId;
	}

	public LinkedHashMap<String, String> getTestRequestResponses() {
		return testRequestResponses;
	}

	public void setTestRequestResponses(
			LinkedHashMap<String, String> testRequestResponses) {
		this.testRequestResponses = testRequestResponses;
	}

	public String getTestCaseDesc() {
		return testCaseDesc;
	}

	public void setTestCaseDesc(String testCaseDesc) {
		this.testCaseDesc = testCaseDesc;
	}

	public String getLastTestResponse() {
		return lastTestResponse;
	}

	public void setLastTestResponse(String lastTestResponse) {
		this.lastTestResponse = lastTestResponse;
	}

	public int getLastTestResultStatus() {
		return lastTestResultStatus;
	}

	public void setLastTestResultStatus(int lastTestResultStatus) {
		this.lastTestResultStatus = lastTestResultStatus;
	}
}
