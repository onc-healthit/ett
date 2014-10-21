package gov.nist.healthcare.ttt.xdr.domain

/**
 * Created by gerardin on 10/9/14.
 */
public class Message<E> {

    final String message

    final Status status

    final E content

    enum Status  {SUCCESS , ERROR}


    public boolean success() {
        return status == Status.SUCCESS
    }

    public Message(Status s){
        this.status = s
    }

    public Message(String msg, Status s){
        this.message = msg
        this.status = s
    }

    public Message(String msg, Status s, E c){
        this.message = msg
        this.status = s
        this.content = c
    }
}