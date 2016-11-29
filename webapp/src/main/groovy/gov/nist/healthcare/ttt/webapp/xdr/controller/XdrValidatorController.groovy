package gov.nist.healthcare.ttt.webapp.xdr.controller;

import java.security.Principal;
import java.util.HashMap

import javax.annotation.PostConstruct
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController
import org.w3c.dom.Document
import org.xml.sax.InputSource

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.database.xdr.XDRVanillaImpl
import gov.nist.healthcare.ttt.database.xdr.XDRVanillaInterface
import gov.nist.healthcare.ttt.parsing.Parsing;
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor;
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager;
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.ui.UIResponse;;
import gov.nist.healthcare.ttt.xdr.api.XdrReceiverImpl
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig;
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import gov.nist.hit.ds.wsseTool.api.config.ContextFactory;
import gov.nist.hit.ds.wsseTool.api.config.GenContext
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess
import gov.nist.hit.ds.wsseTool.api.exceptions.ValidationException
import gov.nist.hit.ds.wsseTool.validation.ValidationResult
import gov.nist.hit.ds.wsseTool.validation.WsseHeaderValidator
import gov.nist.hit.xdrsamlhelper.SamlHeaderApi
import gov.nist.hit.xdrsamlhelper.SamlHeaderApi.SamlHeaderValidationResults
import gov.nist.hit.xdrsamlhelper.SamlHeaderApiImpl.SamlHeaderExceptionImpl
import groovy.util.slurpersupport.GPathResult


@RestController
@RequestMapping("api/xdrvalidator")
public class XdrValidatorController {

	private static Logger log = LoggerFactory.getLogger(XdrValidatorController.class);

	private final TestCaseManager testCaseManager
	private final XdrReceiverImpl receiverImpl

	@Value('${toolkit.user}')
	private String toolkitUser

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
	private DatabaseInstance db;

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

		log.debug("receive a new validation report: $httpBody")

		try {

			def tkValidationReport = new TkValidationReport()
			def report = new XmlSlurper().parseText(httpBody)

			parseReportFormat(tkValidationReport, report)
			parseRequest(tkValidationReport , report.requestMessageBody)
			parseResponse(tkValidationReport, report.responseMessageBody)

			handleSAML(tkValidationReport)

		}
		catch(Exception e) {
			e.printStackTrace();
			//	    log.error("receive an invalid validation report. Bad payload rejected :\n $httpBody")
		}
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
	XDRVanillaInterface status(Principal principal) {

		if (principal == null) {
			throw new Exception("user not identified")
		}

		String id = "XdrVal";

		String simId = toolkitUser + "__" + id + "_" + principal.getName().replace('.', '_');
		simId = simId.toLowerCase();

		if (principal == null) {
			throw new Exception("user not identified")
		}

		log.info("Getting latest XDR for sim " + simId)

		//rename variables to make their semantic more obvious
		def tcid = id
		def username = principal.getName()
		def status
		String msg
		Result result

		log.debug("received status request for tc$id from $username")

		try {
			return db.xdrFacade.getLatestXDRVanillaBySimId(simId)
		} catch(Exception e){
			throw e
		}
	}

	def parseStatus(String registryResponseStatus) {
		if (registryResponseStatus.contains("Failure")) {
			return Status.FAILED
		} else if (registryResponseStatus.contains("Success")) {
			return Status.PASSED
		}
	}

	def parseResponse(TkValidationReport tkValidationReport, GPathResult response){

		//TODO modify : all that to extract registryResponseStatus info!
		String content = response.text()
		def registryResponse = content.split("<.?S:Body>")
		def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
		def registryResponseStatus = registryResponseXml.@status.text()
		def criteriaMet = parseStatus(registryResponseStatus)
		tkValidationReport.status = criteriaMet
	}

	def parseRequest(TkValidationReport tkValidationReport, GPathResult request){
		String text = request.text()
		String unescapeXml = StringEscapeUtils.unescapeXml(text)

		// Get Patient ID
		def patientId = Parsing.getPatientIDFromWsse(text)

		// Parse it to see if it is SAML message
		String saml = Parsing.getWsseHeaderFromMTOM(text)
		if(saml != null) {
			try {
				SamlHeaderValidationResults samlRes = validateSAMLHeader(saml, patientId)
				String samlReport = samlRes.getErrors() == 0 ? "No Errors" : ("Errors: " + String.join("\n", samlRes.getErrors()));
				//samlReport += "Warnings: " + String.join("\n", samlRes.getWarnings());
				//samlReport += "Info: " + String.join("\n", samlRes.getDetails());
				tkValidationReport.samlReport = samlReport;
			} catch(Exception e) {
				tkValidationReport.samlReport = "FAILED";
				throw new Exception(e.getMessage())
			}
		} else {
			tkValidationReport.samlReport = "NOSAML";
		}
	}

	def stripMailTo(String address) {
		if(address!=null) {
			if(address.toLowerCase().startsWith("mailto:")) {
				return address.split("mailto:")[1];
			}
		}
		return address;
	}

	def parseReportFormat(TkValidationReport tkValidationReport,  GPathResult report){
		tkValidationReport.request = report.requestMessageHeader.text() + "\r\n\r\n" + report.requestMessageBody.text()
		tkValidationReport.response = report.responseMessageHeader.text() + "\r\n\r\n" + report.responseMessageBody.text()
		tkValidationReport.simId = report.simulatorUser.text() + "__" + report.simulatorId.text()
	}

	def validateSAMLHeader(String document, String patientId) {
		GenContext context = ContextFactory.getInstance();
		try{
			SamlHeaderApi samlApi = SamlHeaderApi.getInstance();
			
			return samlApi.validate(document, patientId, Thread.currentThread().getContextClassLoader().getResourceAsStream("goodKeystore/goodKeystore"), "1", "changeit", "changeit");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	private void handleSAML(TkValidationReport report) {
		log.info("handle toolkit saml report for sim id: " + report.simId)
		
		try {

			XDRVanillaImpl xdr = new XDRVanillaImpl()
			xdr.setRequest(report.request)
			xdr.setResponse(report.response)
			xdr.setSamlReport(report.samlReport)
			xdr.setSimId(report.simId)

			db.getXdrFacade().addNewXdrVanilla(xdr);
		} catch(Exception e) {
			log.error("Could not store the XDR to the database");
			e.printStackTrace();
		}
	}
}
