package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("api/properties")

public class PropertiesController {

	private static Logger logger = Logger.getLogger(PropertiesController.class.getName());

	@Value("${direct.listener.domainName}")
	String domainName = "localhost";

	@Value("${ttt.lastUpdated}")
	String lastUpdated = "";

	@Value("${ttt.configfile}")
	String configFilePath = "";
	/*Calendar cal = Calendar.getInstance();
	String date = new SimpleDateFormat("MMMM dd, YYYY").format(cal.getTime());
	String lastUpdated = date;*/

	@Value("${ttt.version}")
	String version = "1.0";



	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody HashMap<String, String> getProperties() throws IOException {
    	HashMap<String, String> prop = new HashMap<String, String>();
		try {
			if (configFilePath != null){
				InputStream in = new URL(configFilePath).openStream();
				Properties properties = new Properties();
				properties.load(in);
				version = (String)properties.get("ttt.version");
				lastUpdated = (String)properties.get("ttt.lastUpdated");
			}
		} catch (FileNotFoundException fnfe) {
			logger.info(fnfe.getMessage());
		} finally {
		}
    	prop.put("domainName", domainName);
    	prop.put("lastUpdated", lastUpdated);
    	prop.put("version", version);
    	return prop;
    }

}