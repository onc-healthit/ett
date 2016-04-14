package gov.nist.healthcare.ttt.webapp.xdr.core

import com.fasterxml.jackson.databind.ObjectMapper
import gov.nist.healthcare.ttt.database.xdr.*
import gov.nist.healthcare.ttt.direct.messageGenerator.MDNGenerator
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender
import gov.nist.healthcare.ttt.model.sendDirect.SendDirectMessage
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.SendDirectService
import gov.nist.healthcare.ttt.webapp.direct.listener.ListenerProcessor
import gov.nist.healthcare.ttt.webapp.xdr.domain.helper.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestStepBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Content
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import gov.nist.healthcare.ttt.xdr.api.*
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 *
 * Factors functions that are used by many test cases.
 * See <code>TestCase</code>
 *
 * Created by gerardin on 10/28/14.
 */
@Component
class TestCaseExecutor {

	@Value('${direct.certificates.repository.path}')
	private String directCertPath

	@Value('${direct.certificates.password}')
	private String directPassword

	@Value('${direct.sender.port}')
	private int senderPort

	@Value('${direct.listener.domainName}')
	public String hostname

	public final DatabaseProxy db
	private final XdrReceiver receiver
	private final XdrSender sender
	private final BadXdrSender badSender
	private final Clock clock
	public final TLSReceiver tlsReceiver //we need a unique endpoint for all tls related test
	public final TLSClient tlsClient
	public final SendDirectService directService

	protected static ObjectMapper mapper = new ObjectMapper()

	private static Logger log = LoggerFactory.getLogger(TestCaseExecutor.class)

	@Autowired
	TestCaseExecutor(DatabaseProxy db, XdrReceiver receiver, XdrSender sender, BadXdrSender badSender, TLSReceiver tlsReceiver, TLSClient tlsClient, Clock clock, SendDirectService directService) {
		this.db = db
		this.receiver = receiver
		this.sender = sender
		this.badSender = badSender
		this.tlsReceiver = tlsReceiver
		this.tlsClient = tlsClient
		this.clock = clock
		this.directService = directService
	}

	protected XDRTestStepInterface executeSendXDRStep(Map config) {
		try {
			def r = sender.sendXdr(config)

			XDRReportItemInterface request = new XDRReportItemImpl()
			request.setReport(r.request)
			request.setReportType(XDRReportItemInterface.ReportType.REQUEST)

			XDRReportItemInterface response = new XDRReportItemImpl()
			response.setReport(r.response)
			response.setReportType(XDRReportItemInterface.ReportType.RESPONSE)


			XDRTestStepInterface step = new XDRTestStepImpl()
			step.name = "XDR_SEND_TO_SUT"
			step.xdrReportItems = new LinkedList<XDRReportItemInterface>()
			step.xdrReportItems.add(request)
			step.xdrReportItems.add(response)

			//TODO for now we only send back MANUAL CHECKS
			step.status = Status.MANUAL

			return step
		}
		catch (e) {
			throw new Exception(MsgLabel.SEND_XDR_FAILED.msg, e)
		}

	}

	protected XDRTestStepInterface executeSendBadXDRStep(Map config) {

		def r
		try {
			r = badSender.sendXdr(config)
			XDRReportItemInterface request = new XDRReportItemImpl()
			request.setReport(r.request)
			request.setReportType(XDRReportItemInterface.ReportType.REQUEST)

			XDRReportItemInterface response = new XDRReportItemImpl()
			response.setReport(r.response)
			response.setReportType(XDRReportItemInterface.ReportType.RESPONSE)


			XDRTestStepInterface step = new XDRTestStepImpl()
			step.name = "XDR_SEND_TO_SUT"
			step.xdrReportItems = new LinkedList<XDRReportItemInterface>()
			step.xdrReportItems.add(request)
			step.xdrReportItems.add(response)

			//Eventually we could have automatic checks coming from the toolkit
			step.status = Status.MANUAL

			return step
		}
		catch (e) {
			throw new Exception(MsgLabel.SEND_XDR_FAILED.msg, e)
		}
	}


