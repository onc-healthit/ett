package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.receive
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase3ccr extends TestCase {

    @Autowired
    public TestCase3ccr(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    TestCaseEvent configure(Map context, String username) {

        def config = new HashMap()
        config.type = 'docsrc'
        config.endpoint = context.targetEndpoint
        config.endpointTLS = context.targetEndpointTLS

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        //because Bill does not update existing simulator, we have to generate unique ids each time
        def simId = id+"_"+username+"_"+timeStamp
        sim = registerEndpoint(simId, config)

        executor.createRecordForTestCase(context,username,id,sim)


        context.directTo = "testcase3ccr@nist.gov"
        context.directFrom = "testcase3ccr@nist.gov"
        context.wsaTo = context.targetEndpoint
        context.messageType = ArtifactManagement.Type.XDR_CCR

        context.simId = sim.simulatorId
        context.endpoint = sim.endpointTLS

        XDRTestStepInterface step = executor.executeSendXDRStep2(context)

        //Create a new test record
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        //at this point the test case status is either PASSED or FAILED depending on the result of the validation
        XDRRecordInterface.CriteriaMet testStatus = done(step.criteriaMet, record)


        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)

        def content = new StandardContent()
        return new TestCaseEvent(testStatus, content)
    }

}
