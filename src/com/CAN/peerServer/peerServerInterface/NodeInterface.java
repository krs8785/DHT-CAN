package com.CAN.peerServer.peerServerInterface;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import com.CAN.nodeInfo.ServerInformation;


/**
 * @author karan
 *
 */
public interface NodeInterface extends Remote {

	/**
	 * @param x
	 * @param y
	 * @param ip
	 * @throws RemoteException
	 */
	void routing(int x, int y, String ip) throws RemoteException;

	/**
	 * @return
	 * @throws RemoteException
	 */
	ServerInformation getNodeInfo() throws RemoteException;

	/**
	 * @param _n
	 * @throws RemoteException
	 */
	void addNieghbor(ServerInformation _n) throws RemoteException;

	/**
	 * @return
	 * @throws RemoteException
	 */
	ArrayList<ServerInformation> getNeibhor() throws RemoteException;

	/**
	 * @param _n
	 * @throws RemoteException
	 */
	void removeNiebhor(ServerInformation _n) throws RemoteException;

	/**
	 * @param _lx
	 * @throws RemoteException
	 */
	public void setLX(double _lx) throws RemoteException;

	/**
	 * @param _ly
	 * @throws RemoteException
	 */
	public void setLY(double _ly) throws RemoteException;

	/**
	 * @param _ux
	 * @throws RemoteException
	 */
	public void setUX(double _ux) throws RemoteException;

	/**
	 * @param _uy
	 * @throws RemoteException
	 */
	public void setUY(double _uy) throws RemoteException;

	/**
	 * @param _n
	 * @throws RemoteException
	 */
	void updateNode(ServerInformation _n) throws RemoteException;

	/**
	 * @param hx
	 * @param hy
	 * @param fileName
	 * @param buffer
	 * @param ip
	 * @throws RemoteException
	 */
	void insertRoute(double hx, double hy, File fileName, byte buffer[],
			String ip) throws RemoteException;

	/**
	 * @param hx
	 * @param hy
	 * @param fileName
	 * @param ip
	 * @throws RemoteException
	 */
	void searchRoute(double hx, double hy, File fileName, String ip)
			throws RemoteException;

	/**
	 * @param name
	 * @param ip
	 * @throws RemoteException
	 */
	void printRoute(File name, String ip) throws RemoteException;

	/**
	 * @param _ip
	 * @throws RemoteException
	 */
	void printRouteSingle(String _ip) throws RemoteException;

	/**
	 * @param name
	 * @param buffer
	 * @throws RemoteException
	 */
	void addFile(File name, byte[] buffer) throws RemoteException;

	/**
	 * @param name
	 * @param ip
	 * @throws RemoteException
	 */
	void printRouteNot(File name, String ip) throws RemoteException;

	/**
	 * @return
	 * @throws RemoteException
	 */
	HashMap<File, byte[]> getFileList() throws RemoteException;
	
	/**
	 * @param list
	 * @throws RemoteException
	 */
	void setNeighbor(ArrayList<ServerInformation> list) throws RemoteException;
	
	/**
	 * @param list
	 * @throws RemoteException
	 */
	void setFileList(HashMap<File, byte[]> list) throws RemoteException;
}