	protected XDRTestStepInterface executeSendDirectStep(def context, String msgType) {

		String subject = "hisp testing"
		if(msgType.endsWith(".zip")) {
			subject = "XDM/1.0/DDM"
		}
		SendDirectMessage msg = new SendDirectMessage("hisp testing", subject, context.direct_from,
				context.direct_to, msgType, "good", "", "", true, false)
		directService.sendDirect(msg)


		XDRTestStepInterface step = new XDRTestStepImpl()
		step.name = "SEND_DIRECT"
		step.status = Status.PASSED

		return step
	}


	protected XDRTestStepInterface executeCreateEndpointsStep(String tcid, String username, Map userInput) {

		try {

			//TODO add check. We do not always have to create a brand new endpoint.
			//if an endpoint already exists, we just want to reuse it for the next run.
			//we get the endpoint for the last record (we should check it is alive as well)
			//then we add it to the step and create a new record

			def timestamp = clock.timestamp

			String endpointId = "${username}_${tcid}_${timestamp}"

			XDRSimulatorInterface sim = createEndpoint(endpointId, userInput)

			XDRTestStepInterface step = new XDRTestStepImpl()
			step.name = "CREATE_ENDPOINTS"
			step.status = Status.PASSED
			step.xdrSimulator = sim

			return step
		}
		catch (e) {
			throw new Exception(MsgLabel.CREATE_NEW_ENDPOINTS_FAILED.msg, e)
		}
	}

	protected XDRTestStepInterface executeSendProcessedMDN(TkValidationReport report) {

		XDRTestStepInterface step = new XDRTestStepImpl()

		try {
			sendMDN(report, "processed")
			step.name = "DIRECT_PROCESSED_MDN_SENT"
			step.status = Status.MANUAL

		}
		catch (Exception e) {
			step.name = "DIRECT_PROCESSED_MDN_ERROR"
			step.status = Status.FAILED
		}
		return step
	}

	protected XDRTestStepInterface executeSendFailureMDN(def report) {

		XDRTestStepInterface step = new XDRTestStepImpl()

		try {
			sendMDN(report, "failure")
			step.name = "DIRECT_FAILURE_MDN_SENT"
			step.status = Status.MANUAL

		}
		catch (Exception e) {
			step.name = "DIRECT_FAILURE_MDN_ERROR"
			step.status = Status.FAILED
		}
		return step
	}

	private def sendMDN(TkValidationReport report, String state) {

		String toAddress = report.directFrom
		def generator = new MDNGenerator();

		generator.setReporting_UA_name("direct.nist.gov");
		generator.setReporting_UA_product("Security Agent");
		generator.setDisposition("automatic-action/MDN-sent-automatically;$state");
		generator.setFinal_recipient("from@transport-testing.nist.gov");//COMING FROM MESSAGE TO_ADDRESS
		generator.setFromAddress("from@transport-testing.nist.gov"); //COMING FROM MESSAGE TO_ADDRESS
		generator.setOriginal_message_id("<$report.messageId>");
		generator.setSubject("Automatic MDN");
		generator.setText("Your message was successfully processed.");
		generator.setToAddress(toAddress);
		generator.setEncryptionCert(generator.getEncryptionCertByDnsLookup(toAddress))

		ListenerProcessor listener = new ListenerProcessor()
		listener.setCertificatesPath(directCertPath)
		listener.setCertPassword(directPassword)

		generator.setSigningCert(listener.getSigningPrivateCert("good"))
		generator.setSigningCertPassword(directPassword)

		def mdn = generator.generateMDN()

		def hostname = toAddress

		if (toAddress.contains("@")) {
			hostname = toAddress.split("@")[1]
		}

		log.debug("MDN send. Hostname to lookup : ${hostname}")

		new DirectMessageSender().send(senderPort, hostname, mdn, "from@transport-testing.nist.gov", toAddress)
	}


	XDRSimulatorInterface configureEndpoint(String name, Map params) {
		def sim = createEndpoint(name, params)
		log.debug("new simulator has been created with the following id : $name")

		return sim
	}

	private XDRSimulatorInterface createEndpoint(String endpointId, Map params) {

		EndpointConfig config = new EndpointConfig()

		//TODO harcoded here. This is dangerous!
		//Moreover it has to take into account implicit rules to satisfy Bill's views on ID.
		//For example, no dots are allowed anywhere, thus we need to sanitize username
		//(for example the GUI mandates to use email addresses for username, thus the dots!)
		config.putAll(params)
		config.name = endpointId.replaceAll(/\./, "_")


		log.debug("trying to create new endpoints on toolkit... [${config.name}]")

		return receiver.createEndpoints(config)
	}

