package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase

/**
 * Created by gerardin on 10/29/14.
 */
enum MsgLabel {

    CREATE_NEW_RECORD_FAILED("unable to save new test case record in db"),
    UPDATE_RECORD_FAILED("unable to update record"),
    CREATE_NEW_ENDPOINTS_FAILED("unable to create new endpoints"),
    STORE_XDR_RECEIVE_FAILED("unable to store xdr report"),
    SEND_XDR_FAILED("a problem occured while sending the XDR document")

    private final String msg

    MsgLabel(String msg){
        this.msg = msg
    }


}
