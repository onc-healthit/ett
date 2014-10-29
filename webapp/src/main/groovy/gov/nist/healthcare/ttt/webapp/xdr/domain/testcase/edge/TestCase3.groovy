package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
/**
 * Created by gerardin on 10/27/14.
 */
class TestCase3 extends TestCaseStrategy {

    @Override
    UserMessage run(Object userInput, String username) {
            XDRTestStepInterface step = executeSendXDRStep()

            //Create a new test record.
            XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()

            persist(record)

            String msg = "xdr message has been sent and response received."
            return new UserMessage(UserMessage.Status.SUCCESS, msg, step.xdrReportItems.last())
        }
}
