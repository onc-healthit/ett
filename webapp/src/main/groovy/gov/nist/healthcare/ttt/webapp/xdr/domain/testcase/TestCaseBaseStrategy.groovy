package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface.CriteriaMet
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.xdr.domain.TLSValidationReport
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger

import static org.slf4j.LoggerFactory.getLogger
/**
 * Base class for implementing test cases. It defines a 2-parts contract :
 *
 * 1/ Each test case has its own execution logic and must react to a subset of some application events.
 * TestCaseStrategy defines generic hooks that execute some piece of logic when certain events occur.
 *
 * Possible events are :
 * - user starts a use case (run)
 * - ttt received a notification from Bill's toolkit (notifyXdrReceive)
 * - ttt received a notification from the direct tool (notifyDirectReceive)
 *
 * 2/ Each test case is responsible of managing its lifecycle.
 * When a test case completes, the protected done method MUST be called to set up the final state of the test.
 * Failure to do so will keep its status to the initial PENDING value.
 *
 * Created by gerardin on 10/27/14.
 */
abstract class TestCaseBaseStrategy {

    protected final TestCaseExecutor executor

    public TestCaseBaseStrategy(TestCaseExecutor executor) {
        this.executor = executor
    }

    protected static Logger log = getLogger(TestCaseBaseStrategy.class)

    public abstract TestCaseEvent run(String tcid, Map context, String username)

    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {
        throw UnsupportedOperationException()
    }

    //TODO create
//     public UserMessage notifyDirectReceive(DirectMessage record){
//         throw UnsupportedOperationException()
//     }

    /**
     * Used by all test cases. This method should be called when the test case execution terminates.
     * @param record : the test case record that is completed.
     * @param status : final status of the test case.
     * @return
     */
    protected CriteriaMet done(CriteriaMet status, XDRRecordInterface record) {
        record.criteriaMet = status
        executor.db.updateXDRRecord(record)
        return status
    }

    public void notifyTLSReceive(XDRRecordInterface xdrRecordInterface, TLSValidationReport tlsValidationReport) {
        throw UnsupportedOperationException()
    }
}
