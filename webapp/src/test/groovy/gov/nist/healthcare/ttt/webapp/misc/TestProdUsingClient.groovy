package gov.nist.healthcare.ttt.webapp.misc

import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.xdr.api.CannedXdrSenderImpl
import gov.nist.healthcare.ttt.xdr.api.TLSClientImpl
import gov.nist.healthcare.ttt.xdr.ssl.SSLContextManager
/**
 * Created by gerardin on 2/11/15.
 */
class TestProdUsingClient {


    public static void main(String[] args) {

        def manager = new SSLContextManager()
        def TLSclient = new TLSClientImpl(manager)
        def context = [:]
        context.directTo = "test@test.com"
        context.directFrom = "test@test.com"
        context.wsaTo = "hit-dev.nist.gov:11080/xdstools3/sim/1/docrec/prb"
        context.targetEndpoint = "http://hit-dev.nist.gov:11080/xdstools3/sim/1/docrec/prb"
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA
        def CannedXdrSenderImpl client = new CannedXdrSenderImpl().sendXdr(context)

    }
}
