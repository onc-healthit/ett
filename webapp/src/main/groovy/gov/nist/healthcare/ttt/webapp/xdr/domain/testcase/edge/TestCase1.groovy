package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge
import gov.nist.healthcare.ttt.database.jdbc.DatabaseException
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy

/**
 * Created by gerardin on 10/27/14.
 */
class TestCase1 extends TestCaseStrategy{

    XDRSimulatorInterface sim

    TestCase1(String id, TestCaseManager manager){
        super(id,manager)
    }

    @Override
    UserMessage run(Object userInput, String username) {

        try {
            sim = createEndpoints(username, userInput)
        }
        catch(Exception e){
            return unableToConfigureTestCaseMessage(e)
        }

        //Config succeeded
        //Create steps for this test so execution can proceed
        // step 1 : receive and validate.
        XDRTestStepInterface step1 = new XDRTestStepImpl()
        step1.name = "ttt configures endpoints for receiving xdr message with limited metadata"
        step1.criteriaMet = XDRRecordInterface.CriteriaMet.PASSED
        XDRTestStepInterface step2 = new XDRTestStepImpl()
        step2.name = "ttt receives a message"
        step2.xdrSimulator = sim

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id,username).addStep(step1).addStep(step2).build()

        //persist this record
        try {
            String recordId = manager.db.getXdrFacade().addNewXdrRecord(record)
        }
        catch (DatabaseException e) {
            return unableToSaveInDBMessage(e)
        }

        String msg = "successfully created new endpoints for test case ${id} with config : ${userInput}. Ready to receive message."
        return new UserMessage(UserMessage.Status.SUCCESS, msg, sim)
    }

}
