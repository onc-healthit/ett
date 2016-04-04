package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.commons.notification.IObservable
import gov.nist.healthcare.ttt.commons.notification.IObserver
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl;
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.misc.Configuration;
import gov.nist.healthcare.ttt.xdr.domain.CreateEndpointResponseParser
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.web.GroovyRestClient
import gov.nist.toolkit.configDatatypes.SimulatorActorType
import gov.nist.toolkit.configDatatypes.SimulatorProperties;
import gov.nist.toolkit.toolkitApi.BasicSimParameters
import gov.nist.toolkit.toolkitApi.DocumentRecipient
import gov.nist.toolkit.toolkitApi.SimulatorBuilder
import gov.nist.toolkit.toolkitServicesCommon.SimConfig
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
	
	@Value('${toolkit.url}')
	private String toolkitUrl
	
	@Value('${toolkit.user}')
	private String toolkitUser

    @Value('${xdr.notification.prefix}')
    private String prefix

//    @Value('${toolkit.createSim.url}')
//    private String tkSimCreationUrl

//    @Value('${toolkit.getSimConfig.url}')
//    private String tkSimInfo

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
//        tkSimCreationUrl = tkSimCreationUrl.replaceAll('/$', "")
        notificationUrl = notificationUrl.replaceAll('/$', "")
        fullNotificationUrl = prefix+"://"+hostname+":"+port+contextPath+notificationUrl

        log.debug("notification url is :" + fullNotificationUrl)
    }

    public XDRSimulatorInterface createEndpoints(EndpointConfig config) {

        def createEndpointTkMsg =buildCreateEndpointRequest(config)
        try {
            //For some reason, there response is empty and we need to do a get to retrieve the config!
//            restClient.postXml(createEndpointTkMsg, tkSimCreationUrl+"/"+config.name, timeout)
//            GPathResult response = restClient.getXml(tkSimInfo + "/" + config.name, timeout)
//            def sim = CreateEndpointResponseParser.parse(response, config.name)
			
			SimConfig conf = createDocRecipient(config);
			XDRSimulatorInterface sim = new XDRSimulatorImpl();

			sim.endpoint = getPropertyFromSim(conf, "PnR_endpoint");
			sim.endpointTLS = getPropertyFromSim(conf, "PnR_TLS_endpoint");
			sim.simulatorId = getPropertyFromSim(conf, "Name");
			
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

    @Override
    public def notifyObserver(Message m) {
        observer.getNotification(m)
    }

    @Override
    public def registerObserver(IObserver o) {
        observer = o
    }


    private def buildCreateEndpointRequest(EndpointConfig config) {

        def notificationUrl = ""

        if(config.type == "docrec"){
            notificationUrl = fullNotificationUrl
        }

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
                        text(name : 'msgCallback', value: notificationUrl)
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
                        text(name : 'msgCallback', value: notificationUrl)
                    }
                    webService(value :'prb_TLS')
                }
            }
        }
    }
	
	
	public def createDocRecipient(EndpointConfig config) {
		SimulatorBuilder spi = new SimulatorBuilder(this.toolkitUrl);
		BasicSimParameters recParams = new BasicSimParameters();

		recParams.setId(config.name);
		recParams.setUser(this.toolkitUser);
		recParams.setActorType(SimulatorActorType.DOCUMENT_RECIPIENT);
		recParams.setEnvironmentName("XDR");

//		System.out.println("STEP - DELETE DOCREC SIM");
		spi.delete(recParams.getId(), recParams.getUser());


//		System.out.println("STEP - CREATE DOCREC SIM");
		DocumentRecipient documentRecipient = spi.createDocumentRecipient(
				recParams.getId(),
				recParams.getUser(),
				recParams.getEnvironmentName()
				);

//		System.out.println(documentRecipient.getFullId());

//		System.out.println("This is un-verifiable since notifications are handled through the servlet filter chain which is not configured here");
//		System.out.println("STEP - UPDATE - REGISTER NOTIFICATION");
		documentRecipient.setProperty(SimulatorProperties.TRANSACTION_NOTIFICATION_URI, fullNotificationUrl);
		documentRecipient.setProperty(SimulatorProperties.TRANSACTION_NOTIFICATION_CLASS, "gov.nist.healthcare.ttt.xdr.api.XDRServlet");
		SimConfig withRegistration = documentRecipient.update(documentRecipient.getConfig());
//		System.out.println("Updated Src Sim config is" + withRegistration.describe());
		log.info("TLS Endpoint created: " + getPropertyFromSim(withRegistration, "PnR_TLS_endpoint"))
		log.info("Non TLS Endpoint created: " + getPropertyFromSim(withRegistration, "PnR_endpoint"))

		return withRegistration;
	}
	
	public String getPropertyFromSim(SimConfig sim, String key) {
		for(Object prop in sim.props) {
			String stringified = new String(prop);
			if(stringified.contains("=")) {
				String[] splitted = prop.split("=", 2);
				if(splitted[0].equals(key)) {
					return splitted[1];
				}
			}
		}
		return "";
	}


}
