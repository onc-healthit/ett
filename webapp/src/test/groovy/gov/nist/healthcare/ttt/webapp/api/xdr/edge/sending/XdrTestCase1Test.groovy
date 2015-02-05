package gov.nist.healthcare.ttt.webapp.api.xdr.edge.sending
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

@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase1Test extends XDRSpecification {

    String simId = "1"
    String tcId = "1"
    String simEndpoint = TestUtils.simEndpoint(simId)
    String testCaseConfig =
            """{
        "direct_from": "$fromAddress"
}"""


    def "user succeeds in running test case"() throws Exception {

        when: "receiving a request to configure test case"
        MockHttpServletRequestBuilder configure = TestUtils.configure(tcId,userId,testCaseConfig)

        then: "we receive back a success message with the endpoints info"
        gui.perform(configure)
                .andDo(print())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.value.endpoint").value(simEndpoint))
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))


        when: "receiving a validation report from toolkit. We mock the actual interaction!"
        MockHttpServletRequestBuilder toolkitNotification = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,messageID,fromAddress,toAddress))
        toolkit.perform(toolkitNotification)
                .andDo(print())
                .andReturn()

        then: "we store the validation in the database"
        XDRRecordInterface rec = db.xdrFacade.getLatestXDRRecordBySimulatorId(simId)
        def step = rec.testSteps.find{
            it.name == "XDR_RECEIVE"
        }

        assert !step.xdrReportItems.get(0).report.empty


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

