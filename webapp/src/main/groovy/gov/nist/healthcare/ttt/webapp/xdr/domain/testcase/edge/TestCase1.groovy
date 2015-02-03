package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
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
final class TestCase1 extends TestCase {

    @Autowired
    public TestCase1(TestCaseExecutor ex){
       super(ex)
    }

    @Override
    TestCaseEvent run(String tcid, Map context, String username) {

         XDRTestStepInterface step = executor.executeCreateEndpointsStep(tcid, username, context)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        log.info  "test case ${tcid} : successfully created new endpoints with config : ${context}. Ready to receive message."

        def content = new StandardContent()
        content.endpoint = step.xdrSimulator.endpoint
        content.endpointTLS = step.xdrSimulator.endpointTLS

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        done(XDRRecordInterface.CriteriaMet.MANUAL, updatedRecord)

    }
}
