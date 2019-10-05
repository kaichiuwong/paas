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
	
	public static void main(String[] args) throws IOException {
		try {
			Scanner scanner = new Scanner(System.in);
			InetAddress ip = InetAddress.getByName("127.0.0.1");
			clientSocket = new Socket(ip, 2036);
			dis = new DataInputStream(clientSocket.getInputStream());
			dos = new DataOutputStream(clientSocket.getOutputStream());
			while (true) {
				break;
			}
		}
		catch (IOException ex) {
			
		}
	}
}
