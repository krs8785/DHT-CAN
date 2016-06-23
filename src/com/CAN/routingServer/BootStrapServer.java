package com.CAN.routingServer;

import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.CAN.Constants.Constants;
import com.CAN.routingServer.routingServerInterface.BootStrapInterface;

/**
 *  Bootstrap server which is the entry point for all the server. CAN has 
 *  an associated DNS domain name, and that this resolves to the IP address of
 *  one or more CAN bootstrap server. To join a CAN, a new server looks up the 
 *  CAN domain name in DNS to retrieve a bootstrap node’s IP address. The bootstrap 
 *  server then supplies the IP addresses of several randomly chosen server 
 *  currently in the system
 * 
 * @author karan
 *
 */
public class BootStrapServer extends UnicastRemoteObject implements
		BootStrapInterface, Serializable {

	private static final long serialVersionUID = 1L;
	
	//This will get initialized when you run the server
	//so peer server can use it to get connected
	public String BootStrapIP = null;
	

	/**
	 * @throws RemoteException
	 */
	protected BootStrapServer() throws RemoteException {
		super();
	}

	/**
	 * @param args
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		try {
			InetAddress ip;
			BootStrapServer obj = new BootStrapServer();
			Registry reg = LocateRegistry.createRegistry(Constants.portNumber);
			reg.rebind(Constants.bindNameBootStrap, obj);
			ip = InetAddress.getLocalHost();
			System.out.println("\nThe Server IP is:" + ip);
		} catch (Exception e) {
			System.out.println("Binding err" + e.getMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see BootStrapInterface#emitBootStrapIP()
	 */
	@Override
	public String emitBootStrapIP() throws RemoteException {
		return this.BootStrapIP;
	}

	/* (non-Javadoc)
	 * @see BootStrapInterface#setNewBootStrapIP(java.lang.String)
	 */
	@Override
	public void setNewBootStrapIP(String _ip) throws RemoteException {
		this.BootStrapIP = _ip;
	}
}
