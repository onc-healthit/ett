package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.mu2
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
/**
 * Created by gerardin on 10/27/14.
 */
final class TestCase19 extends TestCaseBaseStrategy {

    public TestCase19(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    TestCaseEvent run(String tcid, Map context, String username) {

        XDRTestStepInterface step = executor.executeDirectAddressCorrelationStep(tcid, context.direct_from)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        log.info  "test case ${tcid} : successfully configured. Ready to receive messages."

        def content = new StandardContent()
        content.endpoint = step.xdrSimulator.endpointTLS

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)
        step.directFrom = report.directFrom
        step.messageId = report.messageId

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        if(record.testSteps.size() != 4) {
            executor.db.updateXDRRecord(record)
        }
        else {

            def steps = record.testSteps.findAll{
                 it.name == "XDR_RECEIVE"
            }



            boolean one = steps[0].messageId != steps[1].messageId
            boolean two = steps[0].messageId != steps[2].messageId
            boolean three = steps[1].messageId != steps[2].messageId
            if(one & two & three) {
                done(XDRRecordInterface.CriteriaMet.PASSED, updatedRecord)
            }
            else{
                done(XDRRecordInterface.CriteriaMet.FAILED, updatedRecord)
            }
        }

    }
}
