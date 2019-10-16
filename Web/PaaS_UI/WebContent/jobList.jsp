<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.io.File" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.parser.*" %>
<%@ page import="java.util.Iterator" %>
<%@include  file="header.html" %>    
<!-- body here -->
<div class="container-flui">
  <div class="card mt-5">
    <div class="card-header">
      <h2>Job List</h2>
    </div>
    <div class="card-body">
      <table id="dtBasicExample" class="table table-striped table-bordered" cellspacing="0" width="100%">
        <thead>
            <tr>
              <th class="th-sm export">ID</th>
              <th class="th-sm export">Last Update Time</th>
              <th class="th-sm export">Status</th>
              <th class="th-sm export">Action</th>
            </tr>
        </thead>
        <tbody>
        <%
        File dir = new File("/home/ubuntu/output/");
        File[] files = dir.listFiles();
        Arrays.sort(files, Collections.reverseOrder());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (int i = 0; i < files.length; i++) {
			String ID = files[i].getName().split("\\.(?=[^\\.]+$)")[0];
		%>
        	<tr>
	                <td><% out.print(ID); %></td>
	                <td><% out.print(sdf.format(files[i].lastModified())); %></td>
	                <td>DONE</td>
	                <td><a href="getResult.jsp?ID=<% out.print(ID); %>" class="btn btn-primary" role="button">Check Result</a></td>
	        </tr>
	       <%
	       	}
           %>
        </tbody>
     
      </table>
    </div>
  </div>
</div>
<!-- footer here -->
<%@include  file="footer.html" %>
