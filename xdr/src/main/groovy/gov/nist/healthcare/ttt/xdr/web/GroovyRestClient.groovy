package gov.nist.healthcare.ttt.xdr.web

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/9/14.
 *
 * This is a groovy implementation of a rest client.
 * It returns GPathResult, the standard way groovy sees xml structures.
 * GPathResults provide a convenient way to see xml docs as trees.
 */
@Component
public class GroovyRestClient {

    Logger logger = LoggerFactory.getLogger(GroovyRestClient.class)

    /**
     *
     * @param payload : xml payload passed as groovy closure.
     * @param url : the url to send this message to.
     * @param timeout : timeout in ms
     * @return GPathResult representing the xml response
     */
    GPathResult postXml(payload, url, timeout) {

        logger.info("posting payload to url : $url with timewout : $timeout")

        def http = new HTTPBuilder(url)

        //HTTPBuilder has no direct methods to add timeouts. We have to add them to the HttpParams of the underlying HttpClient
        http.getClient().getParams().setParameter("http.connection.timeout", timeout)
        http.getClient().getParams().setParameter("http.socket.timeout", timeout)


        try {
            // if ContentType.XML is set, Accept is automatically set to XML as well
            def resp = http.request(Method.POST, ContentType.XML) {
                body = payload

                response.success = { resp, xml ->
                    logger.debug("received xml response :" + XmlUtil.serialize(xml))
                    return xml
                }
            }
            return resp
        }
        finally{
            http.shutdown()
        }
    }


    GPathResult getXml(url, timeout) {

        logger.debug("get on url : $url with timewout : $timeout")

        def http = new HTTPBuilder(url)

        //HTTPBuilder has no direct methods to add timeouts. We have to add them to the HttpParams of the underlying HttpClient
        http.getClient().getParams().setParameter("http.connection.timeout", timeout)
        http.getClient().getParams().setParameter("http.socket.timeout", timeout)


        try {
            // if ContentType.XML is set, Accept is automatically set to XML as well
            def resp = http.request(Method.GET, ContentType.XML) {

                response.success = { resp, xml ->
                    logger.debug("received xml response :" + XmlUtil.serialize(xml))
                    return xml
                }

            }
            return resp
        } catch(Exception e) {
			logger.error("Could not connect to toolkit. XDR won't work!")
			return null
        }

        finally{
            http.shutdown()
        }
    }
}
