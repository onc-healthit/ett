package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.send.mu2

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.tempxdrcommunication.artifact.ArtifactManagement
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.MsgLabel
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseResult
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase14mu2 extends TestCase {


    @Autowired
    TestCase14mu2(TestCaseExecutor executor) {
        super(executor)
    }

    @Override
    TestCaseResult run(Map context, String username) {

        //Context must contain the endpoint to send to


        def config = new HashMap()
        config.type = 'docsrc'
        config.endpoint = context.targetEndpoint
        config.endpointTLS = context.targetEndpointTLS

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        //because Bill does not update existing simulator, we have to generate unique ids each time
        //this is true for the case were we need to send with the simulator
        def simId = id+"_"+username+"_"+timeStamp
        sim = registerEndpoint(simId, config)

        // We don't need to really create a correlation here.
        // When we receive an Direct, people would have previously recorded their from address in the direct testing
        // part of the tool so we know what to look up.
        executor.createRecordForTestCase(username, id, sim, "")


        context.directTo = "Provider1@direct2.sitenv.org"
        context.directFrom = "testcase14mu2@nist.gov"
        context.wsaTo = context.targetEndpoint
        context.messageType = ArtifactManagement.Type.XDR_MINIMAL_METADATA

        context.simId = sim.simulatorId
        context.endpoint = sim.endpointTLS

        XDRTestStepInterface step = executor.executeSendXDRStep(context)

        //cumbersome way of updating an object in the db
        XDRRecordInterface record = executor.db.getLatestXDRRecordByUsernameTestCase(username, id)
        record = new TestCaseBuilder(record).addStep(step).build()
        executor.db.updateXDRRecord(record)

        //manual as we will wait for manual validation of the direct
        Status testStatus = done(Status.MANUAL, record)

        log.info(MsgLabel.XDR_SEND_AND_RECEIVE.msg)

        def content = executor.buildSendXDRContent(step)

        return new TestCaseResult(testStatus, content)
    }

}
