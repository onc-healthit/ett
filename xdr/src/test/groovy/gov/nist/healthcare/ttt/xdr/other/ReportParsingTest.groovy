package gov.nist.healthcare.ttt.xdr.other

import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import spock.lang.Specification

/**
 * Created by gerardin on 12/1/14.
 */
class ReportParsingTest extends Specification {


    def testReport() {

        given:

        def file = this.getClass().getClassLoader().getResourceAsStream("xdr_full_metadata_report.xml")
        def report = new XmlSlurper().parse(file)

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
}
