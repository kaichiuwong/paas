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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
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
    
    private static final String WORKER_PRIVATE_KEY_FILE_PATH="/home/ubuntu/win.ppk";
    private static final String WORKER_PRIVATE_KEY_PASSPHRASE="";
    private static final String WORKER_CLOUD_KEY_PAIR_NAME="496768-418-key1";
    private static final String WORKER_CLOUD_CREDENTIALS_USERNAME="theingiw@utas.edu.au";
    private static final String WORKER_CLOUD_CREDENTIALS_SECERT="OTdmN2MzN2Y2NGYyNjRj";
    private static final String WORKER_CLOUD_PROJECT_ID="d10f74989f604508aafa198d22d96e60";
    
    //For Instance Creation
    private static final String MASTER_INSTANCE_NAME = "master-server";
    private static final String MASTER_INSTANCE_OS_USERNAME="ubuntu";

    private static final String CLIENT_INSTANCE_NAME = "UbuntuWorkerNode";
	private static final String CLIENT_INSTANCE_OS_USERNAME="ubuntu";	

    //For Testing File Transfer (SFTP PUT)
    private static final String REMOTE_FOLDER_PATH = "/home/ubuntu/" ;
    private static final String REMOTE_INIT_FILE_NAME = "init.sh" ;
    
    private static final int MASTER_WORKER_PORT = 12345;
    private static final String MASTER_ADDRESS = "144.6.227.55";
    
    OSClientV3 os = null;
    OSClientV3 osWorker =null;
    private static Thread masterObj;
    private List<WorkerServer> serverList;
    private static List < ? extends Server > servers;
    private static List < ? extends Server > workers;
    
    public CloudControl() {
    	if(osWorker == null) {
    	osWorker = OSFactory.builderV3()
                 .endpoint(CLOUD_CONNECTION_STR)
                 .credentials(WORKER_CLOUD_CREDENTIALS_USERNAME, WORKER_CLOUD_CREDENTIALS_SECERT, Identifier.byName("Default"))
                 .scopeToProject(Identifier.byId(WORKER_CLOUD_PROJECT_ID))
                 .authenticate();
         workers = osWorker.compute().servers().list();
    	}
    	if(os == null) {
        os = OSFactory.builderV3()
            .endpoint(CLOUD_CONNECTION_STR)
            .credentials(CLOUD_CREDENTIALS_USERNAME, CLOUD_CREDENTIALS_SECERT, Identifier.byName("Default"))
            .scopeToProject(Identifier.byId(CLOUD_PROJECT_ID))
            .authenticate();
        servers = os.compute().servers().list();
    	}
       
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
        initServer(serverName,false);
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
    	List<Server> resultServer= new ArrayList<Server>();
        resultServer.addAll(servers);
        resultServer.addAll(workers);
        return resultServer;
        
    }
    public List<?> ListWorkers(){
    	return workers;
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
    
    private boolean waitSFTPService(String ipaddress,String KEY_FILE_PATH,String KEY_PASSPHRASE) throws IOException {
    	
    	File privateKeyFile = new File(KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
		while (true) {
			try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
                //NO KNOWN HOST CHECKING 
	    		config.put("StrictHostKeyChecking", "no");
	    		jsch.addIdentity(KEY_FILE_PATH,KEY_PASSPHRASE);
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
    
    private String uploadFile(String ServerName, String localFile, String remoteFolder, boolean isWorker) throws IOException {
    	String ipaddress = getIP(ServerName);    	
    	String filename = Paths.get(localFile).getFileName().toString();
    	String remoteFile = remoteFolder + filename;
    	
    	String KEY_FILE_PATH="";
    	String KEY_PASSPHRASE="";
    	
    	if(isWorker) {
    		KEY_FILE_PATH = WORKER_PRIVATE_KEY_FILE_PATH;
    		KEY_PASSPHRASE = WORKER_PRIVATE_KEY_PASSPHRASE;
    		
    	}else {
    		KEY_FILE_PATH = PRIVATE_KEY_FILE_PATH;
    		KEY_PASSPHRASE = PRIVATE_KEY_PASSPHRASE;
    	}
    	
    	File privateKeyFile = new File(KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress, KEY_FILE_PATH,KEY_PASSPHRASE);
    		System.out.printf("Starting upload file to %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(KEY_FILE_PATH,KEY_PASSPHRASE);
	    		Session session=jsch.getSession(CLIENT_INSTANCE_OS_USERNAME, ipaddress, 22);
	    		session.setConfig(config);
	    		session.setTimeout(30000);
				session.connect();
				
				ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
				sftpChannel.connect();
				sftpChannel.cd(remoteFolder);
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
    
    private String uploadFile(String ServerName, String localFile,boolean isWorker) throws IOException {
    	String defaultRemoteFolder = REMOTE_FOLDER_PATH + "uploads/";
    	return uploadFile(ServerName, localFile, defaultRemoteFolder, isWorker);
    }
    
    private void downloadFile(String ServerName, String remoteFile, String localFolder, boolean isWorker) throws IOException {
    	String ipaddress = getIP(ServerName);
    	String KEY_FILE_PATH="";
    	String KEY_PASSPHRASE="";
    	
    	if(isWorker) {
    		KEY_FILE_PATH = WORKER_PRIVATE_KEY_FILE_PATH;
    		KEY_PASSPHRASE = WORKER_PRIVATE_KEY_PASSPHRASE;
    		
    	}else {
    		KEY_FILE_PATH = PRIVATE_KEY_FILE_PATH;
    		KEY_PASSPHRASE = PRIVATE_KEY_PASSPHRASE;
    	}
    	File privateKeyFile = new File(KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress,KEY_FILE_PATH,KEY_PASSPHRASE);
    		System.out.printf("Starting download file from %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(KEY_FILE_PATH,KEY_PASSPHRASE);
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
    
    private void executeCommand(String ServerName, String cmd,boolean isWorker) throws IOException {
    	String ipaddress = getIP(ServerName);
    	String KEY_FILE_PATH="";
    	String KEY_PASSPHRASE="";
    	
    	if(isWorker) {
    		KEY_FILE_PATH = WORKER_PRIVATE_KEY_FILE_PATH;
    		KEY_PASSPHRASE = WORKER_PRIVATE_KEY_PASSPHRASE;
    		
    	}else {
    		KEY_FILE_PATH = PRIVATE_KEY_FILE_PATH;
    		KEY_PASSPHRASE = PRIVATE_KEY_PASSPHRASE;
    	}
    	
    	File privateKeyFile = new File(KEY_FILE_PATH);
    	if (!privateKeyFile.exists()) {
    		throw new IOException("Private key file not found: " + privateKeyFile.getAbsolutePath());
    	}
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress,KEY_FILE_PATH,KEY_PASSPHRASE);
    		System.out.printf("Starting execute command at %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(KEY_FILE_PATH,KEY_PASSPHRASE);
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
    	command += String.format("echo 'mkdir -p /home/ubuntu/bin/com/kit418/kernel' >> %s ;", remotePath);
    	command += String.format("echo 'chmod -R 777 /home/ubuntu/bin' >> %s ;", remotePath);
    	command += String.format("echo 'sudo apt-get update' >> %s ;", remotePath);
    	command += String.format("echo 'sudo apt install default-jre -y' >> %s ;", remotePath);
    	command += String.format("echo 'sudo apt install default-jdk -y' >> %s ;", remotePath);
    	command += String.format("echo 'echo \"[OK] Initialisation Completed!\"' >> %s ;", remotePath);
    	command += String.format("chmod 777 %s ", remotePath);
    	executeCommand(serverName,command, false);
    }
    
    private void initServer(String serverName,boolean isWorker) {    	
        try {
        	createinitfile(serverName, REMOTE_FOLDER_PATH+REMOTE_INIT_FILE_NAME);
        	executeCommand(serverName, REMOTE_FOLDER_PATH+REMOTE_INIT_FILE_NAME, false);
        	uploadFile(serverName, "/home/ubuntu/bin/com/kit418/kernel/Worker.class","/home/ubuntu/bin/com/kit418/kernel/", isWorker);
        }
        catch (Exception e) {
        	System.out.println("Exception occur: " + e.getMessage());
        }
    }
    
    
    //@TODO: will further enhance to be thread programming
    
    public String runJar(String JarFilePath, String workNodeName, boolean isWorker) throws IOException {
    	String remoteJarPath = uploadFile(workNodeName, JarFilePath,isWorker);    	
    	String cmd = String.format("java -cp \"/home/ubuntu/bin\" com.kit418.kernel.Worker %s %d %s %s",
    								MASTER_ADDRESS,
    								MASTER_WORKER_PORT,
    								remoteJarPath,
    								"java");
    	executeCommand(workNodeName,cmd,isWorker);
    	return getLatestWorkerID();
    }
    
    public String runPython(String PyFilePath, String workNodeName, boolean isWorker) throws IOException {
    	String remotePyPath = uploadFile(workNodeName, PyFilePath,isWorker);
    	String cmd = String.format("java -cp \"/home/ubuntu/bin\" com.kit418.kernel.Worker %s %d %s %s",
				MASTER_ADDRESS,
				MASTER_WORKER_PORT,
				remotePyPath,
				"python");
    	executeCommand(workNodeName,cmd,isWorker);
    	return getLatestWorkerID();
    }
    
    public String runJar(String JarFilePath, String inputFilePath, String workNodeName, boolean isWorker) throws IOException {
    	String remoteJarPath = uploadFile(workNodeName, JarFilePath,isWorker);
    	String remoteInputFilePath = uploadFile(workNodeName, inputFilePath,isWorker);
    	String cmd = String.format("java -cp \"/home/ubuntu/bin\" com.kit418.kernel.Worker %s %d %s %s",
				MASTER_ADDRESS,
				MASTER_WORKER_PORT,
				remoteJarPath + " " + remoteInputFilePath,
				"python");
    	executeCommand(CLIENT_INSTANCE_NAME,cmd,isWorker);
    	return getLatestWorkerID();
    }
    
    public String runPython(String PyFilePath, String inputFilePath, String workNodeName, boolean isWorker) throws IOException {
    	String remotePyPath = uploadFile(workNodeName, PyFilePath,isWorker);
    	String remoteInputFilePath = uploadFile(workNodeName, inputFilePath,isWorker);
    	String cmd = String.format("java -cp \"/home/ubuntu/bin\" com.kit418.kernel.Worker %s %d %s %s",
				MASTER_ADDRESS,
				MASTER_WORKER_PORT,
				remotePyPath + " " + remoteInputFilePath,
				"python");
    	executeCommand(CLIENT_INSTANCE_NAME,cmd,isWorker);
    	return getLatestWorkerID();
    }
    
    public String getWorkerStatus(String workerID) {
    	Master m = (Master) masterObj;
    	WorkerHandler obj = (WorkerHandler) m.getWorkerList().get(workerID);
    	
    	return obj.getStatus();
    }
    
    public Date getWorkerStartTime(String workerID) {
    	Master m = (Master) masterObj;
    	if(m == null) return null;
    	WorkerHandler obj = (WorkerHandler) m.getWorkerList().get(workerID);
    	if(obj != null) {
    		return obj.getStartTime();
    	}
    	return null;
    }
    
    public Date getWorkerEndTime(String workerID) {
    	Master m = (Master) masterObj;
    	if(m == null) return null;
    	WorkerHandler obj = (WorkerHandler) m.getWorkerList().get(workerID);
    	if(obj != null) {
    		return obj.getEndTime();
    	}
    	return null;
    }
    
    public List<String> getWorkerList() {
		Master m = (Master) masterObj;
		Set<String> wSet = m.getWorkerList().keySet();
		List<String> rtnList = new ArrayList<String>();
		
		if ( rtnList != null ) {
	    	for( String key: wSet) {
	    		rtnList.add(key);
	    	}
		}
		
		return rtnList;
    }
    
    public String getLatestWorkerID() {
    	List<String> wrklist = getWorkerList();
    	String rtnStr = "";
    	
    	for (String str: wrklist) {
    		if (str.compareTo(rtnStr) < 0) {
    			rtnStr = str;
    		}
    	}
    	
    	return rtnStr;
    }
    
    public void startMaster() {
    	masterObj.run();
    }
    
    public void stopMaster() {
    	masterObj.stop();
    }
    
    public boolean isMasterAlive() {
    	return masterObj.isAlive();
    }
    
    public static String getOutputFile(String workerID) {
    	
    	 String FilePath = String.format("/home/ubuntu/output/%s.txt", workerID);
    	
    	//String FilePath  = Paths.get("").toAbsolutePath().toString()+String.format("/output/%s.txt", workerID);
    	String result="";
    	  try
    	  {
    	    BufferedReader reader = new BufferedReader(new FileReader(FilePath));
    	    String line;
    	    while ((line = reader.readLine()) != null)
    	    {
    	    	result += line+"\n";
    	    }
    	    reader.close();
    	    return result;
    	  }
    	  catch(FileNotFoundException e) {
    		 System.err.format("File not found '%s'.", FilePath);
      	    return null;
    	  }
    	  catch (Exception e)
    	  {
    	    System.err.format("Exception occurred trying to read '%s'.", FilePath);
    	    e.printStackTrace();
    	    return null;
    	  }
    }
   
    public String cancelWorker(String workerID) {
    	try {
	    	Master m = (Master) masterObj;
	    	WorkerHandler obj = (WorkerHandler) m.getWorkerList().get(workerID);
	    	if(obj != null  ) {
	    		if(obj.getStatus().toUpperCase() == "DONE")
	    			return "Job completed and can't cancelled.";
    			m.getWorkerList().remove(workerID);
		    	obj.stop();
		    	return "";
	    	}
	    	return "Invalid PassCode.";
    	}catch(Exception ex) {
    		return "Error occured in Request.";
    	}
    }
    /*
     * Console Driver to test the program
    */
    
    public static String testGetWorkerStatus(String workerID) {
    	Master m = (Master) masterObj;
    	WorkerHandler obj = (WorkerHandler) m.getWorkerList().get(workerID);
    	
    	return obj.getStatus();
    }
    
    public static void testOutputFile() {
    	String workerID = "20191010_031508.520_0000001";
    	System.out.println(getOutputFile(workerID));
    }
     
    public static Date testGetWorkerStartime(String workerID) {
    	Master m = (Master) masterObj;
    	WorkerHandler obj = (WorkerHandler) m.getWorkerList().get(workerID);
    	
    	return obj.getStartTime();
    }
    
    public static Date testGetWorkerEndime(String workerID) {
    	Master m = (Master) masterObj;
    	WorkerHandler obj = (WorkerHandler) m.getWorkerList().get(workerID);
    	
    	return obj.getEndTime();
    }
    
    public static void testInitServer(String serverName) {
    	CloudControl openstack = new CloudControl();
    	openstack.createWorkerNode(serverName);
    }
    
    private static void testWorkerExecutePy() {
        CloudControl openstack = new CloudControl();
        try {
        	openstack.runPython("/home/ubuntu/uploads/Helloworld.py", CLIENT_INSTANCE_NAME, false);
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    private static void testWorkerExecuteJar() {
        CloudControl openstack = new CloudControl();
        try {
        	openstack.runJar("/home/ubuntu/uploads/Helloworld.jar", CLIENT_INSTANCE_NAME, false);
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    private static void testStartMaster () {
    	masterObj = new Master();
    	masterObj.start();
    }
    
    @SuppressWarnings("deprecation")
	private static void testStopMaster () {
    	masterObj.stop();
    	masterObj = null;
    }
    
    private static void testMasterStatus () {
    	if ( masterObj != null ) {
	    	if ( masterObj.isAlive() ) {
	    		System.out.println("Master is alive." );
	    		return;
	    	}
    	}
    	System.out.println("Master is not alive.");
    }
    
    private static void testPrintWorkerList() {
		Master m = (Master) masterObj;
		int i = 0;
		System.out.println(String.format("Total: %d workers stored in this Master.", m.getWorkerList().keySet().size()));
    	for( String key: m.getWorkerList().keySet()) {
    		System.out.println(String.format("%d) %s (STATUS: %s)", ++i,key, testGetWorkerStatus(key)));
    	}
    }
    
    private static void testProgramOutput() {
    	System.out.println("Please input WORKER ID: ");
    	testPrintWorkerList();
    	System.out.print("WORKERID> ");
    	Scanner snr = new Scanner(System.in);
    	String input = snr.next();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    	Master m = (Master) masterObj;
    	if (m.getWorkerList().containsKey(input)) {
    		String filepath = String.format("/home/ubuntu/output/%s.txt", input);
			File file = new File(filepath); 
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				String st; 
				System.out.println("Output file location: " + filepath);
				System.out.println("Worker Start time: " + sdf.format(testGetWorkerStartime(input)));
				System.out.println("Worker End time: " + sdf.format(testGetWorkerEndime(input)));
				System.out.println("Worker Current Status: " + testGetWorkerStatus(input));
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
    	synchronized (System.out) {
	    	System.out.println("\n===================================================");
	    	System.out.println("Please Input Testing Option: ");
	    	System.out.println("===================================================");
	    	System.out.println("I : Create New Worker ");
	    	System.out.println("C : Check Master Listener Status ");
	    	System.out.println("M : Start Master Listener ");
	    	System.out.println("S : Stop Master Listener ");
	    	System.out.println("P : Execute a Python File in Worker ");
	    	System.out.println("J : Execute a Jar File in Worker ");
	    	System.out.println("L : Print Worker List in Master ");
	    	System.out.println("O : Get Program Output ");
	    	System.out.println("W : Get Server List ");
	    	System.out.println("WL : Get Worker Server List ");
	    	System.out.println("F : Get Output File ");
	    	System.out.println("Q : Quit this Testing Program ");    	
	    	System.out.println("===================================================");
	    	System.out.print("> ");
    	}
    }
    
    private static void testServerList() {
    	CloudControl openstack = new CloudControl();
    	List<?> lstServer = openstack.ListServers();
    	System.out.printf("Server list  : \n");
		for(int i=0; i<lstServer.size();i++) {
		    Server entry = (Server) lstServer.get(i);
		    System.out.println(entry.getAccessIPv4() +" " + entry.getName());
		}
    }
    
    private static void testWorkerServerList() {
    	CloudControl openstack = new CloudControl();
    	List<?> lstServer = openstack.ListWorkers();
    	System.out.printf("Server list  : \n");
		for(int i=0; i<lstServer.size();i++) {
		    Server entry = (Server) lstServer.get(i);
		    System.out.println(entry.getAccessIPv4() +" " + entry.getName());
		}
    }
    
    public static void main(String[] args) {
    	Scanner snr = new Scanner(System.in);
    	
    	while (true) {
    		PrintCloudMenu();
    		String input = snr.next();
    		switch (input.toUpperCase()) {
    			case "C": testMasterStatus(); break;
    			case "I": testInitServer(CLIENT_INSTANCE_NAME); break;
    			case "J": testWorkerExecuteJar(); break;
    			case "L": testPrintWorkerList(); break;
    			case "M": testStartMaster(); break;
    			case "S": testStopMaster(); break;
    			case "O": testProgramOutput(); break;
    			case "P": testWorkerExecutePy(); break;
    			case "W": testServerList();break;
    			case "WL": testWorkerServerList();break;
    			case "F": testOutputFile();break;
    			case "Q": System.exit(0); break;
    			
    			default: System.out.println("[ERROR] Wrong Input! Please input again."); break;
    		}
    	}
    }
}