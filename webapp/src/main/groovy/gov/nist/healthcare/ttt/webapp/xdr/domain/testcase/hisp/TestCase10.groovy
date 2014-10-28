package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp
import gov.nist.healthcare.ttt.database.jdbc.DatabaseException
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy

/**
 * Created by gerardin on 10/27/14.
 */
class TestCase10 extends TestCaseStrategy{

    TestCase10(String id, TestCaseManager manager){
        super(id,manager)
    }

    @Override
    UserMessage run(Object userInput, String username) {



    }

}
