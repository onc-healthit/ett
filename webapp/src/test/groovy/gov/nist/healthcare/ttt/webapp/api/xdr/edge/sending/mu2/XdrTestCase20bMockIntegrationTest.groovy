package gov.nist.healthcare.ttt.webapp.api.xdr.edge.sending.mu2
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
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

    static String goodEndpointId = "20_goodEndpoint"
    static String badEndpointId = "20_badEndpoint"

    MockMvc mockMvcRunTestCase
    MockMvc mockMvcToolkit
    MockMvc mockMvcCheckTestCaseStatus

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

        mockMvcToolkit = MockMvcBuilders.standaloneSetup(listener)
                .build()

        mockMvcCheckTestCaseStatus = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()
    }


    def "user succeeds in running test case 20b"() throws Exception {

        when: "receiving a request to configure test case 20b"
        MockHttpServletRequestBuilder getRequest = configureTestCaseRequest()

        then: "we receive back a success message with the endpoints info"
        mockMvcRunTestCase.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("MANUAL"))


        when: "receiving 2 xdr messages from toolkit. We mock the actual interaction!"

        println "first message..."
        MockHttpServletRequestBuilder getRequest2 = sendXDRMessageRequest()
        mockMvcToolkit.perform(getRequest2)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
        println "first message done..."

        println "second message..."
        MockHttpServletRequestBuilder getRequest3 = send2ndXDRMessageRequest()
        mockMvcToolkit.perform(getRequest3)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
        println "second message done..."

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
        MockMvcRequestBuilders.post("/api/xdr/tc/20b/configure")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }


    MockHttpServletRequestBuilder sendXDRMessageRequest() {
        MockMvcRequestBuilders.post("/api/xdrNotification")
                .accept(MediaType.ALL)
                .content(toolkitReport)
                .contentType(MediaType.APPLICATION_XML)
    }

    MockHttpServletRequestBuilder send2ndXDRMessageRequest() {
        MockMvcRequestBuilders.post("/api/xdrNotification")
                .accept(MediaType.ALL)
                .content(toolkitReport2)
                .contentType(MediaType.APPLICATION_XML)
    }

    MockHttpServletRequestBuilder checkTestCaseStatusRequest() {
        MockMvcRequestBuilders.get("/api/xdr/tc/20a/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    public String testCaseConfig =
            """{
        "direct_from": "$fromAddress"
}"""


    private toolkitReport =
            """
    <transactionLog type='docrec' simId='$goodEndpointId'>
    <request>
        <header>content-type: multipart/related; boundary=&quot;MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20&quot;; type=&quot;application/xop+xml&quot;; start=&quot;&lt;0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org&gt;&quot;; start-info=&quot;application/soap+xml&quot;; action=&quot;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b&quot;
            user-agent: TempXDRSender
            host: edge.nist.gov:8080
            Content-Length: 123393

        </header>
        <body>
            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20
            Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
            Content-Transfer-Encoding: binary
            Content-ID: &lt;0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org&gt;

            &lt;s:Envelope xmlns:s=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:a=&quot;http://www.w3.org/2005/08/addressing&quot;&gt;
            &lt;soapenv:Header xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
            &lt;direct:metadata-level xmlns:direct=&quot;urn:direct:addressing&quot;&gt;XDS&lt;/direct:metadata-level&gt;
            &lt;direct:addressBlock xmlns:direct=&quot;urn:direct:addressing&quot;
            soapenv:role=&quot;urn:direct:addressing:destination&quot;
            soapenv:relay=&quot;true&quot;&gt;
            &lt;direct:from&gt;$fromAddress&lt;/direct:from&gt;
            &lt;direct:to&gt;$toAddress&lt;/direct:to&gt;
            &lt;/direct:addressBlock&gt;
            &lt;wsa:To soapenv:mustUnderstand=&quot;true&quot; xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;http://transport-testing.nist.gov:12080/ttt/sim/f8488a75-fc7d-4d70-992b-e5b2c852b412/rep/prb&lt;/wsa:To&gt;
            &lt;wsa:MessageID soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;$messageID&lt;/wsa:MessageID&gt;
            &lt;wsa:Action soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b&lt;/wsa:Action&gt;
            &lt;/soapenv:Header&gt;
            &lt;soapenv:Body xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
            &lt;/soapenv:Body&gt;
            &lt;/s:Envelope&gt;

            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20
            Content-Type: application/octet-stream
            Content-Transfer-Encoding: binary
            Content-ID: &lt;1.3293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org&gt;

            content

            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20--
        </body>
    </request>
    <response>
        <header>
            content-type: multipart/related; boundary=MIMEBoundary112233445566778899;  type="application/xop+xml"; start="&lt;doc0@ihexds.nist.gov&gt;"; start-info="application/soap+xml"
        </header>
        <body>
            --MIMEBoundary112233445566778899
            Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
            Content-Transfer-Encoding: binary
            Content-ID: &lt;doc0@ihexds.nist.gov&gt;


            &lt;S:Envelope xmlns:S="http://www.w3.org/2003/05/soap-envelope"&gt;
            &lt;S:Header&gt;
            &lt;wsa:Action s:mustUnderstand="1" xmlns:s="http://www.w3.org/2003/05/soap-envelope"
            xmlns:wsa="http://www.w3.org/2005/08/addressing"&gt;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse&lt;/wsa:Action&gt;
            &lt;wsa:RelatesTo xmlns:wsa="http://www.w3.org/2005/08/addressing"&gt;urn:uuid:2E3E584BB87837BC3B1417028462849&lt;/wsa:RelatesTo&gt;
            &lt;/S:Header&gt;
            &lt;S:Body&gt;
            &lt;rs:RegistryResponse status="urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure"
            xmlns:rs="urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0"&gt;
            &lt;rs:RegistryErrorList&gt;
            &lt;rs:RegistryError errorCode="" codeContext="EXPECTED: XML starts with; FOUND: &amp;lt;soapenv:Body xmlns:soape : MSG Schema: cvc-elt.1: Cannot find the declaration of element 'soapenv:Body'."
            location=""/&gt;
            &lt;/rs:RegistryErrorList&gt;
            &lt;/rs:RegistryResponse&gt;
            &lt;/S:Body&gt;
            &lt;/S:Envelope&gt;

            --MIMEBoundary112233445566778899--
        </body>
    </response>
</transactionLog>
    """


    private toolkitReport2 =
            """
    <transactionLog type='docrec' simId='$badEndpointId'>
    <request>
        <header>content-type: multipart/related; boundary=&quot;MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20&quot;; type=&quot;application/xop+xml&quot;; start=&quot;&lt;0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org&gt;&quot;; start-info=&quot;application/soap+xml&quot;; action=&quot;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b&quot;
            user-agent: TempXDRSender
            host: edge.nist.gov:8080
            Content-Length: 123393

        </header>
        <body>
            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20
            Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
            Content-Transfer-Encoding: binary
            Content-ID: &lt;0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org&gt;

            &lt;s:Envelope xmlns:s=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:a=&quot;http://www.w3.org/2005/08/addressing&quot;&gt;
            &lt;soapenv:Header xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
            &lt;direct:metadata-level xmlns:direct=&quot;urn:direct:addressing&quot;&gt;XDS&lt;/direct:metadata-level&gt;
            &lt;direct:addressBlock xmlns:direct=&quot;urn:direct:addressing&quot;
            soapenv:role=&quot;urn:direct:addressing:destination&quot;
            soapenv:relay=&quot;true&quot;&gt;
            &lt;direct:from&gt;$fromAddress&lt;/direct:from&gt;
            &lt;direct:to&gt;$toAddress&lt;/direct:to&gt;
            &lt;/direct:addressBlock&gt;
            &lt;wsa:To soapenv:mustUnderstand=&quot;true&quot; xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;http://transport-testing.nist.gov:12080/ttt/sim/f8488a75-fc7d-4d70-992b-e5b2c852b412/rep/prb&lt;/wsa:To&gt;
            &lt;wsa:MessageID soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;$messageID&lt;/wsa:MessageID&gt;
            &lt;wsa:Action soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b&lt;/wsa:Action&gt;
            &lt;/soapenv:Header&gt;
            &lt;soapenv:Body xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
            &lt;/soapenv:Body&gt;
            &lt;/s:Envelope&gt;

            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20
            Content-Type: application/octet-stream
            Content-Transfer-Encoding: binary
            Content-ID: &lt;1.3293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org&gt;

            content

            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20--
        </body>
    </request>
    <response>
        <header>
            content-type: multipart/related; boundary=MIMEBoundary112233445566778899;  type="application/xop+xml"; start="&lt;doc0@ihexds.nist.gov&gt;"; start-info="application/soap+xml"
        </header>
        <body>
            --MIMEBoundary112233445566778899
            Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
            Content-Transfer-Encoding: binary
            Content-ID: &lt;doc0@ihexds.nist.gov&gt;


            &lt;S:Envelope xmlns:S="http://www.w3.org/2003/05/soap-envelope"&gt;
            &lt;S:Header&gt;
            &lt;wsa:Action s:mustUnderstand="1" xmlns:s="http://www.w3.org/2003/05/soap-envelope"
            xmlns:wsa="http://www.w3.org/2005/08/addressing"&gt;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse&lt;/wsa:Action&gt;
            &lt;wsa:RelatesTo xmlns:wsa="http://www.w3.org/2005/08/addressing"&gt;urn:uuid:2E3E584BB87837BC3B1417028462849&lt;/wsa:RelatesTo&gt;
            &lt;/S:Header&gt;
            &lt;S:Body&gt;
            &lt;rs:RegistryResponse status="urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure"
            xmlns:rs="urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0"&gt;
            &lt;rs:RegistryErrorList&gt;
            &lt;rs:RegistryError errorCode="" codeContext="EXPECTED: XML starts with; FOUND: &amp;lt;soapenv:Body xmlns:soape : MSG Schema: cvc-elt.1: Cannot find the declaration of element 'soapenv:Body'."
            location=""/&gt;
            &lt;/rs:RegistryErrorList&gt;
            &lt;/rs:RegistryResponse&gt;
            &lt;/S:Body&gt;
            &lt;/S:Envelope&gt;

            --MIMEBoundary112233445566778899--
        </body>
    </response>
</transactionLog>
    """

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

