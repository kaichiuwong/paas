package com.kit418.kernel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class WorkerHandler extends Thread {
	private final DataInputStream dis;
	private final DataOutputStream dos;
	private final Socket s;
	public WorkerHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
	}
	
	public void run() {
		while (true) {
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