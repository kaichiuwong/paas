<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="com.kit418.web.WebConnector" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.parser.*" %>
<%@ page import="java.util.Iterator" %>

<%@include file="header.html" %>    

<!-- body here -->
<div class="container-flui">
  <div class="card mt-5">
    <div class="card-header">
      <h2>Upload Program</h2>
    </div>
    <div class="card-body">
      <table id="dtBasicExample" class="table table-striped table-bordered" cellspacing="0" width="100%">
        <thead>
            <tr>
              <th class="th-sm export">Program Type</th>
              <th class="th-sm export">Expected time (Hours)</th>
              <th class="th-sm export">Upload File</th>
              <th class="th-sm export">Action</th>
            </tr>
        </thead>
          <tbody>
		    <tr>
		    <form action="../PaaS_WS/FileUploadServlet" method="post" enctype="multipart/form-data" onsubmit="return checkFile()">
 
              <th class="th-sm export">
              	<select class="form-control" id="programType" name="programTypeSelect" >
				  <option value="JAVA">JAVA</option>
				  <option value="Python">Python</option>
				</select>
			  </th>
			  <th><input type="text" id="timeExpected" name="timeExpectedBox" value="3" class="form-control" /></th>
              <th><input type="file" id="filePath" name="filePathChoose" onchange="getfolder(event)" webkitdirectory mozdirectory msdirectory odirectory directory multiple required /></th>
              <th><input type="submit" value="Upload" class="btn btn-primary"></th>
		    </form>
		    </tr>
		  </tbody>
      </table>
    </div>
  </div>
</div>
<script>
function checkFile() {
	var rtnResult = false;
	var selectedPrg = $( "#programType option:selected" ).text();
	var uploadFile = $("#filePath").val();
	var uploadFileExt = uploadFile.substr( (uploadFile.lastIndexOf('.') +1) ).toLowerCase();
	
	if (uploadFile) {
		switch (selectedPrg) {
			case "JAVA": if (uploadFileExt === "jar") { rtnResult= true; } break;
			case "Python":  if (uploadFileExt === "py") { rtnResult= true; } break;
			default: break;
		}
	}
	if (!rtnResult) {
		alert("Please select a correct file!");
	}
	return rtnResult;
}
</script>