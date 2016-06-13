package gov.nist.healthcare.ttt.xdr.web
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.jdbc.LogFacade;
import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.parsing.DirectAddressing
import gov.nist.healthcare.ttt.parsing.Parsing;
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

import javax.mail.Address;

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
    @RequestMapping(value = 'api/xdrNotification/{id}', consumes = "application/xml")
    @ResponseBody
    public void receiveBySimulatorId(@RequestBody String httpBody) {

        log.debug("receive a new validation report: $httpBody")
        Message m = null

        try {

            def tkValidationReport = new TkValidationReport()
            def report = new XmlSlurper().parseText(httpBody)

            parseReportFormat(tkValidationReport, report)
            parseRequest(tkValidationReport , report.requestMessageBody)
            parseResponse(tkValidationReport, report.responseMessageBody)

            m = new Message<TkValidationReport>(Message.Status.SUCCESS, "new validation result received...", tkValidationReport)
        }
        catch(Exception e) {
	    e.printStackTrace();
            log.error("receive an invalid validation report. Bad payload rejected :\n $httpBody")

            m = new Message<TkValidationReport>(Message.Status.ERROR, "received unparseable payload...", null)
        }
        finally {
            receiver.notifyObserver(m)
        }
    }

    def parseStatus(String registryResponseStatus) {
        if (registryResponseStatus.contains("Failure")) {
            return Status.FAILED
        } else if (registryResponseStatus.contains("Success")) {
            return Status.PASSED
        }
    }

    def parseResponse(TkValidationReport tkValidationReport, GPathResult response){

        //TODO modify : all that to extract registryResponseStatus info!
        String content = response.text()
        def registryResponse = content.split("<.?S:Body>")
        def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
        def registryResponseStatus = registryResponseXml.@status.text()
        def criteriaMet = parseStatus(registryResponseStatus)
        tkValidationReport.status = criteriaMet
    }

    def parseRequest(TkValidationReport tkValidationReport, GPathResult request){
        String text = request.text()
        String unescapeXml = StringEscapeUtils.unescapeXml(text)

        //TODO: Don't do this with regular expressions...
        //Matcher messageIDMatcher = unescapeXml =~ /(?:MessageID[^>]+>)([^<]+)(?:<)/
        //Matcher directFromMatcher = unescapeXml =~ /from>([^<]+)</
        //Matcher directToMatcher = unescapeXml =~ /to>([^<]+)</        
        Matcher messageIDMatcher = unescapeXml =~ /(?:MessageID[^>]*>)([^<]*)(?:<)/
        Matcher directFromMatcher = unescapeXml =~ /(?:from[^>]*>)([^<]*)(?:<)/
        Matcher directToMatcher = unescapeXml =~ /(?:to[^>]*>)([^<]*)(?:<)/

        //we expect only one match (thus the 0) and we want to get back the first group match
		try {
			DirectAddressing directAdd = Parsing.getDirectAddressing(text);
			tkValidationReport.messageId = directAdd.getMessageID();
			tkValidationReport.directFrom = directAdd.getDirectFrom();
			tkValidationReport.directTo = directAdd.getDirectTo();
		} catch(Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			tkValidationReport.messageId = (messageIDMatcher.find()) ? messageIDMatcher[0][1] : null
			tkValidationReport.directFrom = (directFromMatcher.find()) ? directFromMatcher[0][1] : null
			tkValidationReport.directTo = (directToMatcher.find()) ? directToMatcher[0][1] : null
		}
		
		
		// Strip mailto: from both direct addresses
		tkValidationReport.directFrom = stripMailTo(tkValidationReport.directFrom);
		tkValidationReport.directTo = stripMailTo(tkValidationReport.directTo);
    }
	
	def stripMailTo(String address) {
		if(address!=null) {
			if(address.toLowerCase().startsWith("mailto:")) {
				return address.split("mailto:")[1];
			}
		}
		return address;
	}

    def parseReportFormat(TkValidationReport tkValidationReport,  GPathResult report){
        tkValidationReport.request = report.requestMessageHeader.text() + "\r\n\r\n" + report.requestMessageBody.text()
        tkValidationReport.response = report.responseMessageHeader.text() + "\r\n\r\n" + report.responseMessageBody.text()
        tkValidationReport.simId = report.simulatorUser.text() + "__" + report.simulatorId.text()
    }
}
