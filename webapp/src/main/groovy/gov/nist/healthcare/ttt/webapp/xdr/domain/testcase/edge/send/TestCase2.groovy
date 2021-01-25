package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.send

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tika.Tika
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component

import gov.nist.healthcare.ttt.database.xdr.Status
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.parsing.Parsing;
import gov.nist.healthcare.ttt.parsing.SOAPWithAttachment;
import gov.nist.healthcare.ttt.parsing.Parsing.MetadataLevel
import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Content
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.Result
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCaseSender
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport

/**
 * Created by gerardin on 10/27/14.
 */

@Component
final class TestCase2 extends TestCaseSender {

	@Value('${ett.mdht.r2.url}')
	String mdhtR2Endpoint;
	
	@Value('${ett.mdht.r3.url}')
	String mdhtR3Endpoint;
	
	String ccdaR2Type = "170.315_b2_CIRI_Amb"
	String ccdaR2ReferenceFilename = "170.315_b2_ciri__r11_sample1_v4.xml"
	boolean cures = false;

    @Autowired
    public TestCase2(TestCaseExecutor ex){
        super(ex)
    }

    @Override
    Result run(Map context, String username) {

        executor.validateInputs(context,["direct_from"])
        
 		try {
			 this.cures = context.payload.cures;
			this.ccdaR2ReferenceFilename = context.payload.name;
	        ArrayList<String> path = context.payload.path;
			if(path.size() > 1) {
				this.ccdaR2Type = path.get(path.size() - 1);
			}
		} catch(Exception e) {
			throw new TTTCustomException("0x0080", "Could not get properties from C-CDA widget. Make sure you selected a Document type.");
			/*this.ccdaR2ReferenceFilename = ccdaR2ReferenceFilename;
			this.ccdaR2Type = ccdaR2Type;*/
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
		
		FileUtils.writeStringToFile(new File("response.txt"), report.response);

        //we update the record
        XDRRecordInterface updatedRecord = new TestCaseBuilder(record).addStep(step).build()
		
		// Parsing of the request
		try {
			MetadataLevel level = Parsing.getMetadataLevel(report.request);
			if(level.equals(MetadataLevel.MINIMAL)) {
                                log.info("XDR Test Case 2: Metadata was minimal, should be XDS.  Failure.")
				updatedRecord.status = Status.FAILED
			} else {
				if(Parsing.isRegistryResponseSuccessFullHeaders(report.response)) {
                                        log.info("XDR Test Case 2: Metadata was XDS and NO errors detected by toolkit.")
					updatedRecord.status = Status.PASSED
				} else {
                                        log.info("XDR Test Case 2: Metadata was correctly XDS but had errors detected by toolkit.  Failure.")
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
			List list = new ArrayList(soap.getAttachment());
			println("LIST SIZE"+list.size());
			byte[] v;
			for(byte[] b : list){
				InputStream is = new ByteArrayInputStream(b);
			//	String mimeType = URLConnection.guessContentTypeFromStream(is);
				String mimeType = new Tika().detect(b);
				println("MIME TYPE"+mimeType);
				if(mimeType.equals("application/xml") || mimeType.equals("text/xml")){
					v = b;
				}
			}
			
			String res = validateCCDA_R2(v, updatedRecord, this.cures)
//			log.info("CCDA validation result: " + res);
			
			updatedRecord.setMDHTValidationReport(res);
		}
        executor.db.updateXDRRecord(updatedRecord)	

    }
	public String validateCCDA_R2(byte[] ccdaFile, XDRRecordInterface record, boolean cures) {
		log.info("Validating CCDA " + "ccda" + " with validation objective " + this.ccdaR2Type + " and reference filename " + this.ccdaR2ReferenceFilename);

		// Query MDHT war endpoint
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(this.mdhtR3Endpoint);
		if(cures) {
			post = new HttpPost(this.mdhtR3Endpoint);
		}
		else {
		   post = new HttpPost(this.mdhtR2Endpoint);
		}
		
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

		JSONObject json = new JSONObject(result);
		json.put("hasError", false);
		// Check errors
		JSONArray resultMetadata = json.getJSONObject("resultsMetaData").getJSONArray("resultMetaData");
		for (int i = 0; i < resultMetadata.length(); i++) {
			JSONObject metatada = resultMetadata.getJSONObject(i);
			if(metatada.getString("type").toLowerCase().contains("error")) {
				if(metatada.getInt("count") > 0) {
					json.put("hasError", true);
				}
			}

		}

		return json.toString();
	}

    public Result getReport(XDRRecordInterface record) {
         executor.getSimpleSendReportWithCcda(record)
    }
}
