package com.automation.selenium;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.automation.config.TestConfiguration;
import com.automation.utils.ExecuteShellCommand;

public class BrowserStack {
	
	static String login = TestConfiguration.getBrowserStackConfig().getString("LOGIN");

	public static void main(String[] args) throws ParseException {		
		System.out.println(getPublicUrl("c240476_check_same_name_for_project_name"));		
	}
	
	public static String getJsonValue(String curlResult, String key1, String key2) throws ParseException {
		JSONArray resultArray = (JSONArray) new JSONParser().parse(curlResult);
		JSONObject json1;
		if (System.getenv("BUILD_TAG") != null) {
			json1 = (JSONObject) resultArray.get(0);
		} else {
			json1 = (JSONObject) resultArray.get(1);
		}
		JSONObject json2 = (JSONObject) json1.get(key1);
		String value = json2.get(key2).toString();
		return value;
	}
	
	public static String getPublicUrl(String testCase) throws ParseException {
		
		ExecuteShellCommand es = new ExecuteShellCommand();
		String builds = es.executeCommand("curl --user "+login+" https://www.browserstack.com/automate/builds.json");
		String buildId = getJsonValue(builds, "automation_build", "hashed_id");
		
		String sessions = es.executeCommand("curl --user "+login+" https://www.browserstack.com/automate/builds/"+buildId+"/sessions.json");
		
		JSONArray resultArray = (JSONArray) new JSONParser().parse(sessions);		
		String public_url = null;
		
		for (int i = 0; i < resultArray.size(); i++)
		{
			JSONObject json = (JSONObject) resultArray.get(i);
			
			if (json.toString().contains(testCase)) {
				JSONObject json2 = (JSONObject) json.get("automation_session");
				public_url = json2.get("public_url").toString();
			}
		}		
		return public_url;
	}
}
