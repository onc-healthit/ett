package gov.nist.healthcare.ttt.xdr.domain

import gov.nist.healthcare.ttt.database.xdr.Status

/**
 * Created by gerardin on 12/9/14.
 */
class TLSValidationReport {

    public TLSValidationReport(Status status, String address){
        this.status = status
        this.hostname = address
    }

    Status status

    String hostname
}
