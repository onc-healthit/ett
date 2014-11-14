package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseStrategy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

import java.lang.reflect.Constructor
/**
 * Created by gerardin on 10/21/14.
 */

@Component
class TestCaseManager implements ApplicationListener<ContextRefreshedEvent> {

    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        setupTestCases()
        log.info("application started!")

    }

    TestCaseExecutor executor
    DatabaseProxy db

    private static Logger log = LoggerFactory.getLogger(TestCaseManager.class)

    @Autowired
    TestCaseManager(TestCaseExecutor executor, DatabaseProxy db) {
        this.executor = executor
        this.db = db
    }

    //TODO
    public def setupTestCases() {
        XDRSimulatorInterface sim = db.instance.xdrFacade.getSimulatorBySimulatorId("xdr.global.endpoint.matchby.messageId")

        if (sim == null) {
            executor.configureGlobalEndpoint("xdr.global.endpoint.matchby.messageId", new HashMap())
        }
    }

    public UserMessage<Object> runTestCase(String id, Map userInput, String username) {


        log.info("running test case $id")

        //Check if we have implemented this test case
        TestCaseStrategy testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception) {
            return new UserMessage(UserMessage.Status.ERROR, "test case with id $id is not implemented")
        }

        //TODO each time a test case is run for a user, the previous record status should be set to cancelled if it has not return yet.

        try {
            return testcase.run(id, userInput, username)
        }
        catch (e) {
            e.printStackTrace()
            return new UserMessage(UserMessage.Status.ERROR, e.getMessage(), e.getCause().getMessage())
        }
    }

    //TODO implement. For now just return a bogus success message.
    public XDRRecordInterface.CriteriaMet checkTestCaseStatus(String username, String tcid) {

        XDRRecordInterface record = db.getLatestXDRRecordByUsernameTestCase(username, tcid)

        //IF require manual_check status, we need to send back the validation to the user.

        return record.criteriaMet

    }

    //TODO check if we want to rely on reflection or use spring for that matter
    def findTestCase(String id) {

        Class c

        try {
            c = Class.forName("gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.TestCase$id")
        }
        catch (Exception e) {
            try {
                c = Class.forName("gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.TestCase$id")
            }
            catch (Exception ex) {
                throw ex
            }
        }

        Constructor ctor = c.getDeclaredConstructor(TestCaseExecutor)
        return ctor.newInstance(executor)
    }


}
