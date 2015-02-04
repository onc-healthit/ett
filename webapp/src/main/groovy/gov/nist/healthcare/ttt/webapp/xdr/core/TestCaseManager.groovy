package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
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
    Map<String,TestCase> tcs = [:]

    private static Logger log = LoggerFactory.getLogger(TestCaseManager.class)

    @Autowired
    TestCaseManager(TestCaseExecutor executor, DatabaseProxy db,List<TestCase> tcList) {
        this.executor = executor
        this.db = db
        tcList.each {
            def tcIdAsKey =  it.getClass().getSimpleName().split("TestCase")[1]
            tcs[tcIdAsKey] = it
        }
    }


    public def setupTestCases() {
        //Nothing done here anymore but we leave the hook in case
    }

    TestCaseEvent getTestCaseEndpoint(String id) {

        log.info("running test case $id")

        //Check if we have implemented this test case
        TestCase testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception e) {
            throw new Exception("test case $id is not yet implemented", e)
        }

        List<String> endpoints = testcase.getEndpoints()
        StandardContent content = new StandardContent()
        content.endpoints = endpoints

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.PENDING, content)
    }

    public TestCaseEvent configureTestCase(String id, Map userInput, String username) {
        log.info("configure test case $id")

        //Check if we have implemented this test case
        TestCase testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception e) {
            throw new Exception("test case $id is not yet implemented", e)
        }

        //TODO each time a test case is configure for a user, the previous record status should be set to cancelled if it has not return yet
        testcase.configure(userInput, username)
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
        log.info stepLists.substring(0, stepLists.length() - 1)

        def report = null
        def content = new StandardContent()

        if (record.criteriaMet != XDRRecordInterface.CriteriaMet.PENDING) {

            def step = record.getTestSteps().last()


            if (!step.xdrReportItems.empty) {
                log.info(step.xdrReportItems.size() + " report(s) found.")
                report = step.xdrReportItems
                content.request = report.find { it.reportType == XDRReportItemInterface.ReportType.REQUEST }.report
                content.response = report.find { it.reportType == XDRReportItemInterface.ReportType.RESPONSE }.report
                //  content.report = report.find { it.reportType == XDRReportItemInterface.ReportType.VALIDATION_REPORT}.report
            }
        }

        return new TestCaseEvent(record.criteriaMet, content)

    }

    def findTestCase(String id) {

        def tc = tcs[id]

        if(tc == null){
            throw new Exception("could not find implementation of test case with id $id.")
        }

        log.debug("found test case implementation : " + tc.getClass().getSimpleName())

        return tc
    }
}
