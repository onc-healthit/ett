package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.receive
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
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
    Result run(Map context, String username) {

        executor.validateInputs(context,["targetEndpointTLS"])

        context.directTo = "testcase4a@$executor.hostname"
        context.directFrom = "testcase4a@$executor.hostname"
        context.wsaTo = context.targetEndpointTLS
        context.messageType = ArtifactManagement.Type.NEGATIVE_BAD_SOAP_HEADER

        XDRTestStepInterface step = executor.executeSendBadXDRStep(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()
        record.status = step.status
        executor.db.addNewXdrRecord(record)

        def content = executor.buildSendXDRContent(step)

        new Result(record.criteriaMet,content)
    }
}
