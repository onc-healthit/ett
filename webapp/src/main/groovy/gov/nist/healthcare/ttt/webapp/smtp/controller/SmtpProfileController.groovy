package gov.nist.healthcare.ttt.webapp.smtp.controller

import java.security.Principal
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import gov.nist.healthcare.ttt.database.smtp.SmtpEdgeLogImpl;
import gov.nist.healthcare.ttt.database.smtp.SmtpEdgeProfileImpl;
import gov.nist.healthcare.ttt.database.smtp.SmtpEdgeProfileInterface;
import gov.nist.healthcare.ttt.smtp.ISMTPTestRunner;
import gov.nist.healthcare.ttt.smtp.ITestResult;
import gov.nist.healthcare.ttt.smtp.SMTPTestRunner;
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance;
import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException
import gov.nist.healthcare.ttt.webapp.smtp.model.SmtpTestInput;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/smtpProfile")
class SmtpProfileController {

	@Autowired
	private DatabaseInstance db
	
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	List<SmtpEdgeProfileInterface> getProfile(HttpServletRequest request) throws Exception {
		// Check if user is connected
		Principal principal = request.getUserPrincipal()
		if (principal != null) {
			String username = principal.getName()
			List<SmtpEdgeProfileInterface> res = db.getSmtpEdgeLogFacade().getAllProfilesByUsername(username)
			return res
			
		}
		return null;
		
	}
	
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	boolean saveProfile(@RequestBody HashMap<String, String> profile, Principal principal) throws Exception {
		SmtpEdgeProfileImpl smtpProfile = new SmtpEdgeProfileImpl()
		if (principal == null) {
			throw new TTTCustomException("0x0035", "You must be logged in to save profile")
		} else {
			String username = principal.getName()
			smtpProfile.setUsername(username)
		}
		
		smtpProfile.setProfileName(profile?.get("profileName"))
		smtpProfile.setSutEmailAddress(profile?.get("sutEmailAddress"))
		smtpProfile.setSutSMTPAddress(profile?.get("sutSMTPAddress"))
		smtpProfile.setSutUsername(profile?.get("sutUsername"))
		smtpProfile.setSutPassword(profile?.get("sutPassword"))
		
		db.getSmtpEdgeLogFacade().addNewSmtpProfile(smtpProfile)
	}
	
	@RequestMapping(method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	boolean deleteProfile(HttpServletRequest request) throws Exception {
		
		
	}
	
}
