package gov.nist.healthcare.ttt.webapp.smtp.controller;

import gov.nist.healthcare.ttt.smtp.ISMTPTestRunner;
import gov.nist.healthcare.ttt.smtp.ITestResult;
import gov.nist.healthcare.ttt.smtp.SMTPTestRunner;
import gov.nist.healthcare.ttt.webapp.smtp.model.SmtpTestInput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@Controller
@RequestMapping("/api/smtpTestCases")
public class TestCasesController {

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
    ArrayList<ITestResult> startTestCases(@RequestBody SmtpTestInput ti) throws Exception {
		ISMTPTestRunner smtpTestRunner = new SMTPTestRunner();
		ArrayList<ITestResult> res = new ArrayList<ITestResult>();
		int testCaseNumber = Integer.parseInt(ti.getTestCaseNumber());
		ITestResult[] trs;
		trs = smtpTestRunner.runTestCase(testCaseNumber, ti.convert());
		if (trs != null) {
			for (ITestResult t : trs) {
				res.add(t);
			}
		}
		return res;
	}
}
