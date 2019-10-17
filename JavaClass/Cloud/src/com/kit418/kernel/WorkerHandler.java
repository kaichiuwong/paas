package com.kit418.kernel;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.io.*; 
import java.util.*; 
public class WorkerHandler extends Thread {
	private final DataInputStream dis;
	private final DataOutputStream dos;
	private final Socket s;
	private String workerID ; 
	private String outputPath ;
	
	private Map<String, Thread> workerList;
	private String status;
	private Date StartTime;
	private Date EndTime;
	
	public WorkerHandler(Socket s, DataInputStream dis, DataOutputStream dos, Map<String, Thread>  wl) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		this.workerList = wl;
		this.status = "INIT";
		this.StartTime = new Date();
		this.EndTime = this.StartTime;
	}
	
	public String getworkerID() {
		return workerID;
	}
	
	public Date getStartTime() {
		return StartTime;
	}
	
	public Date getEndTime() {
		return EndTime;
	}
	
	private void saveOutput(String content) {
		try (FileWriter writer = new FileWriter(outputPath);
               BufferedWriter bw = new BufferedWriter(writer)) {
               bw.write(content);

           } catch (IOException ex) {
               ex.printStackTrace();
           }
	}
	
	public String getStatus() {
		return this.status;
	}
	
	private void writeInfo(String workerid) {
		System.out.println("[MASTER] Update Info Start");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	    String str = String.format("%s,%s,%s,%s", 
	    							workerid,
	    							this.status,
	    							sdf.format(this.StartTime),
	    							sdf.format(this.EndTime));
	    try {
	    	String infoPath = "/home/ubuntu/info/"+workerid+".txt";
	    	File file = new File(infoPath);
	    	Files.deleteIfExists(file.toPath());
		    BufferedWriter writer = new BufferedWriter(new FileWriter(infoPath));
		    writer.write(str);
		    writer.close();
	    }
	    catch (Exception e) {
	    	return;
	    }
	    System.out.println("[MASTER] Update Info End");
	}
	
	public void run() {
		this.status = "RUNNING";
		try {
			workerID = dis.readUTF();
			outputPath = String.format("/home/ubuntu/output/%s.txt", workerID);
			synchronized (workerList) {
				workerList.put(workerID, this);
			}
		}
		catch (IOException ex) {
			this.status = "ERROR";
			this.EndTime = new Date();
		}
		if (workerID != null) {
			writeInfo(workerID);
			while (true) {
				try {
					String cmdOutput = dis.readUTF();
					switch (cmdOutput) {
						case "EXIT" : break;
						default: saveOutput(cmdOutput); break;
					}
				}
				catch (IOException ex) {
					this.status = "ERROR";
					this.EndTime = new Date();
					writeInfo(workerID);
				}
				writeInfo(workerID);
				break;
			}
			try {
				this.dis.close();
				this.dos.close();
			}
			catch (Exception ex) {
				this.status = "ERROR";
				this.EndTime = new Date();
				writeInfo(workerID);
			}
		}
		this.status = "DONE";
		this.EndTime = new Date();
		writeInfo(workerID);
	}
}