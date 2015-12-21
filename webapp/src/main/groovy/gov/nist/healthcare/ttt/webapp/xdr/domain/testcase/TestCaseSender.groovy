package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase

import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor

/**
 *
 * Common abstract class for all test cases that need to receive XDR messages (Sending use cases)
 *
 * Created by gerardin on 6/2/15.
 */
abstract class TestCaseSender extends TestCase {

    public TestCaseSender(TestCaseExecutor executor) {
        super(executor)
        try {
            sim = registerDocRecEndpoint(id)
        }
        catch(Exception e){
            log.error("XDR Test Case could not be instantiated. XDR Testing won't work! REASON : " + e.getMessage())
            e.printStackTrace()
        }
    }
}
