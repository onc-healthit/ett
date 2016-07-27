package gov.nist.healthcare.ttt.webapp.xdr.controller;

import java.security.Principal;
import java.util.HashMap

import javax.annotation.PostConstruct;

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor;
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager;
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.ui.UIResponse;;
import gov.nist.healthcare.ttt.xdr.api.XdrReceiverImpl
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig;

@RestController
@RequestMapping("api/xdrvalidator")
public class XdrValidatorController {
	
	private static Logger log = LoggerFactory.getLogger(XdrValidatorController.class);
	
	private final TestCaseManager testCaseManager
	private final XdrReceiverImpl receiverImpl

	@Value('${xdr.notification.prefix}')
	private String prefix

	@Value('${server.contextPath}')
	private String contextPath

	@Value('${toolkit.endpoint.port}')
	private String port

	@Value('${direct.listener.domainName}')
	private String hostname

	private String fullNotificationUrl
	
	@Autowired
	public XdrValidatorController(TestCaseManager manager, XdrReceiverImpl receiverImpl) {
		testCaseManager = manager
		this.receiverImpl = receiverImpl
	}
	
	@PostConstruct
	def buildUrls(){
		fullNotificationUrl = prefix+"://"+hostname+":"+port+contextPath+"/api/xdrvalidator/receive"

		log.debug("notification url is :" + fullNotificationUrl)
	}
	
	@RequestMapping(value = "/endpoints", method = RequestMethod.GET)
	@ResponseBody
	XDRSimulatorInterface configure(Principal principal) throws Exception {
		
		//User must be authenticated in order to run a test case=
		if (principal == null) {
			throw new TTTCustomException("0x0080", "User not identified");
		}

		log.debug("received configure request for xdr validator")
		def username = principal.getName()

		EndpointConfig config = new EndpointConfig();
		config.name = "XdrVal_" + username;
		
		return receiverImpl.createEndpoints(config, fullNotificationUrl);
	}
	
	@RequestMapping(value = 'receive/{id}', consumes = "application/xml")
	@ResponseBody
	public void receiveBySimulatorId(@RequestBody String httpBody) {

		log.info("receive a new validation report: $httpBody")
	}
	
	@RequestMapping(value = "/run", method = RequestMethod.POST)
	@ResponseBody
	UIResponse run(@RequestBody HashMap config, Principal principal) {
		
		String id = "XdrVal";

		//User must be authenticated in order to run a test case
		if (principal == null) {
			return new UIResponse(UIResponse.UIStatus.ERROR, "user not identified")
		}

		//rename variables to make their semantic more obvious
		def tcid = id
		def username = principal.getName()

		log.debug("received run request for tc$tcid from $username")

		try {
			Result event = testCaseManager.run(id, config, username)
			return new UIResponse(UIResponse.UIStatus.SUCCESS,"ran tc $tcid", event)
		}
		catch(Exception e){
			e.printStackTrace() //TODO flag so it is not logged in production
			return new UIResponse(UIResponse.UIStatus.ERROR, e.getMessage(), null)
		}


	}
	
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	@ResponseBody
	UIResponse status(Principal principal) {

		String id = "XdrVal";
		
		if (principal == null) {
			return new UIResponse(UIResponse.UIStatus.ERROR, "user not identified")
		}

		//rename variables to make their semantic more obvious
		def tcid = id
		def username = principal.getName()
		def status
		String msg
		Result result

		log.debug("received status request for tc$id from $username")

		try {
			result = testCaseManager.status(username, tcid)

			log.debug("[status is $result.criteriaMet]")
			status = UIResponse.UIStatus.SUCCESS
			msg = "result of test case $id"
			return new UIResponse<Status>(status, msg , result)
		}catch(Exception e){
			e.printStackTrace()
			status = UIResponse.UIStatus.ERROR
			msg = "error while trying to fetch status for test case $id"
			result = new Result(Status.FAILED,e.getCause())
			return new UIResponse<Status>(status, msg , result)
		}
	}
}
