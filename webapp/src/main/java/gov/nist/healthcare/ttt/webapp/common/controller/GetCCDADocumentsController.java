package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/api/ccdadocuments")
public class GetCCDADocumentsController {

	private static Logger logger = LogManager.getLogger(GetCCDADocumentsController.class.getName());

	@Value("${server.tomcat.basedir}")
	String ccdaFileDirectory;

	@Value("${github.ccda.uscdi.test.data}")
	String githubUscdiTestData;

	@Value("${github.ccda.uscdi.sha}")
	String githubUscdiSha;

	@Value("${github.ccda.uscdi.tree}")
	String githubUscdiTree;		
	
	public List<String> files2ignore = Arrays.asList("LICENSE", "README.md","README.MD");
	
	public List<String> rootId = Arrays.asList("RECEIVER SUT TEST DATA", "SENDER SUT TEST DATA");
	public final String CURES_LABEL = "Cures Update ";
	public final String SVAP_LABEL = "Cures Update Svap Uscdiv2 ";
	public final String USCDIV3_LABEL = "Cures Update Svap Uscdiv3 ";
	public final String USCDIV4_LABEL = "Uscdiv4 Test Data ";
	
	public final String USCDI_V1_TESTDATA = "uscdi-v1-testdata";
	public final String USCDI_V2_TESTDATA = "uscdi-v2-testdata";
	public final String USCDI_V3_TESTDATA = "uscdi-v3-testdata";
	public final String USCDI_V4_TESTDATA = "uscdi-v4-testdata";
	
	public List<String> extension2ignore = Arrays.asList("");

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody HashMap<String, Object> getDocuments(@RequestParam(value = "testCaseType") String testCaseType) throws Exception {
		// Result map
		HashMap<String, Object> resultMap = new HashMap<>();

		// CCDA cache File path
		String ccdaFilePath = getFilterFiles(testCaseType);
		File ccdaObjectivesFile = new File(ccdaFilePath);
		
			if (StringUtils.isNotBlank(githubUscdiTestData) && githubUscdiTestData.length() > 1){
				String sha = getHTML(githubUscdiSha).getJSONObject("commit").get("sha").toString();
				JSONArray filesArray = getHTML(githubUscdiTree + sha + "?recursive=1").getJSONArray("tree");
				
				for(int i=0; i < filesArray.length(); i++) {
					JSONObject file = filesArray.getJSONObject(i);
					
					if(!files2ignore.contains(file.get("path"))) {
						// Get path array
						String[] path = file.get("path").toString().split("/");
						buildJson(resultMap, path);
					}

				}				
			}			
			// Write the cache file
			try{
				JSONObject cacheFile = new JSONObject(resultMap);
				FileUtils.writeStringToFile(ccdaObjectivesFile, cacheFile.toString(2));
			} catch(Exception e) {
				logger.error("Could not create ccda cache file: " + e.getMessage());
				e.printStackTrace();
			}
		//}
		return resultMap;
	}

