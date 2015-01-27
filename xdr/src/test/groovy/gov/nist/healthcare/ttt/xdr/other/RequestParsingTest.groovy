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
class RequestParsingTest extends Specification {


    def testRequestParsingWithEscapedXmlAndHttpHeaders() {

        given:

        def file = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_sample_request_escaped_FOR_REAL.txt")

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


    def testRequestParsingWithEscapedXmlAndHttpHeadersFrom2Files() {

        given:

        def file1 = this.getClass().getClassLoader().getResourceAsStream("new/header.txt")
        def file2 = this.getClass().getClassLoader().getResourceAsStream("new/body.txt")
        when:

        def file3 = new java.io.SequenceInputStream(file1, file2)

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), file3)
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



    def testRequestParsingWithEscapedXmlAndHttpHeadersFrom2FilesWithSerialization() {

        given:

        def file1 = this.getClass().getClassLoader().getResourceAsStream("new/header.txt")
        def file2 = this.getClass().getClassLoader().getResourceAsStream("new/body.txt")
        when:

        String header = org.apache.commons.io.IOUtils.toString(file1, "UTF-8");
        String body = org.apache.commons.io.IOUtils.toString(file2, "UTF-8");

        def request = header + body

        InputStream req = new ByteArrayInputStream(request.getBytes());

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), req)
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
