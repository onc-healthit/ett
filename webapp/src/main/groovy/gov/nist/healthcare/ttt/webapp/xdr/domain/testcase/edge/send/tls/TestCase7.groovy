package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send.tls

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Content
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseSender
import gov.nist.healthcare.ttt.xdr.domain.TLSValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase7 extends TestCaseSender {

    @Autowired
    public TestCase7(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["ip_address"])

        TestCaseBuilder builder = new TestCaseBuilder(id, username)

        def step = executor.correlateRecordWithSimIdAndIpAddress(context.ip_address, sim)

        //build and store the record for this execution
        XDRRecordInterface record = builder.addStep(step).build()
        executor.db.addNewXdrRecord(record)

        //return the endpoint we expect to be reached at
        String endpoint = executor.tlsReceiver.getEndpoint()
        def content = new Content()
        content.endpoint = endpoint

        return new Result(Status.PENDING, content)
    }

    @Override
    public void notifyTLSReceive(XDRRecordInterface record, TLSValidationReport report) {
        record.testSteps.last().status = report.status
        //second step is successful if the client disconnects
        def step = new XDRTestStepImpl();
        step.name = "BAD_TLS_MUST_DISCONNECT"
        record.status = report.status
        record = new TestCaseBuilder(record).addStep(step).build()

        executor.db.updateXDRRecord(record)
    }
}
