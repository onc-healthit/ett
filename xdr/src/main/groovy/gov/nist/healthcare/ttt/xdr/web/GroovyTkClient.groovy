package gov.nist.healthcare.ttt.xdr.web
import gov.nist.healthcare.ttt.xdr.domain.Message
import groovy.xml.XmlUtil
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/9/14.
 */
@Component
public class GroovyTkClient {

    Logger logger = LoggerFactory.getLogger(GroovyTkClient.class)

    /*
      <CreateSim>
            <SimType>XDR Document Recipient</SimType> 
           <SimulatorId>SimpleTest1</SimulatorId>
       <!-- Other option is Minimal --> 
            <MetadataValidationLevel>Full</MetadataValidationLevel>
       <!-- Other option is true --> 
            <CodeValidation>false</CodeValidation>
       <!-- Where to send notification after message is processed -->
             <PostNotification>http://localhost:8086/myendpoint</PostNotification> 
      </CreateSim>
     */

    public Message<String> createEndpoint(config, url) {

        def http = new HTTPBuilder(url)

        def resp = http.request(Method.POST, ContentType.XML) {
            body = config

            response.success = { resp , xml ->
                logger.info(XmlUtil.serialize(xml))
                new Message<String>(xml.status.text(), Message.Status.SUCCESS, xml.endpoint.text())
            }

        }

        return resp
    }
}
