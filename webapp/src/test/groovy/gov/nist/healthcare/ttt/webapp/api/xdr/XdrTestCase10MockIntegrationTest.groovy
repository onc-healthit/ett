package gov.nist.healthcare.ttt.webapp.api.xdr
import com.fasterxml.jackson.databind.ObjectMapper
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.xdr.web.TkListener
import groovy.json.JsonSlurper
import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
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
class XdrTestCase10MockIntegrationTest extends Specification {

    Logger log = LoggerFactory.getLogger(this.class)

    ObjectMapper mapper = new ObjectMapper()


    //depends on the test performed. This pointing to the actual NIST tool
    static String sutDirectAddress = "antoine@transport-testing.nist.gov"
    static String sutDirectPort = "25"

    @Autowired
    XdrTestCaseController controller

    @Autowired
    DatabaseInstance db

    @Autowired
    TkListener listener

    MockMvc mockMvcRunTestCase
    MockMvc mockMvcToolkit
    MockMvc mockMvcCheckTestCaseStatus

    //Because we mock the user as user1 , that are testing the test case 1 and the timestamp is fixed at 2014 by the FakeClock
    static String userId = "user1"

    /*
    Set up mockmvc with the necessary converter (json or xml)
     */

    @Before
    public setup() {

        setupDb()

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


    def "user succeeds in running test case 10"() throws Exception {

        when: "receiving a request to run test case 10"
        MockHttpServletRequestBuilder getRequest = runTestcase10()

        then: "we receive a response that direct has been successfully sent"

        MvcResult res = mockMvcRunTestCase.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andReturn()

        when: "receiving a validation report from toolkit"
        def response = res.response.contentAsString
        def slurper = new JsonSlurper()
        def result = slurper.parseText(response)
        def id = result.content.messageId

        MockHttpServletRequestBuilder getRequest2 = reportRequest(id)

        then: "toolkit receives an ok"

        mockMvcToolkit.perform(getRequest2)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        when: "we check the status of testcase 10"
        MockHttpServletRequestBuilder getRequest3 = checkTestCaseStatusRequest()

        then: "we receive back a success message"
        mockMvcRunTestCase.perform(getRequest3)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content").value("PASSED"))
    }

    MockHttpServletRequestBuilder runTestcase10() {
        MockMvcRequestBuilders.post("/api/xdr/tc/10/run")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    MockHttpServletRequestBuilder reportRequest(String id) {

        String content = toolkitMockMessage(id)

        MockMvcRequestBuilders.post("/api/xdrNotification")
                .accept(MediaType.ALL)
                .content(content)
                .contentType(MediaType.APPLICATION_XML)
    }

    MockHttpServletRequestBuilder checkTestCaseStatusRequest() {
        MockMvcRequestBuilders.get("/api/xdr/tc/10/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    public static String testCaseConfig =
            """{
                "sutDirectAddress" : $sutDirectAddress,
                "sutDirectPort" : $sutDirectPort
}"""


    private String toolkitMockMessage(String id) {

        def cleanedUpId = id.substring(1, id.length() - 1)

        return """
<report>
    <simulatorId>yo</simulatorId>
    <messageId>$cleanedUpId</messageId>
    <status>success</status>
    <details>blabla</details>
</report>
            """
    }

    def setupDb() {
        createUserInDB()
        db.xdrFacade.removeAllByUsername(userId)
        log.info("db data fixture set up.")
    }

    public void createUserInDB() throws Exception {
        if (!db.getDf().doesUsernameExist(userId)) {
            db.getDf().addUsernamePassword(userId, "pass")
        }
        assert db.getDf().doesUsernameExist(userId)


    }
}

