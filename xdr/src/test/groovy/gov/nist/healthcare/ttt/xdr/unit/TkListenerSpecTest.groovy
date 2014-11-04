package gov.nist.healthcare.ttt.xdr.unit

import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.notification.IObserver
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import gov.nist.healthcare.ttt.xdr.helpers.testFramework.TestApplication
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

/**
 * Created by gerardin on 10/14/14.
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
        def mockMvc = MockMvcBuilders.standaloneSetup(listener)
                .setMessageConverters(new Jaxb2RootElementHttpMessageConverter())
                .build()
        def observer = Mock(IObserver)
        receiver.registerObserver(observer)

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post("/$notificationUrl")
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

    private static String GOOD_REPORT_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<report>success</report>";


    def "notify of a bad report"() {

        given: 'a bad validation report notification (cannot be parsed to TkValidationReport)'
        def mockMvc = MockMvcBuilders.standaloneSetup(listener)
                .setMessageConverters(new Jaxb2RootElementHttpMessageConverter())
                .build()
        def observer = Mock(IObserver)
        receiver.registerObserver(observer)

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post("/$notificationUrl")
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
}
