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

    @Value('${xdr.tool.baseurl}')
    private String notificationUrl

    @Value('${toolkit.createSim.url}')
    private String tkSimCreationUrl

    /*
    Synchronous call to the toolkit. Create a simulator in Bill's terminology.
     */
    public XDRSimulatorInterface createEndpoints(EndpointConfig config) {



        def createEndpointTkMsg = buildCreateEndpointRequest(config)
        try {
            GPathResult r = restClient.postXml(createEndpointTkMsg, tkSimCreationUrl, timeout)
            def sim = buildSimulatorFromResponse(r)
            return sim
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



    private def buildCreateEndpointRequest(EndpointConfig config) {
        return {
            createSim {
                SimType("XDR Document Recipient")
                SimulatorId("${config.name}")
                MetadataValidationLevel("Full")
                CodeValidation("false")
                PostNotification("${notificationUrl}")
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
