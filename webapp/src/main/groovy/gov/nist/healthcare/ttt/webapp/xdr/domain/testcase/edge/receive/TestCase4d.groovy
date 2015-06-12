package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.receive
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.RandomIntegerGenerator
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase4d extends TestCase {

    @Autowired
    public TestCase4d(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent configure(Map context, String username) {

        context.directTo = "testcase4b@nist.gov"
        context.directFrom = "testcase4b@nist.gov"
        context.wsaTo = context.targetEndpoint

        context.messageType = chooseMissingMetadata()

        log.debug("building message with missing metadata of type : ${context.messageType}")

        XDRTestStepInterface step = executor.executeSendXDRStep(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        XDRRecordInterface.CriteriaMet testStatus = done(step.criteriaMet, record)

        def content = executor.buildSendXDRContent(step)

        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)

        new TestCaseEvent(testStatus,content)
    }

    def chooseMissingMetadata(){
        def randomChoice = RandomIntegerGenerator.generate(5)
        switch(randomChoice){
            case 1 :
                ArtifactManagement.Type.NEGATIVE_MISSING_METADATA_ELEMENTS1
                break
            case 2 :
                ArtifactManagement.Type.NEGATIVE_MISSING_METADATA_ELEMENTS2
                break
            case 3 :
                ArtifactManagement.Type.NEGATIVE_MISSING_METADATA_ELEMENTS3
                break
            case 4 :
                ArtifactManagement.Type.NEGATIVE_MISSING_METADATA_ELEMENTS4
                break
            case 5 :
                ArtifactManagement.Type.NEGATIVE_MISSING_METADATA_ELEMENTS5
            default:
                ArtifactManagement.Type.NEGATIVE_MISSING_METADATA_ELEMENTS1

        }
    }
}
