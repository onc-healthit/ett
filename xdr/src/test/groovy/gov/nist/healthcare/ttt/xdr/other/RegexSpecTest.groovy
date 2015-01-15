package gov.nist.healthcare.ttt.xdr.other

import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import spock.lang.Specification

/**
 * Created by gerardin on 12/1/14.
 */
class RegexSpecTest extends Specification {


    def testReport() {

        given:
        //a hardcoded report
        def report = new XmlSlurper().parseText(report)

        when:
        //we parse it
        def tkValidationReport = new TkValidationReport()
        tkValidationReport.request = report.request.text()
        tkValidationReport.response = report.response.text()
        String content = report.response.body.text()
        def registryResponse = content.split("<.?S:Body>")
        def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
        def status = registryResponseXml.@status.text()
        then:
        //we can extract the status from the report
        assert status == "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure"


    }


    def report =
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

        &lt;?xml version='1.0' encoding='UTF-8'?&gt;&lt;
        Rest removed because of size
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
