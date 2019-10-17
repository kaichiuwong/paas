<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.io.File" %>
<%@ page import="java.nio.file.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.nio.file.Files" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.Date" %>
<%@include  file="header.html" %>
<%
String ID = request.getParameter("ID");
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
String infodata = ""; 
String infoPath = "/home/ubuntu/info/" + ID + ".txt";
String resultPath = "/home/ubuntu/output/" + ID + ".txt";
File resultFile = new File(resultPath);
String startTime = sdf.format(resultFile.lastModified());
String endTime = sdf.format(resultFile.lastModified());
String status = "DONE";
try {
	infodata = new String(Files.readAllBytes(Paths.get(infoPath))); 
}
catch (Exception e) {}

if (infodata != "") {
	String[] info = infodata.split(",");
	status= info[1];
	startTime= info[2];
	endTime= info[3];
}

Date startTimeVal = sdf.parse(startTime); 
Date endTimeVal = sdf.parse(endTime); 
//time usage in millisecond
double usageAmt = endTimeVal.getTime() - startTimeVal.getTime();
double billamt = (usageAmt / 1000 )*0.05 + 0.005;
%>
<!-- body here -->
<div class="container-flui">
  <div class="card mt-5">
    <div class="card-header">
      <h2>Output for <%= ID %></h2>
    </div>
    <div class="card-body">
<div class="form-group">
  <div class="form-group row">
    <label for="startTime" class="col-sm-2 col-form-label">Current Status</label>
    <div class="col-sm-10">
      <%=status %>
    </div>
  </div>
    <div class="form-group row">
    <label for="startTime" class="col-sm-2 col-form-label">Submission Time</label>
    <div class="col-sm-10">
      <%=startTime %>
    </div>
  </div>
    <div class="form-group row">
    <label for="startTime" class="col-sm-2 col-form-label">Finish Time</label>
    <div class="col-sm-10">
      <%=endTime %>
    </div>
  </div>
    <div class="form-group row">
    <label for="startTime" class="col-sm-2 col-form-label">Billed Amount</label>
    <div class="col-sm-10">
      <%=String.format("$ %.2f", billamt) %>
    </div>
  </div>
  <textarea class="form-control" id="programOuput" rows="7">
<%
String filecontent = "";
try {
	 filecontent =  new String ( Files.readAllBytes( Paths.get(resultPath) ) );
}
catch (IOException e)
{
   
}
out.print(filecontent);
%>
		</textarea>
		</div>
		<a href="jobList.jsp" class="btn btn-primary" role="button">Back</a>
    </div>
  </div>
</div>
<!-- footer here -->
<%@include  file="footer.html" %>
