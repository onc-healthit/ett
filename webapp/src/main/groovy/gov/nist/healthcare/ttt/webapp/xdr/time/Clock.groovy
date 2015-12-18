package gov.nist.healthcare.ttt.webapp.xdr.time

import org.joda.time.DateTime
import org.springframework.stereotype.Component

/**
 *
 * It is sometimes convenient to fake time for testing purpose.
 *
 * Created by gerardin on 10/23/14.
 */

@Component
class Clock {

    public Long getTimestamp(){
        DateTime dateTime = new DateTime();
        Long timestamp = dateTime.getMillis()
        return timestamp
    }
}
