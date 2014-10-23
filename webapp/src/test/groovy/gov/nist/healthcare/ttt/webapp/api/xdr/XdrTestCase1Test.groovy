package gov.nist.healthcare.ttt.webapp.api.xdr
import gov.nist.healthcare.ttt.database.jdbc.XDRFacade
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.webapp.xdr.core.ResponseHandler
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.xdr.domain.Message
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.containsString
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class XdrTestCase1Test extends Specification{


    XdrReceiver receiver = Mock(XdrReceiver)
    ResponseHandler handler = Mock(ResponseHandler)
    DatabaseInstance db = Mock(DatabaseInstance)
    XdrSender sender = Mock(XdrSender)

    TestCaseManager manager = new TestCaseManager(db, receiver,handler, sender)

    XdrTestCaseController tcController = new XdrTestCaseController(manager)

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(tcController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build()


    def "user succeeds in starting test case 1"() throws Exception {

        given: "a mock tk receiver that create a sim for this test case run"
            1 * receiver.createEndpoints(_) >> {
                Message m = Mock(Message)
                1 * m.success() >> {
                    true
                }

                m.content >> {
                    def sim = new XDRSimulatorImpl()
                    sim.endpoint = "http://..."
                    sim.endpointTLS = "https://..."
                }

                return m
            }

        db.getXdrFacade() >> {
            def facade = Mock(XDRFacade)
            facade.addNewXdrRecord() >> {
                "record1"
            }
            return facade
        }

        when : "receiving a request to create an endpoint"
            MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then : "send back a success message"

            mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUCCESS")))

        when : "notification of a message received arrived"
            println "when a validation report arrived"

        then : "check it is logged properly"

            println "it is stored in the db (not tested here)"
    }


    MockHttpServletRequestBuilder createEndpointRequest() {
        MockMvcRequestBuilders.post("/xdr/tc/1")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
    }


    public String testCaseConfig =
            """{
    "tc_config": {
        "endpoint_url": "sut1.testlab1"
    }
}"""
}

