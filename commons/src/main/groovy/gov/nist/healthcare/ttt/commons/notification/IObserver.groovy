package gov.nist.healthcare.ttt.commons.notification
/**
 *
 * Classes implementing this interface
 * will be able to receive notification from the XDR layer.
 *
 * Created by gerardin on 10/14/14.
 */
public interface IObserver {

    def getNotification(Message msg)
}
