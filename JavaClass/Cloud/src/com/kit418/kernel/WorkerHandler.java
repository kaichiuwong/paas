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
import java.io.*; 
import java.util.*; 

public class WorkerHandler extends Thread {
	private final DataInputStream dis;
	private final DataOutputStream dos;
	private final Socket s;
	private String workerID ; 
	private String outputPath ;
	private List<WorkerServer> serverList;
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
	
	private String generatePassCode() {
		Random gen= new Random();
		int result = (gen.nextInt(9000) + 1000);
		return result+"";
	}
	
	private WorkerServer StartWorker(String FilePath, String  Type) {
		WorkerServer resultWorker = null;
		try {
		
			WorkerServer selectedServerWorker = this.serverList.stream().filter(x ->x.getIsBusy() == false).findFirst().get();
			if(selectedServerWorker == null) {
				dos.writeUTF("All the worker is busy at this moment. Please try again later");
				
			}else {
				resultWorker = selectedServerWorker;
				resultWorker.setPassCode(this.generatePassCode());
				resultWorker.setBusy(true);
				
				//String remoteFilePath = uploadFile(selectedWorker.getServer().getInstanceName(), FilePath);
				String remoteFilePath="";
		    	Worker wrk = new Worker(resultWorker.getServer().getAccessIPv4(), 12345, remoteFilePath, Type);
		    	wrk.start();
		    	resultWorker.setWorkId(wrk.getWorkerID());
		    	RemoveWorker(selectedServerWorker);
		    	AddWorker(resultWorker);
		    	
			}
		}catch(Exception e) {
			
		}
		return resultWorker;
	}
	
	private void RemoveWorker(WorkerServer worker) {
		synchronized(this.serverList) {
			this.serverList.remove(worker);
		}
	}
	
	private void AddWorker(WorkerServer worker) {
		synchronized(this.serverList) {
			this.serverList.add(worker);
		}
	}
	
	private String ExecuteJava(){
		return"";
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