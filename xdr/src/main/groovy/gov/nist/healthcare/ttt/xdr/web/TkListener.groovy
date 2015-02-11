package gov.nist.healthcare.ttt.xdr.web
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import groovy.util.slurpersupport.GPathResult
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
     * @param httpBody : the report
     */
    @RequestMapping(value = 'api/xdrNotification', consumes = "application/xml")
    @ResponseBody
    public void receiveBySimulatorId(@RequestBody String httpBody) {

        log.debug("receive a new validation report: $httpBody")
        Message m = null

        try {

            def tkValidationReport = new TkValidationReport()
            def report = new XmlSlurper().parseText(httpBody)

            parseReportFormat(tkValidationReport, report)
            parseRequest(tkValidationReport , report.request)
            parseResponse(tkValidationReport, report.response)

            m = new Message<TkValidationReport>(Message.Status.SUCCESS, "new validation result received...", tkValidationReport)
        }
        catch(Exception e) {
            log.error("receive an invalid validation report. Bad payload rejected :\n $httpBody")

            m = new Message<TkValidationReport>(Message.Status.ERROR, "received unparseable payload...", null)
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

    def parseResponse(TkValidationReport tkValidationReport, GPathResult response){

        //TODO modify : all that to extract registryResponseStatus info!
        String content = response.body.text()
        def registryResponse = content.split("<.?S:Body>")
        def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
        def registryResponseStatus = registryResponseXml.@status.text()
        def criteriaMet = parseStatus(registryResponseStatus)
        tkValidationReport.status = criteriaMet
    }

    def parseRequest(TkValidationReport tkValidationReport, GPathResult request){
        String text = request.body.text()
        String unescapeXml = StringEscapeUtils.unescapeXml(text)

        Matcher messageIDMatcher = unescapeXml =~ /(?:MessageID[^>]+>)([^<]+)(?:<)/
        Matcher directFromMatcher = unescapeXml =~ /from>([^<]+)</

        //we expect only one match (thus the 0) and we want to get back the first group match

        tkValidationReport.messageId = (messageIDMatcher.find()) ? messageIDMatcher[0][1] : null
        tkValidationReport.directFrom = (directFromMatcher.find()) ? directFromMatcher[0][1] : null

    }

    def parseReportFormat(TkValidationReport tkValidationReport,  GPathResult report){
        tkValidationReport.request = report.request.header.text() + "\r\n\r\n" + report.request.body.text()
        tkValidationReport.response = report.response.header.text() + "\r\n\r\n" + report.response.body.text()
        tkValidationReport.simId = report.@simId.text()
    }
}
