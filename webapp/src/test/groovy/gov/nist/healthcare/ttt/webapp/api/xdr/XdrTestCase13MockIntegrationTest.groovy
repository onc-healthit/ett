package gov.nist.healthcare.ttt.webapp.api.xdr
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageInfoForXdr
import gov.nist.healthcare.ttt.webapp.direct.direcForXdr.DirectMessageSenderForXdrNoLookUp
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.core.io.ClassPathResource
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
class XdrTestCase13MockIntegrationTest extends Specification {

    Logger log = LoggerFactory.getLogger(this.class)

    @Value('${direct.listener.domainName}')
    String directListenerDomain

    @Value('${direct.listener.port}')
    int directListenerPort

    @Autowired
    XdrTestCaseController controller

    @Autowired
    DatabaseInstance db

    @Autowired
    TkListener listener

    MockMvc mockMvcRunTestCase
    MockMvc mockMvcToolkit

    //Because we mock the user as user1 , that are testing the test case 1 and the timestamp is fixed at 2014 by the FakeClock
    static String id = "user1.1.2014"
    static String userId = "user1"
    static String tcid = "13"

    /*
    Set up mockmvc with the necessary converter (json or xml)
     */
    @Before
    public setup() {

        setupDbForDirectTesting()

        mockMvcRunTestCase = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()
    }


    def "user succeeds in running test case 13"() throws Exception {

        when: "receiving a request to run test case $tcid"
        MockHttpServletRequestBuilder runTestCase13 = runTestCase13()

        then: "we receive back a message with status and report of the transaction"

        mockMvcRunTestCase.perform(runTestCase13)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))


        when : "a direct message is sent to ttt"
        File cert = new ClassPathResource("directCert/testCert.der").getFile()
        DirectMessageInfoForXdr info = new DirectMessageSenderForXdrNoLookUp().sendDirectWithCCDAForXdrNoDNSLookUp("antoine@$directListenerDomain", directListenerPort,cert.absolutePath)

        then: "ttt sent back a MDN"
        //This is what should happen, we cannot automate the test since the direct sender uses the DNSLookup.

        when : "a user check the test status"
        MockHttpServletRequestBuilder getRequest3 = checkTestCaseStatusRequest()

        then : "we return the result of the direct validation which is successful"
        //TODO not satisfactory. Direct takes some time to receive and validate.
        //We should have a timeout. If result is pending, wait until status is success or pending_manual_check
        //A timeout that expires is equivalent to failing the test.
        Thread.sleep(2000)
        String logId = db.logFacade.getLogIDByMessageId(info.getMessageId())
        assert logId : "sb has no log direct message. Was it given enough time?"

        mockMvcRunTestCase.perform(getRequest3)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content").value("PASSED"))


    }




    MockHttpServletRequestBuilder runTestCase13() {
        MockMvcRequestBuilders.post("/api/xdr/tc/$tcid/run")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    MockHttpServletRequestBuilder checkTestCaseStatusRequest() {
        MockMvcRequestBuilders.get("/api/xdr/tc/$tcid/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }


    public static String testCaseConfig =
            """{
    "targetEndpoint":"https://example.com/xdr"
}"""

    def setupDbForDirectTesting() {
        createUserInDB()
        db.xdrFacade.removeAllByUsername(userId)
        db.df.addNewDirectEmail("directfrom4xdr@localhost")
        log.info("db data fixture set up.")
    }

    public void createUserInDB() throws Exception {
        if (!db.getDf().doesUsernameExist(userId)) {
            db.getDf().addUsernamePassword(userId, "pass")

        }
        assert db.getDf().doesUsernameExist(userId)


    }
}

