package gov.nist.healthcare.ttt.webapp.production

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
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/1/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/1/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

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
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/2/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_FULL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        assert true
    }


    @Test
    def test6(){

        when :
        def manager = new SSLContextManager()
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/6/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/6/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        assert true
    }

	
	@Test
	def test7(){

		when :
		def manager = new SSLContextManager()
		def TLSclient = new TLSClientImpl(manager)
		TLSclient.connectOverBadTLS([ip_address: "hit-dev.nist.gov", port: "12084"])

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
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/19/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/19/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        println "first message..."
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)
        println "second message..."
        def response2 = new CannedXdrSenderImpl(manager).sendXdr(context)
        println "third message..."
        def response3 = new CannedXdrSenderImpl(manager).sendXdr(context)

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
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20a/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20a/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

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
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20b/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20b/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }

    @Test
    def test48(){
        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/48/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/48/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        println "first message..."
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)
        println "second message..."
        def response2 = new CannedXdrSenderImpl(manager).sendXdr(context)
        println "third message..."
        def response3 = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response3
        assert true
    }

    @Test
    def test49(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/49/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/49/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }

    @Test
    def test50a(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50a/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50a/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }


    @Test
    def test50b(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50b/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50b/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }



    def test10(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "antoine@edge.nist.gov"
        context.directFrom = "antoine@edge.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/10/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/10/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }


}
