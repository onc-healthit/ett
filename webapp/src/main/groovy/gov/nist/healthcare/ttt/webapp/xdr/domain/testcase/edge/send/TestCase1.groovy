package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send

import gov.nist.healthcare.ttt.database.log.CCDAValidationReportImpl;
import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.parsing.Parsing;
import gov.nist.healthcare.ttt.parsing.Parsing.MetadataLevel
import gov.nist.healthcare.ttt.parsing.SOAPWithAttachment;
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Content
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseSender
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */

@Component
final class TestCase1 extends TestCaseSender {

	@Value('${ett.mdht.r2.url}')
	String mdhtR2Endpoint;
	
	String ccdaR2Type = "170.315_b2_CIRI_Amb"
	String ccdaR2ReferenceFilename = "170.315_b2_ciri__r11_sample1_v4.xml"
	
    @Autowired
    public TestCase1(TestCaseExecutor ex) {
        super(ex)
    }

    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["direct_from"])
		
		// Set C-CDA variables
		this.ccdaR2ReferenceFilename = context.payload.name;
        ArrayList<String> path = context.payload.path;
		if(path.size() > 1) {
			this.ccdaR2Type = path.get(path.size() - 1);
		}

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

        //we update the record
        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()
		
        // Parsing of the request
		try {
			MetadataLevel level = Parsing.getMetadataLevel(report.request);
			if(level.equals(MetadataLevel.XDS)) {
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
		
		// Extracting C-CDA from the request
		if(updatedRecord.status.equals(Status.PASSED)) {
			SOAPWithAttachment soap = Parsing.parseMtom(report.request);
			String res = validateCCDA_R2(soap.getAttachment().iterator().next(), updatedRecord)
//			log.info("CCDA validation result: " + res);
			updatedRecord.setMDHTValidationReport(res);
		}
        executor.db.updateXDRRecord(updatedRecord)

    }
	
	public String validateCCDA_R2(byte[] ccdaFile, XDRRecordInterface record) {
		log.info("Validating CCDA " + "ccda" + " with validation objective " + this.ccdaR2Type + " and reference filename " + this.ccdaR2ReferenceFilename);

		// Query MDHT war endpoint
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(this.mdhtR2Endpoint);
		
		ContentBody fileBody = new InputStreamBody(new ByteArrayInputStream(ccdaFile), "ccda");
//		FileBody fileBody = new FileBody(ccdaFile);
		//
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addTextBody("validationObjective", this.ccdaR2Type);
		builder.addTextBody("referenceFileName", this.ccdaR2ReferenceFilename);
		builder.addPart("ccdaFile", fileBody);
		HttpEntity entity = builder.build();
		//
		post.setEntity(entity);
		String result = "";
		try {
			HttpResponse response = client.execute(post);
			// CONVERT RESPONSE TO STRING
			result = EntityUtils.toString(response.getEntity());
		} catch(Exception e) {
			log.error("Error validation CCDA " + e.getMessage());
			e.printStackTrace();
		}
		
		return result;
	}

    public Result getReport(XDRRecordInterface record) {
        executor.getSimpleSendReportWithCcda(record)
    }
}
