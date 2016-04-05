package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send.mu2

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Content
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseSender
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase20amu2 extends TestCaseSender {

    @Autowired
    public TestCase20amu2(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["direct_from,targetEndpointTLS"])

        //correlate this test to a direct_from address and a simulator id so we can be notified
        TestCaseBuilder builder = new TestCaseBuilder(id, username)
        XDRTestStepInterface step1 = executor.correlateRecordWithSimIdAndDirectAddress(sim, context.direct_from)
        executor.db.addNewXdrRecord(builder.addStep(step1).build())

        def content = new Content()
        content.endpoint = endpoints[0]
        content.endpointTLS = endpoints[1]
        return new Result(Status.PENDING, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step

        def context = new HashMap()
        XDRTestStepInterface step1
        context.targetEndpointTLS = step1.xdrSimulator.endpointTLS
        sim = registerDocSrcEndpoint(record.username,context)

        // Send an xdr with the endpoint created above
        context.simId = step1.xdrSimulator.simulatorId
        context.endpoint = step1.xdrSimulator.endpointTLS
        context.wsaTo = step1.xdrSimulator.endpointTLS
        context.directTo = step1.directFrom
        context.directFrom = "testcase20a@$executor.hostname"
        context.messageType = ArtifactManagement.Type.DELIVERY_STATUS_NOTIFICATION_SUCCESS

        XDRTestStepInterface step2 = executor.executeSendXDRStep(context)
        record = new TestCaseBuilder(record).addStep(step2).build()
        record.status = step.status

        executor.db.updateXDRRecord(record)

    }
}
