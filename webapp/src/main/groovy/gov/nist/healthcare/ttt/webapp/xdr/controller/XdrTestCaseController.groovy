package gov.nist.healthcare.ttt.webapp.xdr.controller
import com.wordnik.swagger.annotations.ApiOperation
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseStatus
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.security.Principal
/**
 * Created by gerardin on 10/17/14.
 */

@RestController
@RequestMapping("api/xdr/tc")
class XdrTestCaseController {

    private final TestCaseManager testCaseManager

    @Autowired
    public XdrTestCaseController(TestCaseManager manager){
        testCaseManager = manager
    }

    @ApiOperation(value = "run a test case")
    @RequestMapping(value = "/{id}/run", method = RequestMethod.POST)
    @ResponseBody
    UserMessage<XDRSimulatorImpl> run(@PathVariable("id") String id, @RequestBody Object body, Principal principal) {
        //Find user by id, find test case by id -> get the test case description (step to perform etc...)
        //Create a new test execution to hold specific data.
        //Create all the steps necessary. Perform steps if necessary. Return.

        if(id != "1"){
            return new UserMessage(UserMessage.Status.ERROR, "test case not implemented")
        }

        String username

        //TODO enforce user must be authentified or run tests as anonymous?
        if (principal == null) {
            return new UserMessage(UserMessage.Status.ERROR, "user not identified")
        }
        else {
            username = principal.getName();
        }

        testCaseManager.runTestCase1(body, username)

    }


    @ApiOperation(value = "check status of a test case")
    @RequestMapping(value = "/{id}/status", method = RequestMethod.POST)
    @ResponseBody
    UserMessage<TestCaseStatus> status(@PathVariable("id") String id, @RequestBody Object body, Principal principal) {

        if (id != "1") {
            return new UserMessage(UserMessage.Status.ERROR, "test case not implemented")
        }

        String username

        //TODO enforce user must be authentified or run tests as anonymous?
        if (principal == null) {
            return new UserMessage(UserMessage.Status.ERROR, "user not identified")
        } else {

        }

        XDRRecordInterface.CriteriaMet result = testCaseManager.checkTestCaseStatus(body)

        return new UserMessage<XDRRecordInterface.CriteriaMet>(UserMessage.Status.SUCCESS,"result of this test",result)
    }
}
