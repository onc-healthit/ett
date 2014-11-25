package gov.nist.healthcare.ttt.xdr.unit

import groovyx.net.http.HTTPBuilder
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import spock.lang.Specification

import java.security.KeyStore

import static groovyx.net.http.Method.HEAD

class SSLClientSpecTest extends Specification {


    def "SSL Test"() {
        given:

        def http = new HTTPBuilder('https://localhost:8443/ttt')

        def keyStore = KeyStore.getInstance(KeyStore.defaultType)

        getClass().getResource( "/keystore/keystore" ).withInputStream {
            keyStore.load( it, "changeit".toCharArray() )
        }

        when:

        http.client.connectionManager.schemeRegistry.register(
                new Scheme("https", new SSLSocketFactory(keyStore), 8443))

        def status = http.request(HEAD) {
            response.success = { it.status }
        }




        then:
        println status

    }


}
