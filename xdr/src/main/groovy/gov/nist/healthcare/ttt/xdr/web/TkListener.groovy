package gov.nist.healthcare.ttt.xdr.web
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.apache.commons.lang.StringEscapeUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import java.util.regex.Matcher

/**
 * Created by gerardin on 10/14/14.
 *
 * Listener for for the toolkit.
 * It listens for new validation reports.
 * It tries to handle properly communication errors (if it does not understand the payload)
 */


@RestController
public class TkListener {

    private static Logger log = LoggerFactory.getLogger(TkListener)

    //TODO @Antoine. should be used to configure the endpoint or at least to check config
    @Value('${xdr.notification}')
    private String notificationUrl


    @Autowired
    XdrReceiver receiver

    /**
     * Notify of a new validation report
     * @param body : the report
     */
    @RequestMapping(value = 'api/xdrNotification', consumes = "application/xml")
    @ResponseBody
    public void receiveBySimulatorId(@RequestBody String body) {

        log.debug("receive a new validation report: $body")
        Message m = null

        try {

            def report = new XmlSlurper().parseText(body)

            def tkValidationReport = new TkValidationReport()
            tkValidationReport.request = report.request.text()
            tkValidationReport.response = report.response.text()
            tkValidationReport.simId = report.@simId.text()

            //Extract direct from address
            tkValidationReport.directFrom = parseRequestBodyForDirectFrom( report.request.body.text() )
            tkValidationReport.messageId = parseRequestBody( report.request.body.text() )

                    //TODO modify : all that to extract registryResponseStatus info!
            String content = report.response.body.text()
            def registryResponse = content.split("<.?S:Body>")
            def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
            def registryResponseStatus = registryResponseXml.@status.text()
            def criteriaMet = parseStatus(registryResponseStatus)
            tkValidationReport.status = criteriaMet

            m = new Message<TkValidationReport>(Message.Status.SUCCESS, "new validation result received...", tkValidationReport)
        }
        catch(Exception e) {
            log.error("receive an invalid validation report. Bad payload rejected :\n $body")

            m = new Message<TkValidationReport>(Message.Status.ERROR, "new validation result received...")
        }
        finally {
            receiver.notifyObserver(m)
        }
    }

    def parseStatus(String registryResponseStatus) {
        if (registryResponseStatus.contains("Failure")) {
            return XDRRecordInterface.CriteriaMet.FAILED
        } else if (registryResponseStatus.contains("Success")) {
            return XDRRecordInterface.CriteriaMet.PASSED
        }
    }

    def parseRequestBody(String body){

        String unescapeXml = StringEscapeUtils.unescapeXml(body)

        Matcher matcher = unescapeXml =~ /(?:MessageID[^>]+>)([^<]+)(?:<)/

        //we expect only one match (thus the 0) and we want to get back the first group match
        matcher[0][1]

    }

    def parseRequestBodyForDirectFrom(String body){

        String unescapeXml = StringEscapeUtils.unescapeXml(body)

        Matcher matcher2 = unescapeXml =~ /from>([^<]+)</

        matcher2.each{
            println it
        }

        //we expect only one match (thus the 0) and we want to get back the first group match
        matcher2[0][1]

    }
}
