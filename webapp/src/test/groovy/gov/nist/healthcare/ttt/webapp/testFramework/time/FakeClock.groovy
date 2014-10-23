package gov.nist.healthcare.ttt.webapp.testFramework.time
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/23/14.
 */

/**
 * Mock clock that we can use for injecting always the same timestamp.
 * Useful for integration tests.
 */

@Primary
@Component
class FakeClock extends Clock{

    Long timestamp

    public Long getTimestamp(){
//        if(timestamp == null) {
//            DateTime dateTime = new DateTime();
//            Long timestamp = dateTime.getMillis()
//            this.timestamp = timestamp
//        }
//            return timestamp
        return 2014
    }
}
