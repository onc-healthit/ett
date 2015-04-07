package gov.nist.healthcare.ttt.xdr.integration

import gov.nist.healthcare.ttt.xdr.helpers.testFramework.TestApplication
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import groovy.util.slurpersupport.GPathResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification

import javax.annotation.PostConstruct

/**
 * Created by gerardin on 10/9/14.
 */
@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class  RealTkClientSpecTest extends Specification {

    @Value('${xdr.notification')
    private String notificationUrl

    @Value('${toolkit.createSim.url}')
    private String createSimUrl

    @Value('${server.contextPath}')
    private String contextPath

    @Value('${server.port}')
    private String port

    //TODO change that : either find a better way or rename property
    @Value('${direct.listener.domainName}')
    private String hostname

    private String fullNotificationUrl

    @Value('${toolkit.getSimConfig.url}')
    private String getSimConfigUrl

    @Value('${toolkit.request.timeout}')
    private int timeout

    private String username = "ett"

    @Autowired
    GroovyRestClient client

    @PostConstruct
    def buildUrls(){
        fullNotificationUrl = "http://"+hostname+":"+port+contextPath+notificationUrl
    }

    def "test successful endpoint creation"() {
        given:

        def id = "SimpleTest1"

        /*
        <actor type='docrec'>
            <transaction name='prb'>
                <endpoint value='http://localhost:8080/xdstools3/sim/PnrSoapTest/docrec/prb'/>
                <settings>
                  <boolean name='schemaCheck' value='true' />
                  <boolean name='modelCheck' value='false' />
                  <boolean name='codingCheck' value='false' />
                  <boolean name='soapCheck' value='true' />
                  <text name='msgCallback' value='' />
                  <webService value='prb'  />
                </settings>
            </transaction>
        </actor>
         */


        def config = {
            actor(type: 'docrec') {

                transaction(name: 'prb') {
                    endpoint(value: 'NOT_USED')
                    settings {
                        "boolean"(name: 'schemaCheck', value: 'true')
                        "boolean"(name: 'modelCheck', value: 'false')
                        "boolean"(name: 'codingCheck', value: 'false')
                        "boolean"(name: 'soapCheck', value: 'true')
                        text(msgCallBack: "$fullNotificationUrl")
                        webservices(value: 'prb')
                    }
                }
            }
        }

        def url = "$createSimUrl/$username/$id"

        when:
        //we post an endpoint creation request
        def resp = client.postXml(config, url, timeout)

        and :
        //we get the config result through a get request
        //TODO : ask if Bill now returns it in the response. This would simplify the workflow.
        def getConfigUrl = "$getSimConfigUrl/$username/$id"
        GPathResult resp2 = client.getXml(getConfigUrl, timeout)

        then:
        //We check we have created 2 endpoints containing the ids we provided.
        def transactions = resp2.depthFirst().findAll{it.name() == "endpoint"}
        assert transactions.size() == 2
        transactions.each {
            println it.@value
            assert ((String)it.@value).contains(id)
        }


    }


}
