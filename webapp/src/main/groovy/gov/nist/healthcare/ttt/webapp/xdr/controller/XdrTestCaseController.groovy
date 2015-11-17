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

    //@ApiOperation(value = "configure a test case")
    @RequestMapping(value = "/{id}/configure", method = RequestMethod.GET)
    @ResponseBody
    UserMessage configure(@PathVariable("id") String id) {

        log.debug("received configure request for tc$id")


        try {
            TestCaseEvent event = testCaseManager.configure(id)
            return new UserMessage(UserMessage.Status.SUCCESS,"test case with id $id is configured", event)
        }
        catch(Exception e){
            return new UserMessage(UserMessage.Status.ERROR, e.getMessage(), null)
        }


    }


    //@ApiOperation(value = "run a test case")
    @RequestMapping(value = "/{id}/run", method = RequestMethod.POST)
    @ResponseBody
    UserMessage run(@PathVariable("id") String id, @RequestBody HashMap body, Principal principal) {

        //User must be authenticated in order to run a test case=
        if (principal == null) {
            return new UserMessage(UserMessage.Status.ERROR, "user not identified")
        }

        //rename variables to make their semantic more obvious
        def tcid = id
        def username = principal.getName()
        def config = body

        log.debug("received run request for tc$tcid from $username")

        try {
            TestCaseEvent event = testCaseManager.run(id, config, username)
            return new UserMessage(UserMessage.Status.SUCCESS,"ran tc $tcid", event)
        }
        catch(Exception e){
            e.printStackTrace() //TODO flag so it is not logged in production
            return new UserMessage(UserMessage.Status.ERROR, e.getMessage(), null)
        }


    }


    //@ApiOperation(value = "check status of a test case")
    @RequestMapping(value = "/{id}/status", method = RequestMethod.GET)
    @ResponseBody
    UserMessage status(
            @PathVariable("id") String id, Principal principal) {

        if (principal == null) {
            return new UserMessage(UserMessage.Status.ERROR, "user not identified")
        }

        //rename variables to make their semantic more obvious
        def tcid = id
        def username = principal.getName()
        def status
        String msg
        TestCaseEvent result

        log.debug("received status request for tc$id from $username")

        try {
            result = testCaseManager.status(username, tcid)

            log.debug("[status is $result.criteriaMet]")
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
