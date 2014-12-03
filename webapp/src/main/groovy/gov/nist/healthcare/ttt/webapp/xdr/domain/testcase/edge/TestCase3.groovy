package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy

/**
 * Created by gerardin on 10/27/14.
 */
class TestCase3 extends TestCaseStrategy {

    public TestCase3(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    UserMessage run(String tcid, Map context, String username) {
        XDRTestStepInterface step = executor.executeSendXDRStep(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        XDRRecordInterface.CriteriaMet testStatus = done(record,step.criteriaMet)

        return new UserMessage(UserMessage.Status.SUCCESS, MsgLabel.XDR_SEND_AND_RECEIVE.msg, testStatus)
    }
}
