package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.receive
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.helper.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase5 extends TestCase {

    @Autowired
    public TestCase5(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["targetEndpointTLS"])

        // Send an xdr with the endpoint created above
		context.endpoint = context.targetEndpointTLS
        context.simId = id + "_" + username
        context.wsaTo = context.endpointTLS
        context.directTo = "testcase5@$executor.hostname"
        context.directFrom = "testcase5@$executor.hostname"
        context.messageType = ArtifactManagement.Type.XDR_FULL_METADATA
		
        XDRTestStepInterface step1 = executor.executeSendXDRStep(context)

        // Create a new test record
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step1).build()
        record.setStatus(step1.status)
        executor.db.addNewXdrRecord(record)

        // Build the message to return to the gui
        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)
        def content = executor.buildSendXDRContent(step1)
        return new Result(record.criteriaMet, content)
    }


}
