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
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("api/announcement")
public class AnnouncementController {

	@Value("${announcements.path}")
	String announcementPath;

	private static Logger logger = Logger.getLogger(AnnouncementController.class.getName());

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String getProperties() throws IOException {
		try {
			InputStream in = new URL(announcementPath).openStream();
			return IOUtils.toString(in);
		} catch (FileNotFoundException fnfe) {
			logger.info(fnfe.getMessage());
			return "File does not exist";
		} finally {
		}
	}

}