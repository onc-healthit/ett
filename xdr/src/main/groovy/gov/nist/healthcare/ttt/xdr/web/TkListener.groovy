package gov.nist.healthcare.ttt.xdr.web

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
 */


@RestController
public class TkListener {

    private static Logger log = LoggerFactory.getLogger(TkListener)

    @Value('${xdr.tool.baseurl}')
    private String notificationUrl

    @Autowired
    XdrReceiver receiver

    @RequestMapping(value = 'api/xdrNotification', consumes = "application/xml")
    @ResponseBody
    public void receive(@RequestBody TkValidationReport body) {

        log.debug("receive a new validation report: $body")

        def m = new Message<TkValidationReport>(Message.Status.SUCCESS,"new validation result received...",body)
        println body
        receiver.notifyObserver(m)
    }

    @ResponseStatus(value=HttpStatus.NOT_ACCEPTABLE, reason="Bad payload")
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public void contentNotUnderstood(){

        log.error("receive an invalid validation report. Bad payload rejected")

        def m = new Message<TkValidationReport>(Message.Status.ERROR,"new validation result received...")
        receiver.notifyObserver(m)
    }

}
