package gov.nist.healthcare.ttt.xdr.unit

import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.helpers.testFramework.TestApplication
import gov.nist.healthcare.ttt.xdr.web.GroovyTkClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification

/**
 * Created by gerardin on 10/9/14.
 */
@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class TkClientSpecTest extends Specification {

    @Value('${xdr.tool.baseurl}')
    private String notificationUrl

    @Autowired
    GroovyTkClient client

    def "test request"() {
        given:
        def config = {
            createSim {
                SimType("XDR Document Recipient")
                SimulatorId("SimpleTest1")
                MetadataValidationLevel("Full")
                CodeValidation("false")
                PostNotification("http://localhost:8080/ttt/$notificationUrl")
            }
        }

        def url = "http://localhost:8080/ttt/createSim"

        when:
        Message<String> resp = client.createEndpoint(config, url)

        then:
        assert resp.status == Message.Status.SUCCESS
        assert resp.content == "endpoint1.tk"

    }


}
