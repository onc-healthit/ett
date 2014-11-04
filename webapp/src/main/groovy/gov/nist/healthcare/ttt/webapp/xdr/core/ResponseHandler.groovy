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

        String msgId = report.messageId

        //TODO try to correlate with messageId.
   //     XDRRecordInterface rec = db.getRecordByMessageId(id)
        XDRRecordInterface rec = null


        //if not working, find with simulatorId
        if(rec != null) {
            println "handle report for message with messageId : $msgId"
        }
        else{
            String simId = report.simId
            rec = db.getLatestXDRRecordBySimulatorId(simId)
            println "handle report for simulator with simId : $simId"
        }

        //else
        //should report the unability to correlate this report to a test

        TestCaseStrategy testcase = manager.findTestCase(rec.testCaseNumber)
        testcase.notifyXdrReceive(rec, report)
    }


}
