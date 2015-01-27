package gov.nist.healthcare.ttt.xdr.other

import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Specification
/**
 * Created by gerardin on 12/1/14.
 */
class ReportParsingTestMore extends Specification {


    def testRequestParsingWithEscapedXml() {

        given:

        def stream = this.getClass().getClassLoader().getResourceAsStream("sample_report_escaped.txt")
        def report = new XmlSlurper().parse(stream)

        when:

        def header = report.request.header
        def body = report.request.body

//        String raw = header.text() + "\n" +body.text()
//
//        println raw
//
//        String processed = StringEscapeUtils.unescapeXml(raw)
//
//        println processed
//
//        InputStream xml = new ByteArrayInputStream(raw.getBytes());
//
//        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), xml)
//        String content = msg.getContent()
//        MimeBodyPart part1 = content.getBodyPart(0)
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream()
//        part1.writeTo(out)
//        println out.toString()
//
//        String xml2 = org.apache.commons.io.IOUtils.toString(part1.getInputStream(), "UTF-8");
//
//        String processed2 = StringEscapeUtils.unescapeXml(xml2)
//
//        println processed
//
//        def envelope = new XmlSlurper().parseText(processed2)
//
//
//
//        def directFrom = envelope.Header.addressBlock.from.text()


        def text = body.text()
        String processed = StringEscapeUtils.unescapeXml(text)

        then:
        assert directFrom == "directFrom"


    }
}
