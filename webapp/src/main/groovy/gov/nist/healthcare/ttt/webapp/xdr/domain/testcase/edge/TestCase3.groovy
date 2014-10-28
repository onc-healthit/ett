package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge

import gov.nist.healthcare.ttt.database.jdbc.DatabaseException
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemImpl
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.core.TestStepBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy

/**
 * Created by gerardin on 10/27/14.
 */
class TestCase3 extends TestCaseStrategy {


    TestCase3(String id, TestCaseManager manager){
        super(id,manager)
    }

    @Override
    UserMessage run(Object userInput, String username) {


        def step1 = new TestStepBuilder("ttt send a XDR message with limited metadata.").build()
        def step2 = new TestStepBuilder("ttt receives a XDR response.").build()
        XDRRecordInterface record = new TestCaseBuilder(id,username).addStep(step1).addStep(step2).build()

        //TODO should be persisted so we can track back what happened

        Object r
        try {
           r  = manager.sender.sendXdr()
        }
        catch (Exception e){
            e.printStackTrace()
            return new UserMessage(UserMessage.Status.ERROR, "a problem occured while sending the Xdr document. " + e.getMessage())
        }


        String json = mapper.writeValueAsString(r)
        XDRReportItemInterface report = new XDRReportItemImpl()
        report.setReport(json)
        step2.xdrReportItems = new LinkedList<XDRReportItemInterface>()
        step2.xdrReportItems.add(report)

        step1.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        step2.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED

        //persist this record
        try {
            String recordId = manager.db.getXdrFacade().addNewXdrRecord(record)
        }
        catch (DatabaseException e) {
            return unableToSaveInDBMessage(e)
        }

        String msg = "xdr sent. Response received."
        return new UserMessage(UserMessage.Status.SUCCESS, msg, json)
    }
}
