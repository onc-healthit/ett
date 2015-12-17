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
import org.springframework.stereotype.Component

import javax.net.ssl.SSLContext
/**
 * Created by gerardin on 10/21/14.
 */

@Component
class CannedXdrSenderImpl implements BadXdrSender {

    SSLContextManager sslContextManager

    @Autowired
    public CannedXdrSenderImpl(SSLContextManager sslContextManager){
        this.sslContextManager = sslContextManager
    }

    Logger log = LoggerFactory.getLogger(XdrSender.class)

    @Value('${toolkit.request.timeout}')
    Integer timeout = 1000

    @Override
    Object sendXdr(Map config) {

        log.info("try to send xdr with config : $config")

        try {
            Settings settings = prepareMessage(config)



            SSLContext sslContext = null
            if(URLParser.isTLS(config.targetEndpointTLS)){
                log.debug "Using $config.targetEndpointTLS. TLS is turned on..."
                sslContext = sslContextManager.goodSSLContext
            }

            log.info("contacting remote endpoint : $config.targetEndpointTLS ...")

            RequestResponse rr = SimpleSOAPSender.sendMTOMPackage(config.targetEndpointTLS, config.messageType, settings, sslContext);

            def map = [request:rr.getRequest(), response:rr.getResponse()]

            return map
        }
        catch (Exception e) {
            e.printStackTrace()
            log.error("problem occured when trying to send to : $config.targetEndpointTLS")
            throw new RuntimeException(e);
        }
    }

    private def prepareMessage(Map config) {
        Settings settings = new Settings()
        settings.setDirectFrom(config.directFrom)
        settings.setDirectTo(config.directTo)
        settings.setWsaTo(config.wsaTo)

//        String request =
//                ArtifactManagement.getPayload(config.messageType, settings);

        log.info("generated xdr payload successfully")

        return settings
    }

}
