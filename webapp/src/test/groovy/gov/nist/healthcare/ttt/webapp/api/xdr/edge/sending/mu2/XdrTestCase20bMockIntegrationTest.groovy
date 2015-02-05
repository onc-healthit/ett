package gov.nist.healthcare.ttt.webapp.api.xdr.edge.sending.mu2
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.TestUtils
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import sun.security.acl.PrincipalImpl

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase20bMockIntegrationTest extends Specification {

    Logger log = LoggerFactory.getLogger(this.class)

    @Autowired
    XdrTestCaseController controller

    @Autowired
    DatabaseInstance db

    @Autowired
    TkListener listener

    String messageID = 12345
    String fromAddress = "from@hit-dev.nist.gov"
    String toAddress = "to@hit-dev.nist.gov"

    MockMvc mockMvcRunTestCase
    MockMvc mockMvcToolkit
    MockMvc mockMvcCheckTestCaseStatus


    static String tcId = "20b"
    static String simEndpoint = "http://hit-dev.nist.gov:11080/xdstools3/sim/$simId/docrec/prb"
    static String simId = "20b_badEndpoint"
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
                .build()

        mockMvcCheckTestCaseStatus = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()
    }


    def "user succeeds in running test case"() throws Exception {

        when: "receiving a request to configure test case"
        MockHttpServletRequestBuilder getRequest = configureTestCaseRequest()

        then: "we receive back a success message with the endpoints info"
        mockMvcRunTestCase.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("MANUAL"))


        when: "receiving 2 xdr messages from toolkit. We mock the actual interaction!"

        MockHttpServletRequestBuilder getRequest3 = sendXDRMessageRequest()
        mockMvcToolkit.perform(getRequest3)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then: "we store the validation in the database"
        XDRRecordInterface rec = db.xdrFacade.getLatestXDRRecordByDirectFrom("$fromAddress")
        assert rec != null
        assert rec.testSteps.size() == 2


        when: "we check the status of testcase20b"
        MockHttpServletRequestBuilder getRequest4 = checkTestCaseStatusRequest()

        then: "we receive back a success message asking for manual validation"
        mockMvcRunTestCase.perform(getRequest4)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("MANUAL"))
                .andExpect(jsonPath("content.value").exists())
    }

    MockHttpServletRequestBuilder configureTestCaseRequest() {
        MockMvcRequestBuilders.post("/api/xdr/tc/$tcId/configure")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    MockHttpServletRequestBuilder sendXDRMessageRequest() {
        MockMvcRequestBuilders.post("/api/xdrNotification")
                .accept(MediaType.ALL)
                .content(TestUtils.buildReportTemplate(simId,messageID,fromAddress,toAddress))
                .contentType(MediaType.APPLICATION_XML)
    }

    MockHttpServletRequestBuilder checkTestCaseStatusRequest() {
        MockMvcRequestBuilders.get("/api/xdr/tc/$tcId/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    public String testCaseConfig =
            """{
        "direct_from": "$fromAddress"
}"""


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