	protected XDRTestStepInterface executeStoreXDRReport(TkValidationReport report) {

		try {
			XDRTestStepInterface step = new XDRTestStepImpl()
			step.xdrReportItems = new LinkedList<XDRReportItemInterface>()

			//TODO this is where we convert the report into sth we can store
			def response = new XDRReportItemImpl()
			response.report = report.response
			response.reportType = XDRReportItemInterface.ReportType.RESPONSE

			def request = new XDRReportItemImpl()
			request.report = report.request
			request.reportType = XDRReportItemInterface.ReportType.REQUEST

			step.xdrReportItems.add(response)
			step.xdrReportItems.add(request)
			step.status = report.status
			step.name = "XDR_RECEIVE"

			return step
		}
		catch (e) {
			throw new Exception(MsgLabel.STORE_XDR_RECEIVE_FAILED.msg)
		}
	}

	XDRTestStepInterface recordSenderAddress(Map info) {
		XDRTestStepInterface step = new TestStepBuilder("BAD_CERT_MUST_DISCONNECT").build();
		step.hostname = info.ip_address
		return step
	}

	protected XDRTestStepInterface recordSimulator(String endpoint) {
		XDRSimulatorInterface sim = new XDRSimulatorImpl();
		sim.setEndpointTLS(endpoint);

		XDRTestStepInterface step = new XDRTestStepImpl()
		step.name = "STORE_ENDPOINT"
		step.status = Status.PASSED
		step.xdrSimulator = sim

		return step
	}

	Result getSimpleSendReport(XDRRecordInterface record) {
		def content = new Content()

		if (record.criteriaMet != Status.PENDING) {

			def step = record.getTestSteps().last()

			if (!step.xdrReportItems.empty) {
				log.info(step.xdrReportItems.size() + " report(s) found.")
				def report = step.xdrReportItems
				content.request = report.find { it.reportType == XDRReportItemInterface.ReportType.REQUEST }.report
				content.response = report.find { it.reportType == XDRReportItemInterface.ReportType.RESPONSE }.report
			}
		}

		return new Result(record.criteriaMet, content)
	}

	public def correlateRecordWithSimIdAndDirectAddress(XDRSimulatorInterface sim, String directAddress) {
		XDRTestStepInterface step = new XDRTestStepImpl()
		step.name = "CORRELATE_RECORD_WITH_SIMID_AND_DIRECT_ADDRESS"
		step.status = Status.PASSED
		step.xdrSimulator = sim
		//TODO change name in the DB
		step.directFrom = directAddress
		return step
	}

	public def correlateRecordWithSimIdAndIpAddress(String ipAddress, XDRSimulatorInterface sim) {
		XDRTestStepInterface step = new XDRTestStepImpl()
		step.name = "CORRELATE_RECORD_WITH_SIMID_AND_IP_ADDRESS"
		step.status = Status.PASSED
		step.xdrSimulator = sim
		//TODO change name in the DB
		step.hostname = ipAddress
		return step
	}

	def buildSendXDRContent(XDRTestStepInterface step) {
		Content content = new Content()

		step.xdrReportItems.each {
			if (it.reportType == XDRReportItemInterface.ReportType.REQUEST) {
				content.request = it.report
			} else if (it.reportType == XDRReportItemInterface.ReportType.RESPONSE) {
				content.response = it.report
			}
		}

		return content
	}

	def validateInputs(Map<String, String> context, List<String> keys) {
		for (String key : keys) {
			//TODO validate / sanitize inputs
			if (!context.containsKey(key)) {
				throw new Exception("$key not provided")
			}
			if(context.get(key).isEmpty()){
				throw new Exception("empty value for $key");
			}
		}
	}

	def validateInputs(Map<String, String> context, List<String> keys, List<String> optionalKeys) {
		for (String key : keys) {
			//TODO validate / sanitize inputs
			if (!context.containsKey(key)) {
				throw new Exception("$key not provided")
			}
			if(context.get(key).isEmpty()){
				throw new Exception("empty value for $key");
			}
		}

		for(String key : optionalKeys){
			if(context.containsKey(key) && context.get(key).isEmpty()){
				throw new Exception("empty value for $key");
			}
		}
	}
}
