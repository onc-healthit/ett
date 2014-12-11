package gov.nist.healthcare.ttt.webapp.xdr.controller

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager

//import com.wordnik.swagger.annotations.ApiOperation
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
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

//    @ApiOperation(value = "run a test case")
    @RequestMapping(value = "/{id}/run", method = RequestMethod.POST)
    @ResponseBody
    UserMessage run(@PathVariable("id") String id, @RequestBody HashMap body, Principal principal) {

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

        try {
            TestCaseEvent event = testCaseManager.runTestCase(id, config, username)
            return new UserMessage(UserMessage.Status.SUCCESS,"test case with id $id has run successfully", event)
        }
        catch(Exception e){
            return new UserMessage(UserMessage.Status.ERROR, e.getMessage(), null)
        }


    }


//    @ApiOperation(value = "check status of a test case")
    @RequestMapping(value = "/{id}/status", method = RequestMethod.GET)
    @ResponseBody
    UserMessage status(
            @PathVariable("id") String id, Principal principal) {



        //TODO enforce user must be authentified or run tests as anonymous?
        if (principal == null) {
            return new UserMessage(UserMessage.Status.ERROR, "user not identified")
        }

        def tcid = id
        def username = principal.getName()
        def status
        String msg
        TestCaseEvent result

        log.info("received get status of test case $id request from $username")

        try {
            result = testCaseManager.checkTestCaseStatus(username, tcid)

            log.info("[status is $result.criteriaMet]")
            status = UserMessage.Status.SUCCESS
            msg = "result of test case $id"
            return new UserMessage<XDRRecordInterface.CriteriaMet>(status, msg , result)
        }catch(Exception e){
            e.printStackTrace()
            status = UserMessage.Status.ERROR
            msg = "error while trying to fetch status for test case $id"
            result = new TestCaseEvent(XDRRecordInterface.CriteriaMet.FAILED,e.getCause())
            return new UserMessage<XDRRecordInterface.CriteriaMet>(status, msg , result)
        }
    }
}
