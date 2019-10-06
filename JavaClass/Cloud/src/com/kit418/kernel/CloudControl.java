package com.kit418.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.IPProtocol;
import org.openstack4j.model.compute.SecGroupExtension;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * OpenStack Example
 *
 */
public class CloudControl {
    //For Nectar Cloud Connection
    private static final String CLOUD_CONNECTION_STR="https://keystone.rc.nectar.org.au:5000/v3";
    private static final String CLOUD_KEY_PAIR_NAME="kaichiuwong";
    private static final String CLOUD_CREDENTIALS_USERNAME="kaichiu.wong@utas.edu.au";
    private static final String CLOUD_CREDENTIALS_SECERT="MzFkNTczNzYzM2RhYTcw";
    private static final String CLOUD_PROJECT_ID="b32a1d6f70c44be880b86f8f2c09773d";

    //For Instance connection
    private static final String PRIVATE_KEY_FILE_PATH="/home/ubuntu/kaichiuwong.ppk";
    private static final String PRIVATE_KEY_PASSPHRASE="46709394";
    
    //For Instance Creation
    private static final String MASTER_INSTANCE_NAME = "master-server";
    private static final String MASTER_INSTANCE_OS_USERNAME="ubuntu";
	private static final String CLIENT_INSTANCE_NAME = "UbuntuWorkerNode";
	private static final String CLIENT_INSTANCE_OS_USERNAME="ubuntu";

    //For Testing File Transfer (SFTP PUT)
    private static final String REMOTE_FOLDER_PATH = "/home/ubuntu/" ;
    private static final String REMOTE_INIT_FILE_NAME = "init.sh" ;
    
    private static final int MASTER_WORKER_PORT = 12345;
    
    OSClientV3 os = null;
    public CloudControl() {
        os = OSFactory.builderV3()
            .endpoint(CLOUD_CONNECTION_STR)
            .credentials(CLOUD_CREDENTIALS_USERNAME, CLOUD_CREDENTIALS_SECERT, Identifier.byName("Default"))
            .scopeToProject(Identifier.byId(CLOUD_PROJECT_ID))
            .authenticate();
    }
    public void createWorkerNode(String serverName) {
    	deleteServer(serverName);
    	System.out.printf("Creating instance (%s) ...\n", serverName);
    	
    	SecGroupExtension mwGroup = getSecurityGroup("master-worker");
    	if (mwGroup == null) {
    		mwGroup = os.compute().securityGroups().create("master-worker", "Permits Master and worker communication");
        	os.compute().securityGroups()
            .createRule(Builders.secGroupRule()
		        .parentGroupId(mwGroup.getId())
			    .protocol(IPProtocol.TCP)
			    .cidr("0.0.0.0/0")
			    .range(MASTER_WORKER_PORT, MASTER_WORKER_PORT).build()
		       );
    	}
        ServerCreate server = Builders.server()
            .name(serverName)
            .flavor("639b8b2a-a5a6-4aa2-8592-ca765ee7af63")
            .image("69872c6e-4be1-4758-b3da-3b7f4f179c06")
            .keypairName(CLOUD_KEY_PAIR_NAME)
            .addSecurityGroup(getSecurityGroupID("default"))
            .addSecurityGroup(getSecurityGroupID("http"))
            .addSecurityGroup(getSecurityGroupID("icmp"))
            .addSecurityGroup(getSecurityGroupID("ssh"))
            .addSecurityGroup(getSecurityGroupID("master-worker"))
            .build();

        os.compute().servers().boot(server);
        initServer(serverName);
    }
    
    private OSClientV3 getOS() {
    	return os;
    }
    //List of all flavors
    private void ListFlavors() {
        List < Flavor > flavors = (List < Flavor > ) os.compute().flavors().list();
        //System.out.println(flavors);
    }
    //List of all images
    private void ListImages() {
        List < ? extends Image > images = (List < ? extends Image > ) os.compute().images().list();
        //System.out.println(images);
    }
    //List of all Servers
    public List<?> ListServers() {
        List < ? extends Server > servers = os.compute().servers().list();
        return servers;
    }
    //Delete a Server
    private void deleteServer(String serverName) {
    	System.out.printf("Removing instance (%s) ...\n", serverName);
    	Server svr = getServer(serverName);
    	if (svr != null) {
    		String id = svr.getId();
        	//System.out.println(id);
        	os.compute().servers().delete(id);
        	waitServerDisappear(serverName);
    	}
    }
    
    private Server getServer(String serverName) {
    	Server rtnResult = null;
    	
    	List<Server> listServers = (List<Server>) ListServers();
    	for (Server svr: listServers) {
    		if (svr.getName().equals(serverName)) {
    			rtnResult = svr;
    			break;
    		}
    	}
    	return rtnResult;
    }
    
