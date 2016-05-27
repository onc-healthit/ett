package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.parsing.Parsing
import gov.nist.healthcare.ttt.parsing.Parsing.MetadataLevel;
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Content
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseSender
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Created by gerardin on 10/27/14.
 */

@Component
final class TestCase2 extends TestCaseSender {

    @Autowired
    public TestCase2(TestCaseExecutor ex){
        super(ex)
    }

    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["direct_from"])

        //correlate this test to a direct_from address and a simulator id so we can be notified
        TestCaseBuilder builder = new TestCaseBuilder(id, username)
        XDRTestStepInterface step1 = executor.correlateRecordWithSimIdAndDirectAddress(sim, context.direct_from)
        executor.db.addNewXdrRecord(builder.addStep(step1).build())

        def content = new Content()
        content.endpoint = endpoints[0]
        content.endpointTLS = endpoints[1]

        return new Result(Status.PENDING, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step = executor.executeStoreXDRReport(report)
		
		FileUtils.writeStringToFile(new File("response.txt"), report.response);

        //we update the record
        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()
		
		// Parsing of the request
		try {
			MetadataLevel level = Parsing.getMetadataLevel(report.request);
			if(level.equals(MetadataLevel.MINIMAL)) {
				updatedRecord.status = Status.FAILED
			} else {
				if(Parsing.isRegistryResponseSuccessFullHeaders(report.response)) {
					updatedRecord.status = Status.PASSED
				} else {
					updatedRecord.status = Status.FAILED
				}
			}
		} catch(Exception e) {
			log.error(e.getMessage())
			updatedRecord.status = Status.MANUAL
		}
		
        executor.db.updateXDRRecord(updatedRecord)

    }

    public Result getReport(XDRRecordInterface record) {
        executor.getSimpleSendReport(record)
    }
}
