package gov.nist.healthcare.ttt.webapp.api.xdr

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrReceiveController
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.webapp.xdr.core.ResponseHandler
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

class XdrReceiveSpecTest extends Specification{


    XdrReceiver tkReceiver = Mock(XdrReceiver)
    ResponseHandler handler = Mock(ResponseHandler)
    DatabaseInstance db = Mock(DatabaseInstance)

    XdrReceiveController xdrReceiveController = new XdrReceiveController(tkReceiver,handler,db)

    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(xdrReceiveController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build()


    def "user succeeds in creating a new endpoint"() throws Exception {

        given: "a mock toolkit adapter that sends a positive result"
            1 * tkReceiver.createEndpoints(_) >> {
                Message r = Mock(Message)
                1 * r.success() >> {
                    true
                }
                return r
            }

        when : "receiving a request to create an endpoint"
            MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then : "send back a success message"

            mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tc1")))
    }



    def "user fails to creates a new endpoint"() throws Exception {

        given: "a mock toolkit adapter that sends a negative result"
            1 * tkReceiver.createEndpoints(_) >> {

                Message r = Mock(Message)
                1 * r.success() >> {
                    false
                }
                return r
        }

        when : "receiving a request to create an endpoint"
            MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then : "send back a fail message"
            mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ERROR")))
    }


    MockHttpServletRequestBuilder createEndpointRequest() {
        MockMvcRequestBuilders.post("/xdr/receive/tc1/endpoint")
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

