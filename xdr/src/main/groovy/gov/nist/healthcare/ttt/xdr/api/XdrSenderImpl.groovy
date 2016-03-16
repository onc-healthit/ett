package gov.nist.healthcare.ttt.xdr.api
import gov.nist.toolkit.configDatatypes.SimulatorActorType;
import gov.nist.toolkit.configDatatypes.SimulatorProperties;
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Artifacts
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import gov.nist.toolkit.toolkitApi.BasicSimParameters
import gov.nist.toolkit.toolkitApi.DocumentSource
import gov.nist.toolkit.toolkitApi.SimulatorBuilder
import gov.nist.toolkit.toolkitServicesCommon.RawSendRequest
import gov.nist.toolkit.toolkitServicesCommon.RawSendResponse
import gov.nist.toolkit.toolkitServicesCommon.SimConfig
import gov.nist.toolkit.toolkitServicesCommon.resource.DocumentResource
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.apache.commons.io.IOUtils
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
//        Settings settings = new Settings()
//        settings.setDirectFrom(config.directFrom)
//        settings.setDirectTo(config.directTo)
//        settings.setWsaTo(config.wsaTo)
//        settings.setFinalDestinationDelivery(config.finalDestinationDelivery)
//
//        if(config.payload) {
//            StringWriter writer = new StringWriter();
//            InputStream ccdaAttachment = new URL(config.payload.link).openStream();
//            IOUtils.copy(ccdaAttachment, writer, "UTF-8");
//            String payload = writer.toString();
//            settings.setPayload(payload)
//        }
//        Artifacts art = ArtifactManagement.generateArtifacts(config.messageType, settings);
//
//        //creating request for the toolkit
//        def req = """
//            <sendRequest>
//                <simReference>ett/$config.simId</simReference>
//                <transactionName>prb</transactionName>
//                <tls value="true"/>
//                <messageId>$art.messageId</messageId>
//                <metadata>$art.metadata</metadata>
//                <extraHeaders>$art.extraHeaders</extraHeaders>
//                <document id="$art.documentId" mimeType="$art.mimeType">$art.document</document>
//            </sendRequest>
//        """
//
//        try {
//            log.debug("xdr send request :" + req.toString())
//            GPathResult r = restClient.postXml(req, tkSendXdrUrl +"/$config.simId", timeout)
//            parseSendXdrResponse(r)
//
//        }
//        catch (groovyx.net.http.HttpResponseException e) {
//            throw new RuntimeException("could not reach the toolkit or toolkit returned an error. Check response status code",e)
//        }
//        catch (java.net.SocketTimeoutException e) {
//            throw new RuntimeException("connection timeout when calling toolkit.",e)
//        }
//        catch(groovyx.net.http.ResponseParseException e){
//            throw new RuntimeException("could not understand response from toolkit.",e)
//        }
		
		String urlRoot = String.format("http://localhost:8080/xdstools2");

		SimulatorBuilder spi = new SimulatorBuilder(urlRoot);
		BasicSimParameters srcParams = new BasicSimParameters();

		srcParams.setId("source");
		srcParams.setUser("bill");
		srcParams.setActorType(SimulatorActorType.DOCUMENT_SOURCE);
		srcParams.setEnvironmentName("test");
		
		System.out.println("STEP - DELETE DOCSRC SIM");
		spi.delete(srcParams.getId(), srcParams.getUser());

		System.out.println("STEP - CREATE DOCSRC SIM");
		DocumentSource documentSource = spi.createDocumentSource(
		srcParams.getId(),
		srcParams.getUser(),
		srcParams.getEnvironmentName()
		);


		System.out.println("verify sim built");
		System.out.println(documentSource.getId() == srcParams.getId());

		System.out.println("STEP - UPDATE - SET DOC REC ENDPOINTS INTO DOC SRC");
		//		documentSource.setProperty(SimulatorProperties.pnrEndpoint, documentRecipient.asString(SimulatorProperties.pnrEndpoint));
		//		documentSource.setProperty(SimulatorProperties.pnrEndpoint, "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/10/docrec/prb");
		if(config.targetEndpointTLS.startsWith("https")) {
			documentSource.setProperty(SimulatorProperties.pnrTlsEndpoint, config.targetEndpointTLS);
		} else {
			documentSource.setProperty(SimulatorProperties.pnrEndpoint, config.targetEndpointTLS);
		}
		SimConfig updatedVersion = documentSource.update(documentSource.getConfig());
		System.out.println("Updated Src Sim config is " + updatedVersion.describe());

		System.out.println(updatedVersion);
		
		System.out.println("STEP - SEND XDR");
		RawSendRequest req = documentSource.newRawSendRequest();
		
		InputStream ccdaAttachment = new URL(config.payload.link).openStream();
		req.setMetadata(IOUtils.toString(ccdaAttachment));
		DocumentResource document = new DocumentResource();
		document.setContents("Hello World!".getBytes());
		document.setMimeType("text/plain");
		req.addDocument("Document01", document);

		RawSendResponse response = documentSource.sendProvideAndRegister(req);
		
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
