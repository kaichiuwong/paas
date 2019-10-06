package com.kit418.kernel;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class WorkerHandler extends Thread {
	private final DataInputStream dis;
	private final DataOutputStream dos;
	private final Socket s;
	private String workerID ; 
	private String outputPath ;
	private Map<String, Thread> workerList;
	
	public WorkerHandler(Socket s, DataInputStream dis, DataOutputStream dos, Map<String, Thread>  wl) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		this.workerList = wl;
	}
	
	public String getworkerID() {
		return workerID;
	}
	
	private void saveOutput(String content) {
		try (FileWriter writer = new FileWriter(outputPath);
               BufferedWriter bw = new BufferedWriter(writer)) {
               bw.write(content);

           } catch (IOException ex) {
               ex.printStackTrace();
           }
	}
	
	public void run() {
		try {
			workerID = dis.readUTF();
			outputPath = String.format("/home/ubuntu/output/%s.txt", workerID);
			synchronized (workerList) {
				workerList.put(workerID, this);
			}
		}
		catch (IOException ex) {
			
		}
		if (workerID != null) {
			while (true) {
				try {
					String cmdOutput = dis.readUTF();
					switch (cmdOutput) {
						case "EXIT" : break;
						default: saveOutput(cmdOutput); break;
					}
				}
				catch (IOException ex) {
					
				}
				break;
			}
			try {
				this.dis.close();
				this.dos.close();
			}
			catch (Exception ex) {
				
			}
		}
	}
}