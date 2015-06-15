package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Artifacts
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import groovy.util.slurpersupport.GPathResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/21/14.
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

        Settings settings = new Settings()
        settings.setDirectFrom(config.directFrom)
        settings.setDirectTo(config.directTo)
        settings.setWsaTo(config.targetEndpoint)

        Artifacts art = ArtifactManagement.generateArtifacts(ArtifactManagement.Type.XDR_FULL_METADATA, settings);

        def req = """
            <sendRequest>
                <simReference>ett/$config.simId</simReference>
                <transactionName>prb</transactionName>
                <tls value="$config.tls"/>
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
        //we need to parse the response maybe
        return r
    }
}
