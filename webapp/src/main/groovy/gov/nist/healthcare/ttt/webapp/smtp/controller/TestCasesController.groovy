package gov.nist.healthcare.ttt.webapp.smtp.controller;

import java.util.ArrayList;

import gov.nist.healthcare.ttt.smtp.ISMTPTestRunner;
import gov.nist.healthcare.ttt.smtp.ITestResult;
import gov.nist.healthcare.ttt.smtp.SMTPTestRunner;
import gov.nist.healthcare.ttt.webapp.smtp.model.SmtpTestInput;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/smtpTestCases")
class TestCasesController {

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
    ArrayList<ITestResult> startTestCases(@RequestBody SmtpTestInput ti) throws Exception {
		ISMTPTestRunner smtpTestRunner = new SMTPTestRunner()
		ArrayList<ITestResult> res = new ArrayList<ITestResult>()
		def trs = smtpTestRunner.runTestCase(ti.getTestCaseNumber().toInteger(), ti.convert())
		trs.each { res << it }
		
		res
	}
}
