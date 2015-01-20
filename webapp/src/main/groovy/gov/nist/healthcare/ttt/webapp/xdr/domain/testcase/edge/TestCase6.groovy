package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
/**
 * Created by gerardin on 10/27/14.
 */
final class TestCase6 extends TestCaseBaseStrategy {

    public TestCase6(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    TestCaseEvent run(String tcid, Map context, String username) {
        XDRTestStepInterface step = executor.executeCreateEndpointsStep(tcid, username, context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        def content = new StandardContent()
        content.endpoint = step.xdrSimulator.endpointTLS

        log.info "successfully created new endpoints for test case ${tcid} with config : ${context}. Ready to receive message."
        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING , content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        done(XDRRecordInterface.CriteriaMet.MANUAL, updatedRecord)
    }
}
