package gov.nist.healthcare.ttt.xdr.web

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
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

        def report = new XmlSlurper().parseText(body)

        def tkValidationReport = new TkValidationReport()
        tkValidationReport.request = report.request.text()
        tkValidationReport.response = report.response.text()
        tkValidationReport.simId = report.@simId.text()

        //TODO modify : all that to extract registryResponseStatus info!
        String content = report.response.body.text()
        def registryResponse = content.split("<.?S:Body>")
        def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
        def registryResponseStatus = registryResponseXml.@status.text()
        def criteriaMet = parseStatus(registryResponseStatus)
        tkValidationReport.status = criteriaMet

        def m = new Message<TkValidationReport>(Message.Status.SUCCESS,"new validation result received...",tkValidationReport)

        receiver.notifyObserver(m)
    }

    def parseStatus(String registryResponseStatus) {
        if(registryResponseStatus.contains("Failure")){
            return XDRRecordInterface.CriteriaMet.FAILED
        }
        else if(registryResponseStatus.contains("Success")){
            return XDRRecordInterface.CriteriaMet.PASSED
        }
    }
/**
     * Notify of a bad payload received.
     */
    @ResponseStatus(value=HttpStatus.NOT_ACCEPTABLE, reason="Bad payload")
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public void contentNotUnderstood(){

        log.error("receive an invalid validation report. Bad payload rejected")

        def m = new Message<TkValidationReport>(Message.Status.ERROR,"new validation result received...")

        receiver.notifyObserver(m)
    }

}
