package gov.nist.healthcare.ttt.commons.notification
/**
 * Created by gerardin on 10/14/14.
 */
public interface IObservable {

    def notifyObserver(Message m)

    def registerObserver(IObserver o)


}
