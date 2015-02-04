package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase13 extends TestCase {

    @Autowired
    public TestCase13(TestCaseExecutor executor){
        super(executor)
    }


    @Override
    TestCaseEvent configure(String tcid, Map context, String username) {
            XDRTestStepInterface step = executor.executeSendXDRStep(context)

            //Create a new test record.
            XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

            executor.db.addNewXdrRecord(record)

            done(step.criteriaMet, record)
        }



}
