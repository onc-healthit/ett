package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.receive.mu2

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseResult
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase14mu2 extends TestCase {


    @Autowired
    TestCase14mu2(TestCaseExecutor executor) {
        super(executor)
    }

    @Override
    TestCaseResult run(Map context, String username) {

        executor.validateInputs(context, ["targetEndpointTLS"])

        TestCaseBuilder builder = new TestCaseBuilder(id, username)

        // Correlate this test to a direct_from address and a simulator id so we can be notified
        XDRTestStepInterface step1 = executor.correlateRecordWithSimIdAndDirectAddress(sim, context.direct_from)

        sim = registerDocSrcEndpoint(username,context)

        context.simId = sim.simulatorId
        context.endpoint = sim.endpointTLS
        context.wsaTo = sim.endpointTLS
        //address attached to a hisp that is not trusted
        context.directTo = "Provider1@direct2.sitenv.org"
        context.directFrom = "testcase14mu2@nist.gov"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        XDRTestStepInterface step2 = executor.executeSendXDRStep(context)

        // Create a new test record
        XDRRecordInterface record = builder.addStep(step1).addStep(step2).build()
        record.setStatus(step2.status)
        executor.db.addNewXdrRecord(record)

        // Build the message to return to the gui
        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)
        def content = executor.buildSendXDRContent(step2)
        return new TestCaseResult(record.criteriaMet, content)
    }
}
