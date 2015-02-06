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
final class TestCase20a extends TestCase {

    final public String goodEndpoint = id

    @Autowired
    public TestCase20a(TestCaseExecutor ex) {
        super(ex)
        sim = registerGlobalEndpoints(goodEndpoint, new HashMap())

    }

    @Override
    TestCaseEvent configure(Map context, String username) {

        executor.createRecordForSenderTestCase(context,username,id,sim)

        log.info "test case ${id} : successfully configured. Ready to receive messages."

        def content = new StandardContent()

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.MANUAL, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step

        step = executor.executeSendProcessedMDN(report)

        record = new TestCaseBuilder(record).addStep(step).build()

        record.criteriaMet = XDRRecordInterface.CriteriaMet.MANUAL

        executor.db.updateXDRRecord(record)
        executor.db.updateXDRRecord(record)
        executor.db.updateXDRRecord(record)

        done(XDRRecordInterface.CriteriaMet.MANUAL, record)

    }
}
