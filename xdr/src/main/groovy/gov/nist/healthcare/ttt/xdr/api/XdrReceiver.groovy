package gov.nist.healthcare.ttt.xdr.api

import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.Message
import gov.nist.healthcare.ttt.xdr.api.notification.IObservable

/**
 * Created by gerardin on 10/6/14.
 */

public interface XdrReceiver extends IObservable {

    /**
     *
     * @return the result of the operation
     * if Message.status=SUCCESS, Message.content should contain the name of the endpoint created
     */
    public Message<Object> createEndpoints(EndpointConfig config)




}
