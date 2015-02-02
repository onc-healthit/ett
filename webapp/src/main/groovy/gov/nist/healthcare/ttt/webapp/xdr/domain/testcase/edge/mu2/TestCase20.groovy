package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.mu2
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
final class TestCase20 extends TestCase {

    final String goodEndpoint = "xdr_global_endpoint_tc_20_goodEndpoint"
    final String badEndpoint = "xdr.global.endpoint.tc.20.badEndpoint"

    @Autowired
    public TestCase20(TestCaseExecutor ex) {
        super(ex)
        registerGlobalEndpoints(goodEndpoint, new HashMap())
        registerGlobalEndpoints(badEndpoint, new HashMap())
    }

    @Override
    TestCaseEvent run(String tcid, Map context, String username) {

        XDRTestStepInterface step = executor.executeDirectAddressCorrelationStep(tcid, context.direct_from)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        log.info  "test case ${tcid} : successfully configured. Ready to receive messages."

        def content = new StandardContent()

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.MANUAL, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        XDRTestStepInterface step2


            if (report.simId == goodEndpoint) {
                step2 = executor.executeSendProcessedMDN(report)
            } else if (report.simId == badEndpoint) {
                step2 = executor.executeSendFailureMDN(report)
            } else {
                throw new Exception("problem in the workflow")
            }

        updatedRecord = new TestCaseBuilder(updatedRecord).addStep(step).build()

        executor.db.addNewXdrRecord(updatedRecord)

        done(XDRRecordInterface.CriteriaMet.MANUAL, updatedRecord)

    }
}
