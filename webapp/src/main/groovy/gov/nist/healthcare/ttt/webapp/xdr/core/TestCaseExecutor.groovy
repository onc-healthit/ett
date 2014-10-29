package gov.nist.healthcare.ttt.webapp.xdr.core

import com.fasterxml.jackson.databind.ObjectMapper
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemImpl
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/28/14.
 */
@Component
class TestCaseExecutor {

    private final DatabaseInstance db
    private final XdrReceiver receiver
    private final ResponseHandler handler
    private final XdrSender sender
    private final Clock clock

    protected static ObjectMapper mapper = new ObjectMapper()

    @Autowired
    TestCaseExecutor(DatabaseInstance db, XdrReceiver receiver, XdrSender sender, Clock clock) {
        this.db = db
        this.receiver = receiver
        this.sender = sender
        this.clock = clock
    }

    protected XDRTestStepInterface executeSendXDRStep() {

        Object r
        try {
            r  = sender.sendXdr()

            String json = mapper.writeValueAsString(r)
            XDRReportItemInterface report = new XDRReportItemImpl()
            report.setReport(json)
            XDRTestStepInterface step = new XDRTestStepImpl()
            step.xdrReportItems = new LinkedList<XDRReportItemInterface>()
            step.xdrReportItems.add(report)
            step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        }
        catch (e){
            return new Exception(MsgLabel.SEND_XDR_FAILED,e)
        }

    }


    protected XDRTestStepInterface executeCreateEndpointsStep(String username, def userInput){

        try {
            def timestamp = clock.timestamp

            String endpointId = "${username}.${id}.${timestamp}"

            log.info("trying to generate endpoints with id : ${endpointId}")

            EndpointConfig config = new EndpointConfig()
            config.name = endpointId

            log.info("trying to create new endpoints on toolkit...")

            XDRSimulatorInterface sim = receiver.createEndpoints(config)

            XDRTestStepInterface step = new XDRTestStepImpl()
            step.name = "ttt configures endpoints for receiving xdr message with limited metadata"
            step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
            step.xdrSimulator = sim

            return step
        }
        catch(e){
            throw new Exception(MsgLabel.UNABLE_TO_CREATE_NEW_ENDPOINTS, e)
        }
    }

   protected XDRTestStepInterface executeStoreXDRReport(TkValidationReport report){

        try {
            String id = report.simId
            println "handle report for simulator with simID : $id"

            //TODO instead of making it unique, just return the last one (the current)
            XDRRecordInterface rec = db.xdrFacade.getXDRRecordBySimulatorId(id)

            XDRTestStepInterface step = new XDRTestStepImpl()

            //TODO this is where we convert the report into sth we can store
            def reportRecord = new XDRReportItemImpl()
            reportRecord.report = report.status
            step.xdrReportItems.add(reportRecord)
            step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED

            //TODO save new Step in current record
            //an update function is necessary

            return step
        }
        catch(e){
            throw new Exception(MsgLabel.STORE_XDR_RECEIVE_FAILED)
        }
    }

    def persist(XDRRecordInterface record){
        try {
            String recordId = db.getXdrFacade().addNewXdrRecord(record)
            return recordId
        }
        catch(e){
            throw new Exception(MsgLabel.UNABLE_TO_CREATE_NEW_ENDPOINTS,e)
        }
    }


}
