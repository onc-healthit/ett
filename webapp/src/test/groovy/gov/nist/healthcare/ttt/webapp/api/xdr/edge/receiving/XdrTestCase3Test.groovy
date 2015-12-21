package gov.nist.healthcare.ttt.webapp.api.xdr.edge.receiving
import gov.nist.healthcare.ttt.webapp.TestUtils
import gov.nist.healthcare.ttt.webapp.XDRSpecification
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/*
Positive test uses the toolkit v3 as a XDR client
 */
@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase3Test extends XDRSpecification {

    String simId = "3"
    String tcId = "3"
    String simEndpoint = TestUtils.simEndpoint(simId, system)

    public String testCaseConfigTLS =
            """{
    "targetEndpointTLS": "https://transport-testing.nist.gov:12081/ttt/sim/ce45c84c-fc5f-430e-b1cd-aadf592a67ca/rec/xdrpr"
}"""

    def "user succeeds in running test case with a tls endpoint"() throws Exception {

        when: "receiving a request to run test case"
        MockHttpServletRequestBuilder getRequest = TestUtils.run(tcId,userId,testCaseConfigTLS)

        then: "we receive back a message with status and report of the transaction"

        //TODO we cannot validate the body because for now we always get error messages!
        gui.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("MANUAL"))
    }
}

