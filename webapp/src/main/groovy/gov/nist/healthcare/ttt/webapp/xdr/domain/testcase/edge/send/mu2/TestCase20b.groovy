package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send.mu2
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
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
final class TestCase20b extends TestCase {

    final public String badEndpoint = id

    @Autowired
    public TestCase20b(TestCaseExecutor ex) {
        super(ex)
        sim = registerGlobalEndpoints(badEndpoint, new HashMap())
    }

    @Override
    TestCaseEvent configure(Map context, String username) {

        executor.createRecordForSenderTestCase(context,username,id,sim)

        def content = new StandardContent()

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.MANUAL, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step

        step = executor.executeSendFailureMDN(report)

        record = new TestCaseBuilder(record).addStep(step).build()

        record.criteriaMet = XDRRecordInterface.CriteriaMet.MANUAL

        executor.db.updateXDRRecord(record)
        executor.db.updateXDRRecord(record)
        executor.db.updateXDRRecord(record)

        done(XDRRecordInterface.CriteriaMet.MANUAL, record)

    }
}
