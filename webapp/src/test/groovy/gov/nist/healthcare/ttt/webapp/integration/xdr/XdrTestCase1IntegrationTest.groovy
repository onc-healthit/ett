package gov.nist.healthcare.ttt.webapp.integration.xdr

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import sun.security.acl.PrincipalImpl

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase1IntegrationTest extends Specification {


    @Autowired
    XdrTestCaseController controller

    @Autowired
    DatabaseInstance db

    @Autowired
    TkListener listener

    MockMvc mockMvcRunTestCase
    MockMvc mockMvcToolkit
    MockMvc mockMvcCheckTestCaseStatus

    //Because we mock the user as user1 , twe are testing the test case 1 and the timestamp is fixed at 2014
    String id = "user1.1.2014"

    @Before
    public setup() {
        mockMvcRunTestCase = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()

        mockMvcToolkit = MockMvcBuilders.standaloneSetup(listener)
                .setMessageConverters(new Jaxb2RootElementHttpMessageConverter())
                .build()

        mockMvcCheckTestCaseStatus = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()
    }




    def "user succeeds in running test case 1"() throws Exception {

        when: "receiving a postXml to run test case 1"
        MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then: "we receive back a success message"

        mockMvcRunTestCase.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.endpoint").value("http://"))
                .andExpect(jsonPath("content.endpointTLS").value("https://"))

        when: "receiving a validation report from toolkit"
        MockHttpServletRequestBuilder getRequest2 = reportRequest()

        then: "we store the validation in the database"

        mockMvcToolkit.perform(getRequest2)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        List<XDRRecordInterface> records = db.xdrFacade.getXDRRecordsBySimulatorId(id)
        XDRRecordInterface rec = records.last()
        def step = rec.getTestSteps().find {
            it.xdrSimulator.simulatorId == id
        }

        assert step.xdrReportItems.get(0).report == "success"


        when: "check the status of testcase 1"
        MockHttpServletRequestBuilder getRequest3 = checkTestCaseStatusRequest()

        then: "we receive back a success message"
        mockMvcRunTestCase.perform(getRequest3)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content").value("SUCCESS"))
    }


    MockHttpServletRequestBuilder createEndpointRequest() {
        MockMvcRequestBuilders.post("/api/xdr/tc/1/run")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl("user1"))
    }


    MockHttpServletRequestBuilder reportRequest() {
        MockMvcRequestBuilders.post("/api/xdrNotification")
                .accept(MediaType.ALL)
                .content(toolkitMockMessage)
                .contentType(MediaType.APPLICATION_XML)
    }

    MockHttpServletRequestBuilder checkTestCaseStatusRequest() {
        MockMvcRequestBuilders.post("/api/xdr/tc/1/status")
                .accept(MediaType.ALL)
                .content(checkStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl("user1"))
    }

    public static String testCaseConfig =
            """{
    "tc_config": {
        "endpoint_url": "sut1.testlab1"
    }
}"""


    private static String toolkitMockMessage =
            """
<report>
    <simId>user1.1.2014</simId>
    <status>success</status>
    <details>blabla</details>
</report>
            """

    public static String checkStatus =
            """{
            }"""
}

