package gov.nist.healthcare.ttt.webapp.xdr.domain

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Created by gerardin on 10/9/14.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserMessage<E> {

    Status status

    String message

    E content

    enum Status  {SUCCESS , ERROR}


    public UserMessage(String msg){
        this.status = Status.SUCCESS
        this.message = msg
    }

    public UserMessage(Status s, String msg){
        this.status = s
        this.message = msg
    }

    public UserMessage(Status s, String msg, E c){
        this.status = s
        this.message = msg
        this.content= c
    }
}
