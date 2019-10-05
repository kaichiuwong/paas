package com.kit418.kernel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Master extends Thread {
	
	private static int portNum ;
	private static Map<String, Worker> workerList;
	private static Queue<String> requestQueue;
	private static ServerSocket svrSocket;
	
	public Master(int port) {
		try {
			portNum = port;
			workerList = new HashMap<String, Worker>();
			svrSocket = new ServerSocket(portNum);
			requestQueue = new LinkedList<String>();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		while (true) {
			Socket s = null;
			
		}
	}
	
	public void runProgram() {
		
	}
}
