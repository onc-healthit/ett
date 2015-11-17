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
class XdrTestCase19Test extends XDRSpecification {

    String simId = "19"
    String tcId = "19"
    String simEndpoint = TestUtils.simEndpoint(simId, system)
    String testCaseConfig =
            """{
        "direct_from": "$fromAddress"
}"""

    def "user succeeds in running test case - positive test : message ids are all distinct"() throws Exception {

        when : "we looking for the endpoint"
        MockHttpServletRequestBuilder endpoint = TestUtils.configure(tcId,userId,testCaseConfig)

        //TODO find a way to test endpoint
        then: "we receive back a success message with the endpoints info"
        gui.perform(endpoint)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.value.endpoints").exists())
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))

        when: "receiving a request to run test case"
        MockHttpServletRequestBuilder configure = TestUtils.run(tcId,userId,testCaseConfig)

        then: "we receive back a success message"
        gui.perform(configure)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))

        when: "receiving xdr report from toolkit."
        MockHttpServletRequestBuilder toolkitNotification = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,"1",fromAddress,toAddress))
        toolkit.perform(toolkitNotification)
                .andDo(print())
                .andReturn()

        MockHttpServletRequestBuilder toolkitNotification2 = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,"2",fromAddress,toAddress))
        toolkit.perform(toolkitNotification2)
                .andDo(print())
                .andReturn()

        MockHttpServletRequestBuilder toolkitNotification3 = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,"3",fromAddress,toAddress))
        toolkit.perform(toolkitNotification3)
                .andDo(print())
                .andReturn()

        then: "we store the validation in the database"
        XDRRecordInterface rec = db.xdrFacade.getLatestXDRRecordByDirectFrom(fromAddress)
        assert rec.testSteps.size() == 4

        when: "we check the status of testcase"
        MockHttpServletRequestBuilder status = TestUtils.status(tcId,userId)

        then: "we receive back a success message asking for manual validation"
        gui.perform(status)
                .andDo(print())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PASSED"))
                .andExpect(jsonPath("content.value").exists())
    }




    //@Ignore
    def "user  in running test case - negative test : message ids are not unique"() throws Exception {

        when : "we looking for the endpoint"
        MockHttpServletRequestBuilder endpoint = TestUtils.configure(tcId,userId,testCaseConfig)

        //TODO find a way to test endpoint
        then: "we receive back a success message with the endpoints info"
        gui.perform(endpoint)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.value.endpoints").exists())
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))

        when: "receiving a request to run test case"
        MockHttpServletRequestBuilder configure = TestUtils.run(tcId,userId,testCaseConfig)

        then: "we receive back a success message"
        gui.perform(configure)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("PENDING"))

        when: "receiving xdr report from toolkit."
        MockHttpServletRequestBuilder toolkitNotification = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,"1",fromAddress,toAddress))
        toolkit.perform(toolkitNotification)
                .andDo(print())
                .andReturn()

        MockHttpServletRequestBuilder toolkitNotification2 = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,"1",fromAddress,toAddress))
        toolkit.perform(toolkitNotification2)
                .andDo(print())
                .andReturn()

        MockHttpServletRequestBuilder toolkitNotification3 = TestUtils.reportNotification(TestUtils.buildReportTemplate(simId,"3",fromAddress,toAddress))
        toolkit.perform(toolkitNotification3)
                .andDo(print())
                .andReturn()

        then: "we store the validation in the database"
        XDRRecordInterface rec = db.xdrFacade.getLatestXDRRecordByDirectFrom(fromAddress)
        assert rec.testSteps.size() == 3

        when: "we check the status of testcase"
        MockHttpServletRequestBuilder status = TestUtils.status(tcId,userId)

        then: "we receive back a success message asking for manual validation"
        gui.perform(status)
                .andDo(print())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.criteriaMet").value("FAILED"))
                .andExpect(jsonPath("content.value").exists())
    }

}

