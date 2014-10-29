package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger

import static org.slf4j.LoggerFactory.getLogger
/**
 * Created by gerardin on 10/27/14.
 */
 abstract class TestCaseStrategy {


    protected static Logger log = getLogger(TestCaseStrategy.class)

    public abstract UserMessage run(Object userInput, String username)

    public UserMessage notifyXdrReceive(XDRRecordInterface record, XDRTestStepInterface step, TkValidationReport report){
        throw UnsupportedOperationException()
    }


 }
