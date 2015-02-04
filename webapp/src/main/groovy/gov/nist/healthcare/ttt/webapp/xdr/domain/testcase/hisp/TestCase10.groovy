package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase10 extends TestCase{


    @Autowired
    TestCase10(TestCaseExecutor executor) {
        super(executor)
    }

    @Override
    TestCaseEvent configure(String tcid, Map context, String username) {

       XDRTestStepInterface step = executor.executeSendDirectStep(context)

        XDRRecordInterface record = new TestCaseBuilder(tcid,username).addStep(step).build()
        executor.db.addNewXdrRecord(record)

        //TODO return something more meaningful
        def info = step.getXdrReportItems().get(0)

        return new UserMessage(UserMessage.Status.SUCCESS, "direct message sent and response received", new TestCaseEvent(info,XDRRecordInterface.CriteriaMet.PENDING))
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)

        //TODO validate also the content to make sure it matches the direct message ?

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        done(step.criteriaMet, updatedRecord)

    }
}
