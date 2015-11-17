package gov.nist.healthcare.ttt.webapp.xdr.domain

import gov.nist.healthcare.ttt.database.xdr.Status

/**
 * Created by gerardin on 12/8/14.
 */
class TestCaseResult<E> {

    Status criteriaMet = Status.PENDING

    E value

    public TestCaseResult(Status tcStatus, E content){
        this.criteriaMet = tcStatus
        this.value = content
    }
}
