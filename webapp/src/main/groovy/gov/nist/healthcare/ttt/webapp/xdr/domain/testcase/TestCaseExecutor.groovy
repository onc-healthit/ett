package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl

/**
 * Created by gerardin on 10/28/14.
 */
class TestCaseExecutor {

    def execute(XDRRecordInterface record){
        record.testSteps.any{

            XDRTestStepImpl step ->

            if(step.criteriaMet == XDRRecordInterface.CriteriaMet.PASSED){
                return
            }
            else if(step.criteriaMet == XDRRecordInterface.CriteriaMet.CANCELLED){
                execute(step)
            }
            else {
                return true
            }
        }
    }
}
