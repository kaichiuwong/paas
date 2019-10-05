package com.kit418.kernel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Worker {
	private static Socket clientSocket;
	private static DataInputStream dis;
	private static DataOutputStream dos;
	private static String ipaddress; 
	private static int port; 
	
	public Worker() {
		ipaddress = "127.0.0.1";
		port = 2036;
	}
	
	
	public Worker(String ip) {
		ipaddress = ip;
		port = 2036;
	}
	
	public Worker(String ip, int portNum) {
		ipaddress = ip;
		port = portNum;
	}
	
	public static void main(String[] args) throws IOException {
		Worker obj = new Worker();
		obj.run();
	}
	
	public void run() {
		try {
			Scanner scanner = new Scanner(System.in);
			InetAddress ip = InetAddress.getByName(ipaddress);
			clientSocket = new Socket(ip, port);
			dis = new DataInputStream(clientSocket.getInputStream());
			dos = new DataOutputStream(clientSocket.getOutputStream());
			while (true) {
				dos.writeUTF("this is an output of program.");
				dos.writeUTF("EXIT");
				closeConnection();
				break;
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void closeConnection() throws IOException{
		try {
			if (dos != null) {
				dos.close();
			}			
		}
		finally {
			try {
				if (dis != null) {
					dis.close();
				}	
			}
			finally {
				if (clientSocket != null) {
					clientSocket.close();
				}
			}
		}
	}
}
