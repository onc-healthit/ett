package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.receive.tls

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepImpl
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestStepBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Content
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
class TestCase9 extends TestCase {

    @Autowired
    public TestCase9(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["ip_address","port"])

        XDRTestStepImpl step = new TestStepBuilder("SEND_OVER_SSL_WITH_BAD_CERT").build()

        try {
            executor.tlsClient.connectOverBadTLS([ip_address: context.ip_address, port: context.port])
            log.debug("tls connection for tcid $id and user $username succeeded. Test failed.")
            step.status = Status.FAILED
        }
        catch(javax.net.ssl.SSLException e){
            log.debug("tls connection for tcid $id and user $username succeeded. Test succeeded.")
            e.printStackTrace()
            step.status = Status.PASSED
        }

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()
        record.status = step.status
        executor.db.addNewXdrRecord(record)

        def content = new Content()

        new Result(record.criteriaMet,content)
    }
}
