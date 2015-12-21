package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase

import gov.nist.healthcare.ttt.database.xdr.Status

/**
 *
 * This class models result of a test step execution
 *
 * Created by gerardin on 12/8/14.
 */
class Result<E> {

    Status criteriaMet = Status.PENDING

    E value

    public Result(Status tcStatus, E content){
        this.criteriaMet = tcStatus
        this.value = content
    }
}
