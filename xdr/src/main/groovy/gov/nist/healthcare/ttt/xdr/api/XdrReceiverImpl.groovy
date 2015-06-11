package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.commons.notification.IObservable
import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Artifacts
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.Settings
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import groovy.util.slurpersupport.GPathResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

/**
 * Created by gerardin on 10/6/14.
 */

@Component
public class XdrReceiverImpl implements XdrReceiver, IObservable {

    Logger log = LoggerFactory.getLogger(XdrReceiverImpl.class)

    @Value('${toolkit.request.timeout}')
    Integer timeout = 1000

    IObserver observer

    @Autowired
    GroovyRestClient restClient

    @Value('${xdr.notification}')
    private String notificationUrl

    @Value('${xdr.notification.prefix}')
    private String prefix

    @Value('${toolkit.createSim.url}')
    private String tkSimCreationUrl

    @Value('${toolkit.getSimConfig.url}')
    private String tkSimInfo

    @Value('${toolkit.sendXdr.url}')
    private String xdrSendUrl

    @Value('${server.contextPath}')
    private String contextPath

    @Value('${server.port}')
    private String port

    //TODO change that : either find a better way or rename property
    @Value('${direct.listener.domainName}')
    private String hostname

    private String fullNotificationUrl

    @PostConstruct
    def buildUrls(){
        tkSimCreationUrl = tkSimCreationUrl.replaceAll('/$', "")
        notificationUrl = notificationUrl.replaceAll('/$', "")
        fullNotificationUrl = prefix+"://"+hostname+":"+port+contextPath+notificationUrl

        log.debug("notification url is :" + fullNotificationUrl)
    }


    public XDRSimulatorInterface createEndpoints(EndpointConfig config) {

        def createEndpointTkMsg =buildCreateEndpointRequest(config)
        try {
            GPathResult r = restClient.postXml(createEndpointTkMsg, tkSimCreationUrl+"/"+config.name, timeout)

            //TODO check if success first
            GPathResult r2 = restClient.getXml(tkSimInfo + "/" + config.name, timeout)
            def sim = buildSimulatorFromResponse(r2, config.name)
            return sim
        }
        catch (groovyx.net.http.HttpResponseException e) {
            throw new RuntimeException("could not reach the toolkit or toolkit returned an error. Check response status code",e)
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
            actor(type:config.type) {
                environment(name:"NA2015")
                transaction(name: 'prb'){
                    endpoint(value : config.endpoint)
                    settings {
                        "boolean"(name:'schemaCheck' , value:'false')
                        "boolean"(name:'modelCheck' , value:'false')
                        "boolean"(name:'codingCheck' , value:'false')
                        "boolean"(name:'soapCheck' , value:'true')
                        text(name : 'msgCallback', value: fullNotificationUrl)
                    }
                    webService(value :'prb')
                }
                transaction(name: 'prb'){
                    endpoint(value : config.endpointTLS)
                    settings {
                        "boolean"(name:'schemaCheck' , value:'false')
                        "boolean"(name:'modelCheck' , value:'false')
                        "boolean"(name:'codingCheck' , value:'false')
                        "boolean"(name:'soapCheck' , value:'true')
                        text(name : 'msgCallback', value: fullNotificationUrl)
                    }
                    webService(value :'prb_TLS')
                }
            }
        }
    }



    public def sendXdr(Map config) {

        Settings settings = new Settings()
        settings.setDirectFrom(config.directFrom)
        settings.setDirectTo(config.directTo)
        settings.setWsaTo(config.targetEndpoint)

        Artifacts art = ArtifactManagement.generateArtifacts(ArtifactManagement.Type.XDR_FULL_METADATA, settings);

        def req = """
            <sendRequest>
                <simReference>ett/$config.simId</simReference>
                <transactionName>prb</transactionName>
                <tls value="$config.tls"/>
                <messageId>$art.messageId</messageId>
                <metadata>$art.metadata</metadata>
                <extraHeaders>$art.extraHeaders</extraHeaders>
                <document id="$art.documentId" mimeType="$art.mimeType">$art.document</document>
            </sendRequest>
        """

        try {
            log.debug("xdr send request :" + req.toString())
            GPathResult r = restClient.postXml(req, xdrSendUrl +"/$config.simId", timeout)
            parseSendXdrResponse(r)

        }
        catch (groovyx.net.http.HttpResponseException e) {
            throw new RuntimeException("could not reach the toolkit or toolkit returned an error. Check response status code",e)
        }
        catch (java.net.SocketTimeoutException e) {
            throw new RuntimeException("connection timeout when calling toolkit.",e)
        }
        catch(groovyx.net.http.ResponseParseException e){
            throw new RuntimeException("could not understand response from toolkit.",e)
        }
    }

    //TODO improve that, make it its own parser
    private XDRSimulatorInterface buildSimulatorFromResponse(def r, String simId) {
        def transactions = r.depthFirst().findAll{it.name() == "endpoint"}
        XDRSimulatorInterface sim = new XDRSimulatorImpl()
        sim.simulatorId = simId
        sim.endpoint = transactions[0].@value.text()

        //TODO refactor : this exists only if we retrieve a docrec. Test beforehand.
        if(transactions[1]!= null) {
            sim.endpointTLS = transactions[1].@value.text()
        }
        return sim
    }

    private def parseSendXdrResponse(GPathResult r){
        //we need to parse the response maybe
        return r
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
