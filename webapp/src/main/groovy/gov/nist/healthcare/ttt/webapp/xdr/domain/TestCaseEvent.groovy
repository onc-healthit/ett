package gov.nist.healthcare.ttt.webapp.xdr.domain

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface

/**
 * Created by gerardin on 12/8/14.
 */
class TestCaseEvent<E> {

    XDRRecordInterface.CriteriaMet criteriaMet = XDRRecordInterface.CriteriaMet.PENDING

    E value

    public TestCaseEvent(XDRRecordInterface.CriteriaMet tcStatus,E content){
        this.criteriaMet = tcStatus
        this.value = content
    }
}
