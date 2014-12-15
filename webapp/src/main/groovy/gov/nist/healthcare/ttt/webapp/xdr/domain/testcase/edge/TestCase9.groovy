package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestStepBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
/**
 * Created by gerardin on 10/27/14.
 */
class TestCase9 extends TestCaseBaseStrategy {

    public TestCase9(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent run(String tcid, Map context, String username) {

        XDRTestStepImpl step = new TestStepBuilder("SEND_OVER_SSL_WITH_BAD_CERT").build()

        try {
            executor.tlsClient.connectOverBadTLS([hostname: context.hostname, port: context.port])
            log.info("tls connection succeeded.")
            step.criteriaMet = XDRRecordInterface.CriteriaMet.FAILED
        }
        catch(javax.net.ssl.SSLException e){
            log.info("tls connection failed.")
            e.printStackTrace()
            step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        }
        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        XDRRecordInterface.CriteriaMet testStatus = done(step.criteriaMet, record)

        def content = new StandardContent()

        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)

        new TestCaseEvent(testStatus,content)
    }
}
