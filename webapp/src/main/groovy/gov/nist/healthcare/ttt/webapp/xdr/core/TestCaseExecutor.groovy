package gov.nist.healthcare.ttt.webapp.xdr.core
import com.fasterxml.jackson.databind.ObjectMapper
import gov.nist.healthcare.ttt.database.xdr.*
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageInfoForXdr
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageSenderForXdr
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestStepBuilder
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/28/14.
 */
@Component
class TestCaseExecutor {

    private final DatabaseProxy db
    private final XdrReceiver receiver
    private final XdrSender sender
    private final Clock clock

    protected static ObjectMapper mapper = new ObjectMapper()

    private static Logger log = LoggerFactory.getLogger(TestCaseExecutor.class)




    @Autowired
    TestCaseExecutor(DatabaseProxy db, XdrReceiver receiver, XdrSender sender, Clock clock) {
        this.db = db
        this.receiver = receiver
        this.sender = sender
        this.clock = clock
    }

    protected XDRTestStepInterface executeSendXDRStep(Map config) {

        Object r
        try {
            r  = sender.sendXdr(config)
            XDRReportItemInterface report = new XDRReportItemImpl()
            report.setReport(r)
            XDRTestStepInterface step = new XDRTestStepImpl()
            step.xdrReportItems = new LinkedList<XDRReportItemInterface>()
            step.xdrReportItems.add(report)
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


    protected XDRTestStepInterface executeCreateEndpointsStep(String tcid, String username, def userInput){

        try {

            //TODO add check. We do not always have to create a brand new endpoint.
            //if an endpoint already exists, we just want to reuse it for the next run.
            //we get the endpoint for the last record (we should check it is alive as well)
            //then we add it to the step and create a new record

            def timestamp = clock.timestamp

            //TODO harcoded here. This is dangerous!
            //Moreover it has to take into account implicit rules to satisfy Bill's views on ID.
            //For example, no dots are allowed anywhere, thus we need to sanitize username
            //(for example the GUI mandates to use email addresses for username, thus the dots!)
            def sanitized_username = username.replaceAll(/\./,"_")
            String endpointId = "${sanitized_username}_${tcid}_${timestamp}"

            log.info("trying to generate endpoints with id : ${endpointId}")

            EndpointConfig config = new EndpointConfig()
            config.name = endpointId

            log.info("trying to create new endpoints on toolkit...")

            XDRSimulatorInterface sim = receiver.createEndpoints(config)

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


    def configureGlobalEndpoint(String name, Map params) {
        EndpointConfig config = new EndpointConfig()
        config.putAll(params)
        config.name = name

        log.info("trying to create new endpoints on toolkit...")

        XDRSimulatorInterface sim = receiver.createEndpoints(config)

        db.instance.xdrFacade.addNewSimulator(sim)

        log.info("new global simulator has been created.")
    }

    XDRTestStepInterface recordSenderAddress(Map info) {
        XDRTestStepInterface step = new TestStepBuilder("BAD_CERT_MUST_DISCONNECT").build();
        //TODO uncomment when ready
        // step.clientAddress = info.address
        return step
    }
}
