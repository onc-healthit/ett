package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
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
    private final XdrReceiver receiver
    private final DatabaseProxy db

    @Autowired
    public ResponseHandler(TestCaseManager manager, XdrReceiver receiver, DatabaseProxy db){
        this.manager = manager
        this.receiver = receiver
        this.db = db
        receiver.registerObserver(this)
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

        //TODO instead of making it unique, just return the last one (the current)
        XDRRecordInterface rec = db.getXDRRecordBySimulatorId(id)

        TestCaseStrategy testcase = manager.findTestCase(rec.testCaseNumber)
        testcase.notifyXdrReceive(rec, report)
    }


}
