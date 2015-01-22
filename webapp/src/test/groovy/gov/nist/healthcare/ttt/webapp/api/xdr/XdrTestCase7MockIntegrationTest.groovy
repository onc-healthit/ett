package gov.nist.healthcare.ttt.webapp.api.xdr
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.xdr.api.TLSClient
import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
class XdrTestCase7MockIntegrationTest extends Specification {

    Logger log = LoggerFactory.getLogger(this.class)

    @Autowired
    TLSClient client

    @Autowired
    XdrTestCaseController controller

    @Autowired
    DatabaseInstance db

    MockMvc mockMvcRunTestCase
    MockMvc mockMvcToolkit
    MockMvc mockMvcCheckTestCaseStatus


    //TODO change that : either find a better way or rename property
    @Value('${direct.listener.domainName}')
    private String hostname

    @Value('${xdr.tls.test.port}')
    Integer tlsPort

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

        mockMvcCheckTestCaseStatus = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()
    }


    def "user succeeds in running test case 7"() throws Exception {

        when: "receiving a request to run test case 7"
        MockHttpServletRequestBuilder runTc7Request = createHostnameCorrelation()

        then: "we receive back a success message, correlation parameters have been accepted"
        mockMvcRunTestCase.perform(runTc7Request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))


        //setup is completed

        when: "we try to connect to TTT with what we consider a good cert"
        try {
            client.connectOverGoodTLS([hostname: hostname, port: tlsPort])
        }
        catch(Exception e){
            //TODO improve that
            println(e.getCause())
            println("we should throw an exception because TTT give us a bad cert")
        }
        MockHttpServletRequestBuilder checkStatus = checkTestCaseStatusRequest()
        Thread.sleep(4000)

        then: "we receive back a success message if we have disconnect"
        mockMvcRunTestCase.perform(checkStatus)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PASSED"))
    }

    MockHttpServletRequestBuilder createHostnameCorrelation() {
        MockMvcRequestBuilders.post("/api/xdr/tc/7/run")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    MockHttpServletRequestBuilder checkTestCaseStatusRequest() {
        MockMvcRequestBuilders.get("/api/xdr/tc/7/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    public static String testCaseConfig =
            """{
        "hostname": "127.0.0.1"
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

