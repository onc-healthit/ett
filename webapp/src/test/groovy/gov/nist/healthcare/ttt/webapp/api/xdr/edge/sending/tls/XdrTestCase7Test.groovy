package gov.nist.healthcare.ttt.webapp.api.xdr.edge.sending.tls
import gov.nist.healthcare.ttt.webapp.TestUtils
import gov.nist.healthcare.ttt.webapp.XDRSpecification
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import gov.nist.healthcare.ttt.xdr.api.TLSClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase7Test extends XDRSpecification {

    @Autowired
    TLSClient client

    //TODO change that : either find a better way or rename property
    @Value('${direct.listener.domainName}')
    private String hostname

    @Value('${xdr.tls.test.port}')
    Integer tlsPort

    String simId = "7"
    String tcId = "7"
    String simEndpoint = TestUtils.simEndpoint(simId, system)

    public String testCaseConfig =
            """{
        "ip_address": "127.0.0.1"
}"""


    def "user succeeds in running test case"() throws Exception {

        when: "receiving a request to configure test case"
        MockHttpServletRequestBuilder configure = TestUtils.configure(tcId,userId,testCaseConfig)

        then: "we receive back a success message, correlation parameters have been accepted"
        gui.perform(configure)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))


        //setup is completed

        when: "we try to connect to TTT with what we consider a good cert"
        try {
            client.connectOverGoodTLS([ip_address: hostname, port: tlsPort.toString()])
        }
        catch(Exception e){
            //TODO improve that
            println(e.getCause())
            println("Success. We should throw an exception because TTT give us a bad cert")
        }
        MockHttpServletRequestBuilder status = TestUtils.status(tcId,userId)
        Thread.sleep(4000)

        then: "we receive back a success message if we have disconnect"
        gui.perform(status)
                .andDo(print())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PASSED"))
    }

}

