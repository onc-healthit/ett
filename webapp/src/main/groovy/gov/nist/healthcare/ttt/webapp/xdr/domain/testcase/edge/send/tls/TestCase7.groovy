package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send.tls
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import gov.nist.healthcare.ttt.xdr.domain.TLSValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase7 extends TestCase {

    @Autowired
    public TestCase7(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    TestCaseEvent configure(Map context, String username) {

         XDRTestStepInterface step = executor.recordSenderAddress(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        String endpoint = executor.tlsReceiver.getEndpoint()
        def content = new StandardContent()
        content.endpoint = endpoint

        log.info "successfully recorded hostname for test case ${id} with config : ${context}. Ready to test TLS."


        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, content)
    }

    @Override
    public void notifyTLSReceive(XDRRecordInterface record, TLSValidationReport report) {
        record.testSteps.last().criteriaMet = report.status

        done(report.status, record)
    }
}
