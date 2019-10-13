package com.kit418.ws;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;
import org.openstack4j.model.compute.Server;

import com.kit418.kernel.CloudControl;

/*Not Using Only For testing purpose*/
@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String UPLOAD_DIRECTORY="/home/ubuntu/uploads/";
	//private static final String  UPLOAD_DIRECTORY="/Users/theingiwin/upload/";
	private static String CLIENT_INSTANCE_NAME = "UbuntuWorkerNode";
	private CloudControl openstack;
	List<WorkerServer> workerServerList= new ArrayList<WorkerServer>();
	public UploadServlet() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String FileType = request.getParameter("FileType");
		String Time = request.getParameter("Time");
		String outputJSON = "";
		List<String> filesList = new ArrayList<String>();
		List<Part> fileParts = request.getParts().stream().filter(part -> "files".equals(part.getName())).collect(Collectors.toList());
		
	    /*for (Part filePart : fileParts) {
	        String fname = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
	        InputStream fileContent = filePart.getInputStream();
	        filesList.add(fname);
	        byte[] buffer = new byte[fileContent.available()];
		    fileContent.read(buffer);
		    File targetFile = new File(UPLOAD_DIRECTORY+ fname);
		    OutputStream outStream = new FileOutputStream(targetFile);
		    outStream.write(buffer);
	    }*/
	    
	    if(ServletFileUpload.isMultipartContent(request)){
            try {
            	
            	String fname = null;
            	String fsize = null;
            	String ftype = null;
                List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                for(FileItem item : multiparts){
                	InputStream fileContent =item.getInputStream();
                    fname = new File(item.getName()).getName();
                    filesList.add(fname);
                    byte[] buffer = new byte[fileContent.available()];
        		    fileContent.read(buffer);
        		    File targetFile = new File(UPLOAD_DIRECTORY+"/"+ fname);
        		    OutputStream outStream = new FileOutputStream(targetFile);
        		    outStream.write(buffer);
                }
            } catch (Exception ex) {
               request.setAttribute("message", "File Upload Failed due to " + ex);
            }   
         
        }else{
            request.setAttribute("message","Sorry this Servlet only handles file upload request");
        }
	    try {
			outputJSON = Upload(filesList, FileType,Time);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    response.setContentType("application/json");
		response.getWriter().append(outputJSON);
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
				openstack.runJar(javaFilePath,inputFilePath, CLIENT_INSTANCE_NAME,true);
				//return Output(javaFilePath,inputFilePath, CLIENT_INSTANCE_NAME);
			 }else {
				openstack.runJar(javaFilePath, CLIENT_INSTANCE_NAME,true); 
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
				 openstack.runJar(pyFilePath,inputFilePath, CLIENT_INSTANCE_NAME, true);
				 //return Output(pyFilePath,inputFilePath, CLIENT_INSTANCE_NAME);
			 }else {
				openstack.runJar(pyFilePath, CLIENT_INSTANCE_NAME,true); 
				 //return Output(pyFilePath,inputFilePath, CLIENT_INSTANCE_NAME);
			 }
		}break;	
		default : throw new Exception("Invalid File Type"); 
		}
		
		JSONObject jo = new JSONObject();
		jo.put("PassCode",PassCode);
		
		return jo.toJSONString();
		
	}
	private String Output(String FilePath,String inputFilePath,String CLIENT_INSTANCE_NAME) {
		JSONObject jo = new JSONObject();
		jo.put("FilePath",FilePath);
		jo.put("inputFilePath",inputFilePath);
		jo.put("CLIENT_INSTANCE_NAME",CLIENT_INSTANCE_NAME);
		return jo.toJSONString();
	}
	// TODO Auto-generated method stub
	}