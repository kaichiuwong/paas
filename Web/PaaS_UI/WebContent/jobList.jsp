<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.io.File" %>
<%@ page import="java.nio.file.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Date" %>
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
              <th class="th-sm export">Submission Time</th>
              <th class="th-sm export">Last Update Time</th>
              <th class="th-sm export">Status</th>
              <th class="th-sm export">Amount Billed</th>
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
			String infodata = ""; 
			String infoPath = "/home/ubuntu/info/" + ID + ".txt";
			String startTime = sdf.format(files[i].lastModified());
			String endTime = sdf.format(files[i].lastModified());
			String status = "DONE";
			double billamt = 0.0;
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
			billamt = (usageAmt / 1000 )*0.05 + 0.005;
		%>
        	<tr>
	                <td><% out.print(ID); %></td>
	                <td><% out.print(startTime); %></td>
	                <td><% out.print(endTime); %></td>
	                <td><% out.print(status); %></td>
	                <td><% out.print(String.format("$ %.2f", billamt)); %></td>
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
