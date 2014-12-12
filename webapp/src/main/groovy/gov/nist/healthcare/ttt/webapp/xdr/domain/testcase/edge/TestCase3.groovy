package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
/**
 * Created by gerardin on 10/27/14.
 */
class TestCase3 extends TestCaseBaseStrategy {

    public TestCase3(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent run(String tcid, Map context, String username) {

        context.directTo = "testcase3@nist.gov"
        context.directFrom = "testcase3@nist.gov"
        context.wsaTo = context.targetEndpoint
        context.messageType = ArtifactManagement.Type.XDR_FULL_METADATA

        XDRTestStepInterface step = executor.executeSendXDRStep(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        XDRRecordInterface.CriteriaMet testStatus = done(step.criteriaMet, record)

        def content = new StandardContent()
        content.response = step.xdrReportItems.last().report

        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)

        new TestCaseEvent(testStatus,content)
    }
}
