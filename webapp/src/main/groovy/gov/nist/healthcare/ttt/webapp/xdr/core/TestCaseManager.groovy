package gov.nist.healthcare.ttt.webapp.xdr.core

import gov.nist.healthcare.ttt.database.jdbc.DatabaseException
import gov.nist.healthcare.ttt.database.xdr.XDRRecordImpl
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorImpl
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.api.XdrSender
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import gov.nist.healthcare.ttt.xdr.domain.Message
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by gerardin on 10/21/14.
 */
class TestCaseManager {

    private final DatabaseInstance db
    private final XdrReceiver receiver
    private final ResponseHandler handler
    private final XdrSender sender

    @Autowired
    TestCaseManager(DatabaseInstance db, XdrReceiver receiver, ResponseHandler handler, XdrSender sender) {
        this.db = db
        this.receiver = receiver
        this.handler = handler
        receiver.registerObserver(handler)
        this.sender = sender
    }


    UserMessage runTestCase1(Object body) {

        String tcId = 1
        String endpointId = "endpoint1"
        String username = ""

        //Create a new test record.
        XDRRecordInterface record = XDRRecordImpl()
        record.setTestCaseNumber(tcId)
        record.setUsername(username)

        //Config
        //try to effectively create the endpoints
        EndpointConfig config = new EndpointConfig()
        config.name = endpointId
        Message<String> r


        try {
            r = receiver.createEndpoints(config)
        } catch (Exception e) {
            return new UserMessage(UserMessage.Status.ERROR, "unable to create endpoints")
        }
        if (r.success()) {

            //Create steps for this test

            // step 1 : receive and validate
            XDRTestStepInterface step = new XDRTestStepImpl()
            step.setXdrTestStepID("tc1.step1")

            //TODO if interface, replace in method signature Impl by Interface
            record.setTestSteps(new LinkedList<XDRTestStepImpl>(step))
            //Save the simulator info.
            //TODO indicate the step is completed
            XDRSimulatorInterface sim = new XDRSimulatorImpl()
            sim.setXDRSimulatorID(endpointId)
            sim.setXDRSimulatorID(endpointId)
            sim.setEndpoint("http://")
            sim.setEndpointTLS("https://")
            step.xdrSimulators(Collections.singletonList(sim))

            //persist this record
            try {
                String recordId = db.xdrFacade.addNewXdrRecord(record)
            }
            catch (DatabaseException e) {
                return new UserMessage(UserMessage.Status.ERROR, "unable to save new test case record in db")
            }

            return new UserMessage(UserMessage.Status.SUCCESS, "create new endpoint for test case ${tcId} with config : ${body}. Ready to receive message")
        }


        return new UserMessage(UserMessage.Status.ERROR, "unable to create endpoints")
    }

}
