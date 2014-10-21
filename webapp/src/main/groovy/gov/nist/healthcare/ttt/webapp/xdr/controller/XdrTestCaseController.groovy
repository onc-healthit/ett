package gov.nist.healthcare.ttt.webapp.xdr.controller

import com.wordnik.swagger.annotations.ApiOperation
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.component.ResponseHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Created by gerardin on 10/17/14.
 */

@RestController
@RequestMapping("/xdr/tc")
class XdrTestCaseController {

    private final receiver
    private final receiveNotificationHandler
    private final sender
    private final db

    @Autowired
    public XdrTestCaseController(XdrReceiver xdrReceiver, ResponseHandler xdrResponseHandler, XdrSender xdrSender, DatabaseInstance database) {
        this.receiver   = xdrReceiver
        this.receiveNotificationHandler = xdrResponseHandler
        this.sender = xdrSender
        this.db = database
    }

    @ApiOperation(value = "run a test case")
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    @ResponseBody
    UserMessage run(@PathVariable("id") String id, @RequestBody Object body) {
        //Find user by id, find test case by id -> get the test case description (step to perform etc...)
        //Create a new test execution to hold specific data.
        //read the first step. Execute. When done read next step and execute
        //Ex : step1 : send XDR request
        //Ex : step2 : receive XDR response
    }


}
