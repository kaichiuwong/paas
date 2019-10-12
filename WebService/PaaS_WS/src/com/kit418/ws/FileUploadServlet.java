package com.kit418.ws;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet implementation class main
 */
@WebServlet("/FileUploadServlet")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
    /**
     * Default constructor. 
     */
    public FileUploadServlet() {
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String programTypeSelectString = request.getParameter("programTypeSelect");
	    String timeExpectedString = request.getParameter("timeExpectedBox");
	    Part filePart = request.getPart("filePathChoose");
	    
	    String userPassCode = "userPassCode123"; //

		PrintWriter writer = response.getWriter();
		response.setContentType("text/html");
		
//		// For test
//		writer.println("programTypeSelectString: "+programTypeSelectString);
//		writer.println(", timeExpectedString: "+timeExpectedString);
//		writer.println(", filePart: "+filePart);
		
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
		writer.close();
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
