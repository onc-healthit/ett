package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRReportItemInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
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
        String[] simulators  = [
                                "xdr.global.endpoint.matchby.messageId",
                                "xdr.global.endpoint.tc.19"
                                ]

        String[] missingSimulators = simulators.each{
            XDRSimulatorInterface sim = db.instance.xdrFacade.getSimulatorBySimulatorId(it)
            if(sim == null){
                return it
            }
        }

        //if simulators do not exist, we create them
        missingSimulators.each(){
            executor.configureGlobalEndpoint(it, new HashMap())
        }
    }

    public TestCaseEvent runTestCase(String id, Map userInput, String username) {


        log.info("running test case $id")

        //Check if we have implemented this test case
        TestCaseBaseStrategy testcase
        try {
            testcase = findTestCase(id)
        }
        catch (Exception e) {
            throw new Exception("test case $id is not yet implemented",e)
        }

        //TODO each time a test case is run for a user, the previous record status should be set to cancelled if it has not return yet
        testcase.run(id, userInput, username)
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
        def content = new StandardContent()

        if(record.criteriaMet != XDRRecordInterface.CriteriaMet.PENDING) {

            def step = record.getTestSteps().last()


            if(!step.xdrReportItems.empty) {
                log.info(step.xdrReportItems.size() + " report(s) found.")
                report = step.xdrReportItems
                content.request = report.find { it.reportType == XDRReportItemInterface.ReportType.REQUEST }.report
                content.response = report.find { it.reportType == XDRReportItemInterface.ReportType.RESPONSE }.report
                //  content.report = report.find { it.reportType == XDRReportItemInterface.ReportType.VALIDATION_REPORT}.report
            }
        }

        return new TestCaseEvent(record.criteriaMet,content)

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
