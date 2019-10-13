package com.kit418.ws;

import java.io.IOException;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openstack4j.model.compute.Server;
import com.kit418.kernel.CloudControl;

/*Not Using Only For testing purpose*/

/**
 * Servlet implementation class main
 */
@WebServlet("/InstanceControl2Fortest")
public class InstanceControl2Fortest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String INSTANCE_NAME = "UbuntuTest";
	private CloudControl openstack;

    /**
     * Default constructor. 
     */
    public InstanceControl2Fortest() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		openstack = new CloudControl();
		String action = request.getParameter("action");
		String passcode = request.getParameter("passcode");
		String outputJSON = "";
		
		if ( action == null ) {
			action = "";
		}
		
		switch (action) {
			case "EnquireStatus": 
				outputJSON = enquireStatus(passcode); 
				break;
			case "DownloadBill": 
				outputJSON = downloadBill(); 
				break;
			case "CancelJob": 
				outputJSON = cancelJob(); 
				break;
			default: 
				outputJSON = rtnPassCode();
				break;
		}
		response.setContentType("application/json");
		response.getWriter().append(outputJSON);
	}
	
	private String enquireStatus(String passc) {
		String resultE = "";
		JSONObject pcJson = new JSONObject();
		
		if (passc.equals("0000")) {	
			pcJson.put("status", "inprogress");
			
		} else if (passc.equals("1111")){
			pcJson.put("status", "completed");
			pcJson.put("file", "Hello, world!");
			
		} else {
			pcJson.put("status", "wrong_Passcode");
			
		}
		resultE = pcJson.toJSONString();
		return resultE;
	}
	
	private String downloadBill() {
		String resultD = "";
		
		JSONObject pcJson = new JSONObject();
		pcJson.put("price", "1000");
		pcJson.put("description", "complete in 10s, so it's $1000");

		resultD = pcJson.toJSONString();
		return resultD;
	}
	
	private String cancelJob() {
		String resultC = "";
		
		JSONObject pcJson = new JSONObject();
		pcJson.put("result", "Cancelled Successfully");
		pcJson.put("errorMessage", "none");

		resultC = pcJson.toJSONString();
		return resultC;
	}
	
	private String rtnPassCode() {
		String resultP = "";
		
		JSONObject pcJson = new JSONObject();
		pcJson.put("passCode", "1234ppp");
		pcJson.put("location", "aaa/aaa/");

		resultP = pcJson.toJSONString();
		return resultP;
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
