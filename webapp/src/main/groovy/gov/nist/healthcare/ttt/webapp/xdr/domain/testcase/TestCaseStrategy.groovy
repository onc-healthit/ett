package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger

import static org.slf4j.LoggerFactory.getLogger
/**
 * Created by gerardin on 10/27/14.
 */
 abstract class TestCaseStrategy {

    protected final TestCaseExecutor executor

    public TestCaseStrategy(TestCaseExecutor executor){
        this.executor = executor
    }

    protected static Logger log = getLogger(TestCaseStrategy.class)

    public abstract UserMessage run(String tcid, Object userInput, String username)

    public UserMessage notifyXdrReceive(XDRRecordInterface record, TkValidationReport report){
        throw UnsupportedOperationException()
    }


 }
