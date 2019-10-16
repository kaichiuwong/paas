<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.nio.file.Paths" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.nio.file.Files" %>
<%@ page import="java.io.IOException" %>
<%@include  file="header.html" %>    
<!-- body here -->
<div class="container-flui">
  <div class="card mt-5">
    <div class="card-header">
      <h2>Output for <%= request.getParameter("ID") %></h2>
    </div>
    <div class="card-body">
<div class="form-group">
  <textarea class="form-control" id="programOuput" rows="7">
<%
String fileName = "/home/ubuntu/output/"+request.getParameter("ID")+".txt";
String filecontent = "";
try {
	 filecontent =  new String ( Files.readAllBytes( Paths.get(fileName) ) );
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
