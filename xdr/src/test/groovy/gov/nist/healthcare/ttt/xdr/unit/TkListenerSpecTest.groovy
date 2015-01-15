package gov.nist.healthcare.ttt.xdr.unit
import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import gov.nist.healthcare.ttt.xdr.helpers.testFramework.TestApplication
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
/**
 * Created by gerardin on 10/14/14.
 *
 * Here we only test the notification process.
 * If we receive sth on the notification url, we should notify the observer either of a well-formed or problematic message.
 */
@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class TkListenerSpecTest extends Specification {

    @Value('${xdr.notification}')
    private String notificationUrl

    @Autowired
    XdrReceiver receiver

    @Autowired
    TkListener listener

    def "notify of a valid report"() {

        given: 'a valid validation report notification'
        def mockMvc = MockMvcBuilders.standaloneSetup(listener).build()
        def observer = Mock(IObserver)
        receiver.registerObserver(observer)

        println "we want to send to notification url : $notificationUrl"

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(notificationUrl)
                .accept(MediaType.ALL)
                .content(GOOD_REPORT_XML)
                .contentType(MediaType.APPLICATION_XML)

        when: 'a notification by a web client is received'
        mockMvc
                .perform(req)
                .andDo(print())
                .andReturn()

        then: 'the observer is notified with a success message'
        1 * observer.getNotification({
            Message m ->
                m.status.toString() == "SUCCESS"
                m.content instanceof TkValidationReport
        })

    }



    def "notify of a bad report"() {

        given: 'a bad validation report notification (cannot be parsed to TkValidationReport)'
        def mockMvc = MockMvcBuilders.standaloneSetup(listener).build()
        def observer = Mock(IObserver)
        receiver.registerObserver(observer)

        println "URL to send to is : " + notificationUrl

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(notificationUrl)
                .accept(MediaType.ALL)
                .content(BAD_REPORT_XML)
                .contentType(MediaType.APPLICATION_XML)

        when: 'a notification by a web client is received'
        mockMvc
                .perform(req)
                .andDo(print())
                .andReturn()

        then: 'the observer is notified with a success message'
        1 * observer.getNotification({
            Message m ->
                m.status.toString() == "ERROR"
                m.content == null
        })

    }

    private static String BAD_REPORT_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<bad_report>success</bad_report>";

    private static String GOOD_REPORT_XML =
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
