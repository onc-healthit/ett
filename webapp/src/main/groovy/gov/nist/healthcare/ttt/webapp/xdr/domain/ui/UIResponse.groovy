package gov.nist.healthcare.ttt.webapp.xdr.domain.ui
import com.fasterxml.jackson.annotation.JsonInclude
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result

/**
 *
 * Represents the response we send back to the UI.
 *
 * Allows to provide :
 * - @status :the status of the request (we could have rather piggyback on HTTP code conventions)
 * - @message : a user friendly message to describe the error if any.
 * - @content : the payload of the response containing values expected by the UI or a detailed exception
 * //TODO we probably should not leak detailed exception.
 *
 * Created by gerardin on 10/9/14.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class UIResponse {


    UIStatus status

    String message

    Result content

    enum UIStatus {SUCCESS , ERROR}

    public UIResponse(UIStatus s, String msg){
        this.status = s
        this.message = msg
    }

    public UIResponse(UIStatus s, String msg, Result c){
        this.status = s
        this.message = msg
        this.content= c
    }
}
