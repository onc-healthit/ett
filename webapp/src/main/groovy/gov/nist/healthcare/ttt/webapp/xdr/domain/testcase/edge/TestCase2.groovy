package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */

@Component
final class TestCase2 extends TestCase {

    TestCase1 testcase

    @Autowired
    public TestCase2(TestCaseExecutor ex) {
        super(ex)
        testcase = new TestCase1(ex)
    }

    @Override
    TestCaseEvent run(String tcid, Map context, String username) {
        testcase.run(tcid,context,username)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {
        testcase.notifyXdrReceive(record,report)
    }
}
