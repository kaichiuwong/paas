<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="org.openstack4j.model.compute.Server"%>
<%@ page import="com.kit418.kernel.CloudControl" %>
<%@ page import="java.util.List" %>
<%
CloudControl openstack = new CloudControl();
List<?> svrlist = openstack.ListServers();
%>
<%@include  file="header.html" %>    
<!-- body here -->
<div class="container-flui">
  <div class="card mt-5">
    <div class="card-header">
      <h2>Server List</h2>
    </div>
    <div class="card-body">
      <table id="dtBasicExample" class="table table-striped table-bordered" cellspacing="0" width="100%">
        <thead>
            <tr>
              <th class="th-sm export">ID</th>
              <th class="th-sm export">Server Name</th>
              <th class="th-sm export">IP Address</th>
              <th class="th-sm export">Status</th>
              <th class="th-sm export">Action</th>
            </tr>
        </thead>
        <tbody>
        	<% for (Object svr : svrlist) { 
        	   Server instance = (Server) svr;
        	 %>
	              <tr>
	                <td><% out.print(instance.getId()); %></td>
	                <td><% out.print(instance.getName()); %></td>
	                <td><% out.print(instance.getAccessIPv4()); %></td>
	                <td><% out.print(instance.getStatus()); %></td>
	                <% if (instance.getName().contains("master")) { %>
	                	<td></td>
	                <% } else { %>
		                <% if (instance.getStatus().toString().equals("ACTIVE"))  { %>
		                	<td><a href="#" class="btn btn-danger" role="button">Stop Server</a></td>
		                <% } else { %>
		                	<td><a href="#" class="btn btn-success" role="button">Start Server</a></td>
		                <% } %>
		            <% } %>
	              </tr>
			<%} %>
        </tbody>
      </table>
    </div>
  </div>
</div>
<!-- footer here -->
<%@include  file="footer.html" %>
