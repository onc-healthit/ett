package gov.nist.healthcare.ttt.commons.notification

/**
 * Created by gerardin on 10/9/14.
 */
public class Message<E> {


    final Status status

    final String message

    final E content

    enum Status  {SUCCESS , ERROR}


    public boolean success() {
        return status == Status.SUCCESS
    }

    public Message(Status s){
        this.status = s
    }

    public Message(Status s, String msg){
        this.message = msg
        this.status = s
    }

    public Message(Status s, String msg, E c){
        this.message = msg
        this.status = s
        this.content = c
    }
}