package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
import gov.nist.healthcare.ttt.xdr.api.TLSReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.TLSValidationReport
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/14/14.
 */
@Component
class ResponseHandler implements IObserver{

    private final TestCaseManager manager
    private final XdrReceiver xdrReceiver
    private final TLSReceiver tlsReceiver
    private final DatabaseProxy db

    @Autowired
    public ResponseHandler(TestCaseManager manager, XdrReceiver xdrReceiver, TLSReceiver tlsReceiver, DatabaseProxy db){
        this.manager = manager
        this.xdrReceiver = xdrReceiver
        this.tlsReceiver = tlsReceiver
        this.db = db
        xdrReceiver.registerObserver(this)
        tlsReceiver.registerObserver(this)
    }

    @Override
    def getNotification(Message msg) {

        println "notification received"

        if(msg.status == Message.Status.ERROR){
            throw Exception()
        }


        try {
            handle(msg.content)
        }
        catch(Exception e){
            e.printStackTrace()
            println "notification content not understood"
        }
    }

    private handle(TLSValidationReport report){
        println "handle tls report"

        XDRRecordInterface rec = null
        //rec = db.instance.xdrFacade.getXDRRecordByAddress(report.address)
        TestCaseBaseStrategy testcase = manager.findTestCase("7")
        testcase.notifyTLSReceive(rec, report)
    }

    private handle(TkValidationReport report){

        String msgId = report.messageId
        String unescapedMsgId = "<" + msgId + ">"

        XDRRecordInterface rec = db.instance.xdrFacade.getXDRRecordByMessageId(unescapedMsgId)

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

        TestCaseBaseStrategy testcase = manager.findTestCase(rec.testCaseNumber)
        testcase.notifyXdrReceive(rec, report)
    }


}
