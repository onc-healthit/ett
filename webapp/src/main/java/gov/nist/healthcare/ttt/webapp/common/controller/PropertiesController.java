package gov.nist.healthcare.ttt.webapp.common.controller;

import gov.nist.healthcare.ttt.webapp.common.config.ApplicationPropertiesConfig;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("api/properties")
public class PropertiesController {
	
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody HashMap<String, String> getProperties() throws IOException {
    	HashMap<String, String> prop = new HashMap<String, String>();
    	prop.put("domainName", ApplicationPropertiesConfig.getConfig().getProperty("direct.listener.domainName"));
    	prop.put("lastUpdated", ApplicationPropertiesConfig.getConfig().getProperty("ttt.lastUpdated"));
    	prop.put("version", ApplicationPropertiesConfig.getConfig().getProperty("ttt.version"));  	
    	return prop;
    }
	
}