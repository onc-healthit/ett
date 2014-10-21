package gov.nist.healthcare.ttt.xdr.api.notification

import gov.nist.healthcare.ttt.xdr.domain.Message

/**
 * Created by gerardin on 10/14/14.
 */
public interface IObservable {

    def notifyObserver(Message m)

    def registerObserver(IObserver o)


}
