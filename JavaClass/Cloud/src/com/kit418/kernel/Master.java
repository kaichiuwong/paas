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
	
	public Master() {
		portNum = 12345;
	}
	
	public Master(int port) {
		portNum = port;
	}
	
	public void CreateWorker() {
		Worker obj = new Worker();
		obj.run();
	}
	
	public void OpenMasterPort() {
		try {
		svrSocket = new ServerSocket(portNum);
		System.out.println("Master is listening port: " + portNum);
		while (true) {
			Socket s = null ;
			try {
				s = svrSocket.accept();
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				
				Thread t = new WorkerHandler(s, dis, dos);
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