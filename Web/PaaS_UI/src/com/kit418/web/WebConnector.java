package com.kit418.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class WebConnector {
	private final String USER_AGENT = "Mozilla/5.0";

	// HTTP GET request
	public Map<String,String> sendGet(String url) throws Exception {
		Map<String, String> rtnResult = new HashMap<String, String>();
		
		@SuppressWarnings("deprecation")
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);

		// add request header
		request.addHeader("User-Agent", USER_AGENT);

		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(
                       new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		rtnResult.put("ResponseCode", ""+response.getStatusLine().getStatusCode());
		rtnResult.put("ResponseMsg", result.toString());
		
		return rtnResult;
	}

	// HTTP POST request
	public Map<String,String> sendPost(String url, List<NameValuePair> urlParameters) throws Exception {
		Map<String, String> rtnResult = new HashMap<String, String>();

		@SuppressWarnings("deprecation")
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);

		// add header
		post.setHeader("User-Agent", USER_AGENT);

		/*
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
		urlParameters.add(new BasicNameValuePair("cn", ""));
		urlParameters.add(new BasicNameValuePair("locale", ""));
		urlParameters.add(new BasicNameValuePair("caller", ""));
		urlParameters.add(new BasicNameValuePair("num", "12345"));
		*/
		post.setEntity(new UrlEncodedFormEntity(urlParameters));

		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		rtnResult.put("ResponseCode", ""+response.getStatusLine().getStatusCode());
		rtnResult.put("ResponseMsg", result.toString());
		return rtnResult;

	}
}
