package gov.nist.healthcare.ttt.xdr.unit

import gov.nist.healthcare.ttt.xdr.helpers.testFramework.TestApplication
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification

import javax.annotation.PostConstruct

/**
 * Created by gerardin on 10/9/14.
 *
 * Here we only test the http client behavior under various conditions.
 * The fake toolkit we use does not comply with the new API but this is largely irrelevant.
 * We want to know whether the client reacts appropriately.
 */
@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class TkClientSpecTest extends Specification {

    @Value('${xdr.notification')
    private String notificationUrl

    @Value('${server.contextPath}')
    private String contextPath

    @Value('${server.port}')
    private String port

    //TODO change that : either find a better way or rename property
    @Value('${direct.listener.domainName}')
    private String hostname

    private String fullNotificationUrl
    private String fullUrl

    @PostConstruct
    def buildUrls(){
        fullNotificationUrl = "http://"+hostname+":"+port+contextPath+notificationUrl
        fullUrl = "http://"+hostname+":"+port+contextPath
    }

    @Autowired
    GroovyRestClient client

    def "test request on good endpoint"() {
        given:
        // a working url
        def id = "SimpleTest1"

        def config = {
            createSim {
                SimType("XDR Document Recipient")
                SimulatorId(id)
                MetadataValidationLevel("Full")
                CodeValidation("false")
                PostNotification("$fullNotificationUrl")
            }
        }

        def url = fullUrl + "/createSim"

        when:
        def resp = client.postXml(config, url, 1000)

        then:
        //we have a successful interaction
            assert resp.SimulatorId.text() == id

    }

    def sendRawXml(){

        given:
        // a working url
        def id = "SimpleTest1"

        def config = """
<createSim><SimType>XDR Document Recipient</SimType><SimulatorId>SimpleTest1</SimulatorId><MetadataValidationLevel>Full</MetadataValidationLevel><CodeValidation>false</CodeValidation><PostNotification>http://localhost:12080/ttt</PostNotification></createSim>
"""

        def url = fullUrl + "/createSim"

        when:
        def resp = client.postXml(config, url, 1000)

        then:
        //we have a successful interaction
        assert resp.SimulatorId.text() == id

    }

    def "test request on wrong endpoint"() {
        given:

        def id = "SimpleTest1"

        def config = {
            createSim {
                SimType("XDR Document Recipient")
                SimulatorId(id)
                MetadataValidationLevel("Full")
                CodeValidation("false")
                PostNotification("$fullNotificationUrl")
            }
        }

        def url = fullUrl + "/badEndpoint"

        when:
        def resp = client.postXml(config, url, 1000)

        then:
        thrown(groovyx.net.http.HttpResponseException)

    }


    def "test request on good endpoint but bad content"() {
        given:

        def id = "SimpleTest1"

        def config = {
            createSim {
                SimType("XDR Document Recipient")
                SimulatorId(id)
                MetadataValidationLevel("Full")
                CodeValidation("false")
                PostNotification("$fullNotificationUrl")
            }
        }

        def url = fullUrl + "/createSimWithBadContent"

        when:
        def resp = client.postXml(config, url, 1000)

        then:
        thrown(groovyx.net.http.ResponseParseException)

    }

}
