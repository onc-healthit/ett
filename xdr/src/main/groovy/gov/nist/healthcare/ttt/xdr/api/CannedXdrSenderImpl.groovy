package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.tempxdrcommunication.SimpleSOAPSender
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings
import gov.nist.healthcare.ttt.xdr.domain.TkSendReport
import groovy.util.slurpersupport.GPathResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/21/14.
 */

@Primary
@Component
class CannedXdrSenderImpl implements XdrSender {

    Logger log = LoggerFactory.getLogger(XdrSender.class)

    @Value('${toolkit.request.timeout}')
    Integer timeout = 1000

    @Override
    TkSendReport sendXdr(Map config) {

        log.info("try to send xdr with config : $config")

        try {
            def payload = prepareMessage(config)
            log.info("contacting remote endpoint...")
            String response = SimpleSOAPSender.sendMessage(config.targetEndpoint, payload)
            def report = new TkSendReport()
            report.xdrResponse = response
            return report
        }
        catch (Exception e) {
            e.printStackTrace()
            log.error("problem occured when trying to send to : $config.targetEndpoint")
            throw new RuntimeException(e);
        }
    }

    def parseReport(GPathResult response) {
        TkSendReport report = new TkSendReport()
        report.test = response.Test.text()
        report.status = response.Status.text()
        report.result = response.Result.text()
        report.inHeader = response.InHeader.text()
        return report
    }

    private def prepareMessage(Object config) {
        Settings settings = new Settings()
        settings.setDirectFrom(config.directFrom)
        settings.setDirectTo(config.directTo)
        settings.setWsaTo(config.targetEndpoint)

        String request =
                ArtifactManagement.getPayload(config.messageType, settings);

        log.info("generated xdr payload successfully")

        return request
    }

}
