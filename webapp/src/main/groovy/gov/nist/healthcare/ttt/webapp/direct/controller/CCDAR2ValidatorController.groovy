package gov.nist.healthcare.ttt.webapp.direct.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import gov.nist.healthcare.ttt.webapp.common.db.DatabaseInstance
import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.sitenv.referenceccda.validator.RefCCDAValidationResult
import org.sitenv.referenceccda.validator.ReferenceCCDAValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/ccdar2")
public class CCDAR2ValidatorController {
	
	private static Logger logger = Logger.getLogger(CCDAR2ValidatorController.class.getName());
	
	@Autowired
	private DatabaseInstance db;

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody ArrayList<RefCCDAValidationResult> validateCCDAR2(@RequestBody HashMap<String, String> filePath) throws Exception {
		if(filePath.containsKey("messageFilePath")) {
			String messageFilePath = filePath.get("messageFilePath")
			if(messageFilePath != null && !messageFilePath.equals("")) {
				
				// Get the file as string
				FileInputStream input = new FileInputStream(messageFilePath);
				
				byte[] fileData = new byte[input.available()];
				
				input.read(fileData);
				input.close();
				
				String ccdaFile = new String(fileData, "UTF-8");
				
				String validationObjective = "170.315(b)(1)"
				if(filePath.containsKey("validationObjective")) {
					validationObjective = filePath.get("validationObjective")
				}
				
				String referenceFileName = "CP_Sample1.pdf"
				if(filePath.containsKey("referenceFileName")) {
					referenceFileName = filePath.get("referenceFileName")
				}
				
				return ReferenceCCDAValidator.validateCCDAWithReferenceFileName(ccdaFile, referenceFileName, validationObjective)
				
			} else {
				throw new TTTCustomException("0x0050", "No CCDA attachment uploaded");
			}
		}
		
	}
}
