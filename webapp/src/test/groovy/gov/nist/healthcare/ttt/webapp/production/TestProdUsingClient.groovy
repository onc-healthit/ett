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
		TLSclient.connectOverGoodTLS([ip_address: "hit-dev.nist.gov", port: "12084"])

		then :

		assert true
	}

    @Test
    def test19mu2success(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/19mu2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/19mu2/docrec/prb"
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
    def test19mu2failure(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/19mu2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/19mu2/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        context.messageId = "1"
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
    def test20amu2(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"

        //Careful : this address should exists in the tool for the MDN to be sent back properly
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20amu2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20amu2/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }


    @Test
    def test20bmu2(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20bmu2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/20bmu2/docrec/prb"
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
    def test49mu2(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/49mu2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/49mu2/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }

    @Test
    def test50amu2(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50amu2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50amu2/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }


    @Test
    def test50bmu2(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "julien@hit-dev.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50bmu2/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/50bmu2/docrec/prb"
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

    def test11(){

        when :
        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "antoine@edge.nist.gov"
        context.directFrom = "antoine@edge.nist.gov"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/11/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/11/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        println response
        assert true
    }

    @Test
    def test16(){

        when :
        def manager = new SSLContextManager()
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/16/docrec/prb"
        context.targetEndpointTLS = "http://hit-dev.nist.gov:11080/xdstools3/sim/ett/16/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def response = new CannedXdrSenderImpl(manager).sendXdr(context)

        then :

        assert true
    }
	
	@Test
	def test37mu2(){

		when :
		def manager = new SSLContextManager()
		def context = [:]
		context.directTo = "test@test.com"
		context.directFrom = "test@test.com"
		context.wsaTo = "http://localhost:8080/xdstools2/sim/local-ett__37mu2/rep/xdrpr"
		context.targetEndpointTLS = "http://localhost:8080/xdstools2/sim/local-ett__37mu2/rep/xdrpr"
		context.messageType = ArtifactManagement.Type.DELIVERY_STATUS_NOTIFICATION_SUCCESS
		def response = new CannedXdrSenderImpl(manager).sendXdr(context)

		then :

		assert true
	}


}
