package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.send

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
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

    //TODO decide
    // either we create a new event has to capture the default configuration
    // either we hardcode it
    //TODO it cannot find the certificates it looks for  : check
    TestCaseEvent config(){
        StandardContent c = new StandardContent()
        c.endpoint = sim.endpoint
        c.endpointTLS = sim.endpointTLS
        //WE NEED TO PROVIDE THE DIRECT_FROM WE WILL BE SENDING WITH
        new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, c)
    }

    @Override
    TestCaseEvent run(Map context, String username) {

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
        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, new StandardContent())
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        //we parse the XDR report
        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        //we update the record
        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        //we send back a message status to the GUI. This should come from automatic validation but we do it manually for now.
        done(XDRRecordInterface.CriteriaMet.MANUAL, updatedRecord)

    }

    @Override
    public TestCaseEvent getReport(XDRRecordInterface record) {
        executor.getSimpleSendReport(record)
    }
}
