package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
/**
 * Created by gerardin on 10/27/14.
 */
final class TestCase1 extends TestCaseStrategy {

    @Override
    UserMessage run(Object userInput, String username) {
        XDRTestStepInterface step = executeCreateEndpointsStep(username, userInput)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()

        persist(record)

        String msg = "successfully created new endpoints for test case ${id} with config : ${userInput}. Ready to receive message."
        return new UserMessage(UserMessage.Status.SUCCESS, msg, step.xdrSimulator)
    }

    @Override
    def UserMessage notifyXdrReceive(XDRRecordInterface record, XDRTestStepInterface previousStep, TkValidationReport report) {

        XDRTestStepInterface step = executeStoreXDRReport(report)

        String msg = "received xdr message"
        return new UserMessage(UserMessage.Status.SUCCESS, msg, step.criteriaMet)
    }
}
