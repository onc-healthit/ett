package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Artifacts
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by gerardin on 10/21/14.
 *
 * This sender implementation uses the toolkit
 *
 */
@Component
class XdrSenderImpl implements XdrSender{

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
    public def sendXdr(Map config) {

        //generating headers
        Settings settings = new Settings()
        settings.setDirectFrom(config.directFrom)
        settings.setDirectTo(config.directTo)
        settings.setWsaTo(config.wsaTo)
        settings.setFinalDestinationDelivery(config.finalDestinationDelivery)
        settings.payload(config.payload)
        Artifacts art = ArtifactManagement.generateArtifacts(config.messageType, settings);

        //creating request for the toolkit
        def req = """
            <sendRequest>
                <simReference>ett/$config.simId</simReference>
                <transactionName>prb</transactionName>
                <tls value="true"/>
                <messageId>$art.messageId</messageId>
                <metadata>$art.metadata</metadata>
                <extraHeaders>$art.extraHeaders</extraHeaders>
                <document id="$art.documentId" mimeType="$art.mimeType">$art.document</document>
            </sendRequest>
        """

        try {
            log.debug("xdr send request :" + req.toString())
            GPathResult r = restClient.postXml(req, tkSendXdrUrl +"/$config.simId", timeout)
            parseSendXdrResponse(r)

        }
        catch (groovyx.net.http.HttpResponseException e) {
            throw new RuntimeException("could not reach the toolkit or toolkit returned an error. Check response status code",e)
        }
        catch (java.net.SocketTimeoutException e) {
            throw new RuntimeException("connection timeout when calling toolkit.",e)
        }
        catch(groovyx.net.http.ResponseParseException e){
            throw new RuntimeException("could not understand response from toolkit.",e)
        }
    }

    private def parseSendXdrResponse(GPathResult r){
        def report = [:]
        def xml = XmlUtil.serialize(r)
        report['request'] = extractXmlElement(xml,"Request")
        report['response'] = extractXmlElement(xml,"Response")
        //TODO toolkit seems to report SOAPFAULT for its own internal errors.
        //What strategy should we implement in order detect those cases (check toolkit documentation for help).
        return report
    }

    public def extractXmlElement(String xml, String element) {
        Pattern regex = Pattern.compile("<$element>(.*?)</$element>", Pattern.DOTALL);
        Matcher matcher = regex.matcher(xml);
        if (matcher.find()) {
            matcher.group(1);
        }
    }
}
