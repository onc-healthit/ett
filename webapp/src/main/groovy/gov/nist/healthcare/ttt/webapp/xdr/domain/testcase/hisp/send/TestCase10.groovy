package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.send

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseResult
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseSender
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase10 extends TestCaseSender {


    @Autowired
    TestCase10(TestCaseExecutor executor) {
        super(executor)
    }

    @Override
    TestCaseResult configure(){
        StandardContent c = new StandardContent()
        c.endpoint = sim.endpoint
        c.endpointTLS = sim.endpointTLS
        new TestCaseResult(Status.PENDING, c)
    }

    @Override
    TestCaseResult run(Map context, String username) {

        executor.validateInputs(context,["direct_to"])

        TestCaseBuilder builder = new TestCaseBuilder(id, username)

        XDRTestStepInterface step1 = executor.correlateRecordWithSimIdAndDirectAddress(sim, context.direct_to)
        builder.addStep(step1)

        //We provide a direct_from address. This might be used for trace back the message in the SUT logs.
        context.direct_from = "testcase10@nist.gov"

        //We send a direct message with a CCDA payload
        String msgType = "CCDA_Ambulatory.xml"
        XDRTestStepInterface step2 = executor.executeSendDirectStep(context, msgType)

        //We create the record
        def record = builder.addStep(step2).build()
        executor.db.addNewXdrRecord(record)

        //pending as we will wait to receive an XDR back
        StandardContent c = new StandardContent()
        c.endpoint = sim.endpoint
        c.endpointTLS = sim.endpointTLS
        return new TestCaseResult(Status.PENDING, c)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        //we parse the XDR report
        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        //we update the record
        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()
        updatedRecord.status = Status.MANUAL
        executor.db.updateXDRRecord(updatedRecord)
    }

    @Override
    public TestCaseResult getReport(XDRRecordInterface record) {
        executor.getSimpleSendReport(record)
    }
}