	public void buildJson(HashMap<String, Object> resultMapJson, String[] path) {
		try {
			boolean curesFiles = false;
			boolean svapFiles = false;
			boolean uscdiv3Files = false;
			boolean uscdiv4Files = false;
			
			String  currentName = null;
			HashMap<String, Object> current = null;

			if (path.length > 1 ) {
			   currentName = path[1];
			   current = (HashMap<String, Object>) resultMapJson.get(path[1]);				
			}
			
			if (path.length == 2 && rootId.contains(path[1].toUpperCase())) {
				HashMap<String, Object> newObj = new HashMap<>();
				newObj.put("dirs", new ArrayList<HashMap<String, Object>>());
				newObj.put("files", new ArrayList<HashMap<String, Object>>());
				resultMapJson.put(path[1], newObj);
				addRootFiles(resultMapJson,path);
			}else if (path.length == 3) {
				HashMap<String, Object> newObj = new HashMap<>();
				newObj.put("name",path[2]);
				newObj.put("dirs", new ArrayList<HashMap<String, Object>>());
				newObj.put("files", new ArrayList<HashMap<String, Object>>());
				

				if (path[0].contains(USCDI_V4_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(USCDIV4_LABEL+path[1]);
				}else if (path[0].contains(USCDI_V3_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(USCDIV3_LABEL+path[1]);
				}else if (path[0].contains(USCDI_V2_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(SVAP_LABEL+path[1]);
				}else if (path[0].contains(USCDI_V1_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(CURES_LABEL+path[1]);
				}else {
					current = (HashMap<String, Object>) resultMapJson.get(path[1]);	
				}				
				
				List dirsList = (List) current.get("dirs");
				dirsList.add(newObj);				
			}else if (path.length > 3 ) {
				
				if (path[0].contains(USCDI_V4_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(USCDIV4_LABEL+path[1]);
					uscdiv4Files = true;					
				}else if (path[0].contains(USCDI_V3_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(USCDIV3_LABEL+path[1]);
					uscdiv3Files = true;					
				}else if (path[0].contains(USCDI_V2_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(SVAP_LABEL+path[1]);
					svapFiles = true;										
				}else if (path[0].contains(USCDI_V1_TESTDATA)) {
					current = (HashMap<String, Object>) resultMapJson.get(CURES_LABEL+path[1]);
					curesFiles = true;
				}else {
					current = (HashMap<String, Object>) resultMapJson.get(path[1]);	
				}				
				List<Map> directories = (List<Map>) current.get("dirs");
				int objPos = getObjByName(directories, path[2]);
				if (objPos >= 0) {
					current = (HashMap<String, Object>) directories.get(objPos);
				}
				
				String link = getLink(path);
				String fileName = path[path.length-1];
				
				HashMap<String, Object> newFile = new HashMap<>();
				newFile.put("name", fileName);
				newFile.put("link", link);
				newFile.put("cures",curesFiles);
				newFile.put("svap",svapFiles);
				newFile.put("uscdiv3",uscdiv3Files);
				newFile.put("uscdiv4",uscdiv4Files);
				
				List filesList = (List) current.get("files");
				filesList.add(newFile);				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void addRootFiles(HashMap<String, Object> resultMapJson, String[] path) {
		boolean addNewDirFiles = false;
		String sutHeaderVal = "";
		
		if (path[0].contains(USCDI_V1_TESTDATA)) {
			addNewDirFiles = true;
			sutHeaderVal =CURES_LABEL;
		}

		if (path[0].contains(USCDI_V2_TESTDATA)) {
			addNewDirFiles = true;
			sutHeaderVal =SVAP_LABEL;
		}
		
		if (path[0].contains(USCDI_V3_TESTDATA)) {
			addNewDirFiles = true;
			sutHeaderVal =USCDIV3_LABEL;
		}
		
		if (path[0].contains(USCDI_V4_TESTDATA)) {
			addNewDirFiles = true;
			sutHeaderVal =USCDIV4_LABEL;
		}

		if (addNewDirFiles) {
			HashMap<String, Object> newObj = new HashMap<>();
			newObj.put("dirs", new ArrayList<HashMap<String, Object>>());
			newObj.put("files", new ArrayList<HashMap<String, Object>>());
			resultMapJson.put(sutHeaderVal+path[1], newObj);			
		}
	}
	
	public String getLink(String[] path) {
		String linkMaster = String.join("/", path).replace(" ", "%20");
		String link = githubUscdiTestData + linkMaster;	

		return link;
	}

	public static boolean containsName(List<Map> json, String value) {
		for(Map obj : json) {
			if(obj.containsValue(value)) {
				return true;
			}
		}
		return false;
	}

	public static int getObjByName(List<Map> json, String value) {	
		for(int i = 0 ; i < json.size() ; i++) {
			if(json.get(i).containsValue(value)) {
				return i;
			}
		}
		return -1;
	}

	public static JSONObject getHTML(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return new JSONObject(result.toString());
	}
	
	private String getFilterFiles(String testCaseType){
		String fileName = ccdaFileDirectory + File.separator + "ccda_objectives.txt";
		extension2ignore = Arrays.asList("");
		if (testCaseType !=null && testCaseType.equalsIgnoreCase("xdr")){
			fileName = ccdaFileDirectory + File.separator + "ccda_objectives_xdr.txt";
			extension2ignore = Arrays.asList("ZIP","zip");
		}
		return fileName;
	}

}