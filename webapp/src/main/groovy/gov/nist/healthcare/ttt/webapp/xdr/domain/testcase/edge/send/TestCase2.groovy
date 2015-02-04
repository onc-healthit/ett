package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */

@Component
final class TestCase2 extends TestCase {

    @Autowired
    public TestCase2(TestCaseExecutor ex){
        super(ex)
        sim = registerGlobalEndpoints(id, new HashMap())
    }

    @Override
    TestCaseEvent configure(Map context, String username) {
        XDRTestStepInterface step = new XDRTestStepImpl()
        step.name = "CORRELATE_RECORD_WITH_SIMID_AND_DIRECT_FROM_ADDRESS"
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        step.xdrSimulator = sim

        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()
        executor.db.addNewXdrRecord(record)

        def content = new StandardContent()
        content.endpoint = endpoints[0]
        content.endpointTLS = endpoints[1]

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        done(XDRRecordInterface.CriteriaMet.MANUAL, updatedRecord)

    }

    public TestCaseEvent getReport(XDRRecordInterface record) {
        executor.getSimpleSendReport(record)
    }
}
