package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.xdr.domain.TkSendReport
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import groovy.util.slurpersupport.GPathResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/21/14.
 */
@Component
class XdrSenderImpl implements XdrSender{

    Logger log = LoggerFactory.getLogger(XdrSender.class)

    @Autowired
    GroovyRestClient restClient

    @Value('${toolkit.request.timeout}')
    Integer timeout = 1000

    @Value('${toolkit.sendXdr.url}')
    private String tkSendXdrUrl

    @Value('${toolkit.testName}')
    private String testName


    /*
    <TestClientRequest>

<!-- Identifies a test pre-installed in toolkit - required-->

<TestName>11696</TestName>

<!-- Either TargetEndpoint or Site must be specified, not both -->

<TargetEndpoint>https://example.com/xdr</TargetEndpoint>

<Site>siteName</Site>

<DirectAddressBlock>

<!-- To be stuffed into the correct place - optional -->

</DirectAddressBlock>

<!-- Message ID to be inserted into SOAP Header - optional -->

<MessageId>xxxx</MessageId>

</TestClientRequest>


     */
    @Override
    TkSendReport sendXdr(Map config) {

        log.debug("try to send xdr with config : $config")

        def sendXdrMessage = sendXdrMessage(config)
        try {
            GPathResult r = restClient.postXml(sendXdrMessage, tkSendXdrUrl, timeout)
            def report = parseReport(r)
            return report
        }
        catch (groovyx.net.http.HttpResponseException e) {
            return new RuntimeException("could not reach the toolkit.",e)
        }
        catch (java.net.SocketTimeoutException e) {
            return new RuntimeException("connection timeout when calling toolkit.",e)
        }
        catch(groovyx.net.http.ResponseParseException e){
            return new RuntimeException("could not understand response from toolkit.",e)
        }
    }

    def parseReport(GPathResult response) {
        TkSendReport report = new TkSendReport()
        report.test = response.Test.text()
        report.status = response.Status.text()
        report.result = response.Result.text()
        report.inHeader = response.InHeader.text()
        return report
    }

    private def sendXdrMessage(Object config) {
        return {
            TestClientRequest {
                TestName(testName)
                TargetEndpoint(config.targetEndpoint)
                DirectAddressBlock(config.addressBlock)
                MessageId(config.messageId)
            }
        }
    }
}
