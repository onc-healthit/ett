package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseManager
import gov.nist.healthcare.ttt.webapp.xdr.domain.UserMessage
import gov.nist.healthcare.ttt.xdr.domain.EndpointConfig
import org.slf4j.Logger
import static org.slf4j.LoggerFactory.getLogger

/**
 * Created by gerardin on 10/27/14.
 */
 abstract class TestCaseStrategy {

    protected static Logger log = getLogger(TestCaseStrategy.class)
    protected TestCaseManager manager
    protected String id

    public TestCaseStrategy(String id, TestCaseManager manager){
         this.id = id
         this.manager = manager
     }

    public abstract UserMessage run(Object userInput, String username)



    protected XDRSimulatorInterface createEndpoints(String username, def userInput){

        def timestamp = manager.clock.timestamp

        String endpointId = "${username}.${id}.${timestamp}"

        log.info("trying to generate endpoints with id : ${endpointId}")

        EndpointConfig config = new EndpointConfig()
        config.name = endpointId

        log.info("trying to create new endpoints on toolkit...")

        return manager.receiver.createEndpoints(config)
    }

    protected unableToSaveInDBMessage(Exception e ){
        return new UserMessage(UserMessage.Status.ERROR, "unable to save new test case record in db" + e.getMessage())
    }

    protected unableToConfigureTestCaseMessage(Exception e){
        return new UserMessage(UserMessage.Status.ERROR, "unable to configure this test case.\n" + e.getMessage())
    }
}
