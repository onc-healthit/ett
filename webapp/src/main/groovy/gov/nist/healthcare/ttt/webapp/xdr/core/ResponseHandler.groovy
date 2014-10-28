package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemImpl
import gov.nist.healthcare.ttt.xdr.api.notification.IObserver
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/14/14.
 */
@Component
class ResponseHandler implements IObserver{


    private final TestCaseManager manager

    @Autowired
    public ResponseHandler(TestCaseManager manager){
        this.manager = manager
        manager.receiver.registerObserver(this)
    }

    @Override
    def getNotification(Message msg) {



        println "notification received"

        try {
            handle(msg.content)
        }
        catch(Exception e){
            e.printStackTrace()
            println "notification content not understood"
        }
    }


    private handle(TkValidationReport report){

        String id = report.simId
        println "handle report for simulator with simID : $id"
        XDRRecordInterface rec = manager.db.xdrFacade.getXDRRecordBySimulatorId(id)
        def step = rec.getTestSteps().find {
            it.xdrSimulator?.simulatorId == id
        }

        def reportRecord =  new XDRReportItemImpl()
        reportRecord.report = report.status
        step.xdrReportItems.add(reportRecord)
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED


        //TODO we need to handle the validation report and change the status accordingly
        //an update function is necessary
        manager.db.xdrFacade.addNewReportItem(step.xdrTestStepID,reportRecord)

    }
}
