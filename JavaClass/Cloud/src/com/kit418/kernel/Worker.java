package com.kit418.kernel;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Worker extends Thread{
	private static Socket clientSocket;
	private static DataInputStream dis;
	private static DataOutputStream dos;
	private static String ipaddress; 
	private static int port; 
	private static String type;
	private static String filepath;
	private static String workerID;
	
	public Worker() {
		ipaddress = "127.0.0.1";
		port = 12345;
		Thread currentThread = Thread.currentThread();
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
		workerID = String.format("%s_%07d", timestamp, currentThread.getId());
	}
	
	
	public Worker(String ip) {
		ipaddress = ip;
		port = 12345;
		Thread currentThread = Thread.currentThread();
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
		workerID = String.format("%s_%07d", timestamp, currentThread.getId());
	}
	
	public Worker(String ip, int portNum) {
		ipaddress = ip;
		port = portNum;
		Thread currentThread = Thread.currentThread();
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
		workerID = String.format("%s_%07d", timestamp, currentThread.getId());
	}
	
	public Worker(String ip, int portNum, String programPath, String programtype) {
		ipaddress = ip;
		port = portNum;
		type = programtype;
		filepath = programPath;
		Thread currentThread = Thread.currentThread();
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
		workerID = String.format("%s_%07d", timestamp, currentThread.getId());
	}
	
	public static void main(String[] args) throws IOException {
		Worker obj ;
		if (args.length >= 2) {
			obj = new Worker(args[0], Integer.parseInt(args[1]));
		}
		else {
			obj = new Worker();
		}
		obj.run();
	}
	
	public String getWorkerID() {
		return workerID;
	}
	
	public void run() {
		try {
			Scanner scanner = new Scanner(System.in);
			InetAddress ip = InetAddress.getByName(ipaddress);
			System.out.println("[WORKER] Connecting to Master (" + ipaddress + ":" + port + ")");
			clientSocket = new Socket(ip, port);
			dis = new DataInputStream(clientSocket.getInputStream());
			dos = new DataOutputStream(clientSocket.getOutputStream());
			System.out.println("[WORKER] Connected to Master (" + ipaddress + ":" + port + ")");
			dos.writeUTF(workerID);
			while (true) {
				System.out.println("[WORKER] WORKER ID: " + workerID);
				System.out.println(String.format("[WORKER] Start to run %s program: %s", type, filepath));
				try {
					String cmdExec = "ls -l ";
					switch (type) {
						case "java": cmdExec = "java -jar " ; break;
						case "python": cmdExec = "python3 " ; break;
					}
					Process process = Runtime.getRuntime().exec(String.format("%s %s", cmdExec, filepath));
					StringBuilder output = new StringBuilder();
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	
					String line;
					while ((line = reader.readLine()) != null) {
						output.append(line + "\n");
					}
	
					int exitVal = process.waitFor();
					if (exitVal == 0) {
						System.out.println("[WORKER] Execute Completed");
						System.out.println("[WORKER] Program Output of " + filepath);
						System.out.println(output);
						dos.writeUTF(output.toString());
					} else {
						System.out.println("[WORKER] Exit with Exception");
						System.out.println("[WORKER] Program Output of " + filepath);
						System.out.println(output);
						dos.writeUTF(output.toString());
					}
				}
				catch (IOException ex) {
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				dos.writeUTF("EXIT");
				closeConnection();
				break;
			}
			System.out.println("[WORKER] Connection closed");
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
