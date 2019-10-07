package com.kit418.kernel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Master extends Thread {
	private static int portNum;
	private ServerSocket svrSocket;
	private Map<String, Thread> workerList;
	public Master() {
		portNum = 12345;
		workerList = new HashMap<String, Thread>();
	}
	
	public Master(int port) {
		portNum = port;
		workerList = new HashMap<String, Thread>();
	}
	
	public void OpenMasterPort() {
		try {
		svrSocket = new ServerSocket(portNum);
		System.out.println("\nMaster is listening port: " + portNum);
		while (true) {
			Socket s = null ;
			try {
				s = svrSocket.accept();
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				
				Thread t = new WorkerHandler(s, dis, dos, workerList);		
				t.start();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				s.close();
			}
		}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Map<String, Thread> getWorkerList() {
		return workerList;
	}
	
	public void run() {
		OpenMasterPort();
	}
	
	public static void main(String[] args)  {
		Master obj ;
		if (args.length >= 1) {
			obj = new Master(Integer.parseInt(args[0]));
		}
		else {
			obj = new Master();
		}
		obj.OpenMasterPort();
	}
}