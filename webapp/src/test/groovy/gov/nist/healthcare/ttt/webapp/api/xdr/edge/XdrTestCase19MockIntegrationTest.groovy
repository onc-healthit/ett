package gov.nist.healthcare.ttt.webapp.api.xdr.edge
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
class XdrTestCase19MockIntegrationTest extends Specification {

    Logger log = LoggerFactory.getLogger(this.class)

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
    static String testId = "19"
    static String id = "user1_" + testId + "_2014"
    static String userId = "user1"

    String directFromAddress = "from@edge.nist.gov"

    String messageID = 1
    String messageID2 = 2
    String messageID3 = 3
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


    def "user succeeds in running test case 19"() throws Exception {

        when : "we looking for the endpoint"
        MockHttpServletRequestBuilder endpointRequest = getEndpoints()

        //TODO find a way to test endpoint
        then: "we receive back a success message with the endpoints info"
        mockMvcRunTestCase.perform(endpointRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.value.endpoints").exists())
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))


        when: "receiving a request to configure test case $testId"
        MockHttpServletRequestBuilder getRequest = configure()

        then: "we successfully configured our test"
        mockMvcRunTestCase.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))

        when: "receiving a validation report from toolkit. We mock the actual interaction!"

        MockHttpServletRequestBuilder getRequest2 = reportRequest(toolkitReport)
        mockMvcToolkit.perform(getRequest2)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        messageID = "2"

        MockHttpServletRequestBuilder getRequest3 = reportRequest(toolkitReport2)
        mockMvcToolkit.perform(getRequest3)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        messageID = "3"

        MockHttpServletRequestBuilder getRequest4 = reportRequest(toolkitReport3)
        mockMvcToolkit.perform(getRequest4)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then: "we store the validation in the database"
        XDRRecordInterface rec = db.xdrFacade.getLatestXDRRecordByDirectFrom(directFromAddress)

        assert rec.testSteps.size() == 4

        when: "we check the status of testcase $testId"
        MockHttpServletRequestBuilder getRequest5 = checkTestCaseStatusRequest()

        then: "we receive back a success message asking for manual validation"
        mockMvcRunTestCase.perform(getRequest5)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PASSED"))
                .andExpect(jsonPath("content.value").exists())
    }

    MockHttpServletRequestBuilder getEndpoints() {
        MockMvcRequestBuilders.get("/api/xdr/tc/$testId/endpoint")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    MockHttpServletRequestBuilder configure() {
        MockMvcRequestBuilders.post("/api/xdr/tc/$testId/configure")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    MockHttpServletRequestBuilder reportRequest(String toolkitReport) {
        MockMvcRequestBuilders.post("/api/xdrNotification")
                .accept(MediaType.ALL)
                .content(toolkitReport)
                .contentType(MediaType.APPLICATION_XML)
    }

    MockHttpServletRequestBuilder checkTestCaseStatusRequest() {
        MockMvcRequestBuilders.get("/api/xdr/tc/$testId/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    public static String testCaseConfig =
            """{
        "direct_from": "from@edge.nist.gov"
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


    def toolkitReport =
    """
    <transactionLog type='docrec' simId='1'>
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
            &lt;direct:from&gt;from@edge.nist.gov&lt;/direct:from&gt;
            &lt;direct:to&gt;to@edge.nist.gov&lt;/direct:to&gt;
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


    def toolkitReport2 =
            """
    <transactionLog type='docrec' simId='1'>
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
            &lt;direct:from&gt;from@edge.nist.gov&lt;/direct:from&gt;
            &lt;direct:to&gt;to@edge.nist.gov&lt;/direct:to&gt;
            &lt;/direct:addressBlock&gt;
            &lt;wsa:To soapenv:mustUnderstand=&quot;true&quot; xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;http://transport-testing.nist.gov:12080/ttt/sim/f8488a75-fc7d-4d70-992b-e5b2c852b412/rep/prb&lt;/wsa:To&gt;
            &lt;wsa:MessageID soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;$messageID2&lt;/wsa:MessageID&gt;
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


    def toolkitReport3 =
            """
    <transactionLog type='docrec' simId='1'>
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
            &lt;direct:from&gt;from@edge.nist.gov&lt;/direct:from&gt;
            &lt;direct:to&gt;to@edge.nist.gov&lt;/direct:to&gt;
            &lt;/direct:addressBlock&gt;
            &lt;wsa:To soapenv:mustUnderstand=&quot;true&quot; xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;http://transport-testing.nist.gov:12080/ttt/sim/f8488a75-fc7d-4d70-992b-e5b2c852b412/rep/prb&lt;/wsa:To&gt;
            &lt;wsa:MessageID soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;$messageID3&lt;/wsa:MessageID&gt;
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
}

