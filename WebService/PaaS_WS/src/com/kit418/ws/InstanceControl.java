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
import com.kit418.CloudControl;
/**
 * Servlet implementation class main
 */
@WebServlet("/InstanceControl")
public class InstanceControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String INSTANCE_NAME = "UbuntuTest";
	private CloudControl openstack;

    /**
     * Default constructor. 
     */
    public InstanceControl() {
    	openstack = new CloudControl();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		String svrid = request.getParameter("id");
		String outputJSON = "";
		
		if ( action == null ) {
			action = "";
		}
		
		switch (action) {
			case "create": 
				outputJSON = createWorker(svrid); 
				break;
			case "remove": 
				outputJSON = removeWorker(svrid); 
				break;
			default: 
				outputJSON = listServer();
				break;
		}
    
		response.setContentType("application/json");
		response.getWriter().append(outputJSON);
	}
	
	private String createWorker(String svrName) {
		String result = "";
		openstack.createServer(svrName);
		return result;
	}
	
	private String removeWorker(String svrName) {
		String result = "";
		openstack.deleteServer(svrName);
		return result;
	}
	
	private String listServer() {
		String result = "";
		List<?> svrlist = openstack.ListServers();

	    JSONArray svrresult = new JSONArray();
	    
	    for (Object svr : svrlist) {
	    	Server instance = (Server) svr;
	    	JSONObject svrObject = new JSONObject();
	    	svrObject.put("id", instance.getId());
	    	svrObject.put("name", instance.getName());
	    	svrObject.put("ipv4", instance.getAccessIPv4());
	    	svrObject.put("status", instance.getStatus());
	    	svrresult.add(svrObject);
	    }
	    
	    result = svrresult.toJSONString();
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
