package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase

/**
 * Created by gerardin on 10/29/14.
 */
enum MsgLabel {
    PERSIST_NEW_RECORD_FAILED("unable to save new test case record in db"),
    UNABLE_TO_CREATE_NEW_ENDPOINTS("unable to create new endpoints"),
    STORE_XDR_RECEIVE_FAILED("unable to store xdr report"),
    SEND_XDR_FAILED("a problem occured while sending the XDR document")
}
