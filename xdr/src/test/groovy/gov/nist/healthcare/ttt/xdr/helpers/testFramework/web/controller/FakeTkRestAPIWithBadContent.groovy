package gov.nist.healthcare.ttt.xdr.helpers.testFramework.web.controller

import groovy.util.slurpersupport.GPathResult
import org.springframework.web.bind.annotation.*

@RestController
public class FakeTkRestAPIWithBadContent {


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
        println "toolkit receive postXml at endpoint /createSim : $body"
        String id = xml.SimulatorId.text()
        return body
    }

    @RequestMapping(value = "/createSimWithBadContent", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    def receiveAndGenerateBadContent(@RequestBody String body) {
        GPathResult xml =  new XmlSlurper().parseText(body)
        println "toolkit receive postXml at endpoint /createSim : $body"
        String id = xml.SimulatorId.text()
        return "<>"
    }




}