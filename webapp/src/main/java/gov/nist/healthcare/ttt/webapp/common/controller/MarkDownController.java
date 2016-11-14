package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("api/markdown")
public class MarkDownController {

	@Value("${announcements.path}")
	String announcementPath;

	@Value("${faq.path}")
	String faqPath;

	private static Logger logger = Logger.getLogger(MarkDownController.class.getName());

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String getProperties(@RequestParam("moduleInfo") String moduleInfo) throws IOException {
		try {
			InputStream in = new URL(getFilePath(moduleInfo)).openStream();
			return IOUtils.toString(in);
		} catch (FileNotFoundException fnfe) {
			logger.info(fnfe.getMessage());
			return "File does not exist";
		} finally {
		}
	}

	private String getFilePath(String moduleInfo){
		String fileName ="";
		if (moduleInfo.equalsIgnoreCase("announcement")){
			fileName = announcementPath;
		}else if(moduleInfo.equalsIgnoreCase("faq")){
			fileName = faqPath;
		}
		return fileName;
	}

}