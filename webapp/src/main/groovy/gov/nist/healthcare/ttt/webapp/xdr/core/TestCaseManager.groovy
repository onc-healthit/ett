package gov.nist.healthcare.ttt.webapp.xdr.core

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseResult
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

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
    Map<String, TestCase> tcs = [:]

    private static Logger log = LoggerFactory.getLogger(TestCaseManager.class)

    @Autowired
    TestCaseManager(TestCaseExecutor executor, DatabaseProxy db, List<TestCase> tcList) {
        this.executor = executor
        this.db = db
        tcList.each {
            def tcIdAsKey = it.getClass().getSimpleName().split("TestCase")[1]
            tcs[tcIdAsKey] = it
        }
    }


    public def setupTestCases() {
        //Nothing done here anymore but we leave the hook in case
    }


    public TestCaseResult configure(String id) {

        log.debug("run test case $id")

        //Check if we have implemented this test case
        TestCase testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception e) {
            throw new Exception("test case $id is not yet implemented", e)
        }

        testcase.configure()
    }

    public TestCaseResult run(String id, Map userInput, String username) {
        log.debug("run test case $id")

        //Check if we have implemented this test case
        TestCase testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception e) {
            throw new Exception("test case $id is not yet implemented", e)
        }

        //TODO
        // Discuss this :
        // Eeach time a test case is configured for a user,
        // the previous record status could be set to cancelled if it has not return yet
        // This is for book-keeping only since we only fetch the last record anyway.
        testcase.run(userInput, username)
    }


    public TestCaseResult status(String username, String id) {

        log.debug("check status for test case $id")

        //Check if we have implemented this test case
        TestCase testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception e) {
            throw new Exception("test case $id is not yet implemented", e)
        }

        XDRRecordInterface record = db.getLatestXDRRecordByUsernameTestCase(username, id)

        log.debug("number of test steps found : " + record.testSteps.size())

        def stepLists = "test steps recorded :"
        record.getTestSteps().each {
            stepLists <<= "$it.name , "
        }

        log.info stepLists.substring(0, stepLists.length() - 1)

        testcase.getReport(record)
    }

    def findTestCase(String id) {

        def tc = tcs[id]

        if (tc == null) {
            throw new Exception("could not find implementation of test case with id $id.")
        }

        log.debug("found test case implementation : " + tc.getClass().getSimpleName())

        return tc
    }
}
