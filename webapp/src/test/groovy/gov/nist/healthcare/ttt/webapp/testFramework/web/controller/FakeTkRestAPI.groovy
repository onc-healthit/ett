package gov.nist.healthcare.ttt.webapp.testFramework.web.controller

import groovy.util.slurpersupport.GPathResult
import org.springframework.web.bind.annotation.*

@RestController
public class FakeTkRestAPI {

//    """
//                createSim {
//                SimType("XDR Document Recipient")
//                SimulatorId("config.name")
//                MetadataValidationLevel("Full")
//                CodeValidation("false")
//                PostNotification("notificationUrl")
//            }
//    """


    @RequestMapping(value = "/createSim", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    def receive(@RequestBody String body) {
        GPathResult xml =  new XmlSlurper().parseText(body)
        println "toolkit receive request at endpoint /createSim : $body"
        String id = xml.SimulatorId.text()
        return "<response><status>ok</status><endpoint>"+ id + "</endpoint></response>"
    }





}