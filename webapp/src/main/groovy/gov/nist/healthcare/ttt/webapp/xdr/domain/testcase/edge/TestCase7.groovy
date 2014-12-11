package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
/**
 * Created by gerardin on 10/27/14.
 */
final class TestCase7 extends TestCaseBaseStrategy {

    public TestCase7(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    TestCaseEvent run(String tcid, Map context, String username) {

         XDRTestStepInterface step = executor.recordSenderAddress(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        log.info "successfully created new endpoints for test case ${tcid} with config : ${context}. Ready to receive message."
        return new TestCaseEvent(context, XDRRecordInterface.CriteriaMet.PENDING)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {
        record.testSteps.last().criteriaMet = report.status

        done(report.status, record)
    }
}
