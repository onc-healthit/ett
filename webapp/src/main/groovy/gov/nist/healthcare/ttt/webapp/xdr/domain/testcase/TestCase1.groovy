package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase
import gov.nist.healthcare.ttt.database.jdbc.DatabaseException
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
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
        XDRTestStepInterface step = new XDRTestStepImpl()
        step.name = "ttt has received a valid XDR message with limited metadata."
        step.xdrSimulator = sim

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id,username).addStep(step).build()

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
