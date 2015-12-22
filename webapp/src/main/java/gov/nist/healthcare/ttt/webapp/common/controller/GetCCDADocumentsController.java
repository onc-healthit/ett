package gov.nist.healthcare.ttt.webapp.common.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/ccdadocuments")
public class GetCCDADocumentsController {
	
	public List<String> files2ignore = Arrays.asList("LICENSE", "README.md");
	public String extensionRegex = ".*\\.[a-zA-Z0-9]{3}$";

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody HashMap<String, Object> getDocuments() throws Exception {
		JSONObject res = new JSONObject();
		String sha = getHTML("https://api.github.com/repos/siteadmin/2015-Certification-C-CDA-Test-Data/branches/master")
				.getJSONObject("commit").get("sha").toString();
		JSONArray filesArray = getHTML("https://api.github.com/repos/siteadmin/2015-Certification-C-CDA-Test-Data/git/trees/" 
				+ sha + "?recursive=1").getJSONArray("tree");

		HashMap<String, Object> resultMap = new HashMap<>();
		
		for(int i=0; i < filesArray.length(); i++) {
			JSONObject file = filesArray.getJSONObject(i);
			if(!files2ignore.contains(file.get("path"))) {				
				// Get path array
				String[] path = file.get("path").toString().split("/");
				buildJson(resultMap, path);
			}

		}
//		return new JSONObject(resultMap);
		return resultMap;
	}
	
	public void buildJson(HashMap<String, Object> json, String[] path) {
		if(path.length == 1) {
			HashMap<String, Object> newObj = new HashMap<>();
			newObj.put("dirs", new ArrayList<HashMap<String, Object>>());
			newObj.put("files", new ArrayList<HashMap<String, Object>>());
			json.put(path[0], newObj);

		} else {
			HashMap<String, Object> current = (HashMap<String, Object>) json.get(path[0]);
			for(int i = 1 ; i < path.length ; i++) {
				String currentName = path[i];
				if(Pattern.matches(extensionRegex, currentName)) {
					HashMap<String, Object> newFile = new HashMap<>();
					newFile.put("name", currentName);
					newFile.put("link", getLink(path));
					List filesList = (List) current.get("files");
					filesList.add(newFile);
				} else {
					if(containsName((List<Map>) current.get("dirs"), currentName)) {
						List<Map> directories = (List<Map>) current.get("dirs");
						current = (HashMap<String, Object>) directories.get(getObjByName(directories, currentName));
					} else {
						HashMap<String, Object> newObj = new HashMap<>();
						newObj.put("name", currentName);
						newObj.put("dirs", new ArrayList<HashMap<String, Object>>());
						newObj.put("files", new ArrayList<HashMap<String, Object>>());
						List dirsList = (List) current.get("dirs");
						dirsList.add(newObj);
					}
				}
			}			
		}
	}
	
	public String getLink(String[] path) {
		String link = String.join("/", path).replace(" ", "%20");
		link = "https://raw.githubusercontent.com/siteadmin/2015-Certification-C-CDA-Test-Data/master/" + link;
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
	
	
}
