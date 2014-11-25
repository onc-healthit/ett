package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.IOException;
import java.util.HashMap;

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