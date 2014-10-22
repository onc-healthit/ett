package gov.nist.healthcare.ttt.webapp.testFramework.web.controller

import org.springframework.web.bind.annotation.*

@RestController
public class FakeTkRestAPI {


    @RequestMapping(value = "/createSim", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    def receive(@RequestBody String body) {
        println "request : $body"
        return "<response><status>ok</status><endpoint>endpoint1.tk</endpoint></response>"
    }





}