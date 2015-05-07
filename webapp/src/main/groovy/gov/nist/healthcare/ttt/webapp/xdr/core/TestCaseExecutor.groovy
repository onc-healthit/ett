package gov.nist.healthcare.ttt.webapp.xdr.core

import com.fasterxml.jackson.databind.ObjectMapper
import gov.nist.healthcare.ttt.database.xdr.*
import gov.nist.healthcare.ttt.direct.messageGenerator.MDNGenerator
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageInfoForXdr
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageSenderForXdr
import gov.nist.healthcare.ttt.webapp.direct.listener.ListenerProcessor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestStepBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import gov.nist.healthcare.ttt.xdr.api.TLSClient
import gov.nist.healthcare.ttt.xdr.api.TLSReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
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

    public final DatabaseProxy db
    private final XdrReceiver receiver
    private final XdrSender sender
    private final Clock clock
    public final TLSReceiver tlsReceiver
    public final TLSClient tlsClient

    protected static ObjectMapper mapper = new ObjectMapper()

    private static Logger log = LoggerFactory.getLogger(TestCaseExecutor.class)

    @Autowired
    TestCaseExecutor(DatabaseProxy db, XdrReceiver receiver, XdrSender sender, TLSReceiver tlsReceiver, TLSClient tlsClient, Clock clock) {
        this.db = db
        this.receiver = receiver
        this.sender = sender
        this.tlsReceiver = tlsReceiver
        this.tlsClient = tlsClient
        this.clock = clock
    }

    protected XDRTestStepInterface executeSendXDRStep(Map config) {

        def r
        try {
            r = sender.sendXdr(config)
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
            step.criteriaMet = XDRRecordInterface.CriteriaMet.MANUAL

            return step
        }
        catch (e) {
            throw new Exception(MsgLabel.SEND_XDR_FAILED.msg, e)
        }

    }


    protected XDRTestStepInterface executeSendDirectStep(def context) {

        DirectMessageInfoForXdr info = new DirectMessageSenderForXdr().sendDirectWithCCDAForXdr(context.sutDirectAddress, Integer.parseInt(context.sutDirectPort))

        XDRTestStepInterface step = new XDRTestStepImpl()
        step.name = "SEND_DIRECT"
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        step.messageId = info.messageId
        step.xdrReportItems = new LinkedList<>()

        //TODO we need to store a info object
        XDRReportItemInterface report = new XDRReportItemImpl()
        report.report = info.messageId
        step.xdrReportItems.add(report)

        return step
    }


    protected XDRTestStepInterface executeCreateEndpointsStep(String tcid, String username, Map userInput) {

        try {

            //TODO add check. We do not always have to create a brand new endpoint.
            //if an endpoint already exists, we just want to reuse it for the next configure.
            //we get the endpoint for the last record (we should check it is alive as well)
            //then we add it to the step and create a new record

            def timestamp = clock.timestamp

            String endpointId = "${username}_${tcid}_${timestamp}"

            XDRSimulatorInterface sim = createEndpoint(endpointId, userInput)

            XDRTestStepInterface step = new XDRTestStepImpl()
            step.name = "CREATE_ENDPOINTS"
            step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
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
            step.criteriaMet = XDRRecordInterface.CriteriaMet.MANUAL

        }
        catch (Exception e) {
            step.name = "DIRECT_PROCESSED_MDN_ERROR"
            step.criteriaMet = XDRRecordInterface.CriteriaMet.FAILED
        }
        return step
    }

    protected XDRTestStepInterface executeSendFailureMDN(def report) {

        XDRTestStepInterface step = new XDRTestStepImpl()

        try {
            sendMDN(report, "failure")
            step.name = "DIRECT_FAILURE_MDN_SENT"
            step.criteriaMet = XDRRecordInterface.CriteriaMet.MANUAL

        }
        catch (Exception e) {
            step.name = "DIRECT_FAILURE_MDN_ERROR"
            step.criteriaMet = XDRRecordInterface.CriteriaMet.FAILED
        }
        return step
    }

    private def  sendMDN(TkValidationReport report, String state) {

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

        if(toAddress.contains("@")) {
            hostname = toAddress.split("@")[1]
        }

        log.debug("MDN send. Hostname to lookup : ${hostname}")

        new DirectMessageSender().send(senderPort, hostname, mdn, "from@transport-testing.nist.gov", toAddress)
    }


    XDRSimulatorInterface configureGlobalEndpoint(String name, Map params) {

        XDRSimulatorInterface sim = db.instance.xdrFacade.getSimulatorBySimulatorId(name)

        if (sim == null) {
            log.debug("simulator with id $name does not exists. It will be created now!")
            sim = createEndpoint(name, params)
            String id = db.instance.xdrFacade.addNewSimulator(sim)
            log.debug("new global simulator has been created with the following id : $id")
        } else {
            log.debug("simulator with id $name already exists.")
        }

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


        log.info("trying to create new endpoints on toolkit... [${config.name}]")

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
            step.criteriaMet = report.status
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

    TestCaseEvent getSimpleSendReport(XDRRecordInterface record) {
        def content = new StandardContent()

        if (record.criteriaMet != XDRRecordInterface.CriteriaMet.PENDING) {

            def step = record.getTestSteps().last()

            if (!step.xdrReportItems.empty) {
                log.info(step.xdrReportItems.size() + " report(s) found.")
                def report = step.xdrReportItems
                content.request = report.find { it.reportType == XDRReportItemInterface.ReportType.REQUEST }.report
                content.response = report.find { it.reportType == XDRReportItemInterface.ReportType.RESPONSE }.report
            }
        }

        return new TestCaseEvent(record.criteriaMet, content)
    }

    def createRecordForSenderTestCase(Map context, String username, String tcid, XDRSimulatorInterface sim) {
        def step = executeCorrelationStep(context, sim)
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()
        db.addNewXdrRecord(record)
    }

    def executeCorrelationStep(Map context, XDRSimulatorInterface sim) {
        XDRTestStepInterface step = new XDRTestStepImpl()
        step.name = "CORRELATE_RECORD_WITH_SIMID_AND_DIRECT_FROM_ADDRESS"
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        step.xdrSimulator = sim
        step.directFrom = context.direct_from
        step.hostname = context.ip_address
        return step
    }
}
