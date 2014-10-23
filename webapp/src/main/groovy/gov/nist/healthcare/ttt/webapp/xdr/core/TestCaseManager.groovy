package gov.nist.healthcare.ttt.webapp.xdr.core
import gov.nist.healthcare.ttt.database.jdbc.DatabaseException
import gov.nist.healthcare.ttt.database.xdr.*
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.webapp.xdr.time.Clock
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/21/14.
 */

@Component
class TestCaseManager {


    @Autowired
    private final Clock clock

    private final DatabaseInstance db
    private final XdrReceiver receiver
    private final ResponseHandler handler
    private final XdrSender sender

    private static Logger log = LoggerFactory.getLogger(TestCaseManager.class)

    @Autowired
    TestCaseManager(DatabaseInstance db, XdrReceiver receiver, ResponseHandler handler, XdrSender sender) {
        this.db = db
        this.receiver = receiver
        this.handler = handler
        receiver.registerObserver(handler)
        this.sender = sender
    }


    public UserMessage<XDRSimulatorImpl> runTestCase1(Object userInput, String username) {

        //Info from context : what test case, what user
        //EndpointId is generated from that
        String tcId = 1

        def timestamp = clock.timestamp

        String endpointId = "${username}.${tcId}.${timestamp}"

        log.info("endpoint id generated is : ${endpointId}")

        //Info from user input
        // ... anything we need to know for creating the sim

        //=> All necessary info are collected

        //Create a new test record.
        XDRRecordInterface record = new XDRRecordImpl()
        record.setTestCaseNumber(tcId)
        record.setUsername(username)

        //Check we can perform the config
        //here, we try to effectively create the endpoints
        EndpointConfig config = new EndpointConfig()
        config.name = endpointId
        Message<Object> r = receiver.createEndpoints(config)

        //Config failed?
        if (!r.success()) {
            return new UserMessage(UserMessage.Status.ERROR, "unable to configure this test case")
        }

        //Config succeeded

        //Create steps for this test so execution can proceed

        // step 1 : receive and validate.
        // We create a simulator with the simID.
        XDRTestStepInterface step = new XDRTestStepImpl()
        step.setXdrTestStepID("tc1.step1")
        XDRSimulatorInterface sim = new XDRSimulatorImpl()
        sim.simulatorId = r.content.simId.text()
        sim.endpoint = r.content.endpoint.text()
        sim.endpointTLS = r.content.endpointTLS.text()
        step.setXdrSimulator(sim)



        //add step to the list of steps
        def steps = new LinkedList<XDRTestStepImpl>()
        steps.add(step)
        record.setTestSteps(steps)

        //persist this record
        try {
            String recordId = db.getXdrFacade().addNewXdrRecord(record)
        }
        catch (DatabaseException e) {
            return new UserMessage(UserMessage.Status.ERROR, "unable to save new test case record in db")
        }

        //Now we can wait for receiving a message
        String msg = "create new endpoint for test case ${tcId} with config : ${userInput}. Ready to receive message."
        return new UserMessage(UserMessage.Status.SUCCESS, msg, sim)
    }

}