    private Server getServerByID(String serverID) {
    	Server rtnResult = null;
    	
    	List<Server> listServers = (List<Server>) ListServers();
    	for (Server svr: listServers) {
    		if (svr.getId().equals(serverID)) {    			
    			rtnResult = svr;
    			break;
    		}
    	}
    	return rtnResult;
    }
    
    
    private String getIP(String serverName) {
    	Server obj = waitServerReady(serverName);
    	
    	if (obj != null) {
    		return obj.getAccessIPv4();
    	}
    	
    	return null;
    }
    
    /*
     * All wait functions
     */
    
    private boolean waitSFTPService(String ipaddress) throws IOException {
    	File privateKeyFile = new File(PRIVATE_KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
		while (true) {
			try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
                //NO KNOWN HOST CHECKING 
	    		config.put("StrictHostKeyChecking", "no");
	    		jsch.addIdentity(PRIVATE_KEY_FILE_PATH,PRIVATE_KEY_PASSPHRASE);
	    		Session session=jsch.getSession(CLIENT_INSTANCE_OS_USERNAME, ipaddress, 22);
	    		session.setConfig(config);
	    		session.setTimeout(30000);
				session.connect();
				session.disconnect();
				System.out.printf("%s SFTP Service is up (Status: UP)... \n", ipaddress);
				return true;
			}
			catch (Exception e) {
    			System.out.printf("Waiting %s SFTP Service is up (Status: DOWN)... \n", ipaddress);
    			try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					break;
				}
			}
		}
		return false;
    }
    
    private boolean waitReachable(String ipaddress) {
        boolean reachable = false;
		try {
	        if (ipaddress != null) {
	        	while (true) {
	        		reachable = hasHeartBeat(ipaddress,5000);
	        		if (!reachable) {
	        			System.out.printf("Waiting %s to be reachable (Ping Status: %s)... \n", ipaddress, reachable);
	        			TimeUnit.SECONDS.sleep(1);
	        		}
	        		else {
	        			System.out.printf("%s is reachable now! (Ping Status: %s) \n", ipaddress, reachable);
	        			break;
	        		}
	        	}
	        }
		}
		catch (InterruptedException e) {
				e.printStackTrace();
		 }
		
		return false;
    }
    
    private boolean hasHeartBeat(String addr, int timeout) {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, 22), timeout);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    private Server waitServerReady(String serverName) {
    	Server svr = getServer(serverName);
    	
    	if (svr != null) {
	    	while (true) {
	    		try {
	    			svr = getServer(serverName);
	    			if (svr.getStatus() != Server.Status.ACTIVE) {
	    				System.out.printf("Waiting %s to ready (Current Status: %s)... \n", svr.getName(), svr.getStatus());
	    				TimeUnit.SECONDS.sleep(1);
	    			}
	    			else {
	    				System.out.printf("Server %s is ready! (IP Address: %s) \n", svr.getName(), svr.getAccessIPv4());
	    				return svr;
	    			}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
    	}
    	
    	return svr;
    }
    
    private boolean waitServerDisappear(String serverName) {
    	Server svr = getServer(serverName);
    	
    	if (svr != null) {
	    	while (true) {
	    		try {
	    			svr = getServer(serverName);
	    			if (svr == null) {
	    				System.out.printf("Server %s disappearead! \n", serverName);
	    				return true;
	    			}
	    			else {
	    				System.out.printf("Waiting %s to disappear (Current Status: %s)... \n", svr.getName(), svr.getStatus());
	    				TimeUnit.SECONDS.sleep(1);
	    			}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
    	}
    	
    	return false;
    }
    
    /*
     * Security Group
     */
    private List<?> getSecurityGroupList() {
    	List<? extends SecGroupExtension> sg = os.compute().securityGroups().list();
    	return sg;
    }
    
    private SecGroupExtension getSecurityGroup(String sgName) {
    	SecGroupExtension rtnObj = null;
    	for (Object sgObj: getSecurityGroupList()) {
    		SecGroupExtension obj = (SecGroupExtension)sgObj;
    		if (obj.getName().equals(sgName)) {
    			return obj;
    		}
    	}
    	return rtnObj ;
    }
    
    private String getSecurityGroupID(String GrpName) {
    	SecGroupExtension rtn = getSecurityGroup(GrpName);
    	if (rtn != null) {
    		return getSecurityGroup(GrpName).getId();
    	}
    	return null;
    }
    
    /*
     * Operations
     */
    private String uploadFile(String ServerName, String localFile) throws IOException {
    	String ipaddress = getIP(ServerName);
    	String defaultRemoteFolder = REMOTE_FOLDER_PATH + "uploads/";
    	String filename = Paths.get(localFile).getFileName().toString();
    	String remoteFile = defaultRemoteFolder + filename;
    	File privateKeyFile = new File(PRIVATE_KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress);
    		System.out.printf("Starting upload file to %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(PRIVATE_KEY_FILE_PATH,PRIVATE_KEY_PASSPHRASE);
	    		Session session=jsch.getSession(CLIENT_INSTANCE_OS_USERNAME, ipaddress, 22);
	    		session.setConfig(config);
	    		session.setTimeout(30000);
				session.connect();
				
				ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
				sftpChannel.connect();
				sftpChannel.cd(defaultRemoteFolder);
				sftpChannel.put(localFile, remoteFile, ChannelSftp.OVERWRITE);
				sftpChannel.chmod(Integer.parseInt("777",8), remoteFile);
				//listSftpDirectory(sftpChannel, defaultRemoteFolder);
				sftpChannel.disconnect();
				session.disconnect();
				System.out.printf("Upload file complete.\n");
			}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    		return remoteFile;
    	}
    	return null;
    }
    
    private void downloadFile(String ServerName, String remoteFile, String localFolder) throws IOException {
    	String ipaddress = getIP(ServerName);
    	File privateKeyFile = new File(PRIVATE_KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress);
    		System.out.printf("Starting download file from %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(PRIVATE_KEY_FILE_PATH,PRIVATE_KEY_PASSPHRASE);
	    		Session session=jsch.getSession(CLIENT_INSTANCE_OS_USERNAME, ipaddress, 22);
	    		session.setConfig(config);
	    		session.setTimeout(30000);
				session.connect();
				
				ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
				sftpChannel.connect();
				sftpChannel.get(remoteFile, localFolder);
				sftpChannel.disconnect();
				session.disconnect();
				System.out.printf("Download file complete.\n");
			}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void executeCommand(String ServerName, String cmd) throws IOException {
    	String ipaddress = getIP(ServerName);
    	File privateKeyFile = new File(PRIVATE_KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress);
    		System.out.printf("Starting execute command at %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(PRIVATE_KEY_FILE_PATH,PRIVATE_KEY_PASSPHRASE);
	    		Session session=jsch.getSession(CLIENT_INSTANCE_OS_USERNAME, ipaddress, 22);
	    		session.setConfig(config);
	    		session.setTimeout(30000);
				session.connect();
				
				Channel channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(cmd);
				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(System.err);

				InputStream in = channel.getInputStream();
	            
	            channel.connect();
	    		byte[] tmp = new byte[1024];
	    		while (true) {
	    			while (in.available() > 0) {
	    				int i = in.read(tmp, 0, 1024);

	    				if (i < 0)
	    					break;
	    				String CommandOutput = new String(tmp, 0, i);
	    				System.out.print(CommandOutput);
	    			}

	    			if (channel.isClosed()) {
	    				break;
	    			}
	    			try {
	    				Thread.sleep(1000);
	    			} catch (Exception ee) {
	    			}
	    		}
	    		channel.disconnect();
	    		session.disconnect();
			}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void listSftpDirectory(ChannelSftp sftpChannel, String dir) {
    	Vector<?> filelist;
    	System.out.printf("File list of %s : \n", dir);
		try {
			filelist = sftpChannel.ls(dir);
	        for(int i=0; i<filelist.size();i++) {
	            LsEntry entry = (LsEntry) filelist.get(i);
	            System.out.println(entry.getFilename());
	        }
		} catch (SftpException e) {
			e.printStackTrace();
		}
    }
    
    private void createinitfile(String serverName, String remotePath) throws IOException {
    	String command = String.format("echo '#!/bin/bash' > %s ;", remotePath);
    	command += String.format("echo 'mkdir /home/ubuntu/uploads' >> %s ;", remotePath);
    	command += String.format("echo 'chmod 777 /home/ubuntu/uploads' >> %s ;", remotePath);
    	command += String.format("echo 'sudo apt-get update' >> %s ;", remotePath);
    	command += String.format("echo 'sudo apt install default-jre -y' >> %s ;", remotePath);
    	command += String.format("echo 'sudo apt install default-jdk -y' >> %s ;", remotePath);
    	command += String.format("echo 'echo \"[OK] Initialisation Completed!\"' >> %s ;", remotePath);
    	command += String.format("chmod 777 %s ", remotePath);
    	executeCommand(serverName,command);
    }
    
    private void initServer(String serverName) {
        try {
	    	createinitfile(serverName, REMOTE_FOLDER_PATH+REMOTE_INIT_FILE_NAME);
	        executeCommand(serverName, REMOTE_FOLDER_PATH+REMOTE_INIT_FILE_NAME);
        }
        catch (Exception e) {
        	System.out.println("Exception occur: " + e.getMessage());
        }
    }
    
    //@TODO: will further enhance to be thread programming
    public void runJar(String JarFilePath, String workNodeName) throws IOException {
    	String remoteJarPath = uploadFile(workNodeName, JarFilePath);
    	Worker wrk = new Worker("144.6.227.55", 12345, remoteJarPath, "java");
    	wrk.start();
    }
    
    public void runPython(String PyFilePath, String workNodeName) throws IOException {
    	String remotePyPath = uploadFile(workNodeName, PyFilePath);
    	Worker wrk = new Worker("144.6.227.55", 12345, remotePyPath, "python");
    	wrk.start();
    }
    
    public void runJar(String JarFilePath, String inputFilePath, String workNodeName) throws IOException {
    	String remoteJarPath = uploadFile(workNodeName, JarFilePath);
    	String remoteInputFilePath = uploadFile(workNodeName, inputFilePath);
    	Worker wrk = new Worker("144.6.227.55", 12345, remoteJarPath + " " + remoteInputFilePath, "java");
    	wrk.start();
    }
    
    public void runPython(String PyFilePath, String inputFilePath, String workNodeName) throws IOException {
    	String remotePyPath = uploadFile(workNodeName, PyFilePath);
    	String remoteInputFilePath = uploadFile(workNodeName, inputFilePath);
    	Worker wrk = new Worker("144.6.227.55", 12345, remotePyPath + " " + remoteInputFilePath, "python");
    	wrk.start();
    }
    
    /*
     * Console Driver to test the program
     */
    
    private static void testWorkerExecutePy() {
        CloudControl openstack = new CloudControl();
        try {
        	openstack.runPython("/home/ubuntu/uploads/Helloworld.py", CLIENT_INSTANCE_NAME);
        }
        catch (Exception ex) {
        	
        }
    }
    
    private static void testWorkerExecuteJar() {
        CloudControl openstack = new CloudControl();
        try {
        	openstack.runJar("/home/ubuntu/uploads/Helloworld.jar", CLIENT_INSTANCE_NAME);
        }
        catch (Exception ex) {
        	
        }
    }
    
    private static void testStartMaster (Thread obj) {
    	obj.start();
    }
    
    private static void testPrintWorkerList(Thread obj) {
		Master m = (Master) obj;
		int i = 0;
		System.out.println(String.format("Total: %d workers stored in this Master.", m.getWorkerList().keySet().size()));
    	for( String key: m.getWorkerList().keySet()) {
    		System.out.println(String.format("%d) %s", ++i,key));
    	}
    }
    
    private static void testProgramOutput(Thread obj) {
    	System.out.println("Please input WORKER ID: ");
    	testPrintWorkerList(obj);
    	System.out.print("WORKERID> ");
    	Scanner snr = new Scanner(System.in);
    	String input = snr.next();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    	Master m = (Master) obj;
    	if (m.getWorkerList().containsKey(input)) {
    		String filepath = String.format("/home/ubuntu/output/%s.txt", input);
			File file = new File(filepath); 
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				String st; 
				System.out.println("Output file location: " + filepath);
				System.out.println("Finish time: " + sdf.format(file.lastModified()));
				System.out.println("Program output for worker " + input);
				System.out.println("********** START OUTPUT ********************");
				while ((st = br.readLine()) != null)  {
				    System.out.println(st); 
				}
				System.out.println("*********** END OUTPUT ********************");
				System.out.println();
			} catch (FileNotFoundException e) {
				System.out.println("[ERROR] Output file not found from master for Worker ID: " + input);
			} catch (IOException e) {

			} 
    	}
    	else {
    		System.out.println("[ERROR] Worker ID not found from master.");
    	}
    }
    
    private static void PrintCloudMenu() {
    	System.out.println("\n===================================================");
    	System.out.println("Please Input Testing Option: ");
    	System.out.println("===================================================");
    	System.out.println("M : Start Master Listener ");
    	System.out.println("P : Execute a Python File in Worker ");
    	System.out.println("J : Execute a Jar File in Worker ");
    	System.out.println("L : Print Worker List in Master ");
    	System.out.println("O : Get Program Output ");
    	System.out.println("Q : Quit this Testing Program ");    	
    	System.out.println("===================================================");
    	System.out.print("> ");
    }
    public static void main(String[] args) {
    	Scanner snr = new Scanner(System.in);
    	Thread obj = new Master();
    	
    	while (true) {
    		PrintCloudMenu();
    		String input = snr.next();
    		switch (input.toUpperCase()) {
    			case "J": testWorkerExecuteJar(); break;
    			case "L": testPrintWorkerList(obj); break;
    			case "M": testStartMaster(obj); break;
    			case "O": testProgramOutput(obj); break;
    			case "P": testWorkerExecutePy(); break;
    			case "Q": System.exit(0); break;
    			default: System.out.println("[ERROR] Wrong Input! Please input again."); break;
    		}
    	}
    }
}