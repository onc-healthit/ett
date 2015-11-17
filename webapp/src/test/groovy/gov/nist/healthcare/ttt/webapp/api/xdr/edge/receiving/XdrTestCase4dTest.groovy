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
Negative test cases use canned XDR message to send bad XDR
 */
@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase4dTest extends XDRSpecification {

    String simId = "4d"
    String tcId = "4d"
    String simEndpoint = TestUtils.simEndpoint(simId, system)

    public String testCaseConfigTLS =
            """{
    "targetEndpoint": "https://transport-testing.nist.gov:12081/ttt/sim/ce45c84c-fc5f-430e-b1cd-aadf592a67ca/rec/xdrpr"
}"""

    def "user succeeds in running test case 4d using TLS"() throws Exception {

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

