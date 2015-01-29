package gov.nist.healthcare.ttt.xdr.other

import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Specification

import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import java.util.regex.Matcher
/**
 * Created by gerardin on 12/1/14.
 */
class ReportParsingTest extends Specification {


    def testReportResponseParsing() {

        given:

        def file = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_report.xml")
        XmlSlurper s = new XmlSlurper()
        s.setKeepIgnorableWhitespace(true)
        def report = s.parse(file)

        when:

        when:
        //we parse it
        def request = report.request.text()
        def response = report.response.text()
        String content = report.response.body.text()
        def registryResponse = content.split("<.?S:Body>")
        def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
        def status = registryResponseXml.@status.text()
        then:
        //we can extract the status from the report
        assert status == "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure"
    }

    //TODO fix
    /*
    xmlSlurper handles the text in some fashion that changes the text (mess w spaces/end of lines)
     */
    def testReportRequestParsing() {

        given:

        def file = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_report.xml")
        XmlSlurper s = new XmlSlurper()
        s.setKeepIgnorableWhitespace(true)
        def report = s.parse(file)

        assert s.isKeepIgnorableWhitespace()

        when:
        //we parse it
        String header = report.request.header.text()
        String body = report.request.body.text()

        String unescapedHeader = StringEscapeUtils.unescapeXml(header)
        String unescapedBody = StringEscapeUtils.unescapeXml(body)

        def fullRequest = unescapedHeader + unescapedBody

        println fullRequest

        InputStream req = new ByteArrayInputStream(fullRequest.getBytes());

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), req)
        Multipart content = msg.getContent()
        MimeBodyPart part1 = content.getBodyPart(0)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        part1.writeTo(out)
        println out.toString()

        String xml = org.apache.commons.io.IOUtils.toString(part1.getInputStream(), "UTF-8");

        def envelope = new XmlSlurper().parseText(xml)
        def directFrom = envelope.Header.addressBlock.from.text()

        println directFrom

        then:

        assert directFrom == "directFrom"


    }



    def testReportRequestScrubbing() {

        given:

        def file = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_report.xml")
        XmlSlurper s = new XmlSlurper()
        s.setKeepIgnorableWhitespace(true)
        def report = s.parse(file)

        assert s.isKeepIgnorableWhitespace()

        when:
        //we parse it
        String header = report.request.header.text()
        String body = report.request.body.text()


        Matcher matcher2 = body =~ /from>([^<]+)</

        matcher2.each{
            println it
        }

        println matcher2[0][1]

        return


      //  Matcher matcher = body =~ /MessageID[^>]+>([^<]+)</
        Matcher matcher = body =~ /(?:MessageID[^>]+>)([^<]+)(?:<)/

        //we expect only one match (thus the 0) and we want to get back the first group match

        println matcher[0][1]


        assert matcher.size() == 1

        def updated = body.replaceAll(/(MessageID[^>]+>)([^<]+)(<)/){
            String it , pre, id , post ->
                    return pre + "1" + post
        }


        then:
        println updated
    }
}
