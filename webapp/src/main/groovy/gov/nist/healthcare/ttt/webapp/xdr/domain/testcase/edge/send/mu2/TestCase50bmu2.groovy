package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send.mu2

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl;
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
final class TestCase50bmu2 extends TestCaseSender {

    @Autowired
    public TestCase50bmu2(TestCaseExecutor ex) {
        super(ex)
    }
	
	@Override
	Result configure(){
		Content c = new Content()
		c.endpoint = sim.endpoint
		c.endpointTLS = sim.endpointTLS
		new Result(Status.PENDING, c)
	}

    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["direct_from", "targetEndpointTLS"])

        //correlate this test to a direct_from address and a simulator id so we can be notified
        TestCaseBuilder builder = new TestCaseBuilder(id, username)
        XDRTestStepInterface step1 = executor.correlateRecordWithSimIdAndDirectAddress(sim, context.direct_from);
		XDRTestStepInterface step2 = executor.recordSimulator(context.targetEndpointTLS);
        executor.db.addNewXdrRecord(builder.addStep(step1).addStep(step2).build())

        def content = new Content()
        content.endpoint = endpoints[0]
        content.endpointTLS = endpoints[1]
		
        return new Result(Status.MANUAL, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)
		
		XDRTestStepImpl storeStep = record.getTestSteps().find() {
			it.name == "STORE_ENDPOINT"
		}

        def context = new HashMap()
        XDRTestStepInterface step1 = executor.executeStoreXDRReport(report)
        context.targetEndpointTLS = storeStep.simulator.endpointTLS
        sim = registerDocSrcEndpoint(record.username,context)

        // Send an xdr with the endpoint created above
        context.simId = id + "_" + record.username
        context.endpoint = storeStep.simulator.endpointTLS
        context.wsaTo = storeStep.simulator.endpointTLS
        context.directTo = report.directFrom
        context.directFrom = "testcase50b@$executor.hostname"
        context.messageType = ArtifactManagement.Type.DELIVERY_STATUS_NOTIFICATION_FAILURE
		context.relatesTo = report.messageId

        XDRTestStepInterface step2 = executor.executeSendXDRStep(context)
        record = new TestCaseBuilder(record).addStep(step2).build()
        record.status = step.status

        executor.db.updateXDRRecord(record)

    }
}
