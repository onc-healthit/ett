package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("api/announcement")
public class AnnouncementController {

	private static Logger logger = Logger.getLogger(AnnouncementController.class.getName());
	private @Autowired ApplicationContext appContext;

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String getProperties() throws IOException {
		try {
			Resource resource = appContext.getResource("file:" + System.getProperty("user.dir")+System.getProperty("file.separator")+"announcements.txt");
			return FileUtils.readFileToString(resource.getFile());
		}catch(FileNotFoundException fnfe){
			logger.info(fnfe.getMessage());
			return "File does not exist";
		}finally {
		}
	}

}