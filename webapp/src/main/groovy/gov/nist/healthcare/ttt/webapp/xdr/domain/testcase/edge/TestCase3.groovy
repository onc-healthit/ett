package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
/**
 * Created by gerardin on 10/27/14.
 */
class TestCase3 extends TestCaseStrategy {

    public TestCase3(TestCaseExecutor executor){
        super(executor)
    }


    @Override
    UserMessage run(String tcid, Object userInput, String username) {
            XDRTestStepInterface step = executor.executeSendXDRStep()

            //Create a new test record.
            XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

            executor.db.addNewXdrRecord(record)

            String msg = "xdr message has been sent and response received."
            return new UserMessage(UserMessage.Status.SUCCESS, msg, step.xdrReportItems.last())
        }
}
