package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
/**
 * Created by gerardin on 10/27/14.
 */
final class TestCase1 extends TestCaseStrategy {

    public TestCase1(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    UserMessage run(String tcid, Map context, String username) {

         XDRTestStepInterface step = executor.executeCreateEndpointsStep(tcid, username, context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        String msg = "successfully created new endpoints for test case ${tcid} with config : ${context}. Ready to receive message."
        return new UserMessage(UserMessage.Status.SUCCESS, msg, new TestCaseEvent(step.xdrSimulator.endpoint, XDRRecordInterface.CriteriaMet.PENDING))
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        done(updatedRecord,step.criteriaMet)

    }
}
