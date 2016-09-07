package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("api/properties")
public class PropertiesController {
	
	@Value("${direct.listener.domainName}")
	String domainName = "localhost";
	
	@Value("${ttt.lastUpdated}")
	String lastUpdated = ""; 
	/*Calendar cal = Calendar.getInstance();
	String date = new SimpleDateFormat("MMMM dd, YYYY").format(cal.getTime());
	String lastUpdated = date;*/
	
	@Value("${ttt.version}")
	String version = "1.0";
	
	
	
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody HashMap<String, String> getProperties() throws IOException {
    	HashMap<String, String> prop = new HashMap<String, String>();
    	prop.put("domainName", domainName);
    	prop.put("lastUpdated", lastUpdated);
    	prop.put("version", version);  	
    	return prop;
    }
	
}