package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;
import org.unix4j.unix.grep.GrepOption;

import gov.nist.healthcare.ttt.webapp.common.model.logViewModel.LogLevelModel;
import gov.nist.healthcare.ttt.webapp.common.model.logViewModel.LogLevelModel.LogLevel;
import gov.nist.healthcare.ttt.webapp.common.model.logViewModel.LogViewModel;

@Controller
@RequestMapping("/api/logview")
public class LogViewerController {

	@Value("${server.tomcat.basedir}")
	String logsBasedir;

	private static Logger logger = Logger.getLogger(LogViewerController.class.getName());

	private final String datePattern = "^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2} ";

	private Unix4jCommandBuilder unix4j = Unix4j.builder();

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

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody HashMap<String, String> getGrepedLogs(@RequestBody LogViewModel logModel) throws Exception {
		HashMap<String, String> res = new HashMap<String, String>();

		String resLogs = unix4j.fromString(logModel.getLogs()).grep(logModel.getGrep()).toStringResult();

		res.put("file", resLogs);

		return res;
	}

	@RequestMapping(value = "/level", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody HashMap<String, String> getLevelLogs(@RequestBody LogLevelModel logLevel) throws Exception {
		HashMap<String, String> res = new HashMap<String, String>();

		String pattern = datePattern + "(";
		String allLvls = "INFO|WARNING|ERROR|DEBUG";
		for (LogLevel level : logLevel.getLevels()) {
			if (level.equals(LogLevel.INFO)) {
				allLvls = allLvls.replace(level.toString(), "");
			} else {
				allLvls = allLvls.replace("|" + level.toString(), "");
			}
		}
		if (allLvls.startsWith("|")) {
			allLvls = allLvls.substring(1);
		}

		pattern += allLvls + ")";

		String resLogs = unix4j.fromString(logLevel.getLogs()).grep(GrepOption.invertMatch, pattern).toStringResult();

		res.put("file", resLogs);

		return res;
	}

}
