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
        def link = "https://github.com/onc-healthit/2015-edition-cures-update-uscdi-v2-testdata/blob/master/Cures%20Update%20Svap%20Uscdiv2%20Receiver%20SUT%20Test%20Data/NegativeTesting_CarePlan/NT_CP_Sample1_r21_v5.xml"

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
