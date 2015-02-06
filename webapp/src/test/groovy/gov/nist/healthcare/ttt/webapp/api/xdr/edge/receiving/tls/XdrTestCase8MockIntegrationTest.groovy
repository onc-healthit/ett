package gov.nist.healthcare.ttt.webapp.api.xdr.edge.receiving.tls
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
class XdrTestCase8MockIntegrationTest extends Specification {

    Logger log = LoggerFactory.getLogger(this.class)

    @Autowired
    XdrTestCaseController controller

    @Autowired
    DatabaseInstance db

    @Autowired
    TkListener listener

    MockMvc mockMvcRunTestCase
    MockMvc mockMvcToolkit

    //Because we mock the user as user1 , that are testing the test case 1 and the timestamp is fixed at 2014 by the FakeClock
    static String id = "user1_1_2014"
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
    }


    def "user succeeds in running test case 8"() throws Exception {

        when: "receiving a request to configure test case 8"
        MockHttpServletRequestBuilder getRequest = sendXdrRequest()

        then: "we receive back a message with status and report of the transaction"

        mockMvcRunTestCase.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PASSED"))
    }


    MockHttpServletRequestBuilder sendXdrRequest() {
        MockMvcRequestBuilders.post("/api/xdr/tc/8/configure")
                .accept(MediaType.ALL)
                .content(testCaseConfigGoodTLSPort)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    public static String testCaseConfigGoodTLSPort =
            """{
    "ip_address": "127.0.0.1",
    "port": 12085
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

