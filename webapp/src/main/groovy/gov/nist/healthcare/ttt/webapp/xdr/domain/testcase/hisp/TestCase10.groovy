package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp

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

    @Override
    TestCaseEvent configure(Map context, String username) {

        //basically we need 4 piece of data to inplement the workflow :
        // for the system : the direct address of the SUT, and the endpoint it should send back to,
        // for the GUI : the username et the test case id.
        //When we receive an XDR on this endpoint we know which record to update thanks to direct-from address.
        //When the user check the status of the test, we just need the username-tcid combinaison to look up the result.
        executor.createRecordForTestCase(context, username, id, sim)

        //We send a direct message
        String msgType = "Direct"
        XDRTestStepInterface step = executor.executeSendDirectStep(context, msgType)

        //cumbersome way of updating an object in the db
        XDRRecordInterface record = executor.db.getLatestXDRRecordByUsernameTestCase(username, id)
        record = new TestCaseBuilder(record).addStep(step).build()
        executor.db.updateXDRRecord(record)

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
