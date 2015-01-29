package gov.nist.healthcare.ttt.webapp.xdr.core
import com.fasterxml.jackson.databind.ObjectMapper
import gov.nist.healthcare.ttt.database.xdr.*
import gov.nist.healthcare.ttt.direct.messageGenerator.MDNGenerator
import gov.nist.healthcare.ttt.direct.sender.DirectMessageSender
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageInfoForXdr
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageSenderForXdr
import gov.nist.healthcare.ttt.webapp.direct.listener.ListenerProcessor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestStepBuilder
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
            r  = sender.sendXdr(config)
            XDRReportItemInterface request = new XDRReportItemImpl()
            request.setReport(r.request)
            request.setReportType(XDRReportItemInterface.ReportType.REQUEST)

            XDRReportItemInterface response = new XDRReportItemImpl()
            response.setReport(r.response)
            response.setReportType(XDRReportItemInterface.ReportType.RESPONSE)


            XDRTestStepInterface step = new XDRTestStepImpl()
            step.xdrReportItems = new LinkedList<XDRReportItemInterface>()
            step.xdrReportItems.add(request)
            step.xdrReportItems.add(response)
            step.criteriaMet = XDRRecordInterface.CriteriaMet.MANUAL

            return step
        }
        catch (e){
            throw new Exception(MsgLabel.SEND_XDR_FAILED.msg,e)
        }

    }


    protected XDRTestStepInterface executeSendDirectStep(def context){

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


    protected XDRTestStepInterface executeCreateEndpointsStep(String tcid, String username, Map userInput){

        try {

            //TODO add check. We do not always have to create a brand new endpoint.
            //if an endpoint already exists, we just want to reuse it for the next run.
            //we get the endpoint for the last record (we should check it is alive as well)
            //then we add it to the step and create a new record

            def timestamp = clock.timestamp

            String endpointId = "${username}_${tcid}_${timestamp}"

            XDRSimulatorInterface sim = createEndpoint(endpointId,userInput)

            XDRTestStepInterface step = new XDRTestStepImpl()
            step.name = "CREATE_ENDPOINTS"
            step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
            step.xdrSimulator = sim

            return step
        }
        catch(e){
            throw new Exception(MsgLabel.CREATE_NEW_ENDPOINTS_FAILED.msg, e)
        }
    }

    protected XDRTestStepInterface executeSendMDN(def report){
        def generator = new MDNGenerator();
        generator.setReporting_UA_name("direct.nist.gov");
        generator.setReporting_UA_product("Security Agent");
        generator.setDisposition("automatic-action/MDN-sent-automatically;processed");
        generator.setFinal_recipient("transport-testing.nist.gov");
        generator.setFromAddress("from@transport-testing.nist.gov");
        generator.setOriginal_message_id("<812748939.14.1386951907564.JavaMail.tomcat7@ip-10-185-147-33.ec2.internal>");
        generator.setSubject("Automatic MDN");
        generator.setText("Your message was successfully processed.");
        generator.setToAddress("to@hit-dev.nist.gov");
        generator.setEncryptionCert(generator.getEncryptionCertByDnsLookup("to@hit-dev.nist.gov"))

        ListenerProcessor listener = new ListenerProcessor()
        listener.setCertificatesPath(directCertPath)
        listener.setCertPassword(directPassword)

        generator.setSigningCert(listener.getSigningPrivateCert("good"))
        generator.setSigningCertPassword(directPassword)

        def mdn = generator.generateMDN()

        //TODO fix
        try {
            new DirectMessageSender().send(25, "toAddress@hit-dev.nist.gov", mdn, "from@transport-testing.nist.gov", "toAddress@hit_dev.nist.gov")
        }
        catch(Exception e){
            log.error("for now we do not complain but fix that")
        }

        XDRTestStepInterface step = new XDRTestStepImpl()
        step.name = "DIRECT_MDN_SENT"
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PENDING

        return step
    }

    protected XDRTestStepInterface executeSendFailureMDN(def report){
        def generator = new MDNGenerator();
        generator.setReporting_UA_name("direct.nist.gov");
        generator.setReporting_UA_product("Security Agent");
        generator.setDisposition("automatic-action/MDN-sent-automatically;failure");
        generator.setFinal_recipient("transport-testing.nist.gov");
        generator.setFromAddress("from@transport-testing.nist.gov");
        generator.setOriginal_message_id("<812748939.14.1386951907564.JavaMail.tomcat7@ip-10-185-147-33.ec2.internal>");
        generator.setSubject("Automatic MDN");
        generator.setText("Your message was NOT successfully processed.");
        generator.setToAddress("to@hit-dev.nist.gov");
        generator.setEncryptionCert(generator.getEncryptionCertByDnsLookup("to@hit-dev.nist.gov"))

        ListenerProcessor listener = new ListenerProcessor()
        listener.setCertificatesPath(directCertPath)
        listener.setCertPassword(directPassword)

        generator.setSigningCert(listener.getSigningPrivateCert("good"))
        generator.setSigningCertPassword(directPassword)

        def mdn = generator.generateMDN()

        //TODO fix
        try {
            new DirectMessageSender().send(25, "toAddress@hit-dev.nist.gov", mdn, "from@transport-testing.nist.gov", "toAddress@hit_dev.nist.gov")
        }
        catch(Exception e){
            log.error("for now we do not complain but fix that")
        }

        XDRTestStepInterface step = new XDRTestStepImpl()
        step.name = "DIRECT_FAILURE_MDN_SENT"
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PENDING

        return step
    }

    def configureGlobalEndpoint(String name, Map params) {

        //TODO params not used for now
        XDRSimulatorInterface sim = createEndpoint(name, params)

        db.instance.xdrFacade.addNewSimulator(sim)

        log.info("new global simulator has been created.")
    }

    private XDRSimulatorInterface createEndpoint(String endpointId, Map params){

        EndpointConfig config = new EndpointConfig()

        //TODO harcoded here. This is dangerous!
        //Moreover it has to take into account implicit rules to satisfy Bill's views on ID.
        //For example, no dots are allowed anywhere, thus we need to sanitize username
        //(for example the GUI mandates to use email addresses for username, thus the dots!)
        config.putAll(params)
        config.name = endpointId.replaceAll(/\./,"_")


        log.info("trying to create new endpoints on toolkit... [${config.name}]")

        return receiver.createEndpoints(config)
    }

   protected XDRTestStepInterface executeStoreXDRReport(TkValidationReport report){

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
        catch(e){
            throw new Exception(MsgLabel.STORE_XDR_RECEIVE_FAILED.msg)
        }
    }

    XDRTestStepInterface recordSenderAddress(Map info) {
        XDRTestStepInterface step = new TestStepBuilder("BAD_CERT_MUST_DISCONNECT").build();
        step.hostname = info.ip_address
        return step
    }

    XDRTestStepInterface executeDirectAddressCorrelationStep(String tcid, String directFrom) {
        XDRTestStepInterface step = new TestStepBuilder("CORRELATE_ENDPOINT_WITH_DIRECTFROM_ADDRESS").build()

        //TODO handle exception if not found

        //TODO have a util method instead
        String simId = ("xdr.global.endpoint.tc."+tcid).replaceAll(/\./,"_")
        XDRSimulatorInterface sim = db.instance.xdrFacade.getSimulatorBySimulatorId(simId)
        step.xdrSimulator = sim
        step.directFrom = directFrom

        log.debug("$simId found. Correlation with direct address $directFrom performed.")
        return step
    }
}
