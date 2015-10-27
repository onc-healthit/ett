package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/logview")
public class LogViewerController {

	@Value("${server.tomcat.basedir}")
	String logsBasedir;

	private static Logger logger = Logger.getLogger(LogViewerController.class.getName());

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody HashMap<String, String> getLogs() throws Exception {
		HashMap<String, String> res = new HashMap<String, String>();
		
		String logsPath = this.logsBasedir + File.separator + "logs" + File.separator + "catalina.out";
		String logsString = "";
		try {
			logsString = IOUtils.toString(new FileInputStream(new File(logsPath)), Charsets.UTF_8);
		} catch (Exception e) {
			logger.error("Could not read logs file: " + e.getMessage());
			throw e;
		}
		
		res.put("file", logsString);
		
		return res;
	}

}
