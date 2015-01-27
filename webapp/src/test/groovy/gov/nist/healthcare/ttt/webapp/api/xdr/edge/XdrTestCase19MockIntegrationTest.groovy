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

    String msgId = "0e023fc1-aca2-4125-862e-860ebf7295a-1"
    String directFromAddress = "from@edge.nist.gov"

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

        when: "receiving a request to run test case $testId"
        MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then: "we receive back a success message with the endpoints info"
        mockMvcRunTestCase.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.value.endpoint")
                .value("https://hit-dev.nist.gov:11080/xdstools3/sim/xdr_global_endpoint_tc_19/docrec/prb"))
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))

        when: "receiving a validation report from toolkit. We mock the actual interaction!"

        MockHttpServletRequestBuilder getRequest2 = reportRequest()
        mockMvcToolkit.perform(getRequest2)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        msgId = "0e023fc1-aca2-4125-862e-860ebf7295a-2"

        MockHttpServletRequestBuilder getRequest3 = reportRequest()
        mockMvcToolkit.perform(getRequest3)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        msgId = "0e023fc1-aca2-4125-862e-860ebf7295a-3"

        MockHttpServletRequestBuilder getRequest4 = reportRequest()
        mockMvcToolkit.perform(getRequest4)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then: "we store the validation in the database"
        XDRRecordInterface rec = db.xdrFacade.getLatestXDRRecordByDirectFrom(directFromAddress)
        def step = rec.testSteps.find{
            it.name == "XDR_RECEIVE"
        }


        assert !step.xdrReportItems.get(0).report.empty


        when: "we check the status of testcase $testId"
        MockHttpServletRequestBuilder getRequest5 = checkTestCaseStatusRequest()

        then: "we receive back a success message asking for manual validation"
        mockMvcRunTestCase.perform(getRequest5)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("MANUAL"))
                .andExpect(jsonPath("content.value").exists())
    }

    MockHttpServletRequestBuilder createEndpointRequest() {
        MockMvcRequestBuilders.post("/api/xdr/tc/$testId/run")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }


    MockHttpServletRequestBuilder reportRequest() {
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


    def toolkitReport =
            """
<transactionLog type='docrec' simId='1'>
    <request>
        <header>content-type: multipart/related; boundary="MIMEBoundary_f41f86a92d39c3883023f2dbbaee45f5ae5bba5d4ffbfe70"; type="application/xop+xml"; start="&lt;0.c41f86a92d39c3883023f2dbbaee45f5ae5bba5d4ffbfe70@apache.org&gt;"; start-info="application/soap+xml"; action="urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b"
        user-agent: Axis2
        host: localhost:9080
        transfer-encoding: chunked
        </header>
        <body>
        --MIMEBoundary_f41f86a92d39c3883023f2dbbaee45f5ae5bba5d4ffbfe70
        Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
        Content-Transfer-Encoding: binary
        Content-ID: &lt;0.c41f86a92d39c3883023f2dbbaee45f5ae5bba5d4ffbfe70@apache.org&gt;

&lt;s:Envelope xmlns:s=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
    xmlns:a=&quot;http://www.w3.org/2005/08/addressing&quot;&gt;
    &lt;soapenv:Header xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
        &lt;direct:metadata-level xmlns:direct=&quot;urn:direct:addressing&quot;&gt;XDS&lt;/direct:metadata-level&gt;
        &lt;direct:addressBlock xmlns:direct=&quot;urn:direct:addressing&quot;
            soapenv:role=&quot;urn:direct:addressing:destination&quot;
            soapenv:relay=&quot;true&quot;&gt;
            &lt;direct:from&gt;directFrom&lt;/direct:from&gt;
            &lt;direct:to&gt;directTo&lt;/direct:to&gt;
        &lt;/direct:addressBlock&gt;
        &lt;wsa:To soapenv:mustUnderstand=&quot;true&quot; xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;http://transport-testing.nist.gov:12080/ttt/sim/f8488a75-fc7d-4d70-992b-e5b2c852b412/rep/prb&lt;/wsa:To&gt;
        &lt;wsa:MessageID soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;30f7b099-8886-48b7-8918-73c8e188dff2&lt;/wsa:MessageID&gt;
        &lt;wsa:Action soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b&lt;/wsa:Action&gt;
    &lt;/soapenv:Header&gt;
    &lt;soapenv:Body xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
&lt;xdsb:ProvideAndRegisterDocumentSetRequest xmlns:xdsb=&quot;urn:ihe:iti:xds-b:2007&quot;&gt;
    &lt;lcm:SubmitObjectsRequest xmlns:lcm=&quot;urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0&quot;&gt;
        &lt;rim:RegistryObjectList xmlns:rim=&quot;urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0&quot;&gt;
            &lt;rim:ExtrinsicObject id=&quot;id_extrinsicobject&quot;
                mimeType=&quot;text/xml&quot; objectType=&quot;urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1&quot;&gt;
                &lt;rim:Slot name=&quot;creationTime&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;20120806&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Slot name=&quot;languageCode&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;en-us&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Slot name=&quot;serviceStartTime&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;200612230800&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Slot name=&quot;serviceStopTime&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;200612230900&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Slot name=&quot;sourcePatientId&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;1^^^&amp;amp;2.16.840.1.113883.4.6&amp;amp;ISO&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Slot name=&quot;sourcePatientInfo&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;PID-3|1^^^&amp;amp;2.16.840.1.113883.4.6&amp;amp;ISO&lt;/rim:Value&gt;
                        &lt;rim:Value&gt;PID-5|Jones^Isabella^^^^&lt;/rim:Value&gt;
                        &lt;rim:Value&gt;PID-7|19470501&lt;/rim:Value&gt;
                        &lt;rim:Value&gt;PID-8|F&lt;/rim:Value&gt;
                        &lt;rim:Value&gt;PID-11|1357 Amber Drive^^Beaverton^OR^97006^&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Name&gt;
                    &lt;rim:LocalizedString value=&quot;DocA&quot;/&gt;
                &lt;/rim:Name&gt;
                &lt;rim:Description/&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d&quot;
                    classifiedObject=&quot;id_extrinsicobject&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_1&quot;&gt;
                    &lt;rim:Slot name=&quot;authorPerson&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;7.6^Epic - Version 7.6&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Slot name=&quot;authorInstitution&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;Get Well Clinic&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                &lt;/rim:Classification&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a&quot;
                    classifiedObject=&quot;id_extrinsicobject&quot;
                    nodeRepresentation=&quot;34133-9&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_3&quot;&gt;
                    &lt;rim:Slot name=&quot;codingScheme&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;HITSP/C80&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;Summarization of Episode Note&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:Classification&gt;

                &lt;!-- Value from HITSP/C80 table 2-146 --&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f&quot;
                    classifiedObject=&quot;id_extrinsicobject&quot;
                    nodeRepresentation=&quot;N&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_4&quot;&gt;
                    &lt;rim:Slot name=&quot;codingScheme&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;HITSP/C80&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;Normal&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:Classification&gt;

                &lt;!-- Not using HITSP/C80 Table 2-152 because none applied for C-CDA --&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d&quot;
                    classifiedObject=&quot;id_extrinsicobject&quot;
                    nodeRepresentation=&quot;CDAR2/IHE 1.0&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_5&quot;&gt;
                    &lt;rim:Slot name=&quot;codingScheme&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;Connect-a-thon formatCodes&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;CDAR2/IHE 1.0&quot;/&gt;
                    &lt;/rim:Name&gt;

                &lt;/rim:Classification&gt;


                &lt;!-- Value from HITSP/C80 table 2-146 --&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1&quot;
                    classifiedObject=&quot;id_extrinsicobject&quot;
                    nodeRepresentation=&quot;72311000&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_6&quot;&gt;
                    &lt;rim:Slot name=&quot;codingScheme&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;HITSP/C80&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;Health maintenance organization&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:Classification&gt;

                &lt;!-- Value from HITSP/C80 table 2-149 --&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead&quot;
                    classifiedObject=&quot;id_extrinsicobject&quot;
                    nodeRepresentation=&quot;408443003&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_7&quot;&gt;
                    &lt;rim:Slot name=&quot;codingScheme&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;Connect-a-thon practiceSettingCodes&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;General medical practice&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:Classification&gt;


                &lt;!-- LOINC code from HITSP/C80 table 2-144  --&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:f0306f51-975f-434e-a61c-c59651d33983&quot;
                    classifiedObject=&quot;id_extrinsicobject&quot;
                    nodeRepresentation=&quot;34133-9&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_10&quot;&gt;
                    &lt;rim:Slot name=&quot;codingScheme&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;LOINC&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;Summarization of episode note&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:Classification&gt;
                &lt;rim:ExternalIdentifier
                    identificationScheme=&quot;urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427&quot;
                    value=&quot;1^^^&amp;amp;2.16.840.1.113883.4.6&amp;amp;ISO&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier&quot;
                    id=&quot;id_11&quot; registryObject=&quot;id_extrinsicobject&quot;&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;XDSDocumentEntry.patientId&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:ExternalIdentifier&gt;
                &lt;rim:ExternalIdentifier
                    identificationScheme=&quot;urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab&quot;
                    value=&quot;1.42.20140915172101.10.1&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier&quot;
                    id=&quot;id_12&quot; registryObject=&quot;id_extrinsicobject&quot;&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;XDSDocumentEntry.uniqueId&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:ExternalIdentifier&gt;
            &lt;/rim:ExtrinsicObject&gt;
            &lt;rim:RegistryPackage id=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;
                objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage&quot;&gt;
                &lt;rim:Slot name=&quot;submissionTime&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;20110117211159&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Slot name=&quot;intendedRecipient&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;Some
                            Hospital^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45|^Wel^Marcus^^^Dr^MD|^^Internet^mwel@healthcare.example.org&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
                &lt;rim:Name&gt;
                    &lt;rim:LocalizedString value=&quot;Physical&quot;/&gt;
                &lt;/rim:Name&gt;
                &lt;rim:Description&gt;
                    &lt;rim:LocalizedString value=&quot;Annual physical&quot;/&gt;
                &lt;/rim:Description&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d&quot;
                    classifiedObject=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;
                    nodeRepresentation=&quot;&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_13&quot;&gt;
                    &lt;rim:Slot name=&quot;authorPerson&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;7.6^Epic - Version 7.6&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Slot name=&quot;authorInstitution&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;Get Well Clinic&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Slot name=&quot;authorTelecommunication&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;^^Internet^john.doe@healthcare.example.org&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                &lt;/rim:Classification&gt;
                &lt;rim:Classification
                    classificationScheme=&quot;urn:uuid:aa543740-bdda-424e-8c96-df4873be8500&quot;
                    classifiedObject=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;
                    nodeRepresentation=&quot;34133-9&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;
                    id=&quot;id_14&quot;&gt;
                    &lt;rim:Slot name=&quot;codingScheme&quot;&gt;
                        &lt;rim:ValueList&gt;
                            &lt;rim:Value&gt;HITSP/C80&lt;/rim:Value&gt;
                        &lt;/rim:ValueList&gt;
                    &lt;/rim:Slot&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;Summarization of Episode Note&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:Classification&gt;
                &lt;rim:ExternalIdentifier
                    identificationScheme=&quot;urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8&quot;
                    value=&quot;2.16.840.1.113883.3.72.5.1418156750379&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier&quot;
                    id=&quot;id_15&quot; registryObject=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;XDSSubmissionSet.uniqueId&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:ExternalIdentifier&gt;
                &lt;rim:ExternalIdentifier id=&quot;fefcba76-ab23-4138-96ce-795f02b26d79&quot;
                    registryObject=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;
                    identificationScheme=&quot;urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832&quot;
                    value=&quot;1.2.840.114350.1.13.252.1.7.2.688879&quot;&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;XDSSubmissionSet.sourceId&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:ExternalIdentifier&gt;

                &lt;rim:ExternalIdentifier
                    identificationScheme=&quot;urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446&quot;
                    value=&quot;1^^^&amp;amp;2.16.840.1.113883.4.6&amp;amp;ISO&quot;
                    objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier&quot;
                    id=&quot;id_17&quot; registryObject=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;&gt;
                    &lt;rim:Name&gt;
                        &lt;rim:LocalizedString value=&quot;XDSSubmissionSet.patientId&quot;/&gt;
                    &lt;/rim:Name&gt;
                &lt;/rim:ExternalIdentifier&gt;
            &lt;/rim:RegistryPackage&gt;
            &lt;rim:Classification classifiedObject=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;
                classificationNode=&quot;urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd&quot;
                id=&quot;urn:uuid:c6e5bf3c-3e5b-4777-bfcd-85e5c66328e1&quot;
                objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification&quot;/&gt;
            &lt;rim:Association
                associationType=&quot;urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember&quot;
                sourceObject=&quot;urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10&quot;
                targetObject=&quot;id_extrinsicobject&quot;
                id=&quot;id_association&quot;
                objectType=&quot;urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association&quot;&gt;
                &lt;rim:Slot name=&quot;SubmissionSetStatus&quot;&gt;
                    &lt;rim:ValueList&gt;
                        &lt;rim:Value&gt;Original&lt;/rim:Value&gt;
                    &lt;/rim:ValueList&gt;
                &lt;/rim:Slot&gt;
            &lt;/rim:Association&gt;
        &lt;/rim:RegistryObjectList&gt;
    &lt;/lcm:SubmitObjectsRequest&gt;
&lt;/xdsb:ProvideAndRegisterDocumentSetRequest&gt;
    &lt;/soapenv:Body&gt;
&lt;/s:Envelope&gt;

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



    private String toolkitReport2 =
            """<transactionLog type='docrec' simId='$id'>
    <request>
        <header>
        Content-Type: multipart/related; boundary="MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20"; type="application/xop+xml"; start="<0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org>"; start-info="application/soap+xml"; action="urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b"
        User-Agent: TempXDRSender
        Host: edge.nist.gov:8080
        Content-Length: 125362
        </header>
        <body>


--MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20
Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
Content-Transfer-Encoding: binary
Content-ID: <0.0293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org>

<?xml version='1.0' encoding='UTF-8'?>
<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"
    xmlns:a="http://www.w3.org/2005/08/addressing">
    <soapenv:Header xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope">
        <direct:metadata-level xmlns:direct="urn:direct:addressing">XDS</direct:metadata-level>
        <direct:addressBlock xmlns:direct="urn:direct:addressing"
            soapenv:role="urn:direct:addressing:destination" soapenv:relay="true">
            <direct:from>directFrom</direct:from>
            <direct:to>directTo</direct:to>
        </direct:addressBlock>
        <wsa:To soapenv:mustUnderstand="true"
            xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"
            xmlns:wsa="http://www.w3.org/2005/08/addressing">http://transport-testing.nist.gov:12080/ttt/sim/9fdc17ba-0191-4d0c-be2a-c4ea5294b861/rec/xdrpr</wsa:To>
        <wsa:MessageID soapenv:mustUnderstand="true"
            xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"
            xmlns:wsa="http://www.w3.org/2005/08/addressing">$msgId</wsa:MessageID>
        <wsa:Action soapenv:mustUnderstand="true"
            xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"
            xmlns:wsa="http://www.w3.org/2005/08/addressing"
            >urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b</wsa:Action>
    </soapenv:Header>
    <soapenv:Body xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope">
        <xdsb:ProvideAndRegisterDocumentSetRequest xmlns:xdsb="urn:ihe:iti:xds-b:2007">
            <lcm:SubmitObjectsRequest xmlns:lcm="urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0">
                <rim:RegistryObjectList xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0">
                    <rim:ExtrinsicObject id="id_extrinsicobject" mimeType="text/xml"
                        objectType="urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1">
                        <rim:Slot name="creationTime">
                            <rim:ValueList>
                                <rim:Value>20120806</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Slot name="languageCode">
                            <rim:ValueList>
                                <rim:Value>en-us</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Slot name="serviceStartTime">
                            <rim:ValueList>
                                <rim:Value>200612230800</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Slot name="serviceStopTime">
                            <rim:ValueList>
                                <rim:Value>200612230900</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Slot name="sourcePatientId">
                            <rim:ValueList>
                                <rim:Value>1^^^&amp;2.16.840.1.113883.4.6&amp;ISO</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Slot name="sourcePatientInfo">
                            <rim:ValueList>
                                <rim:Value>PID-3|1^^^&amp;2.16.840.1.113883.4.6&amp;ISO</rim:Value>
                                <rim:Value>PID-5|Jones^Isabella^^^^</rim:Value>
                                <rim:Value>PID-7|19470501</rim:Value>
                                <rim:Value>PID-8|F</rim:Value>
                                <rim:Value>PID-11|1357 Amber Drive^^Beaverton^OR^97006^</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Name>
                            <rim:LocalizedString value="DocA"/>
                        </rim:Name>
                        <rim:Description/>
                        <rim:Classification
                            classificationScheme="urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d"
                            classifiedObject="id_extrinsicobject"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_1">
                            <rim:Slot name="authorPerson">
                                <rim:ValueList>
                                    <rim:Value>7.6^Epic - Version 7.6</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Slot name="authorInstitution">
                                <rim:ValueList>
                                    <rim:Value>Get Well Clinic</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                        </rim:Classification>
                        <rim:Classification
                            classificationScheme="urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a"
                            classifiedObject="id_extrinsicobject" nodeRepresentation="34133-9"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_3">
                            <rim:Slot name="codingScheme">
                                <rim:ValueList>
                                    <rim:Value>2.16.840.1.113883.6.1</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Name>
                                <rim:LocalizedString value="Summarization of Episode Note"/>
                            </rim:Name>
                        </rim:Classification>

                        <!-- Value from HITSP/C80 table 2-146 -->
                        <rim:Classification
                            classificationScheme="urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f"
                            classifiedObject="id_extrinsicobject" nodeRepresentation="N"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_4">
                            <rim:Slot name="codingScheme">
                                <rim:ValueList>
                                    <rim:Value>HITSP/C80</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Name>
                                <rim:LocalizedString value="Normal"/>
                            </rim:Name>
                        </rim:Classification>

                        <!-- Not using HITSP/C80 Table 2-152 because none applied for C-CDA -->
                        <rim:Classification
                            classificationScheme="urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d"
                            classifiedObject="id_extrinsicobject" nodeRepresentation="CDAR2/IHE 1.0"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_5">
                            <rim:Slot name="codingScheme">
                                <rim:ValueList>
                                    <rim:Value>Connect-a-thon formatCodes</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Name>
                                <rim:LocalizedString value="CDAR2/IHE 1.0"/>
                            </rim:Name>

                        </rim:Classification>


                        <!-- Value from HITSP/C80 table 2-146 -->
                        <rim:Classification
                            classificationScheme="urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1"
                            classifiedObject="id_extrinsicobject" nodeRepresentation="72311000"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_6">
                            <rim:Slot name="codingScheme">
                                <rim:ValueList>
                                    <rim:Value>2.16.840.1.113883.6.96</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Name>
                                <rim:LocalizedString value="Health maintenance organization"/>
                            </rim:Name>
                        </rim:Classification>

                        <!-- Value from HITSP/C80 table 2-149 -->
                        <rim:Classification
                            classificationScheme="urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead"
                            classifiedObject="id_extrinsicobject" nodeRepresentation="408443003"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_7">
                            <rim:Slot name="codingScheme">
                                <rim:ValueList>
                                    <rim:Value>2.16.840.1.113883.6.96</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Name>
                                <rim:LocalizedString value="General medical practice"/>
                            </rim:Name>
                        </rim:Classification>


                        <!-- LOINC code from HITSP/C80 table 2-144  -->
                        <rim:Classification
                            classificationScheme="urn:uuid:f0306f51-975f-434e-a61c-c59651d33983"
                            classifiedObject="id_extrinsicobject" nodeRepresentation="34133-9"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_10">
                            <rim:Slot name="codingScheme">
                                <rim:ValueList>
                                    <rim:Value>2.16.840.1.113883.6.1</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Name>
                                <rim:LocalizedString value="Summarization of episode note"/>
                            </rim:Name>
                        </rim:Classification>
                        <rim:ExternalIdentifier
                            identificationScheme="urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427"
                            value="1^^^&amp;2.16.840.1.113883.4.6&amp;ISO"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier"
                            id="id_11" registryObject="id_extrinsicobject">
                            <rim:Name>
                                <rim:LocalizedString value="XDSDocumentEntry.patientId"/>
                            </rim:Name>
                        </rim:ExternalIdentifier>
                        <rim:ExternalIdentifier
                            identificationScheme="urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab"
                            value="1.42.20140915172101.10.1"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier"
                            id="id_12" registryObject="id_extrinsicobject">
                            <rim:Name>
                                <rim:LocalizedString value="XDSDocumentEntry.uniqueId"/>
                            </rim:Name>
                        </rim:ExternalIdentifier>
                    </rim:ExtrinsicObject>
                    <rim:RegistryPackage id="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10"
                        objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage">
                        <rim:Slot name="submissionTime">
                            <rim:ValueList>
                                <rim:Value>20110117211159</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Slot name="intendedRecipient">
                            <rim:ValueList>
                                <rim:Value>Some
                                    Hospital^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45|^Wel^Marcus^^^Dr^MD|^^Internet^mwel@healthcare.example.org</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                        <rim:Name>
                            <rim:LocalizedString value="Physical"/>
                        </rim:Name>
                        <rim:Description>
                            <rim:LocalizedString value="Annual physical"/>
                        </rim:Description>
                        <rim:Classification
                            classificationScheme="urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d"
                            classifiedObject="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10"
                            nodeRepresentation=""
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_13">
                            <rim:Slot name="authorPerson">
                                <rim:ValueList>
                                    <rim:Value>7.6^Epic - Version 7.6</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Slot name="authorInstitution">
                                <rim:ValueList>
                                    <rim:Value>Get Well Clinic</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Slot name="authorTelecommunication">
                                <rim:ValueList>
                                    <rim:Value>^^Internet^john.doe@healthcare.example.org</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                        </rim:Classification>
                        <rim:Classification
                            classificationScheme="urn:uuid:aa543740-bdda-424e-8c96-df4873be8500"
                            classifiedObject="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10"
                            nodeRepresentation="34133-9"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"
                            id="id_14">
                            <rim:Slot name="codingScheme">
                                <rim:ValueList>
                                    <rim:Value>2.16.840.1.113883.6.1</rim:Value>
                                </rim:ValueList>
                            </rim:Slot>
                            <rim:Name>
                                <rim:LocalizedString value="Summarization of Episode Note"/>
                            </rim:Name>
                        </rim:Classification>
                        <rim:ExternalIdentifier
                            identificationScheme="urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8"
                            value="2.16.840.1.113883.3.72.5.1422048976492"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier"
                            id="id_15"
                            registryObject="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10">
                            <rim:Name>
                                <rim:LocalizedString value="XDSSubmissionSet.uniqueId"/>
                            </rim:Name>
                        </rim:ExternalIdentifier>
                        <rim:ExternalIdentifier id="fefcba76-ab23-4138-96ce-795f02b26d79"
                            registryObject="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10"
                            identificationScheme="urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832"
                            value="1.2.840.114350.1.13.252.1.7.2.688879">
                            <rim:Name>
                                <rim:LocalizedString value="XDSSubmissionSet.sourceId"/>
                            </rim:Name>
                        </rim:ExternalIdentifier>

                        <rim:ExternalIdentifier
                            identificationScheme="urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446"
                            value="1^^^&amp;2.16.840.1.113883.4.6&amp;ISO"
                            objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier"
                            id="id_17"
                            registryObject="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10">
                            <rim:Name>
                                <rim:LocalizedString value="XDSSubmissionSet.patientId"/>
                            </rim:Name>
                        </rim:ExternalIdentifier>
                    </rim:RegistryPackage>
                    <rim:Classification
                        classifiedObject="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10"
                        classificationNode="urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd"
                        id="urn:uuid:c6e5bf3c-3e5b-4777-bfcd-85e5c66328e1"
                        objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification"/>
                    <rim:Association
                        associationType="urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember"
                        sourceObject="urn:uuid:96bd4589-6975-43bf-81e8-9cf1701d0f10"
                        targetObject="id_extrinsicobject" id="id_association"
                        objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association">
                        <rim:Slot name="SubmissionSetStatus">
                            <rim:ValueList>
                                <rim:Value>Original</rim:Value>
                            </rim:ValueList>
                        </rim:Slot>
                    </rim:Association>
                </rim:RegistryObjectList>
            </lcm:SubmitObjectsRequest>
        </xdsb:ProvideAndRegisterDocumentSetRequest>
    </soapenv:Body>
</s:Envelope>

--MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20
Content-Type: application/octet-stream
Content-Transfer-Encoding: binary
Content-ID: <1.3293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20@apache.org>

PD94bWwg

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

