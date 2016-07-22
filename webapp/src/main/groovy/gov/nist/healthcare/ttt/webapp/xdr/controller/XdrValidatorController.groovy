package gov.nist.healthcare.ttt.webapp.xdr.controller;

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController

import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager;
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.ui.UIResponse;;

@RestController
@RequestMapping("api/xdrvalidator")
public class XdrValidatorController {
	
	private static Logger log = LoggerFactory.getLogger(XdrValidatorController.class);
	
	private final TestCaseManager testCaseManager\
	
	@Autowired
	public XdrValidatorController(TestCaseManager manager) {
		testCaseManager = manager
	}
	
	@RequestMapping(value = "/endpoints", method = RequestMethod.GET)
	@ResponseBody
	UIResponse configure() {

		log.debug("received configure request for xdr validator")

		try {
			Result event = testCaseManager.configure("XdrValidator")
			return new UIResponse(UIResponse.UIStatus.SUCCESS,"XdrValidator is configured", event)
		}
		catch(Exception e){
			return new UIResponse(UIResponse.UIStatus.ERROR, e.getMessage(), null)
		}


	}
}
