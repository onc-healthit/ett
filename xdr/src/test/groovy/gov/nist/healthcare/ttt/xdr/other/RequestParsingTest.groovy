package gov.nist.healthcare.ttt.xdr.other

import spock.lang.Specification

import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
/**
 * Created by gerardin on 12/1/14.
 */
class RequestParsingTest extends Specification {


    def testRequestParsing() {

        given:

        def file = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_sample_request.txt")

        when:

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), file)
        Multipart content = msg.getContent()
        MimeBodyPart part1 = content.getBodyPart(0)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        part1.writeTo(out)
        println out.toString()

        def envelope = new XmlSlurper().parse(part1.getInputStream())

        def directFrom = envelope.Header.addressBlock.from.text()

        println directFrom

        then:
        assert directFrom == "directFrom"


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
