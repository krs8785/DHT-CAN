package com.CAN.routingServer.routingServerInterface;
import java.rmi.*;

/**
 * The bootstrap interface essentially contains the methods require
 * by the bootstrap to connect the server
 * 
 * @author karan
 *
 */
public interface BootStrapInterface extends Remote {
	/**
	 * @param _ip
	 * @throws RemoteException
	 */
	void setNewBootStrapIP(String _ip) throws RemoteException;

	/**
	 * @return
	 * @throws RemoteException
	 */
	String emitBootStrapIP() throws RemoteException;
}
