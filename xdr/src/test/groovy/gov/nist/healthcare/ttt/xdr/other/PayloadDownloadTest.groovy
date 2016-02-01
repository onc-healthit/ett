package gov.nist.healthcare.ttt.xdr.other

import org.apache.commons.io.IOUtils
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
class PayloadDownloadTest extends Specification {


    def test() {

        given:
        def link = "https://raw.githubusercontent.com/siteadmin/2015-Certification-C-CDA-Test-Data/master/Receiver%20SUT%20Test%20Data/NegativeTesting_CarePlan/NT_Missing_PatientSuffix_r21_v1.xml"

        when:
            StringWriter writer = new StringWriter();
            InputStream ccdaAttachment = new URL(link).openStream();
            IOUtils.copy(ccdaAttachment, writer, "UTF-8");
            String payload = writer.toString();
        then:
            println payload
        assert payload != null
    }
}
