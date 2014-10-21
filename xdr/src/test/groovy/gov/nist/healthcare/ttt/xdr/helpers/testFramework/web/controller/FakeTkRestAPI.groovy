package gov.nist.healthcare.ttt.xdr.helpers.testFramework.web.controller

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
public class FakeTkRestAPI {


    @RequestMapping(value = "/createSim", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    def receive(@RequestBody String body) {
        println "request : $body"
        return "<response><status>ok</status><endpoint>endpoint1.tk</endpoint></response>"
    }





}