package com.kit418.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.openstack4j.model.compute.Server;

import com.kit418.kernel.CloudControl;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;


import java.io.File;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;


/**
 * Servlet implementation class main
 */
@WebServlet("/FileUploadServlet")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
	private static final String UPLOAD_DIRECTORY="/home/ubuntu/uploads/";
	//private static final String  UPLOAD_DIRECTORY="/Users/theingiwin/upload/";
	private static String CLIENT_INSTANCE_NAME = "UbuntuWorkerNode";
	private CloudControl openstack;
	List<WorkerServer> workerServerList= new ArrayList<WorkerServer>();
	
    /**
     * Default constructor. 
     */
    public FileUploadServlet() {
    	workerServerList = new ArrayList<WorkerServer>();
    	if(openstack == null) {
    		openstack = new CloudControl();
    	}
		List<?> svrlist = openstack.ListWorkers();
		 for (Object svr : svrlist) {
		   Server instance = (Server) svr;
		   workerServerList.add(new WorkerServer(instance));
		 }
    }
    

	private void getServer() {
		WorkerServer selectedServer = workerServerList.stream().filter(u -> u.getIsBusy() == false ).findFirst().get();
		workerServerList.remove(selectedServer);
		selectedServer.setBusy(true);
		workerServerList.add(selectedServer);
		CLIENT_INSTANCE_NAME = selectedServer.getServer().getName();
	}
	
	private void setAvailableServer(String serverName) {
		WorkerServer selectedServer = workerServerList.stream().filter(u -> u.getServer().getName() == serverName ).findFirst().get();
		if(selectedServer != null) {
			workerServerList.remove(selectedServer);
			selectedServer.setBusy(false);
			workerServerList.add(selectedServer);
		}
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String programTypeSelectString = request.getParameter("programTypeSelect");
	    String timeExpectedString = request.getParameter("timeExpectedBox");
	    Part fileParttt = request.getPart("filePathChoose");
	    List<Part> fileParts = request.getParts().stream().filter(part -> "filePathChoose".equals(part.getName())).collect(Collectors.toList());
		
	    List<String> filesList = new ArrayList<String>();
	    String userPassCode = "userPassCode123"; //

		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		request.setAttribute("errorMessage", "");
		for (Part filePart : fileParts) {
	        String fname = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
	        InputStream fileContent = filePart.getInputStream();
	        filesList.add(fname);
	        byte[] buffer = new byte[fileContent.available()];
		    fileContent.read(buffer);
		    File targetFile = new File(UPLOAD_DIRECTORY+ fname);
		    OutputStream outStream = new FileOutputStream(targetFile);
		    outStream.write(buffer);
	    }
		try {
			userPassCode = Upload(filesList, programTypeSelectString,timeExpectedString);
			if(userPassCode == "") {
				userPassCode ="userPassCode123";
			}
			
			writer.println("<!DOCTYPE html>"
					+"				<html>"
					+"				<head>"
					+"				    <title>PaaS Control Panel</title>"
					+"				    <meta charset=\"utf-8\">"
					+"				    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">"
					+"				    <script src=\"https://code.jquery.com/jquery-3.3.1.js\"></script>"
					+"				    <script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js\"></script>"
					+"				    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.1.3/css/bootstrap.css\" />"
					+"				</head>"
					+"				<body class=\"bg-light\">"
					+"				<!-- Navigator start -->"
					+"				    <nav class=\"navbar navbar-expand-lg navbar-dark bg-dark\">"
					+"				      <a class=\"navbar-brand\" href=\"./index.jsp\">PaaS</a>"
					+"				      <button class=\"navbar-toggler\" type=\"button\" data-toggle=\"collapse\" data-target=\"#navbarNavDropdown\" aria-controls=\"navbarNavDropdown\" aria-expanded=\"false\" aria-label=\"Toggle navigation\">"
					+"				        <span class=\"navbar-toggler-icon\"></span>"
					+"				      </button>"
					+"				      <div class=\"collapse navbar-collapse\" id=\"navbarNavDropdown\">"
					+"				        <ul class=\"navbar-nav mr-auto\">"
					+"				          <li class=\"nav-item dropdown active\">"
					+"				            <a class=\"nav-link dropdown-toogle\" href=\"#\" id=\"navBarDropdownMenuLink\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">"
					+"				            	Master<span class=\"sr-only\">(current)</span>"
					+"				            </a>"
					+"				            <div class=\"dropdown-menu\" aria-labelledby=\"navBarDropdownMenuLink\">"
					+"				            	<a class=\"dropdown-item\" href=\"../PaaS_UI/index.jsp\">Server List</a>"
					+"				            </div>"
					+"				          </li>"
					+"				          <li class=\"nav-item dropdown\">"
					+"				            <a class=\"nav-link dropdown-toogle\" href=\"#\" id=\"navBarDropdownMenuLink\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">"
					+"				            	Platform"
					+"				            </a>"
					+"				            <div class=\"dropdown-menu\" aria-labelledby=\"navBarDropdownMenuLink\">"
					+"				            	<a class=\"dropdown-item\" href=\"../PaaS_UI/uploadProgram.jsp\">Upload Program</a>"
					+"				            	<a class=\"dropdown-item\" href=\"../PaaS_UI/checkStatus.jsp\">Check Status</a>"
					+"				            	<div class=\"dropdown-divider\"></div>"
					+"				            	<a class=\"dropdown-item\" href=\"../PaaS_UI/programResult.jsp\">Execution Result</a>"
					+"				            	<a class=\"dropdown-item\" href=\"../PaaS_UI/downloadBill.jsp\">Download Bill</a>"
					+"				            </div>"
					+"				          </li>"
					+"				        </ul>"
					+"				      </div>"
					+"				    </nav>"
					+"					<div class=\"container-flui\">"
					+"					  <div class=\"card mt-5\">"
					+"					    <div class=\"card-header\">"
					+"					      <h2>Upload Program</h2>"
					+"					    </div>"
					+"					    <div class=\"card-body\">"
					+"					      <table class=\"table table-striped table-bordered\" cellspacing=\"0\" width=\"100%\">"
					+"					        <thead>"
					+"					            <tr>"
					+"					              <th>Message</th>"
					+"					              <th>User Passcode</th>"
					+"					              <th>Action</th>"
					+"					            </tr>"
					+"					        </thead>"
					+"					          <tbody>"
					+"							    <tr>"
					+"							    <form action=\"\" method=\"post\">"
					+"								  <th><p id=\"message\">Request Accepted</p></th>"
					+"					              <th><input style=\"color:red\" type=\"text\" id=\"passcode\" name=\"passcodeShowBox\" class=\"form-control\" value=\""+userPassCode+"\" readonly/></th>"
					+"					              <th><input id=\"copybutton\" type=\"button\" value=\"Copy\" onclick=\"copyFunction();\" class=\"btn btn-light\" role=\"button\" ></th>"
					+"							    </form>"
					+"							    </tr>"
					+"							  </tbody>"
					+"					      </table>"
					+"					      <a href=\"../PaaS_UI/checkStatus.jsp\" id=\"submitAgain\" class=\"btn btn-primary\" role=\"button\" aria-pressed=\"true\">Check Status</a>"
					+"					      <a href=\"../PaaS_UI/uploadProgram.jsp\" id=\"uploadAnother\" class=\"btn btn-outline-primary\" role=\"button\" aria-pressed=\"true\">Upload Another</a>"
					+"					    </div>"
					+"					  </div>"
					+"					</div>"
					+"					<script type=\"text/javascript\">"
					+"					function copyFunction() {"
					+"					  document.getElementById(\"test\").style.display = \"\";"
					+"					  var copyText = document.getElementById(\"passcode\");"
					+"					  copyText.select();"
					+"					  copyText.setSelectionRange(0, 99999)"
					+"					  document.execCommand(\"copy\");"
					+"					  alert(\"Copied the passcode: \" + copyText.value);"
					+"					}"
					+"					</script>"
					//+"					<p id=\"test\" style=\"color:red\">11></p>"
					+"				  </body>"
					+"				</html>");
							
		} catch (Exception e) {
			// TODO Auto-generated catch block
			writer.println("<script type='text/javascript'>");
			writer.println("alert(" + "'" + e.getMessage() + "'" + ");</script>"
					+"					      <a href=\"../PaaS_UI/uploadProgram.jsp\" id=\"uploadAgain\" class=\"btn btn-outline-primary\" role=\"button\" aria-pressed=\"true\">Upload Again</a>");
			writer.println("</head><body></body></html>");
			
		}finally {
			writer.close();
		}
	}
	
	private String Upload(List<String> fileList,String FileType,String Time) throws Exception {
		String PassCode="";
		if(fileList.size() == 0)
			throw new Exception("InputFileLoation doesn't allow empty.");
		if(FileType == "") throw new Exception("File Type is require.");
		switch(FileType.toUpperCase()) {
		case "JAVA":{
			 String javaFilePath ="";
			 String inputFilePath = "";
			 for (String s: fileList) {
				    if (s.toLowerCase().endsWith(".jar")) {
				    	javaFilePath = UPLOAD_DIRECTORY+"/"+ s;
				    }else {
				    	inputFilePath = UPLOAD_DIRECTORY+"/"+ s;
				    }
			  }
			 if(javaFilePath == "")
				 throw new Exception(".jar file is missing.Please select jar file. "); 
			 getServer();
			 if(inputFilePath != "") {
				 PassCode= openstack.runJar(javaFilePath,inputFilePath, CLIENT_INSTANCE_NAME,true);
				 setAvailableServer(CLIENT_INSTANCE_NAME);
				//return Output(javaFilePath,inputFilePath, CLIENT_INSTANCE_NAME);
			 }else {
				 PassCode= openstack.runJar(javaFilePath, CLIENT_INSTANCE_NAME,true);
				 setAvailableServer(CLIENT_INSTANCE_NAME);
				 //return Output(javaFilePath,inputFilePath, CLIENT_INSTANCE_NAME);
			 }
		}break;	
		case "PYTHON":{
			 String pyFilePath ="";
			 String inputFilePath = "";
			 for (String s: fileList) {
				    if (s.toLowerCase().endsWith(".py")) {
				    	pyFilePath = UPLOAD_DIRECTORY+"/"+ s;
				    }else {
				    	inputFilePath = UPLOAD_DIRECTORY+"/"+ s;
				    }
			  }
			 if(pyFilePath == "")
				 throw new Exception(".py file is missing.Please select py file. "); 
			 getServer();
			 if(inputFilePath != "") {
				 PassCode= openstack.runJar(pyFilePath,inputFilePath, CLIENT_INSTANCE_NAME, true);
				 setAvailableServer(CLIENT_INSTANCE_NAME);
				 //return Output(pyFilePath,inputFilePath, CLIENT_INSTANCE_NAME);
			 }else {
				 PassCode= openstack.runJar(pyFilePath, CLIENT_INSTANCE_NAME,true);
				 setAvailableServer(CLIENT_INSTANCE_NAME);
				 //return Output(pyFilePath,inputFilePath, CLIENT_INSTANCE_NAME);
			 }
		}break;	
		default : throw new Exception("Invalid File Type"); 
		}
		
		JSONObject jo = new JSONObject();
		jo.put("PassCode",PassCode);
		
		return jo.toJSONString();
		//return PassCode;
		
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
