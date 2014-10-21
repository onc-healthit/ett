package gov.nist.healthcare.ttt.webapp.api.xdr
import gov.nist.healthcare.ttt.webapp.Application
import gov.nist.healthcare.ttt.webapp.xdr.component.ResponseHandler
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
/**
 * Created by gerardin on 10/14/14.
 */
@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = Application.class)
class XdrValidationReportSpecTest extends Specification {

    @Value('${xdr.tool.baseurl}')
    private String notificationUrl

    @Autowired
    ResponseHandler handler

    @Autowired
    TkListener listener

    def "test notification"() {

        given:  'a validation report notification'
        def mockMvc = MockMvcBuilders.standaloneSetup(listener)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post("/$notificationUrl")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)

        when: 'a notification by a web client is received'

        mockMvc
                .perform(req)
                .andDo(print())
                .andReturn()

        then: 'the observer is notified'
                true
    }


    public String testCaseConfig =
            """{
    "tc_config": {
        "endpoint_url": "sut1.testlab1"
    }
}"""
}
