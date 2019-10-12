package com.kit418.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openstack4j.model.compute.Server;
import com.kit418.kernel.CloudControl;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;



/**
 * Servlet implementation class main
 */
@WebServlet("/InstanceControl")
public class InstanceControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String INSTANCE_NAME = "UbuntuTest";
	private CloudControl openstack;
	private static final String  UPLOAD_DIRECTORY="/Users/theingiwin/upload/";
    /**
     * Default constructor. 
     */
    public InstanceControl() {
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(openstack == null) {
			openstack = new CloudControl();
    	}
		String action = request.getParameter("action");
		
		String outputJSON = "";
		
		if ( action == null ) {
			action = "";
		}else {
			action = action.toLowerCase();
		}
		
		switch (action) {
			case "create": 
			{
				String svrid = request.getParameter("id");
				outputJSON = createWorker(svrid); 
				
			}
			break;
			case "remove":
			{
				String svrid = request.getParameter("id");
				outputJSON = removeWorker(svrid); 
			}
				break;
				
			case "enquirestatus": 
			{
				String workerId = request.getParameter("passcode");
				
				try {
					outputJSON = enquireStatus(workerId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}break;
			case "downloadoutputfile": 
			{
				String workerId = request.getParameter("passcode");
				try {
					outputJSON = getOutputFile(workerId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}break;
			case "downloadbill": 
			{
				String workerId = request.getParameter("passcode");
				try {
					outputJSON = generateBill(workerId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}break;
			case "canceljob": 
			{
				String workerId = request.getParameter("passcode");
				try {
					outputJSON = generateBill(workerId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}break;
			default: 
				outputJSON = listServer();
				break;
		}
		response.setContentType("application/json");
		response.getWriter().append(outputJSON);
	}
	
	
	
	private String enquireStatus(String workerID) {
		String result = "";
		try {
			JSONObject svrObject = new JSONObject();
			String Status = openstack.getWorkerStatus(workerID);
			Status = Status != "" ? Status.toUpperCase() : Status;
			String ErrorMessage ="System can't proceed with this request.Please contact system administrator.";
			switch(Status) {
				case "INIT":
				case "RUNNING":
				{
					svrObject.put("status", "inprogress");
					result = svrObject.toJSONString();
				}break;
				case "ERROR":
				{
					svrObject.put("status",ErrorMessage);
					result = svrObject.toJSONString();
				}break;
				case "DONE":
					{
						svrObject.put("status", "completed");
						String FileContent = openstack.getOutputFile(workerID);
						svrObject.put("file", FileContent);
						result = svrObject.toJSONString();
					}break;
			}
		
		}catch(Exception e) {
			
		}
		return result;
	}
	
	private String getOutputFile(String workerID) {
		JSONObject svrObject = new JSONObject();
		String FileContent = openstack.getOutputFile(workerID);
		svrObject.put("Files", FileContent);
		return svrObject.toJSONString();
	}
	
	private String generateBill(String workerID) {
		Date startTime = openstack.getWorkerStartTime(workerID);
		Date endTime = openstack.getWorkerEndTime(workerID);
		JSONObject svrObject = new JSONObject();
		
		/*Calendar calendar = Calendar.getInstance();
	    Date  startTime=calendar.getTime();
	    calendar.add(Calendar.MINUTE, 5);
	    Date endTime =calendar.getTime();*/
		
		if(startTime == null || endTime == null)
		{
			svrObject.put("Error", String.format("Invalid pass code.%s", workerID));
			
		}else {
			long Duration = Math.abs(endTime.getTime() - startTime.getTime());
			int seconds = (int) Duration/1000;
			int unitPrice =  5;
			int totalPrice = seconds * unitPrice; //5$ per second;
			String Description = String.format("Request started : %s \n Request ended : %s \n Unit Price: %d", startTime, endTime, unitPrice);
			svrObject.put("price", totalPrice);
			svrObject.put("description", Description);
		}
		return svrObject.toJSONString();
	}
	
	private String createWorker(String svrName) {
		String result = "";
		//openstack.createServer(svrName);
		return result;
	}
	
	private String removeWorker(String svrName) {
		String result = "";
		//openstack.deleteServer(svrName);
		return result;
	}
	
	private String listServer() {
		String result = "";
		List<?> svrlist = openstack.ListServers();
		JSONObject jo = new JSONObject();

	    JSONArray svrresult = new JSONArray();
	    
	    for (Object svr : svrlist) {
	    	Server instance = (Server) svr;
	    	JSONObject svrObject = new JSONObject();
	    	svrObject.put("id", instance.getId());
	    	svrObject.put("name", instance.getName());
	    	svrObject.put("ipv4", instance.getAccessIPv4());
	    	svrObject.put("status", instance.getStatus().toString());
	    	svrresult.add(svrObject);
	    }
	    
	    jo.put("ServerList", svrresult);
	    result = jo.toJSONString();
		return result;
		
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
