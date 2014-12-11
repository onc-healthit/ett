package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
/**
 * Created by gerardin on 10/27/14.
 */
class TestCase4 extends TestCaseBaseStrategy {

    public TestCase4(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent run(String tcid, Map context, String username) {

        //TODO allow for bad content
        context.directTo = "directTo"
        context.directFrom = "directFrom"
        context.wsaTo = context.targetEndpoint
        context.messageType = ArtifactManagement.Type.XDR_FULL_METADATA

        XDRTestStepInterface step = executor.executeSendXDRStep(context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        XDRRecordInterface.CriteriaMet testStatus = done(XDRRecordInterface.CriteriaMet.MANUAL, record)

        return new TestCaseEvent(step.xdrReportItems.last(),testStatus)
    }
}
