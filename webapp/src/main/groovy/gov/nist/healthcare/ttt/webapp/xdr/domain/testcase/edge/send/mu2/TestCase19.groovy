package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send.mu2
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase19 extends TestCase {

    @Autowired
    public TestCase19(TestCaseExecutor ex) {
        super(ex)
        sim = registerGlobalEndpoints(id,new HashMap())
    }

    @Override
    TestCaseEvent configure(Map context, String username) {

        executor.createRecordForSenderTestCase(context,username,id,sim)

        log.info  "test case ${id} : successfully configured. Ready to receive messages."

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, new StandardContent())
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)
        step.directFrom = report.directFrom
        step.messageId = report.messageId

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        //TODO cleaner implementation : choose relevant steps + better way to compare message ids.

        if(record.testSteps.size() != 4) {
            executor.db.updateXDRRecord(record)
        }
        else {

            def steps = record.testSteps.findAll{
                 it.name == "XDR_RECEIVE"
            }

            def messageId1 = steps[0].messageId
            def messageId2 = steps[1].messageId
            def messageId3 = steps[2].messageId

            log.info(" comparing ${messageId1}, ${messageId2}, ${messageId3}")

            boolean one = messageId1 != messageId2
            boolean two = messageId1 != messageId3
            boolean three = messageId2 != messageId3
            if(one & two & three) {
                done(XDRRecordInterface.CriteriaMet.PASSED, updatedRecord)
            }
            else{
                done(XDRRecordInterface.CriteriaMet.FAILED, updatedRecord)
            }
        }

    }
}
