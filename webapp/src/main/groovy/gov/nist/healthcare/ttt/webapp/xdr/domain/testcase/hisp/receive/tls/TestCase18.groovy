package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.hisp.receive.tls

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
final class TestCase18 extends TestCase {

    @Autowired
    public TestCase18(TestCaseExecutor executor) {
        super(executor)
    }


    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["ip_address","port"])

        XDRTestStepImpl step = new TestStepBuilder("SEND_OVER_SSL_WITH_GOOD_CERT").build()

        try {
            executor.tlsClient.connectOverGoodTLS([ip_address: context.ip_address, port: context.port])
            log.debug("tls connection for tcid $id and user $username succeeded.")
            step.status = Status.PASSED
        }
        catch(IOException e){
            log.debug("tls connection for tcid $id and user $username failed.")
            e.printStackTrace()
            step.status = Status.FAILED
        }

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(id, username).addStep(step).build()
        record.status = step.status
        executor.db.addNewXdrRecord(record)

        def content = new Content()

        new Result(record.criteriaMet,content)
    }
}
