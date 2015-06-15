package gov.nist.healthcare.ttt.xdr.domain

import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface

/**
 * Created by gerardin on 6/11/15.
 */
class CreateEndpointResponseParser {

    //TODO improve that, make it its own parser
    public static XDRSimulatorInterface parse(def response, String simId) {
        def transactions = response.depthFirst().findAll{it.name() == "endpoint"}
        XDRSimulatorInterface sim = new XDRSimulatorImpl()
        sim.simulatorId = simId
        sim.endpoint = transactions[0].@value.text()

        //TODO refactor : this exists only if we retrieve a docrec. Test beforehand.
        if(transactions[1]!= null) {
            sim.endpointTLS = transactions[1].@value.text()
        }
        return sim
    }
}
