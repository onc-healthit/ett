package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.receive.tls
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestStepBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase8 extends TestCase {

    @Autowired
    public TestCase8(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent configure(Map context, String username) {

        XDRTestStepImpl step = new TestStepBuilder("SEND_OVER_SSL_WITH_GOOD_CERT").build()

        try {
            executor.tlsClient.connectOverGoodTLS([ip_address: context.ip_address, port: context.port])
            log.info("tls connection succeeded.")
            step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        }
        catch(IOException e){
            log.info("tls connection failed.")
            e.printStackTrace()
            step.criteriaMet = XDRRecordInterface.CriteriaMet.FAILED
        }

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        XDRRecordInterface.CriteriaMet testStatus = done(step.criteriaMet, record)

        def content = new StandardContent()

        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)

        new TestCaseEvent(testStatus,content)
    }
}
