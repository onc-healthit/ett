package gov.nist.healthcare.ttt.webapp.api.xdr.edge.sending.mu2
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.webapp.TestUtils
import gov.nist.healthcare.ttt.webapp.XDRSpecification
import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase20aTest extends XDRSpecification {

    String simId = "20a"
    String tcId = "20a"
    String simEndpoint = TestUtils.simEndpoint(simId, system)

    String fromAddress = "from@hit-dev.nist.gov"
    String toAddress = "to@hit-dev.nist.gov"

    String testCaseConfig =
            """{
        "direct_from": "$fromAddress"
}"""


    def "user succeeds in running test case"() throws Exception {

        when: "receiving a request to run test case"
        MockHttpServletRequestBuilder configure = TestUtils.run(tcId,userId,testCaseConfig)

        then: "we receive back a success message with manual validation"
        gui.perform(configure)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))


        when: "receiving xdr report from toolkit."
        MockHttpServletRequestBuilder toolkitNotification = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,messageID,fromAddress,toAddress))
        toolkit.perform(toolkitNotification)
                .andDo(print())
                .andReturn()

        then: "we store the validation in the database"
        XDRRecordInterface rec = db.xdrFacade.getLatestXDRRecordByDirectFrom("$fromAddress")
        assert rec != null
        assert rec.testSteps.size() == 2


        when: "we check the status of testcase"
        MockHttpServletRequestBuilder status = TestUtils.status(tcId,userId)

        then: "we receive back a success message asking for manual validation"
        gui.perform(status)
                .andDo(print())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("MANUAL"))
                .andExpect(jsonPath("content.value").exists())
    }
}

