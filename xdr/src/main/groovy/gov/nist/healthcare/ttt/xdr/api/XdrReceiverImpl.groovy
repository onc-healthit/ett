package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.xdr.api.notification.IObservable
import gov.nist.healthcare.ttt.xdr.api.notification.IObserver
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.web.GroovyTkClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/6/14.
 */

@Component
public class XdrReceiverImpl implements XdrReceiver, IObservable {

    IObserver observer

    @Autowired
    GroovyTkClient restClient

    @Value('${xdr.tool.baseurl}')
    private String notificationUrl

    @Value('${toolkit.createSim.url}')
    private String tkSimCreationUrl

    public Message<Object> createEndpoints(EndpointConfig config){

        //TODO what if not / or if exist already ?
        if(config.name == null){
            throw new RuntimeException("invalid null endpoint")
        }

        def createEndpointTkMsg = {
            createSim {
                SimType("XDR Document Recipient")
                SimulatorId("${config.name}")
                MetadataValidationLevel("Full")
                CodeValidation("false")
                PostNotification("${notificationUrl}")
            }
        }

        def resp = restClient.createEndpoint(createEndpointTkMsg, tkSimCreationUrl)
        return resp
    }

    @Override
    def notifyObserver(Message m) {
        observer.getNotification(m)
    }

    @Override
    def registerObserver(IObserver o){
        observer = o
    }
}
