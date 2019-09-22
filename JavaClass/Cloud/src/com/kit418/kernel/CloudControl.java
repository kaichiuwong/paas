package com.kit418.kernel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
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
    private static final String PRIVATE_KEY_FILE_PATH="/Users/chiu0907/Downloads/Cloud/kaichiuwong.ppk";
    private static final String PRIVATE_KEY_PASSPHRASE="46709394";
    
    //For Instance Creation
	private static final String INSTANCE_NAME = "UbuntuTest";
	private static final String INSTANCE_OS_USERNAME="ubuntu";

    //For Testing File Transfer (SFTP PUT)
    private static final String LOCAL_FILE_PATH = "/Users/chiu0907/Downloads/Cloud/init.sh";
    private static final String REMOTE_FOLDER_PATH = "/home/ubuntu/" ;
    private static final String REMOTE_FILE_NAME = "init.sh" ;
    
    OSClientV3 os = null;
    public CloudControl() {
        os = OSFactory.builderV3()
            .endpoint(CLOUD_CONNECTION_STR)
            .credentials(CLOUD_CREDENTIALS_USERNAME, CLOUD_CREDENTIALS_SECERT, Identifier.byName("Default"))
            .scopeToProject(Identifier.byId(CLOUD_PROJECT_ID))
            .authenticate();
    }
    public void createServer(String serverName) {
    	deleteServer(serverName);
    	System.out.printf("Creating instance (%s) ...\n", serverName);
        ServerCreate server = Builders.server()
            .name(serverName)
            .flavor("639b8b2a-a5a6-4aa2-8592-ca765ee7af63")
            .image("69872c6e-4be1-4758-b3da-3b7f4f179c06")
            .keypairName(CLOUD_KEY_PAIR_NAME)
            .addSecurityGroup(getSecurityGroupID("default"))
            .addSecurityGroup(getSecurityGroupID("http"))
            .addSecurityGroup(getSecurityGroupID("icmp"))
            .addSecurityGroup(getSecurityGroupID("ssh"))
            .build();

        os.compute().servers().boot(server);
    }
    
    public OSClientV3 getOS() {
    	return os;
    }
    //List of all flavors
    public void ListFlavors() {
        List < Flavor > flavors = (List < Flavor > ) os.compute().flavors().list();
        //System.out.println(flavors);
    }
    //List of all images
    public void ListImages() {
        List < ? extends Image > images = (List < ? extends Image > ) os.compute().images().list();
        //System.out.println(images);
    }
    //List of all Servers
    public List<?> ListServers() {
        List < ? extends Server > servers = os.compute().servers().list();
        return servers;
    }
    //Delete a Server
    public void deleteServer(String serverName) {
    	System.out.printf("Removing instance (%s) ...\n", serverName);
    	Server svr = getServer(serverName);
    	if (svr != null) {
    		String id = svr.getId();
        	//System.out.println(id);
        	os.compute().servers().delete(id);
        	waitServerDisappear(serverName);
    	}
    }
    
    public Server getServer(String serverName) {
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
    
    public Server getServerByID(String serverID) {
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
    
    
    public String getIP(String serverName) {
    	Server obj = waitServerReady(serverName);
    	
    	if (obj != null) {
    		return obj.getAccessIPv4();
    	}
    	
    	return null;
    }
    
    /*
     * All wait functions
     */
    
    public boolean waitSFTPService(String ipaddress) {
		while (true) {
			try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
                //NO KNOWN HOST CHECKING 
	    		config.put("StrictHostKeyChecking", "no");
	    		jsch.addIdentity(PRIVATE_KEY_FILE_PATH,PRIVATE_KEY_PASSPHRASE);
	    		Session session=jsch.getSession(INSTANCE_OS_USERNAME, ipaddress, 22);
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
    
    public boolean waitReachable(String ipaddress) {
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
    
    public boolean hasHeartBeat(String addr, int timeout) {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, 22), timeout);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    public Server waitServerReady(String serverName) {
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
    
    public boolean waitServerDisappear(String serverName) {
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
    public List<?> getSecurityGroupList() {
    	List<? extends SecGroupExtension> sg = os.compute().securityGroups().list();
    	return sg;
    }
    
    public SecGroupExtension getSecurityGroup(String sgName) {
    	SecGroupExtension rtnObj = null;
    	for (Object sgObj: getSecurityGroupList()) {
    		SecGroupExtension obj = (SecGroupExtension)sgObj;
    		if (obj.getName().equals(sgName)) {
    			return obj;
    		}
    	}
    	return rtnObj ;
    }
    
    public String getSecurityGroupID(String GrpName) {
    	return getSecurityGroup(GrpName).getId();
    }
    
    /*
     * Operations
     */
    public void transferFile(String ServerName, String localFile) {
    	String ipaddress = getIP(ServerName);
    	String remoteFolder = REMOTE_FOLDER_PATH;
    	String remoteFile = REMOTE_FILE_NAME;
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress);
    		System.out.printf("Starting transfer file to %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(PRIVATE_KEY_FILE_PATH,PRIVATE_KEY_PASSPHRASE);
	    		Session session=jsch.getSession(INSTANCE_OS_USERNAME, ipaddress, 22);
	    		session.setConfig(config);
	    		session.setTimeout(30000);
				session.connect();
				
				ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
				sftpChannel.connect();
				sftpChannel.cd(remoteFolder);
				sftpChannel.put(localFile, remoteFile, ChannelSftp.OVERWRITE);
				sftpChannel.chmod(Integer.parseInt("777",8), remoteFile);
				listSftpDirectory(sftpChannel, remoteFolder);
				sftpChannel.disconnect();
				session.disconnect();
				System.out.printf("Transfer file complete.\n");
			}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public void executeCommand(String ServerName, String cmd) {
    	String ipaddress = getIP(ServerName);
    	
    	if (ipaddress != null) {
    		waitReachable(ipaddress);
    		waitSFTPService(ipaddress);
    		System.out.printf("Starting execute command at %s ...\n", ServerName);
    		try {
	    		JSch jsch=new JSch();
	    		Properties config = new Properties(); 
	    		config.put("StrictHostKeyChecking", "no");
	    		
	    		jsch.addIdentity(PRIVATE_KEY_FILE_PATH,PRIVATE_KEY_PASSPHRASE);
	    		Session session=jsch.getSession(INSTANCE_OS_USERNAME, ipaddress, 22);
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
	    				// System.out.print(new String(tmp, 0, i));
	    				String CommandOutput = new String(tmp, 0, i);
	    				System.out.print(CommandOutput);
	    			}

	    			if (channel.isClosed()) {
	    				// System.out.println("exit-status: " +
	    				// channel.getExitStatus());
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
    
    public void listSftpDirectory(ChannelSftp sftpChannel, String dir) {
    	Vector filelist;
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

    
    /*
     * main
     */
    public static void main(String[] args) {
        CloudControl openstack = new CloudControl();
        
        System.out.println(openstack.ListServers());
        //System.out.println(openstack.getIP(INSTANCE_NAME));
        //openstack.createServer(INSTANCE_NAME);
        //openstack.transferFile(INSTANCE_NAME, LOCAL_FILE_PATH);
        //openstack.executeCommand(INSTANCE_NAME, REMOTE_FOLDER_PATH+REMOTE_FILE_NAME);
        
        //openstack.ListFlavors();
        //openstack.ListImages();        
    }
}