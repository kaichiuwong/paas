package com.kit418.kernel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Master {
	private static int portNum;
	public Master(int port) {
		portNum = 2036;
	}
	
	public static void main(String[] args) throws IOException {
		ServerSocket svrSocket = new ServerSocket(portNum);
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
				s.close();
			}
		}
	}
}