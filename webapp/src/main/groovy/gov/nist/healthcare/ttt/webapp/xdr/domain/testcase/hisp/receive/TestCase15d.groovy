package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.receive
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.RandomIntegerGenerator
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseResult
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase15d extends TestCase {

    @Autowired
    public TestCase15d(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseResult run(Map context, String username) {

        executor.validateInputs(context,["targetEndpoint"])

        context.directTo = "testcase15d@nist.gov"
        context.directFrom = "testcase15b@nist.gov"
        context.wsaTo = context.targetEndpoint

        context.messageType = chooseMissingMetadata()

        log.debug("building message with missing metadata of type : ${context.messageType}")

        XDRTestStepInterface step = executor.executeSendBadXDRStep(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()
        record.status = step.status
        executor.db.addNewXdrRecord(record)

        def content = executor.buildSendXDRContent(step)

        new TestCaseResult(record.criteriaMet,content)
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
