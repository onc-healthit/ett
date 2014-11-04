package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp
import gov.nist.healthcare.ttt.database.xdr.*
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageInfoForXdr
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageSenderForXdr
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport

/**
 * Created by gerardin on 10/27/14.
 */
class TestCase10 extends TestCaseStrategy{


    TestCase10(TestCaseExecutor executor) {
        super(executor)
    }

    @Override
    UserMessage run(String tcid, Map context, String username) {

        DirectMessageInfoForXdr info = new DirectMessageSenderForXdr().sendDirectWithCCDAForXdr(context.sutDirectAddress, Integer.parseInt(context.sutDirectPort))

        XDRTestStepInterface step = new XDRTestStepImpl()
        step.name = "SEND_DIRECT"
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        step.messageId = info.messageId
        step.xdrReportItems = new LinkedList<>()

        //TODO we need to store a info object
        XDRReportItemInterface report = new XDRReportItemImpl()
        report.report = info.messageId
        step.xdrReportItems.add(report)

        XDRRecordInterface record = new TestCaseBuilder(tcid,username).addStep(step).build()
        executor.db.addNewXdrRecord(record)

        return new UserMessage(UserMessage.Status.SUCCESS,"direct message sent and response received",info)
    }

    @Override
    def UserMessage notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)


        String msg = "received xdr message"
        if(! step.criteriaMet){
            def msg2 = "test failed."
            return new UserMessage(UserMessage.Status.SUCCESS, msg + msg2 , step.criteriaMet)
        }

        //TODO validate also the content to make sure it matches the direct message ?

        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()

        executor.db.updateXDRRecord(updatedRecord)

        def msg3 = "test succeeded"
        return new UserMessage(UserMessage.Status.SUCCESS, msg + msg3, step.criteriaMet)
    }
}
