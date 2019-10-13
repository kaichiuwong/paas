<%@ page import="com.kit418.web.WebConnector" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.parser.*" %>
<%@ page import="java.util.Map" %>

<%
String rtnResultPasscode = "";
String rtnResultStatus = "";
String rtnResultFile = "";
String rtnResultPrice = "";
String rtnResultDescription = "";
String actionSent = "";
String rtnResultResult = "";
String rtnResultErrorMessage = "";

WebConnector httpclient = new WebConnector();

actionSent = request.getParameter("sendAction");
rtnResultPasscode = request.getParameter("userPasscodeValue");

if (actionSent.equals("enquireStatus")){

	Map<String,String> result_status = httpclient.sendGet("http://localhost:8080/PaaS_WS/InstanceControl", "action=EnquireStatus&passcode="+rtnResultPasscode);
	JSONObject jo_status = (JSONObject) new JSONParser().parse(result_status.get("ResponseMsg"));
	if(jo_status.get("status") != null){
		rtnResultStatus = jo_status.get("status").toString();
	}
	if (rtnResultStatus.equals("inprogress")){
		rtnResultStatus = "0";
		rtnResultFile = "No_Available_File";
	} else if (rtnResultStatus.equals("completed")){
		rtnResultStatus = "1";
		rtnResultFile = jo_status.get("file").toString();
		
		Map<String,String> result_bill = httpclient.sendGet("http://localhost:8080/PaaS_WS/InstanceControl", "action=DownloadBill&passcode="+rtnResultPasscode);
		JSONObject jo_bill = (JSONObject) new JSONParser().parse(result_bill.get("ResponseMsg"));
		
		rtnResultPrice = "$" + jo_bill.get("price").toString();
		rtnResultDescription = jo_bill.get("description").toString();
		%>
		<%=rtnResultStatus %>
		<%=";"%>
		<%=rtnResultFile %>
		<%=";"%>
		<%=rtnResultPrice %>
		<%=";"%>
		<%=rtnResultDescription %>
		<%=";"%>
		<%
	} else if (rtnResultStatus.equals("wrong_Passcode")){
		//
	}
	else{
		rtnResultStatus="-1";
		rtnResultErrorMessage = jo_status.get("errorMessage").toString();
		 %>
		<%=rtnResultStatus %>
		<%=";"%>
		<%=rtnResultErrorMessage %>
	<% }%>
	
<% 
} else if (actionSent.equals("cancelJob")){
	
	Map<String,String> result_cancel = httpclient.sendGet("http://localhost:8080/PaaS_WS/InstanceControl2Fortest", "action=CancelJob&passcode="+rtnResultPasscode);
	JSONObject jo_cancel = (JSONObject) new JSONParser().parse(result_cancel.get("ResponseMsg"));
	rtnResultResult = jo_cancel.get("result").toString();
	rtnResultErrorMessage = jo_cancel.get("errorMessage").toString();
	%>
	
	<%=rtnResultResult%>
	<%=";"%>
	<%=rtnResultErrorMessage%>
	<% 
	
}
%>







