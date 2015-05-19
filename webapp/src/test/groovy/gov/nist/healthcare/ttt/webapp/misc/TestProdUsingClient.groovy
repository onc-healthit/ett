package gov.nist.healthcare.ttt.webapp.misc

import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.xdr.api.CannedXdrSenderImpl
import gov.nist.healthcare.ttt.xdr.api.TLSClientImpl
import gov.nist.healthcare.ttt.xdr.ssl.SSLContextManager
import org.junit.Before
import org.junit.Test
import spock.lang.Specification

/**
 * Created by gerardin on 2/11/15.
 */
class TestProdUsingClient extends Specification{


@Before
def setup(){
    }


    @Test
    def test1(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://edge.nist.gov:11080/xdstools3/sim/1/docrec/prb"
        context.targetEndpoint = "http://edge.nist.gov:11080/xdstools3/sim/1/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl().sendXdr(context)

        then :

        println response
        assert true
    }

    @Test
    def test2(){

        when :
        def manager = new SSLContextManager()
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "hit-dev.nist.gov:11080/xdstools3/sim/2/docrec/prb"
        context.targetEndpoint = "http://hit-dev.nist.gov:11080/xdstools3/sim/2/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_FULL_METADATA
        def response = new CannedXdrSenderImpl().sendXdr(context)

        then :

        assert true
    }


    @Test
    def test7(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        TLSclient.connectOverGoodTLS([ip_address: "hit-dev.nist.gov", port: "12084"])

        then :

        assert true
    }


    @Test
    def test19(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://edge.nist.gov:11080/xdstools3/sim/19/docrec/prb"
        context.targetEndpoint = "http://edge.nist.gov:11080/xdstools3/sim/19/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        println "first message..."
        def response = new CannedXdrSenderImpl().sendXdr(context)
        println "second message..."
        def response2 = new CannedXdrSenderImpl().sendXdr(context)
        println "third message..."
        def response3 = new CannedXdrSenderImpl().sendXdr(context)

        then :

        println response3
        assert true
    }

    @Test
    def test20a(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://edge.nist.gov:11080/xdstools3/sim/20a/docrec/prb"
        context.targetEndpoint = "http://edge.nist.gov:11080/xdstools3/sim/20a/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl().sendXdr(context)

        then :

        println response
        assert true
    }


    @Test
    def test20b(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://edge.nist.gov:11080/xdstools3/sim/20a/docrec/prb"
        context.targetEndpoint = "http://edge.nist.gov:11080/xdstools3/sim/20b/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl().sendXdr(context)

        then :

        println response
        assert true
    }


}
