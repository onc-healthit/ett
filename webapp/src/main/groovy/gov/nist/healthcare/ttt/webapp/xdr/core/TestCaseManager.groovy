package gov.nist.healthcare.ttt.webapp.xdr.core

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.lang.reflect.Constructor
/**
 * Created by gerardin on 10/21/14.
 */

@Component
class TestCaseManager {

    private final DatabaseInstance db
    private final XdrReceiver receiver
    private final ResponseHandler handler
    private final XdrSender sender
    private final Clock clock

    private static Logger log = LoggerFactory.getLogger(TestCaseManager.class)

    @Autowired
    TestCaseManager(DatabaseInstance db, XdrReceiver receiver, XdrSender sender, Clock clock) {
        this.db = db
        this.receiver = receiver
        this.sender = sender
        this.clock = clock
    }


    public UserMessage<Object> runTestCase(TestCaseStrategy testcase, Object userInput, String username) {

        log.info("running test case $testcase.id")

        return testcase.run(userInput,username)
    }

    //TODO implement. For now just return a bogus success message.
    public XDRRecordInterface.CriteriaMet checkTestCaseStatus(String username, String tcid) {


        XDRRecordInterface record = db.xdrFacade.getLatestXDRRecordByUsernameTestCase(username,tcid)

        return record.criteriaMet

    }


    def findTestCase(String id) {

        Class c

        try {
            c = Class.forName("gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.TestCase$id")
        }
        catch(Exception e){
            try{
                c = Class.forName("gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.TestCase$id")
            }
            catch(Exception ex){
                throw ex
            }
        }

            Constructor ctor = c.getDeclaredConstructor(String,TestCaseManager)
            return ctor.newInstance(id,this)
    }
}
