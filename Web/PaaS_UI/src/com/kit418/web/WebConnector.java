package com.kit418.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WebConnector {
	
	private final String USER_AGENT = "Mozilla/5.0";
	// HTTP GET request
	public Map<String,String> sendGet(String urlAddr) throws Exception {
		return sendGet(urlAddr,"");
	}
	
	public Map<String,String> sendGet(String urlAddr, String urlParameters) throws Exception {
		Map<String, String> rtnResult = new HashMap<String, String>();
		
		URL url = new URL(urlAddr+"?"+urlParameters);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setRequestMethod("GET");
		int status = con.getResponseCode();
		BufferedReader in = new BufferedReader(
				  new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
		    content.append(inputLine);
		}
		in.close();
				
		con.disconnect();
		
		rtnResult.put("ResponseCode", ""+status);
		rtnResult.put("ResponseMsg", content.toString());
		
		return rtnResult;
	}

	// HTTP POST request
	public Map<String,String> sendPost(String urlAddress, String urlParameters) throws Exception {
		Map<String, String> rtnResult = new HashMap<String, String>();
		
		URL obj = new URL(urlAddress);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		rtnResult.put("ResponseCode", ""+responseCode);
		rtnResult.put("ResponseMsg", response.toString());
		return rtnResult;
	}
}
