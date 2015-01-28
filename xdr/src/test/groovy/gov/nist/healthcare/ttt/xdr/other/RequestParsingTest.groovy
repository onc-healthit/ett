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

        def stream = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_sample_request_escaped.txt")
        String string = org.apache.commons.io.IOUtils.toString(stream, "UTF-8");
        String unescapedString = StringEscapeUtils.unescapeXml(string)
        InputStream unescapedStream = new ByteArrayInputStream(unescapedString.getBytes())

        when:

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), unescapedStream)
        Multipart content = msg.getContent()
        MimeBodyPart part1 = content.getBodyPart(0)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        part1.writeTo(out)
        println out.toString()

        String xml = org.apache.commons.io.IOUtils.toString(part1.getInputStream(), "UTF-8");
        println xml

        def envelope = new XmlSlurper().parseText(xml)
        def directFrom = envelope.Header.addressBlock.from.text()

        then:
        println directFrom
        assert directFrom == "directFrom"


    }


    def testRequestParsingWithEscapedXmlAndHttpHeadersFrom2Files() {

        given:
        def file1 = this.getClass().getClassLoader().getResourceAsStream("new/header.txt")
        def file2 = this.getClass().getClassLoader().getResourceAsStream("new/body.txt")

        when:
        String string1 = org.apache.commons.io.IOUtils.toString(file1, "UTF-8");
        String unescapedString1 = StringEscapeUtils.unescapeXml(string1)
        InputStream unescapedStream1 = new ByteArrayInputStream(unescapedString1.getBytes())

        String string2 = org.apache.commons.io.IOUtils.toString(file2, "UTF-8");
        String unescapedString2 = StringEscapeUtils.unescapeXml(string2)
        InputStream unescapedStream2 = new ByteArrayInputStream(unescapedString2.getBytes())

        def file3 = new java.io.SequenceInputStream(unescapedStream1, unescapedStream2)

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), file3)
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



    def testRequestParsingWithEscapedXmlAndHttpHeadersFrom2FilesWithSerialization() {

        given:

        def file1 = this.getClass().getClassLoader().getResourceAsStream("new/header.txt")
        def file2 = this.getClass().getClassLoader().getResourceAsStream("new/body.txt")
        when:

        String header = org.apache.commons.io.IOUtils.toString(file1, "UTF-8");
        String body = org.apache.commons.io.IOUtils.toString(file2, "UTF-8");
        String unescapedString1 = StringEscapeUtils.unescapeXml(header)
        String unescapedString2 = StringEscapeUtils.unescapeXml(body)

        def request = unescapedString1 + unescapedString2

        println request

        InputStream req = new ByteArrayInputStream(request.getBytes());

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





}
