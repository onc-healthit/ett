package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
/**
 * Created by gerardin on 10/27/14.
 */
final class TestCase6 extends TestCaseBaseStrategy {

    TestCase2 testcase

    public TestCase6(TestCaseExecutor ex) {
        super(ex)
        testcase = new TestCase2(ex)
    }

    @Override
    UserMessage run(String tcid, Map context, String username) {
        testcase.run()
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {
        testcase.notifyXdrReceive(record,report)
    }
}
