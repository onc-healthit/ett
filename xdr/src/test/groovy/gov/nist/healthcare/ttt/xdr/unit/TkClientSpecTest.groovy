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
    GroovyRestClient client

    def "test request"() {
        given:

        def id = "SimpleTest1"

        def config = {
            createSim {
                SimType("XDR Document Recipient")
                SimulatorId(id)
                MetadataValidationLevel("Full")
                CodeValidation("false")
                PostNotification("http://localhost:8080/ttt/$notificationUrl")
            }
        }

        def url = "http://localhost:8080/ttt/createSim"

        when:
        def resp = client.postXml(config, url)

        then:
            assert resp.simId.text() == id

    }


}
