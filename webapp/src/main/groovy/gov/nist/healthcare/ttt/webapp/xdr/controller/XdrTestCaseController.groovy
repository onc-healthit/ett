package gov.nist.healthcare.ttt.webapp.xdr.controller
import com.wordnik.swagger.annotations.ApiOperation
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.security.Principal
/**
 * Created by gerardin on 10/17/14.
 */

@RestController
@RequestMapping("api/xdr/tc")
class XdrTestCaseController {

    private static Logger log = LoggerFactory.getLogger(XdrTestCaseController.class)

    private final TestCaseManager testCaseManager

    @Autowired
    public XdrTestCaseController(TestCaseManager manager) {
        testCaseManager = manager
    }

    @ApiOperation(value = "run a test case")
    @RequestMapping(value = "/{id}/run", method = RequestMethod.POST)
    @ResponseBody
    UserMessage<XDRSimulatorImpl> run(@PathVariable("id") String id, @RequestBody HashMap body, Principal principal) {

        //User must be authenticated for this test case to be run
        String username
        //TODO enforce user must be authentified or run tests as anonymous?
        if (principal == null) {
            return new UserMessage(UserMessage.Status.ERROR, "user not identified")
        } else {
            username = principal.getName();
        }

        log.info("received run test case $id request from $username")

        //We get the config from the client
        def config = body

        testCaseManager.runTestCase(id, config, username)

    }


    @ApiOperation(value = "check status of a test case")
    @RequestMapping(value = "/{id}/status", method = RequestMethod.GET)
    @ResponseBody
    UserMessage<XDRRecordInterface.CriteriaMet> status(
            @PathVariable("id") String id, Principal principal) {



        //TODO enforce user must be authentified or run tests as anonymous?
        if (principal == null) {
            return new UserMessage(UserMessage.Status.ERROR, "user not identified")
        }

        def tcid = id
        def username = principal.getName()

        log.info("received get status of test case $id request from $username")

        XDRRecordInterface.CriteriaMet result = testCaseManager.checkTestCaseStatus(username,tcid)

        log.info("[status is $result]")

        //TODO change just for test we return passed
        return new UserMessage<XDRRecordInterface.CriteriaMet>(UserMessage.Status.SUCCESS, "result of this test", result)

    }
}
