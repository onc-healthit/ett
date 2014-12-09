package gov.nist.healthcare.ttt.webapp.xdr.domain

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface

/**
 * Created by gerardin on 12/8/14.
 */
class TestCaseEvent {

    String value

    XDRRecordInterface.CriteriaMet criteriaMet = XDRRecordInterface.CriteriaMet.PENDING

    public TestCaseEvent(String content, XDRRecordInterface.CriteriaMet tcStatus){
        this.value = content
        this.criteriaMet = tcStatus
    }
}
