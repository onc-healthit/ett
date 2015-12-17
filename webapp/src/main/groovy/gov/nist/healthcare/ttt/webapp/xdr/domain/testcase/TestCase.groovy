package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseResult
import gov.nist.healthcare.ttt.xdr.domain.TLSValidationReport
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.slf4j.Logger

import java.text.SimpleDateFormat

import static org.slf4j.LoggerFactory.getLogger
/**
 * Base class for implementing test cases.
 *
 * Each test case is implemented by a subclass of TestCase.
 * This allows to implement a Strategy pattern :
 * The TestCase superclass provide the general contract
 * that says when a TestCase can react to events.
 * Each TestCase subclass is responsible for how it reacts to those events.
 *
 *
 * Created by gerardin on 10/27/14.
 *
 */
abstract class TestCase {

    protected XDRSimulatorInterface sim

    protected final String id

    protected final TestCaseExecutor executor

    public TestCase(TestCaseExecutor executor) {
        this.id = this.getClass().getSimpleName().split("TestCase")[1]
        this.executor = executor
    }

    protected static Logger log = getLogger(TestCase.class)

    public abstract TestCaseResult run(Map context, String username)

    public TestCaseResult configure() {
        throw UnsupportedOperationException()
    }

    public TestCaseResult getReport(XDRRecordInterface record){
        log.warn("no report info available for this test case")
        return new TestCaseResult(record.criteriaMet, new StandardContent())
    }

    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {
        throw UnsupportedOperationException()
    }

    public void notifyTLSReceive(XDRRecordInterface xdrRecordInterface, TLSValidationReport tlsValidationReport) {
        throw UnsupportedOperationException()
    }

    
    public XDRSimulatorInterface registerDocRecEndpoint(String simId){
        def config = new HashMap()
        config.type = 'docrec'
        config.endpoint = 'NO_VALUE'
        executor.configureEndpoint(simId, config)
    }

    // Create an docsrc endpoint on the toolkit
    // because the toolkit does not allow updating existing simulators, we have to generate unique ids each time
    // we want to send a message to another direct address
    public XDRSimulatorInterface registerDocSrcEndpoint(String username, Map context){
        def config = new HashMap()
        config.type = 'docsrc'
        //generate unique simId
        config.endpointTLS = context.targetEndpointTLS
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        def simId = id + "_" + username + "_" + timeStamp
        executor.configureEndpoint(simId, config)
    }

    public List<String> getEndpoints() {
        return [sim.endpoint, sim.endpointTLS]
    }
}
