package gov.nist.healthcare.ttt.webapp.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetCCDAFolderTest {

	public static List<String> files2ignore = Arrays.asList("LICENSE", "README.md");
	public static String extensionRegex = ".*\\.[a-zA-Z0-9]{3}$";

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		JSONObject res = new JSONObject();
		HashMap<String, Object> json = new HashMap<>();
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
//				System.out.println(String.join("/", path));
				//				for(String dir : path) {
				//					if(Pattern.matches(extensionRegex, dir)) {
				//						System.out.println("File!! " + dir);
				//					}
				//				}
//								json = buildJson2(new HashMap<>(), path, false, true);
//								resultMap = deepMerge(resultMap, json);
//								System.out.println(new JSONObject(json).toString(2));
				buildJson3(resultMap, path);
			}

		}
//		String[] test = {"test", "retest", "file.txt"};
//		json = buildJson2(json, test, false, true);
		System.out.println(new JSONObject(resultMap).toString(2));
		PrintWriter out = new PrintWriter("filename.txt");
		out.println(new JSONObject(resultMap).toString(2));

		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Running time: " + totalTime/1000.0 + "s");

		// Try download
		//		URL website = new URL("https://raw.githubusercontent.com/siteadmin/2015-Certification-C-CDA-Test-Data/master/Receiver%20SUT%20Test%20Data/170.315_b1_ToC_Amb/170.315_b1_toc_amb_ccd_r11_sample1_v1.xml");
		//		InputStream strm = website.openStream();
		//		System.out.println(IOUtils.toString(strm));
	}

	public static HashMap<String, Object> buildJson(HashMap<String, Object> parent, String[] path) {
		if(path.length == 1) {
			if(Pattern.matches(extensionRegex, path[0])) {
				return createFile(path[0]);
			} else {
				return createDir(path[0]);
			}
		} else {
			HashMap<String, Object> nextParent = createDir(path[0]);
			parent.putAll(buildJson(nextParent, Arrays.copyOfRange(path, 1, path.length)));
			return parent;
		}
	}
	
	public static void buildJson3(HashMap<String, Object> json, String[] path) {
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
	
	public static String getLink(String[] path) {
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

	public static HashMap<String, Object> buildJson2(HashMap<String, Object> child, String[] path, boolean isFile, boolean isFirst) {
		if(path.length > 0) {
			String current = path[path.length - 1];
			HashMap<String, Object> res = new HashMap<>();
			res.put("name", current);
			if(Pattern.matches(extensionRegex, current)) {
				return buildJson2(res, Arrays.copyOf(path, path.length-1), true, false);
			} else {
				List childs = new ArrayList<>();
				if(!child.isEmpty()) {
					childs.add(child);
				}
				if(isFile) {
					res.put("files", childs);
				} else {
					res.put("dirs", childs);
				}
				return buildJson2(res, Arrays.copyOf(path, path.length-1), false, false);
			}
		} else {
			return child;
		}
	}

	public static HashMap<String, Object> createFile(String filename) {
		HashMap<String, Object> res = new HashMap<>();
		HashMap<String, String> fileHash = new HashMap<>();
		fileHash.put("name", filename);
		List filesList = new ArrayList<>();
		filesList.add(fileHash);
		res.put("files", filesList);
		return res;
	}

	public static HashMap<String, Object> createDir(String dirname) {
		HashMap<String, Object> res = new HashMap<>();
		HashMap<String, String> fileHash = new HashMap<>();
		fileHash.put("name", dirname);
		List dirsList = new ArrayList<>();
		dirsList.add(fileHash);
		res.put("dirs", dirsList);
		return res;
	}

	private static Map deepMerge(Map original, Map newMap) {
        for (Object key : newMap.keySet()) {
            if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                Map originalChild = (Map) original.get(key);
                Map newChild = (Map) newMap.get(key);
                original.put(key, deepMerge(originalChild, newChild));
            } else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
                List originalChild = (List) original.get(key);
                List newChild = (List) newMap.get(key);
                for (Object each : newChild) {
                	if(originalChild.contains(each)) {
                		System.out.println("test");
                	} else {
                		originalChild.add(each);
                	}
                }
            } else {
                original.put(key, newMap.get(key));
            }
        }
        return original;
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
