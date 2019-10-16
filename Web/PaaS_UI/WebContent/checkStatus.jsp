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
      <h2>Check Status</h2>
    </div>
    <div class="card-body">
      <table class="table table-striped table-bordered" cellspacing="0" width="100%">
        <thead>
            <tr>
              <th>User Passcode</th>
              <th>Action</th>
            </tr>
        </thead>
          <tbody>
		    <tr>
			  <th><input type="text" id="userPasscode" name="userPasscodeBox" value="20191006_065717.189_0000001" class="form-control" /></th>
              <th><input type="button" id="submitPasscode" value="Submit" onclick="getFromServer();" class="btn btn-primary" role="button"></th>
		    </tr>
		  </tbody>
      </table>
	  <div id="errorAlert" style="display:none" class="alert alert-danger" role="alert"></div>
      <table style="display:none" id="passCodeSubmitted" class="table table-striped table-bordered" cellspacing="0" width="100%">
        <thead>
            <tr>
              <th>Status</th>
              <th>Action</th>
            </tr>
        </thead>
          <tbody>
		    <tr id="InProgressBar" style="display:none">
			  <th><button type="button" class="btn btn-warning" disabled>In progress</button></th>
              <th>
	   			<div class="input-group mb-3">
				  <div class="input-group-prepend">
				    <button id="cancelButton" class="btn btn-outline-danger" type="button" onclick="toCancelJob();" >Cancel this job</button>
				  </div>
				  <input id="cancelResult" type="text" class="form-control" placeholder="" aria-label="" aria-describedby="basic-addon1" disabled>
				</div>
		   	   </th>
		    </tr>
		    
		    <tr id="CompletedBar" style="display:none">
			  <th><button type="button" class="btn btn-success" disabled>Completed</button></th>
              <th>
					<div class="input-group mb-3">
					  <div class="input-group-prepend">
					    <button class="btn btn-secondary" type="button" disabled>Program Output</button>
					  </div>
					  <input id="fileLocation" type="text" class="form-control" placeholder="" aria-label="" aria-describedby="basic-addon1" disabled>
					</div>
	              <input  id="copybutton" type="button" value="Download Bill" onclick="billtoTxtFile()" class="btn btn-outline-info" role="button">
              </th>
		    </tr>
		  </tbody>
		  <!-- For test -->
		  <div id="photo_snapshot2"></div>
		  <input style="display:none" type="text" id="test" name="testBox" value="" class="form-control" />
		  <!-- For test -->
      </table>
      <a href="./checkStatus.jsp" style="display:none" id="submitAgain" class="btn btn-outline-primary" role="button" aria-pressed="true">Submit Again</a>
    </div>
  </div>
</div>
  
<script type="text/javascript">
var billFileName;
var billContent;

function getFromServer()
{
	var id = $('#userPasscode').val();
	window.location.href = "./getResult.jsp?ID=" + id;
}

function billtoTxtFile() {
	download(billFileName, billContent);
}

function download(filename, text) {
	  var element = document.createElement('a');
	  element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
	  element.setAttribute('download', filename);

	  element.style.display = 'none';
	  document.body.appendChild(element);

	  element.click();
	  document.body.removeChild(element);
	}
	
function toCancelJob()
{
  var xmlhttp_cancel;
  if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
    xmlhttp_cancel=new XMLHttpRequest();
  }
  else
  {// code for IE6, IE5
   xmlhttp_cancel=new ActiveXObject("Microsoft.XMLHTTP");
  }
 
  xmlhttp_cancel.open("POST","gethttp.jsp",true);
  xmlhttp_cancel.setRequestHeader("Content-type","application/x-www-form-urlencoded");
  xmlhttp_cancel.send("userPasscodeValue="+document.getElementById("userPasscode").value
  				+"&testComplete=XXX"+"&sendAction=cancelJob");
  
  xmlhttp_cancel.onreadystatechange=function()
  {
   if (xmlhttp_cancel.readyState==4 && xmlhttp_cancel.status==200)
   {
     var res_cancel = xmlhttp_cancel.responseText;
     var cancelResult = res_cancel.split(";")[0]; 
     var cancelErrorMessage = res_cancel.split(";")[1];

     document.getElementById("cancelResult").value = cancelResult+" | "+cancelErrorMessage;
     document.getElementById("cancelButton").disabled = true;
     
     // For test
     //document.getElementById("photo_snapshot2").style.display = "none";
     //document.getElementById("test").style.display = "none";
     
   } else {
     // For test
     //document.getElementById("test").value="[Error] xmlhttp.readyState: "+xmlhttp.readyState +", xmlhttp.status: "+ xmlhttp.status;
     //var uri_fail = "https://stickershop.line-scdn.net/stickershop/v1/product/1128841/LINEStorePC/main.png";
     //document.getElementById("photo_snapshot2").innerHTML = '<img src="'+uri_fail+'" height="180" width="180"/>';
     //document.getElementById("test").style.display = "";
   } 
  }
}	

</script>

