package gov.nist.healthcare.ttt.xdr.other

import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Specification

import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
/**
 * Created by gerardin on 12/1/14.
 */
class BillsRequestParsingTest extends Specification {


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


    def testRequestParsingWithEscapedXml() {

        given:

        def file = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_sample_request_escaped.txt")

        when:

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), file)
        Multipart content = msg.getContent()
        MimeBodyPart part1 = content.getBodyPart(0)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        part1.writeTo(out)
        println out.toString()

        String xml = org.apache.commons.io.IOUtils.toString(part1.getInputStream(), "UTF-8");

        String processed = StringEscapeUtils.unescapeXml(xml)

        println processed

        def envelope = new XmlSlurper().parseText(processed)



        def directFrom = envelope.Header.addressBlock.from.text()

        println directFrom

        then:
        assert directFrom == "directFrom"


    }
}
