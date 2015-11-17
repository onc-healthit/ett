package gov.nist.healthcare.ttt.webapp

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import sun.security.acl.PrincipalImpl

/**
 *
 * Templates for rest call and associated data
 *
 * Created by gerardin on 2/5/15.
 */
class TestUtils {

    /*
    GUI Mock calls
     */

    static MockHttpServletRequestBuilder run(String tcId, String userId, String testCaseConfig) {
        MockMvcRequestBuilders.post("/api/xdr/tc/$tcId/run")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    static MockHttpServletRequestBuilder status(tcId, userId) {
        MockMvcRequestBuilders.get("/api/xdr/tc/$tcId/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    static MockHttpServletRequestBuilder configure(String tcId, String userId, String testCaseConfig) {
        MockMvcRequestBuilders.get("/api/xdr/tc/$tcId/configure")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl(userId))
    }

    /*
    Toolkit mock calls
     */
    static MockHttpServletRequestBuilder reportNotification(String toolkitReport) {
        MockMvcRequestBuilders.post("/api/xdrNotification")
                .accept(MediaType.ALL)
                .content(toolkitReport)
                .contentType(MediaType.APPLICATION_XML)
    }

    /*
    Template for toolkit data
     */
    static def buildReportTemplate(String simId,String messageID, String fromAddress, String toAddress) {
        """
    <transactionLog type='docrec' simId='$simId'>
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
    }

    static String simEndpoint(String simId, String system) {
        "http://hit-dev.nist.gov:11080/xdstools3/sim/$system/$simId/docrec/prb"
    }
}
