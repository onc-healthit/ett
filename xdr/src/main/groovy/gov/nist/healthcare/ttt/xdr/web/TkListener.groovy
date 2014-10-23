package gov.nist.healthcare.ttt.xdr.web
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Created by gerardin on 10/14/14.
 */


@RestController
public class TkListener {

    @Value('${xdr.tool.baseurl}')
    private String notificationUrl

    @Autowired
    XdrReceiver receiver

    @RequestMapping(value = '/xdrNotification', consumes = "application/xml")
    @ResponseBody
    TkValidationReport receive(@RequestBody TkValidationReport body) {

        def m = new Message<Object>("new validation result received...", Message.Status.SUCCESS,body)
        println body
        receiver.notifyObserver(m)

        return body
    }

}
