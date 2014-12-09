package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.tempxdrcommunication.SimpleSOAPSender
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings
import gov.nist.healthcare.ttt.xdr.domain.TkSendReport
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import groovy.util.slurpersupport.GPathResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    GroovyRestClient restClient

    @Value('${toolkit.request.timeout}')
    Integer timeout = 1000

    @Value('${toolkit.sendXdr.url}')
    private String tkSendXdrUrl

    @Value('${toolkit.testName}')
    private String testName

    @Override
    TkSendReport sendXdr(Map config) {

        log.debug("try to send xdr with config : $config")

        try {

            URI uri = ArtifactManagement.class.getClassLoader().getResource("Xdr_full_metadata.xml").toURI()
            println uri.toString()

            InputStream is = ArtifactManagement.class.getClassLoader().getResourceAsStream("Xdr_full_metadata.xml")
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            System.out.println(out.toString());   //Prints the string content read from input stream
            reader.close();



            def payload = prepareMessage(config)
            String response = SimpleSOAPSender.sendMessage(config.targetEndpoint, payload)
            def responseXML = new XmlSlurper().parseText(response)

            def report = new TkSendReport()
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
        String directTo = "directTo";
        String directFrom = "directFrom";
        String relatesTo = "relatesTo";
        String recipient = "recipient";
        String wsaTo = config.targetEndpoint;

        Settings settings = new Settings();
        settings.setDirectFrom(directFrom);
        settings.setDirectTo(directTo);
        settings.setWsaTo(config.targetEndpoint);

        String request =
                ArtifactManagement.getPayload(ArtifactManagement.Type.XDR_FULL_METADATA, settings);

        log.info("generated payload is :\n $request")

        return request
    }

}
