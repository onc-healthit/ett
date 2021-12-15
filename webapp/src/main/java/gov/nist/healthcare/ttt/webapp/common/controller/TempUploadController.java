package gov.nist.healthcare.ttt.webapp.common.controller;

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
	
	String tDir = System.getProperty("java.io.tmpdir");
	private static Logger logger = LogManager.getLogger(TempUploadController.class.getName());
	
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody FileInfo uploadCert(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		FileInfo fileInfo = new FileInfo();
		
		fileInfo.setAttributes(request);
		
		// Extract the file
		
		Iterator<String> itr = request.getFileNames();

        MultipartFile file = request.getFile(itr.next());
        
        File temp;
        
        // Unique uuid for filename
        UUID fileuuid = UUID.randomUUID();
		logger.info("fileInfo.getFlowFilename() 1111:::::"+fileInfo.getFlowFilename());
        if(!fileInfo.getFlowFilename().equals("")) {
    		Path path  = Paths.get(fileInfo.getFlowFilename());
    		Path normalizedPath =  path.normalize();
    		fileInfo.setFlowFilename(normalizedPath.toString());
    		logger.info("FlowFilename normalizedPath.toString() :::::"+normalizedPath.toString());
    		logger.info("FlowFilename fileInfo.getFlowFilename() 22222 :::::"+fileInfo.getFlowFilename());
    		temp = new File(tDir + File.separator + normalizedPath.toString() + "-ett_" + fileuuid + "_ett");
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

    	File f = new File(tDir + File.separator + normalizedPath.toString());        	
		if(f.exists()) {
			fileInfo.setFlowRelativePath(f.getAbsolutePath());
			return fileInfo;
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		return fileInfo;
	}

}