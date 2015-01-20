package gov.nist.healthcare.ttt.xdr.domain

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface.CriteriaMet

/**
 * Created by gerardin on 12/9/14.
 */
class TLSValidationReport {

    public TLSValidationReport(CriteriaMet status, String address){
        this.status = status
        this.hostname = address
    }

    XDRRecordInterface.CriteriaMet status

    String hostname
}
