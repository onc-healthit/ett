package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase

import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor

/**
 * Created by gerardin on 6/2/15.
 */
abstract class TestCaseSender extends TestCase {

    public TestCaseSender(TestCaseExecutor executor) {
        super(executor)
        def config = new HashMap()
        config.type = 'docrec'
        config.endpoint = 'NO_VALUE'

        try {
            sim = registerEndpoint(id, config)
        }
        catch(Exception e){
            log.error("XDR Test Case could not be instantiated. XDR Testing won't work! REASON : " + e.getMessage())
            e.printStackTrace()
        }
    }
}
