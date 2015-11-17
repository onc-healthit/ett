package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.receive
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase4a extends TestCase {

    @Autowired
    public TestCase4a(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent configure(Map context, String username) {

        context.directTo = "testcase4a@nist.gov"
        context.directFrom = "testcase4a@nist.gov"
        context.wsaTo = context.targetEndpoint
        context.messageType = ArtifactManagement.Type.NEGATIVE_BAD_SOAP_HEADER

        XDRTestStepInterface step = executor.executeSendBadXDRStep(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        XDRRecordInterface.CriteriaMet testStatus = done(step.criteriaMet, record)

        def content = executor.buildSendXDRContent(step)

        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)

        new TestCaseEvent(testStatus,content)
    }
}
