package gov.nist.healthcare.ttt.webapp.common.controller;

import gov.nist.healthcare.ttt.webapp.common.model.exceptionJSON.TTTCustomException;
import gov.nist.healthcare.ttt.webapp.common.model.FileInfo.FileInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Controller
@RequestMapping("/api/upload")

public class TempUploadController {
	
	private static boolean exceededFileSizeLimit;
	private static boolean fileSizeExists;
	String tDir = System.getProperty("java.io.tmpdir");
	private static Logger logger = LogManager.getLogger(TempUploadController.class.getName());
	
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody FileInfo uploadCert(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, Exception {
		
		FileInfo fileInfo = new FileInfo();
		
		fileInfo.setAttributes(request);
		
		exceededFileSizeLimit = false;
		fileSizeExists = true;
		
		//Check file size exists
		if(fileInfo.getFlowTotalSize() == null) {
			logger.info("Unknown file size");
			fileSizeExists = false;
			return fileInfo;
		}
		
		//Check file size
		if(Integer.parseInt(fileInfo.getFlowTotalSize()) > 1000000) {
			logger.info("File size exceeded, upload cancelled. Limit: 1000000 File size: " +fileInfo.getFlowTotalSize());
			exceededFileSizeLimit = true;
			return fileInfo;
		}
		
		// Extract the file
		Iterator<String> itr = request.getFileNames();

        MultipartFile file = request.getFile(itr.next());
        
        File temp;
        
        // Unique uuid for filename
        UUID fileuuid = UUID.randomUUID();
		logger.info("fileInfo.getFlowFilename() 1111:::::"+fileInfo.getFlowFilename());
		if(!fileInfo.getFlowFilename().equals("")){
			logger.info("fileInfo.getFlowFilename() startsWith../ :::::"+fileInfo.getFlowFilename().startsWith("../"));
			logger.info("fileInfo.getFlowFilename() startsWith\\ :::::"+fileInfo.getFlowFilename().startsWith("\\"));
		}


        if(!fileInfo.getFlowFilename().equals("") && !fileInfo.getFlowFilename().startsWith("../") &&
		!fileInfo.getFlowFilename().startsWith("\\")) {
    		Path path  = Paths.get(fileInfo.getFlowFilename());
    		Path normalizedPath =  path.normalize();
    		logger.info("FlowFilename normalizedPath.toString() :::::"+normalizedPath.toString());

			String fileName = normalizedPath.getFileName().toString();
    		logger.info("FlowFilename fileName :::::"+fileName);
			
    		fileInfo.setFlowFilename(fileName);
    		logger.info("FlowFilename fileInfo.getFlowFilename() 22222 :::::"+fileInfo.getFlowFilename());
    		temp = new File(tDir + File.separator + fileName + "-ett_" + fileuuid + "_ett");
        } else {
        	temp = File.createTempFile("tempfile", ".tmp");
        }
        
        temp.deleteOnExit();
        
        // Write the file
        file.transferTo(temp);

        fileInfo.setFlowRelativePath(temp.getAbsolutePath());
        
		return fileInfo;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody FileInfo uploadCert(@RequestParam(value = "flowFilename") String filename, HttpServletResponse response) throws IOException {
		FileInfo fileInfo = new FileInfo();
		Path path  = Paths.get(filename);
		Path normalizedPath =  path.normalize();
		logger.info("FlowFilename normalizedPath.toString() 22222 :::::"+normalizedPath.toString());

		String fileName = normalizedPath.getFileName().toString();
		logger.info("FlowFilename fileName :::::"+fileName);
			
    	File f = new File(tDir + File.separator + fileName);        	
		if(f.exists()) {
			fileInfo.setFlowRelativePath(f.getAbsolutePath());
			return fileInfo;
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		return fileInfo;
	}
	
	public static boolean isValidFileSize() {
		return exceededFileSizeLimit;
	}
	
	public static boolean hasFileSize() {
		return fileSizeExists;
	}

}