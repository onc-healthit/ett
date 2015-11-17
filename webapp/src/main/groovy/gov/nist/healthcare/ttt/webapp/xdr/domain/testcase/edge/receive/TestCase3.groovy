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

import java.text.SimpleDateFormat
/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase3 extends TestCase {

    @Autowired
    public TestCase3(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent run(Map context, String username) {

        executor.validateInputs(context,["targetEndpointTLS"])

        TestCaseBuilder builder = new TestCaseBuilder(id, username)

        // Correlate this test to a direct_from address and a simulator id so we can be notified
        XDRTestStepInterface step1 = executor.correlateRecordWithSimIdAndDirectAddress(sim, context.direct_from)

        // Create an endpoint on the toolkit
        // because the toolkit does not allow updating existing simulators, we have to generate unique ids each time
        //this is true for the case were we need to send with the simulator
        def config = new HashMap()
        config.type = 'docsrc'
        config.endpointTLS = context.targetEndpointTLS
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        def simId = id+"_"+username+"_"+timeStamp
        sim = registerEndpoint(simId, config)

        // Send an xdr with the endpoint created above
        context.simId = sim.simulatorId
        context.endpoint = sim.endpointTLS
        context.wsaTo = sim.endpointTLS
        context.directTo = "testcase3@nist.gov"
        context.directFrom = "testcase3@nist.gov"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        XDRTestStepInterface step2 = executor.executeSendXDRStep(context)

        // Create a new test record
        XDRRecordInterface record = builder.addStep(step1).addStep(step2).build()
        record.setCriteriaMet(step2.criteriaMet)
        executor.db.addNewXdrRecord(record)

        // Build the message to return to the gui
        log.debug(MsgLabel.XDR_SEND_AND_RECEIVE.msg)
        def content = executor.buildSendXDRContent(step2)
        return new TestCaseEvent(record.criteriaMet, content)
    }


}
