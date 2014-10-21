package gov.nist.healthcare.ttt.webapp.integration.xdr

import gov.nist.healthcare.ttt.webapp.Application
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrReceiveController
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.containsString
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = Application.class)
class XdrReceiveIntegrationSpecTest extends Specification{

    @Autowired
    XdrReceiveController xdrReceiveController

    MockMvc mockMvc

    @Before
    public setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(xdrReceiveController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()
    }

    def "user fails in creating a new endpoint because system is unavailable (exception occured)"() throws Exception {

        when : "receiving a request to create an endpoint"
            MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then : "send back a success message"
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

