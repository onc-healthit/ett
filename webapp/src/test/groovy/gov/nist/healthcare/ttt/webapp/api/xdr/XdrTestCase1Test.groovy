package gov.nist.healthcare.ttt.webapp.api.xdr
import gov.nist.healthcare.ttt.database.jdbc.XDRFacade
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.testFramework.time.FakeClock
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.webapp.xdr.core.ResponseHandler
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import sun.security.acl.PrincipalImpl

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class XdrTestCase1Test extends Specification{


    XdrReceiver receiver = Mock(XdrReceiver)
    ResponseHandler handler = Mock(ResponseHandler)
    DatabaseInstance db = Mock(DatabaseInstance)
    XdrSender sender = Mock(XdrSender)
    Clock clock = new FakeClock()

    TestCaseManager manager = new TestCaseManager(db, receiver, sender, clock)

    XdrTestCaseController tcController = new XdrTestCaseController(manager)

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(tcController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build()


    def "user succeeds in starting test case 1"() throws Exception {

        given: "a mock tk receiver that create a sim for this test case run"
            1 * receiver.createEndpoints(_) >> {
                XDRSimulatorInterface sim = Mock(XDRSimulatorInterface)
                sim.endpoint >> { "http://..." }
                sim.endpointTLS >> {"https://..."}
                sim.simulatorId >> {"user1.1.2014"}

                return sim
            }

        db.getXdrFacade() >> {
            def facade = Mock(XDRFacade)
            facade.addNewXdrRecord() >> {
                "record1"
            }
            return facade
        }

        when : "receiving a postXml to create an endpoint"
            MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then : "send back a success message"

            mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))

    }


    MockHttpServletRequestBuilder createEndpointRequest() {
        MockMvcRequestBuilders.post("/api/xdr/tc/1/run")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl("user1"))
    }


    public String testCaseConfig =
            """{
    "tc_config": {
        "endpoint_url": "sut1.testlab1"
    }
}"""
}

