package gov.nist.healthcare.ttt.xdr.api
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.commons.notification.IObservable
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
/**
 * Created by gerardin on 10/6/14.
 */

public interface XdrReceiver extends IObservable {

    public XDRSimulatorInterface createEndpoints(EndpointConfig config)

    public def sendXdr(Map config)



}
