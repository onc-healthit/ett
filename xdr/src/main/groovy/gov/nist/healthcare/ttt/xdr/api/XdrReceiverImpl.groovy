package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.xdr.api.notification.IObservable
import gov.nist.healthcare.ttt.xdr.api.notification.IObserver
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import groovy.util.slurpersupport.GPathResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

/**
 * Created by gerardin on 10/6/14.
 */

@Component
public class XdrReceiverImpl implements XdrReceiver, IObservable {


    @Value('${toolkit.request.timeout}')
    Integer timeout = 1000

    IObserver observer

    @Autowired
    GroovyRestClient restClient

    @Value('${xdr.notification}')
    private String notificationUrl

    @Value('${toolkit.createSim.url}')
    private String tkSimCreationUrl

    @PostConstruct
    def cleanUrls(){
        tkSimCreationUrl = tkSimCreationUrl.replaceAll('/$', "")
        notificationUrl = notificationUrl.replaceAll('/$', "")
    }

    /*
    Synchronous call to the toolkit. Create a simulator in Bill's terminology.
     */
    public XDRSimulatorInterface createEndpoints(EndpointConfig config) {

        def createEndpointTkMsg = buildCreateEndpointRequest(config)
        try {
            GPathResult r = restClient.postXml(createEndpointTkMsg, tkSimCreationUrl+"/"+config.name, timeout)
            def sim = buildSimulatorFromResponse(r)
            return sim
        }
        catch (groovyx.net.http.HttpResponseException e) {
            throw new RuntimeException("could not reach the toolkit.",e)
        }
        catch (java.net.SocketTimeoutException e) {
            throw new RuntimeException("connection timeout when calling toolkit.",e)
        }
        catch(groovyx.net.http.ResponseParseException e){
            throw new RuntimeException("could not understand response from toolkit.",e)
        }
    }



    private def buildCreateEndpointRequest(EndpointConfig config) {
        return {
            actor(type:'docrec') {
                transaction(name: 'prb'){
                    endpoint(value : 'NOT_USED')
                    settings {
                        "boolean"(name:'schemaCheck' , value:'true')
                        "boolean"(name:'modelCheck' , value:'false')
                        "boolean"(name:'codingCheck' , value:'false')
                        "boolean"(name:'soapCheck' , value:'true')
                        text(msgCallBack : "http://localhost:8080/ttt/$notificationUrl")
                        webservices( value :'prb')
                    }
                }
            }
        }
    }

    private XDRSimulatorInterface buildSimulatorFromResponse(def r) {
        XDRSimulatorInterface sim = new XDRSimulatorImpl()
        sim.simulatorId = r.simId.text()
        sim.endpoint = r.endpoint.text()
        sim.endpointTLS = r.endpointTLS.text()
        return sim
    }


    @Override
    def notifyObserver(Message m) {
        observer.getNotification(m)
    }

    @Override
    def registerObserver(IObserver o) {
        observer = o
    }
}
