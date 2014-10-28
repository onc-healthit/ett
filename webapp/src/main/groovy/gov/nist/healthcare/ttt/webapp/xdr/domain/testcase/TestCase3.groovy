package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase

import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage

/**
 * Created by gerardin on 10/27/14.
 */
class TestCase3 extends TestCaseStrategy {


    TestCase3(String id, TestCaseManager manager){
        super(id,manager)
    }

    @Override
    UserMessage run(Object userInput, String username) {

        Object report
        try {
           report  = manager.sender.sendXdr()
        }
        catch (Exception e){
            return new UserMessage(UserMessage.Status.ERROR, "a problem occured while sending the Xdr document. " + e.getMessage())
        }

        String msg = "xdr sent. Response received."
        return new UserMessage(UserMessage.Status.SUCCESS, msg, report)
    }
}
