package com.kit418.kernel;

import java.util.Date;

import org.openstack4j.model.compute.Server;

public class WorkerServer{
	
	private String _WorkId;
	private String _PassCode;
	private String _OutputPath;
	private Server _Server;
	private String _BillPath;
	private boolean _IsBusy;
	
	public WorkerServer(Server server) {
		this._Server = server;
		this._WorkId ="";
		this._OutputPath="";
		this._BillPath="";
		this._IsBusy=false;
		
	}
	
	public String getPassCode() {
	  return this._PassCode;
	}
	public void setPassCode(String value) {
	  this._PassCode = value;
	}
	public String getWorkId() {
	  return this._WorkId;
	}
	
	public void setWorkId(String value) {
	  this._WorkId = value;
	}
	
	public String getOutputPath() {
	  return this._OutputPath;
	}
	
	public void setOutputPath(String value) {
	  this._OutputPath = value;
	}
	
	public Server getServer() {
	  return this._Server;
	}

	public void setServer(Server value) {
	  this._Server = value;
	}
	
	public boolean getIsBusy() {
	  return this._IsBusy;
	}

	public void setBusy(boolean value) {
	  this._IsBusy = value;
	}
	
	public String getBillPath() {
	  return this._BillPath;
	}
	
	public void setBillPath(String value) {
	  this._BillPath = value;
	}
}