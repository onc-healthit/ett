package gov.nist.healthcare.ttt.xdr.helpers.testFramework.web.controller
import groovy.util.slurpersupport.GPathResult
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
public class FakeTkRestAPI {

    def log = LoggerFactory.getLogger(this.class)

    @RequestMapping(value = "/createSim", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    def createSim(@RequestBody String body) {
        GPathResult xml =  new XmlSlurper().parseText(body)
        log.info "toolkit receive a xml post at endpoint /createSim : $body"
        String id = xml.SimulatorId.text()
        return "<response>" +
                "<status>ok</status>" +
                "<simId>"+ id +"</simId>" +
                "<endpoint>http://ttt.test.endpoint1</endpoint>" +
                "<endpointTLS>https://ttt.test.endpoint2</endpointTLS>" +
                "</response>"
    }


    @RequestMapping(value = "/sendXdr", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    def sendXdr(@RequestBody String body) {
        GPathResult xml =  new XmlSlurper().parseText(body)
        log.info "toolkit receive a xml post at endpoint /sendXdr : $body"
        return "<TestClientResponse>" +
                "<Test>1666</Test>" +
                "<Status>Success</Status>" +
                "<InHeader>some headers</InHeader>" +
                "<Result>validation report</Result>" +
                "</TestClientResponse>"
    }





}