package gov.nist.healthcare.ttt.webapp.xdr.core

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
/**
 * Created by gerardin on 10/27/14.
 *
 * Little helper class to set up canonical test step
 */
class TestStepBuilder {

    private final XDRTestStepInterface step

    public TestStepBuilder(String name){
        step = new XDRTestStepImpl()
        step.name = name
        step.criteriaMet = XDRRecordInterface.CriteriaMet.PENDING
        step.xdrReportItems = new LinkedList<XDRReportItemInterface>()
    }

    public XDRTestStepInterface build(){
        return step
    }

    public TestStepBuilder addReport(XDRReportItemInterface reportItem){
        step.xdrReportItems.add(reportItem)
        return this
    }


}
