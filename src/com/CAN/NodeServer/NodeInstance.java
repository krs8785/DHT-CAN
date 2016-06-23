package com.CAN.NodeServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import com.CAN.Constants.Constants;
import com.CAN.NodeServer.NodeServerInterface.NodeInterface;
import com.CAN.routingServer.routingServerInterface.BootStrapInterface;
import com.CAN.serverInfo.ServerInformation;

/**
 * The Node class is the main server class which consists of all the different functionality 
 * that it can perform. The peer essentially is a server that when loaded up connects to the system
 * via the bootstrap server. The server coantins its own information about the location ie coordinates,
 * IP, etc. It always maintains a list of neighbouring server for the routing algorithm. 
 * NOTE: In order to be distrubted in true sense it does not need to contain all the files or any metadata
 *, it routes through the system to feth the file. 
 * 
 * @author karan
 *
 */
public class NodeInstance extends UnicastRemoteObject implements Serializable,
		NodeInterface {


	private static final long serialVersionUID = 1L;
	
	/**
	 * Information such as the NodeInfo which contains the
	 * lx ly ux uy and IP. 
	 * List of neighbors
	 * List of files
	 */
	static ServerInformation node = new ServerInformation();
	static ArrayList<ServerInformation> neighbours;
	static HashMap<File, byte[]> allFile = new HashMap<File, byte[]>();

	/**
	 * @Constructor
	 * Set the IP
	 * Initialize the list
	 * @throws Exception
	 */
	public NodeInstance() throws Exception {

		try {
			node.setPeerIP(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		neighbours = new ArrayList<ServerInformation>();
		System.out.println("*Ip of this peer: " + node.getPeerIP() + "*");
	}

	/**
	 * Ask Peer to perform operations
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		int optionToJoin;
		int optionToPerformFuntion;
		boolean flag = true;
		while (flag == true) {
			System.out.println("1. Join");
			System.out.println("2. Exit");
			optionToJoin = sc.nextInt();
			switch (optionToJoin) {
			case 1:
				boolean checkConnect = true;
				try {
					joinNetwork();
				} catch (Exception e) {
					checkConnect = false;
					System.out.println("Exception err " + e.getMessage());
					e.printStackTrace();
				}
				flag = false;
				if (checkConnect == true) {
					while (true) {
						System.out.println("\n1. Insert ");
						System.out.println("2. Search");
						System.out.println("3. Leave ");
						System.out.println("4. View");
						System.out.println("5. View Neibhors Info");
						System.out.println("6. View File list Info");
						System.out.println("7. Quit ");
						optionToPerformFuntion = sc.nextInt();

						switch (optionToPerformFuntion) {
						case 1:
							insertFile();
							break;
						case 2:
							searchFile();
							break;
						case 3:
							leave();
							break;
						case 4:
							printPeer();
							break;
						case 5:
							print2(neighbours, node.getPeerIP());
							break;
						case 6:
							printFiles(allFile, node.getPeerIP());
							break;
						case 7:
							System.out.println("Quiting! Thank you.");
							System.exit(0);
							break;
						default:
							System.out.println("Invalid! Enter valid option");
						}
					}
				}

			case 2:
				System.out.println("Exiting! Thank you!");
				System.exit(0);
				break;
			default:
				System.out.println("Invalid! Enter Valid option");
				break;
			}
		}
	}
	
	/**
	 * This method require bootstrap IP which will connect the peer into 
	 * the network. Instead of randomly sending incoming server to any 
	 * other server we always send it to the first. Once the zone of the 
	 * server is decided it follows the routing algorithm that divides
	 * the space/files.
	 * 
	 * @throws Exception
	 */
	public static void joinNetwork() throws Exception {

		System.out.println("Enter BootStrap IP");
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		String newIP = sc.nextLine();
		try {
			NodeInstance newPeerIncoming = new NodeInstance();
			Registry regi = LocateRegistry.getRegistry(newIP, 9898);
			BootStrapInterface bsObj = (BootStrapInterface) regi
					.lookup(Constants.bindNameBootStrap);
			String bootStrapIP = bsObj.emitBootStrapIP();
			if (bootStrapIP == null) {
				node.setLowerX(0);
				node.setLowerY(0);
				node.setUpperX(10);
				node.setUpperY(10);
				bsObj.setNewBootStrapIP(node.getPeerIP());
				Registry reg = LocateRegistry.createRegistry(9898);
				reg.rebind(Constants.bindNamePeer, newPeerIncoming);
			} else {
				Registry regNew = LocateRegistry.getRegistry(bootStrapIP, 9898);
				NodeInterface bsObjNew = (NodeInterface) regNew.lookup(Constants.bindNamePeer);
				int randomX = (int) (1 + (Math.random() * ((9 - 1) + 1)));
				int randomY = (int) (1 + (Math.random() * ((9 - 1) + 1)));
				Registry regclient = LocateRegistry.createRegistry(9898);
				regclient.rebind(Constants.bindNamePeer, newPeerIncoming);
				bsObjNew.routing(randomX, randomY, node.getPeerIP());
			}
		} catch (Exception e) {
			System.out.println("Exception err " + e.getMessage());
			e.printStackTrace();
		}
	}	

	/**
	 * The routing method checks where the new server belongs to until it 
	 * finds it recursively. Once it finds the zone it calls the splitZone
	 * which is responsible for the split.
	 * 
	 * @param randomX
	 * @param randomY
	 * @param peerIP
	 */
	@Override
	public void routing(int randomX, int randomY, String peerIP) throws RemoteException {

		// check if within the lower x and greater x
		if (node.getUpperX() >= randomX && node.getLowerX() <= randomX) {
			// check if within bounds of lower y and greater y
			if (node.getUpperY() >= randomY && node.getLowerY() <= randomY) {
				splitZone(peerIP);
			}
		} else {
			ServerInformation nearestPeerFound = routeThrough(neighbours, randomX, randomY);
			try {
				Registry tempObj = LocateRegistry
						.getRegistry(nearestPeerFound.getPeerIP(), Constants.portNumber);
				NodeInterface peerInformationTemp = (NodeInterface) tempObj.lookup(Constants.bindNamePeer);
				peerInformationTemp.routing(randomX, randomY, peerIP);
			} catch (Exception e) {
				System.out.println("Expcetion" + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * The routeThrough finds the nearest server where the files might be present.
	 * The routing will exhaust the neigbhour list before looping through the 
	 * nearest server. This will recursively happen until the zone/file is found	 * 
	 * 
	 * @param neigbhours
	 * @param horizontalDistancX
	 * @param horizontalDistancY
	 * @return
	 */
	public static ServerInformation routeThrough(ArrayList<ServerInformation> neigbhours,
			double horizontalDistancX, double horizontalDistancY) {
		double centreX = (neighbours.get(0).getLowerX() + neighbours.get(0).getUpperX()) / 2;
		double centreY = (neighbours.get(0).getLowerY() + neighbours.get(0).getUpperY()) / 2;
		double shortDist = Math.sqrt(Math.pow(centreX - horizontalDistancX, 2)
				+ Math.pow(centreY - horizontalDistancY, 2));
		ServerInformation nearestServer = new ServerInformation();
		for (int i = 0; i < neighbours.size(); i++) {
			double centreX_temp = (neighbours.get(i).getLowerX() + neighbours.get(i).getUpperX()) / 2;
			double centreY_temp = (neighbours.get(i).getLowerY() + neighbours.get(i).getUpperY()) / 2;
			double dist = Math.sqrt(Math.pow(centreX_temp - horizontalDistancX, 2)
					+ Math.pow(centreY_temp - horizontalDistancY, 2));
			if (dist <= shortDist) {
				shortDist = dist;
				nearestServer = neighbours.get(i);
			}
		}
		return nearestServer;
	}
	
	/**
	 * Spitzone depends on how the zone should be split, horizontal or vertical?
	 * After the calculation based on the coordinate position the split is done 
	 * and the files are assigned based to the server respecively. 
	 * We also need to update the servers neighbour list now.
	 * 
	 * @param peerIP
	 */
	public void splitZone(String peerIP) {

		ArrayList<ServerInformation> tempNeigbhor = new ArrayList<ServerInformation>();
		tempNeigbhor = neighbours;
		try {
			// to split vertically
			Registry toSplitObj = LocateRegistry.getRegistry(peerIP, Constants.portNumber);
			NodeInterface toSplitPeer = (NodeInterface) toSplitObj
					.lookup(Constants.bindNamePeer);
			ServerInformation tempNode = toSplitPeer.getNodeInfo();
			if (node.getUpperX() - node.getLowerX() >= node.getUpperY() - node.getLowerY()) {
				// old peer stays on left
				double tempX = node.getUpperX();
				node.setUpperX( (node.getUpperX() + node.getLowerX()) / 2 );
				tempNode.setLowerX(node.getUpperX());
				tempNode.setLowerY(node.getLowerY());
				tempNode.setUpperX(tempX);
				tempNode.setUpperY(node.getUpperY());
				double neighborX = node.getUpperX();
				toSplitPeer.updateNode(tempNode);
				updateFiles(toSplitPeer, tempNode);
				changeNeighborsVertical(tempNeigbhor, tempNode, toSplitPeer, neighborX);
			} else {// old peer stays below
				double tempY = node.getUpperY();
				node.setUpperY( (node.getUpperY() + node.getLowerY()) / 2 );
				tempNode.setLowerX(node.getLowerX());
				tempNode.setLowerY(node.getUpperY());
				tempNode.setUpperX(node.getUpperX());
				tempNode.setUpperY(tempY);
				toSplitPeer.updateNode(tempNode);
				double neigbhorY = node.getUpperY();
				updateFiles(toSplitPeer, tempNode);
				changeNeighborsHorizontal(tempNeigbhor, tempNode, toSplitPeer, neigbhorY);
			}
		} catch (Exception e) {
			System.out.println("Exception " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Based on the orientation of the split server information is updated
	 * 
	 * @param neighbourList
	 * @param nodeToUpdate
	 * @param toSplitPeer
	 * @param distance
	 * 
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void changeNeighborsHorizontal(ArrayList<ServerInformation> neighbourList,
			ServerInformation nodeToUpdate, NodeInterface toSplitPeer, double distance)
			throws RemoteException, NotBoundException {

		if (neighbourList.isEmpty() == true) {
			ServerInformation addNodeTemp = new ServerInformation();
			ServerInformation addNodeTemp2 = new ServerInformation();
			addNodeTemp = nodeToUpdate;
			neighbours.add(addNodeTemp);
			addNodeTemp2 = node;
			toSplitPeer.addNieghbor(addNodeTemp2);
		} else {
			ArrayList<ServerInformation> oldPeerList_down = new ArrayList<ServerInformation>();
			ArrayList<ServerInformation> newPeerList_up = new ArrayList<ServerInformation>();
			ServerInformation tmp1 = new ServerInformation();
			ServerInformation tmp2 = new ServerInformation();
			for (int i = 0; i < neighbourList.size(); i++) {
				// when below
				if (neighbourList.get(i).getUpperY() <= distance && neighbourList.get(i).getLowerY() <= distance) {
					oldPeerList_down.add(neighbourList.get(i));
					Registry neigbhorReg5 = LocateRegistry.getRegistry(
							neighbourList.get(i).getPeerIP(), Constants.portNumber);
					NodeInterface pObj5 = (NodeInterface) neigbhorReg5
							.lookup(Constants.bindNamePeer);
					ArrayList<ServerInformation> tempList = pObj5.getNeibhor();
					ServerInformation old_temp = new ServerInformation();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).getPeerIP().equals(node.getPeerIP())) {
							pObj5.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj5.addNieghbor(old_temp);

				}
				// when above
				else if (neighbourList.get(i).getUpperY() >= distance
						&& neighbourList.get(i).getLowerY() >= distance) {
					newPeerList_up.add(neighbourList.get(i));
					Registry neigbhorReg6 = LocateRegistry.getRegistry(
							neighbourList.get(i).getPeerIP(), Constants.portNumber);
					NodeInterface pObj6 = (NodeInterface) neigbhorReg6
							.lookup(Constants.bindNamePeer);
					ArrayList<ServerInformation> tempList = pObj6.getNeibhor();
					ServerInformation old_temp = new ServerInformation();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).getPeerIP().equals(node.getPeerIP())) {
							pObj6.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj6.addNieghbor(old_temp);
				}
				// when above/below
				else {
					oldPeerList_down.add(neighbourList.get(i));
					newPeerList_up.add(neighbourList.get(i));
					Registry neigbhorReg7 = LocateRegistry.getRegistry(
							neighbourList.get(i).getPeerIP(), Constants.portNumber);
					NodeInterface pObj7 = (NodeInterface) neigbhorReg7
							.lookup(Constants.bindNamePeer);
					ArrayList<ServerInformation> tempList = pObj7.getNeibhor();
					ServerInformation old_temp = new ServerInformation();
					ServerInformation new_temp = new ServerInformation();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).getPeerIP().equals(node.getPeerIP())) {
							pObj7.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					new_temp = toSplitPeer.getNodeInfo();
					pObj7.addNieghbor(old_temp);
					pObj7.addNieghbor(new_temp);
				}
			}
			neighbours = oldPeerList_down;
			for (int l = 0; l < newPeerList_up.size(); l++) {
				toSplitPeer.addNieghbor(newPeerList_up.get(l));
			}
			tmp1 = toSplitPeer.getNodeInfo();
			neighbours.add(tmp1);
			tmp2 = node;
			toSplitPeer.addNieghbor(tmp2);
		}

	}

	/**
	 * Based on the orientation we update the servers
	 * 
	 * @param neighbourList
	 * @param nodeToUpdate
	 * @param toSplitPeer
	 * @param distance
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void changeNeighborsVertical(ArrayList<ServerInformation> neighbourList,
			ServerInformation nodeToUpdate, NodeInterface toSplitPeer, double distance)
			throws RemoteException, NotBoundException {
		if (neighbourList.isEmpty() == true) {
			ServerInformation addNodeTemp = new ServerInformation();
			ServerInformation addNodeTemp2 = new ServerInformation();
			addNodeTemp = nodeToUpdate;
			neighbours.add(addNodeTemp);
			addNodeTemp2 = node;
			toSplitPeer.addNieghbor(addNodeTemp2);
		} else {
			ArrayList<ServerInformation> oldPeerList_left = new ArrayList<ServerInformation>();
			ArrayList<ServerInformation> newPeerList_right = new ArrayList<ServerInformation>();
			ServerInformation tmp3 = new ServerInformation();
			ServerInformation tmp4 = new ServerInformation();
			for (int i = 0; i < neighbourList.size(); i++) {
				// when left
				if (neighbourList.get(i).getLowerX() <= distance && neighbourList.get(i).getUpperX() <= distance) {
					oldPeerList_left.add(neighbourList.get(i));
					Registry neigbhorReg1 = LocateRegistry.getRegistry(
							neighbourList.get(i).getPeerIP(), Constants.portNumber);
					NodeInterface pObj1 = (NodeInterface) neigbhorReg1
							.lookup(Constants.bindNamePeer);
					ArrayList<ServerInformation> tempList = pObj1.getNeibhor();
					ServerInformation old_temp = new ServerInformation();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).getPeerIP().equals(node.getPeerIP())) {
							pObj1.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj1.addNieghbor(old_temp);
				}
				// when right
				else if (neighbourList.get(i).getLowerX() >= distance
						&& neighbourList.get(i).getUpperX() >= distance) {
					newPeerList_right.add(neighbourList.get(i));
					Registry neigbhorReg2 = LocateRegistry.getRegistry(
							neighbourList.get(i).getPeerIP(), Constants.portNumber);
					NodeInterface pObj2 = (NodeInterface) neigbhorReg2
							.lookup(Constants.bindNamePeer);
					ArrayList<ServerInformation> tempList = pObj2.getNeibhor();
					ServerInformation old_temp = new ServerInformation();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).getPeerIP().equals(node.getPeerIP())) {
							pObj2.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj2.addNieghbor(old_temp);
				}
				// when above/below
				else {
					oldPeerList_left.add(neighbourList.get(i));
					newPeerList_right.add(neighbourList.get(i));
					Registry neigbhorReg3 = LocateRegistry.getRegistry(
							neighbourList.get(i).getPeerIP(), Constants.portNumber);
					NodeInterface pObj4 = (NodeInterface) neigbhorReg3
							.lookup(Constants.bindNamePeer);
					ArrayList<ServerInformation> tempList = pObj4.getNeibhor();
					ServerInformation old_temp = new ServerInformation();
					ServerInformation new_temp = new ServerInformation();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).getPeerIP().equals(node.getPeerIP())) {
							pObj4.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					new_temp = toSplitPeer.getNodeInfo();
					pObj4.addNieghbor(old_temp);
					pObj4.addNieghbor(new_temp);
				}
			}
			neighbours = oldPeerList_left;
			for (int l = 0; l < newPeerList_right.size(); l++) {
				toSplitPeer.addNieghbor(newPeerList_right.get(l));
			}
			tmp3 = toSplitPeer.getNodeInfo();
			neighbours.add(tmp3);
			tmp4 = node;
			toSplitPeer.addNieghbor(tmp4);
		}

	}
	
	/**
	 * Insert a file in the server
	 * @throws IOException
	 */
	public static void insertFile() throws IOException {

		System.out.println("Enter the file name");
		@SuppressWarnings("resource")
		Scanner sc1 = new Scanner(System.in);
		String fileName = sc1.nextLine();
		File file = new File(fileName);
		byte buffer[] = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(buffer);
		in.close();
		double hash_x = getHashX(file);
		double hash_y = getHashY(file);

		if (node.getLowerX() <= hash_x && node.getUpperX() >= hash_x && node.getLowerY() <= hash_y
				&& node.getUpperY() >= hash_y) {
			System.out.println("\nFile inserted in SAME Peer: " + node.getPeerIP());
			allFile.put(file, buffer);
		} else {
			ServerInformation temp = routeThrough(neighbours, hash_x, hash_y);
			try {
				Registry tempObj_insert = LocateRegistry.getRegistry(
						temp.getPeerIP(), Constants.portNumber);
				NodeInterface peerInsert = (NodeInterface) tempObj_insert
						.lookup(Constants.bindNamePeer);
				System.out.println("Route from Peer to Destination: \n"
						+ node.getPeerIP() + "\n" + temp.getPeerIP());
				peerInsert.insertRoute(hash_x, hash_y, file, buffer,
						node.getPeerIP());
			} catch (Exception e) {
				System.out.println("Expcetion" + e);
				e.printStackTrace();
			}
		}

	}

	/**
	 * Search for a file in the server. If not found we route through the nearest
	 * server and so on.
	 * 
	 */
	private static void searchFile() {
		@SuppressWarnings("resource")
		Scanner sc2 = new Scanner(System.in);
		System.out.println("Enter File to search:");
		String searchFile = sc2.nextLine();
		File fileSearch = new File(searchFile);
		double hash_x = getHashX(searchFile);
		double hash_y = getHashY(searchFile);

		if (node.getLowerX() <= hash_x && node.getUpperX() >= hash_x && node.getLowerY() <= hash_y
				&& node.getUpperY() >= hash_y) {
			if (allFile.containsKey(fileSearch)) {
				System.out.println("\nFile found in SAME Peer: " + node.getPeerIP());
				System.out.println("File " + searchFile + " found.");
			} else {
				System.out.println("\nFile not found");
			}
		} else {
			ServerInformation temp = routeThrough(neighbours, hash_x, hash_y);
			try {
				Registry tempObj_search = LocateRegistry.getRegistry(
						temp.getPeerIP(), Constants.portNumber);
				NodeInterface peerSearch = (NodeInterface) tempObj_search
						.lookup(Constants.bindNamePeer);
				System.out.println("Route from Peer to Destination: \n"
						+ node.getPeerIP() + "\n" + temp.getPeerIP());
				peerSearch.searchRoute(hash_x, hash_y, fileSearch, node.getPeerIP());
			} catch (Exception e) {
				System.out.println("Expcetion" + e);
				e.printStackTrace();
			}
		}

	}

	/* (non-Javadoc)
	 * @see PeerInterface#insertRoute(double, double, java.io.File, byte[], java.lang.String)
	 */
	@Override
	public void insertRoute(double hx, double hy, File fileName, byte buffer[],
			String ip) throws RemoteException {

		if (node.getLowerX() <= hx && node.getUpperX() >= hx && node.getLowerY() <= hy && node.getUpperY() >= hy) {
			allFile.put(fileName, buffer);
			// allFiles.add(fileName);
		} else {
			ServerInformation temp = routeThrough(neighbours, hx, hy);
			try {
				Registry tempObj_insertRoute = LocateRegistry.getRegistry(
						temp.getPeerIP(), Constants.portNumber);
				NodeInterface peerInsertRoute = (NodeInterface) tempObj_insertRoute
						.lookup(Constants.bindNamePeer);
				peerInsertRoute.printRouteSingle(temp.getPeerIP());
				peerInsertRoute.insertRoute(hx, hy, fileName, buffer, ip);
			} catch (Exception e) {
				System.out.println("Expcetion" + e);
				e.printStackTrace();
			}
		}

	}

	/* (non-Javadoc)
	 * @see PeerInterface#searchRoute(double, double, java.io.File, java.lang.String)
	 */
	@Override
	public void searchRoute(double hx, double hy, File searchName, String ip)
			throws RemoteException {

		if (node.getLowerX() <= hx && node.getUpperX() >= hx && node.getLowerY() <= hy && node.getUpperY() >= hy) {
			try {
				Registry finalReg = LocateRegistry.getRegistry(ip, Constants.portNumber);
				NodeInterface finalPI = (NodeInterface) finalReg.lookup(Constants.bindNamePeer);
				if (allFile.containsKey(searchName)) {
					finalPI.printRoute(searchName, ip);
				} else {
					finalPI.printRouteNot(searchName, ip);
				}
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

		} else {
			ServerInformation temp = routeThrough(neighbours, hx, hy);
			try {
				Registry tempObj_searchRoute = LocateRegistry.getRegistry(
						temp.getPeerIP(), Constants.portNumber);
				NodeInterface peerSearchRoute = (NodeInterface) tempObj_searchRoute
						.lookup(Constants.bindNamePeer);
				Registry tempObj_searchRoute1 = LocateRegistry.getRegistry(ip,
						9898);
				NodeInterface peerSearchRoute1 = (NodeInterface) tempObj_searchRoute1
						.lookup(Constants.bindNamePeer);
				peerSearchRoute1.printRouteSingle(temp.getPeerIP());
				peerSearchRoute.searchRoute(hx, hy, searchName, ip);
			} catch (Exception e) {
				System.out.println("Expcetion" + e);
				e.printStackTrace();
			}
		}
	}	

	/**
	 * @param neigbhours
	 * @param hx
	 * @param hy
	 * @return
	 */
	public static ServerInformation routeThrough1(ArrayList<ServerInformation> neigbhours,
			double hx, double hy) {
		double centreX = (neighbours.get(0).getLowerX() + neighbours.get(0).getUpperX()) / 2;
		double centreY = (neighbours.get(0).getLowerY() + neighbours.get(0).getUpperY()) / 2;
		double shortDist = Math.sqrt(Math.pow(centreX - hx, 2)
				+ Math.pow(centreY - hy, 2));
		ServerInformation temp = new ServerInformation();
		for (int i = 0; i < neighbours.size(); i++) {
			if (neigbhours.get(i).getLowerY() <= node.getLowerY()
					|| neigbhours.get(i).getUpperY() >= node.getUpperY()) {
				double centreX_temp = (neighbours.get(i).getLowerX() + neighbours.get(i).getUpperX()) / 2;
				double centreY_temp = (neighbours.get(i).getLowerY() + neighbours.get(i).getUpperY()) / 2;
				double dist = Math.sqrt(Math.pow(centreX_temp - hx, 2)
						+ Math.pow(centreY_temp - hy, 2));
				if (dist <= shortDist) {
					shortDist = dist;
					temp = neighbours.get(i);
				}
			}
		}
		return temp;
	}

	
	/**
	 * Server exits the network. Its zone is given to another server
	 * along with the files. Update the server info again
	 * 
	 * @throws RemoteException
	 * @throws Exception
	 */
	private static void leave() throws RemoteException, Exception {
		// if two peers in network
		if (neighbours.size() == 1) {
			String n_ip = neighbours.get(0).getPeerIP();
			Registry leaveObj = LocateRegistry.getRegistry(n_ip, Constants.portNumber);
			NodeInterface leavePI = (NodeInterface) leaveObj.lookup(Constants.bindNamePeer);
			leavePI.setLX(0);
			leavePI.setLY(0);
			leavePI.setUX(10);
			leavePI.setUY(10);
			leavePI.removeNiebhor(neighbours.get(0));

			@SuppressWarnings("rawtypes")
			Iterator it = allFile.entrySet().iterator();
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				HashMap.Entry pair = (HashMap.Entry) it.next();
				File temps1 = (File) pair.getKey();
				leavePI.addFile(temps1, (byte[]) pair.getValue());
			}
			ArrayList<ServerInformation> abc = new ArrayList<ServerInformation>();
			leavePI.setNeighbor(abc);

			System.out.println("Leaving. thanks");
			System.exit(0);
		} else {
			if (neighbours.size() == 2) {
				if (node.getUpperX() - node.getLowerX() >= node.getUpperY() - node.getLowerY()) {
					// square
					// System.out.println("SQUARE");
					double centreX = node.getUpperX() + node.getLowerX() / 2;
					double centreY = node.getUpperY() + node.getLowerY() / 2;
					ServerInformation t = routeThrough1(neighbours, centreX, centreY);
					Registry leaveObj1 = LocateRegistry.getRegistry(t.getPeerIP(),
							Constants.portNumber);
					NodeInterface leavePI1 = (NodeInterface) leaveObj1
							.lookup(Constants.bindNamePeer);
					ServerInformation tempOr = leavePI1.getNodeInfo();
					if (node.getLowerY() > tempOr.getLowerY()) {
						leavePI1.setUY(node.getUpperY());
						leavePI1.setUX(node.getUpperX());
					} else {
						leavePI1.setLX(tempOr.getLowerX());
						leavePI1.setLY(tempOr.getLowerY());
					}
					// files
					tempOr = leavePI1.getNodeInfo();
					@SuppressWarnings("rawtypes")
					Iterator it = allFile.entrySet().iterator();
					while (it.hasNext()) {
						@SuppressWarnings("rawtypes")
						HashMap.Entry pair = (HashMap.Entry) it.next();
						File temps1 = (File) pair.getKey();
						leavePI1.addFile(temps1, (byte[]) pair.getValue());
					}
					// update neighbors

					if (neighbours.size() >= leavePI1.getNeibhor().size()) {
						leavePI1.setNeighbor(neighbours);
						leavePI1.removeNiebhor(leavePI1.getNodeInfo());
					}
					ArrayList<ServerInformation> tempList = leavePI1.getNeibhor();
					for (int i = 0; i < tempList.size(); i++) {
						ServerInformation x = tempList.get(i);
						Registry leaveObj2 = LocateRegistry.getRegistry(
								x.getPeerIP(), Constants.portNumber);
						NodeInterface leavePI2 = (NodeInterface) leaveObj2
								.lookup(Constants.bindNamePeer);
						ArrayList<ServerInformation> tempList1 = leavePI2.getNeibhor();
						for (int j = 0; j < tempList1.size(); j++) {
							ServerInformation x1 = tempList1.get(j);
							if (tempList1.get(j).getPeerIP().equals(node.getPeerIP())) {
								leavePI2.removeNiebhor(x1);
							}
							if (tempList1.get(j).getPeerIP().equals(tempOr.getPeerIP())) {
								leavePI2.removeNiebhor(x1);
								leavePI2.addNieghbor(tempOr);
							}
						}
					}
					System.out.println("Leaving. Thanks");
					System.exit(0);
				} else {
					// rectangle
					//System.out.println("RECTANGLE");
					ServerInformation n1 = neighbours.get(0);
					ServerInformation n2 = neighbours.get(1);
					Registry o1 = LocateRegistry.getRegistry(n1.getPeerIP(), Constants.portNumber);
					NodeInterface p1 = (NodeInterface) o1.lookup(Constants.bindNamePeer);
					Registry o2 = LocateRegistry.getRegistry(n2.getPeerIP(), Constants.portNumber);
					NodeInterface p2 = (NodeInterface) o2.lookup(Constants.bindNamePeer);
					ServerInformation x1 = p1.getNodeInfo();
					ServerInformation x2 = p2.getNodeInfo();
					if (node.getUpperY() == x1.getUpperY()) {
						// up
						p2.setUX(x1.getUpperX());
						p2.setUY(x1.getUpperY());
						p1.setLX(node.getLowerX());
						p1.setLY(node.getLowerY());
						p1.setUY(node.getUpperY());
						p1.setUX(node.getUpperX());
						
						HashMap<File,byte[]> f = p1.getFileList();
						Iterator it = f.entrySet().iterator();
						while (it.hasNext()) {
							HashMap.Entry pair = (HashMap.Entry) it.next();
							File temps1 = (File) pair.getKey();
							p2.addFile(temps1, (byte[]) pair.getValue());							
						}
						p1.setFileList(allFile);

					} else {
						// down
						p1.setUX(x2.getUpperX());
						p1.setUY(x2.getUpperY());
						p2.setLX(node.getLowerX());
						p2.setLY(node.getLowerY());
						p2.setUY(node.getUpperY());
						p2.setUX(node.getUpperX());
						
						HashMap<File,byte[]> f = p2.getFileList();
						Iterator it = f.entrySet().iterator();
						while (it.hasNext()) {
							HashMap.Entry pair = (HashMap.Entry) it.next();
							File temps1 = (File) pair.getKey();
							p1.addFile(temps1, (byte[]) pair.getValue());							
						}
						p2.setFileList(allFile);
					}
					x1 = p1.getNodeInfo();
					x2 = p2.getNodeInfo();
					ArrayList<ServerInformation> first = new ArrayList<ServerInformation>();
					ArrayList<ServerInformation> sec = new ArrayList<ServerInformation>();
					first.add(x1);
					sec.add(x2);
					p2.setNeighbor(first);
					p1.setNeighbor(sec);
					
					System.out.println("Leaving. Thanks");
					System.exit(0);
				}
			} else {
				System.out.println("You Cannot leave the system. lol!");
			}
		}

	}

	/**
	 * print peer information
	 */
	private static void printPeer() {
		System.out.println("Enter peer IP whose Information you require:");
		Scanner sc5 = new Scanner(System.in);
		String IP = sc5.nextLine();

		try {
			Registry printObj = LocateRegistry.getRegistry(IP, Constants.portNumber);
			NodeInterface printPI = (NodeInterface) printObj.lookup(Constants.bindNamePeer);
			ServerInformation t = printPI.getNodeInfo();
			System.out
					.println("\nPeer IP:" + t.getPeerIP() + " LowerX:" + t.getLowerX()
							+ " LowerY:" + t.getLowerY() + " UpperX:" + t.getUpperX()
							+ " UpperY:" + t.getUpperY());
			print2(printPI.getNeibhor(), t.getPeerIP());
			printFiles(printPI.getFileList(), t.getPeerIP());

		} catch (Exception e) {
			System.out.println("Exception e " + e);
			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see PeerInterface#addFile(java.io.File, byte[])
	 */
	@Override
	public void addFile(File name, byte[] buffer) throws RemoteException {
		allFile.put(name, buffer);
	}

	/**
	 * @param keyword
	 * @return
	 */
	public static double getHashY(String keyword) {
		int hashy = 0;
		for (int i = 1; i < keyword.length(); i = i + 2) {
			hashy = hashy + keyword.charAt(i);
		}
		return hashy % 10;
	}

	/**
	 * @param keyword
	 * @return
	 */
	public static double getHashX(String keyword) {
		int hashx = 0;
		for (int i = 0; i < keyword.length(); i = i + 2) {
			hashx = hashx + keyword.charAt(i);
		}
		return hashx % 10;
	}

	/**
	 * @param _keyword
	 * @return
	 */
	public static double getHashX(File _keyword) {
		String keyword = _keyword.getName();
		int hashx = 0;
		for (int i = 0; i < keyword.length(); i = i + 2) {
			hashx = hashx + keyword.charAt(i);
		}
		return hashx % 10;
	}

	/**
	 * @param _keyword
	 * @return
	 */
	public static double getHashY(File _keyword) {
		String keyword = _keyword.getName();
		int hashy = 0;
		for (int i = 1; i < keyword.length(); i = i + 2) {
			hashy = hashy + keyword.charAt(i);
		}
		return hashy % 10;
	}

	/**
	 * @param list
	 * @param ip
	 */
	public static void print2(ArrayList<ServerInformation> list, String ip) {
		System.out.println("\nNeighbors of Peer: " + ip);
		System.out.println("Peer IP \t lx \t ly \t ux \t uy");
		for (int i = 0; i < list.size(); i++) {
			ServerInformation tmp = list.get(i);
			System.out.println(tmp.getPeerIP() + "\t" + tmp.getLowerX() + "\t" + tmp.getLowerY()
					+ "\t" + tmp.getUpperX() + "\t" + tmp.getUpperY());
		}
		System.out.println();
	}

	/**
	 * @param toSplitPeer
	 * @param tempNode
	 * @throws RemoteException
	 */
	public void updateFiles(NodeInterface toSplitPeer, ServerInformation tempNode)
			throws RemoteException {
		Iterator it = allFile.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) it.next();
			File temps1 = (File) pair.getKey();
			String temps = temps1.getName();
			double hx = getHashX(temps);
			double hy = getHashY(temps);
			if (tempNode.getLowerX() <= hx && tempNode.getUpperX() >= hx && tempNode.getLowerY() <= hy
					&& tempNode.getUpperY() >= hy) {
				it.remove();
				toSplitPeer.addFile(temps1, (byte[]) pair.getValue());
			}
		}
	}

	

	/* (non-Javadoc)
	 * @see PeerInterface#getNodeInfo()
	 */
	@Override
	public ServerInformation getNodeInfo() throws RemoteException {
		return node;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#addNieghbor(NodeInfo)
	 */
	@Override
	public void addNieghbor(ServerInformation _n) throws RemoteException {
		neighbours.add(_n);
	}

	/* (non-Javadoc)
	 * @see PeerInterface#getNeibhor()
	 */
	@Override
	public ArrayList<ServerInformation> getNeibhor() throws RemoteException {
		return neighbours;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#removeNiebhor(NodeInfo)
	 */
	@Override
	public void removeNiebhor(ServerInformation _n) throws RemoteException {
		for (int i = 0; i < neighbours.size(); i++) {
			if (neighbours.get(i).getPeerIP().equals(_n.getPeerIP()))
				neighbours.remove(neighbours.get(i));
		}
	}

	/**
	 * @param allFile2
	 * @param ip
	 */
	public static void printFiles(HashMap<File, byte[]> allFile2, String ip) {
		if (allFile2.isEmpty()) {
			System.out
					.println("\nFile is Empty. Peer does not contain any files.");
		} else {
			System.out.println("\nList of Files in Peer: " + ip);
			int count = 1;
			for (File key : allFile2.keySet()) {
				System.out.println(count + ". " + key.getName());
				count++;
			}
		}
		System.out.println();
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setLX(double)
	 */
	public void setLX(double _lx) {
		node.setLowerX(_lx);
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setUX(double)
	 */
	public void setUX(double _ux) {
		node.setUpperX(_ux);
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setLY(double)
	 */
	public void setLY(double _ly) {
		node.setLowerY(_ly);
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setUY(double)
	 */
	public void setUY(double _uy) {
		node.setUpperY(_uy);
	}

	/* (non-Javadoc)
	 * @see PeerInterface#updateNode(NodeInfo)
	 */
	@Override
	public void updateNode(ServerInformation _n) throws RemoteException {
		node = _n;
	}
	
	/* (non-Javadoc)
	 * @see PeerInterface#printRouteSingle(java.lang.String)
	 */
	@Override
	public void printRouteSingle(String _ip) throws RemoteException {
		System.out.println(_ip);
	}

	/* (non-Javadoc)
	 * @see PeerInterface#printRoute(java.io.File, java.lang.String)
	 */
	@Override
	public void printRoute(File name, String ip) throws RemoteException {
		System.out.println("File: " + name + " found.");
	}

	/* (non-Javadoc)
	 * @see PeerInterface#printRouteNot(java.io.File, java.lang.String)
	 */
	@Override
	public void printRouteNot(File name, String ip) throws RemoteException {
		System.out.println("\nFile not found");
	}

	/* (non-Javadoc)
	 * @see PeerInterface#getFileList()
	 */
	@Override
	public HashMap<File, byte[]> getFileList() throws RemoteException {
		return allFile;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setNeighbor(java.util.ArrayList)
	 */
	@Override
	public void setNeighbor(ArrayList<ServerInformation> list) throws RemoteException {
		// TODO Auto-generated method stub
		neighbours = list;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setFileList(java.util.HashMap)
	 */
	@Override
	public void setFileList(HashMap<File, byte[]> list) throws RemoteException {
		allFile = list;		
	}	

}
