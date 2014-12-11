package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBaseStrategy
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

    public UserMessage runTestCase(String id, Map userInput, String username) {


        log.info("running test case $id")

        //Check if we have implemented this test case
        TestCaseBaseStrategy testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception e) {
            return new UserMessage(UserMessage.Status.ERROR, "test case with id $id is not implemented", e.getCause().getMessage())
        }

        //TODO each time a test case is run for a user, the previous record status should be set to cancelled if it has not return yet.

            return testcase.run(id, userInput, username)
    }

    //TODO implement. For now just return a bogus success message.
    public TestCaseEvent checkTestCaseStatus(String username, String tcid) {

        log.info("check status for test case $tcid")

        XDRRecordInterface record = db.getLatestXDRRecordByUsernameTestCase(username, tcid)

        log.info("number of test steps found : " + record.testSteps.size())

        def stepLists = "test steps recorded :"

        record.getTestSteps().each {
            stepLists <<= "$it.name , "
        }

        log.info stepLists.substring(0,stepLists.length()-1)


        def report = null


        if(record.criteriaMet != XDRRecordInterface.CriteriaMet.PENDING) {
//            record.getTestSteps().each {
//                log.info it.name
//                if(it.xdrReportItems != null && it.xdrReportItems.size() != 0){
//                    report = it.xdrReportItems.last().report
//                }
//            }

            //TODO find by name and also ask Andrew to return an ordered list (last added is first for now)
            def step = record.getTestSteps().find {
                it.name == "XDR_RECEIVE"
            }

            log.info("found XDR_RECEIVE step. " + step.xdrReportItems.size() + " report found.")

           report = step.xdrReportItems
        }

        if(report != null){
            log.info("found report")
        }

        return new TestCaseEvent(record.criteriaMet,report)

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
