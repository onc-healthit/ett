package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.tempxdrcommunication.RequestResponse
import gov.nist.healthcare.ttt.tempxdrcommunication.SimpleSOAPSender
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings
import gov.nist.healthcare.ttt.xdr.ssl.SSLContextManager
import gov.nist.healthcare.ttt.xdr.web.URLParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import javax.net.ssl.SSLContext

/**
 * Created by gerardin on 10/21/14.
 */

@Primary
@Component
class CannedXdrSenderImpl implements XdrSender {

    @Autowired SSLContextManager sslContextManager

    Logger log = LoggerFactory.getLogger(XdrSender.class)

    @Value('${toolkit.request.timeout}')
    Integer timeout = 1000

    @Override
    Object sendXdr(Map config) {

        log.info("try to send xdr with config : $config")

        try {
            Settings settings = prepareMessage(config)

            log.info("contacting remote endpoint...")

            SSLContext sslContext = null
            if(URLParser.isTLS(config.targetEndpoint)){
                "Using $config.targetEndpoint. TLS is turned on"
                sslContext = sslContextManager.goodSSLContext
            }

            RequestResponse rr = SimpleSOAPSender.sendMTOMPackage(config.targetEndpoint, config.messageType, settings, sslContext);

            def map = [request:rr.getRequest(), response:rr.getResponse()]

            return map
        }
        catch (Exception e) {
            e.printStackTrace()
            log.error("problem occured when trying to send to : $config.targetEndpoint")
            throw new RuntimeException(e);
        }
    }

    private def prepareMessage(Object config) {
        Settings settings = new Settings()
        settings.setDirectFrom(config.directFrom)
        settings.setDirectTo(config.directTo)
        settings.setWsaTo(config.targetEndpoint)

//        String request =
//                ArtifactManagement.getPayload(config.messageType, settings);

        log.info("generated xdr payload successfully")

        return settings
    }

}
