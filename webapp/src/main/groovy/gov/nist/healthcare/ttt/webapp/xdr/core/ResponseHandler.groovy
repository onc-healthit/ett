package gov.nist.healthcare.ttt.webapp.xdr.core

import gov.nist.healthcare.ttt.xdr.api.notification.IObserver
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/14/14.
 */
@Component
class ResponseHandler implements IObserver{

    @Override
    def getNotification(Object msg) {
        println "notification received"
    }
}
