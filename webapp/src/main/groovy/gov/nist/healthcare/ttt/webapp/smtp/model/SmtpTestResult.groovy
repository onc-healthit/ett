package gov.nist.healthcare.ttt.webapp.smtp.model;

import gov.nist.healthcare.ttt.smtp.ITestResult;
import gov.nist.healthcare.ttt.smtp.TestResult;

import java.util.LinkedHashMap;

public class SmtpTestResult {

	boolean testSuccess
	boolean criteriaMet
	int testCaseId
	LinkedHashMap<String, String> testRequestResponses
	String testCaseDesc
	String lastTestResponse
	int lastTestResultStatus

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

}
