package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase;

import java.util.Map

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface;
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement;
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.helper.MsgLabel;
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport;;

@Component
public class TestCaseXdrVal extends TestCase {

	@Autowired
	public TestCaseXdrVal(TestCaseExecutor executor) {
		super(executor);
	}

	@Override
	public Result run(Map context, String username) {
		executor.validateInputs(context,["targetEndpointTLS"])

        // Send an xdr with the endpoint created above
        context.endpoint = context.targetEndpointTLS
        context.simId = id + "_" + username
        context.wsaTo = context.targetEndpointTLS
        context.directTo = "xdrvalidator@$executor.hostname"
        context.directFrom = "xdrvalidator@$executor.hostname"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA

        XDRTestStepInterface step1 = executor.executeSendXDRStep(context)

        // Create a new test record
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step1).build()
        record.setStatus(step1.status)
        executor.db.addNewXdrRecord(record)

        // Build the message to return to the gui
        log.debug(MsgLabel.XDR_SEND_AND_RECEIVE.msg)
        def content = executor.buildSendXDRContent(step1)
        return new Result(record.criteriaMet, content)
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

		// Send an xdr with the endpoint created above
		context.simId = id + "_" + record.username
		context.endpoint = storeStep.simulator.endpointTLS
		context.wsaTo = storeStep.simulator.endpointTLS
		context.directTo = report.directFrom
		context.directFrom = "xdrvalidator@$executor.hostname"
		context.messageType = ArtifactManagement.Type.DELIVERY_STATUS_NOTIFICATION_SUCCESS
		context.relatesTo = report.messageId
		// TODO
//		context.messageId = report.messageId

		XDRTestStepInterface step2 = executor.executeSendXDRStep(context)
		record = new TestCaseBuilder(record).addStep(step2).build()
		record.status = step.status

		executor.db.updateXDRRecord(record)

	}

}
