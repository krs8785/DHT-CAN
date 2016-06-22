package peerServer;
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

import peerServer.peerServerInterface.PeerInterface;
import nodeInfo.NodeInfo;
import routingServer.routingServerInterface.BootStrapInterface;

/**
 * The Peer class is the main server class which consists of all the different functionality 
 * that it can perform. The peer essentially is a server that when loaded up connects to the system
 * via the bootstrap server. The server coantins its own information about the location ie coordinates,
 * IP, etc. It always maintains a list of neighbouring server for the routing algorithm. 
 * NOTE: In order to be distrubted in true sense it does not need to contain all the files or any metadata
 *, it routes through the system to feth the file. 
 * 
 * @author karan
 *
 */
public class Peer extends UnicastRemoteObject implements Serializable,
		PeerInterface {

	/**
	 * Information such as the NodeInfo which contains the
	 * lx ly ux uy and IP. 
	 * List of neighbors
	 * List of files
	 */
	private static final long serialVersionUID = 1L;
	static NodeInfo node = new NodeInfo();
	static ArrayList<NodeInfo> neighbours;
	static HashMap<File, byte[]> allFile = new HashMap<File, byte[]>();

	/**
	 * Set the IP
	 * Initialize the list
	 * @throws Exception
	 */
	public Peer() throws Exception {

		try {
			node.setIP(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		neighbours = new ArrayList<NodeInfo>();
		System.out.println("*Ip of this peer: " + node.peerIP + "*");
	}

	/**
	 * Ask Peer to perform operations
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		int option;
		int option2;
		boolean flag = true;
		while (flag == true) {
			System.out.println("1. Join");
			System.out.println("2. Exit");
			option = sc.nextInt();
			switch (option) {
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
						option2 = sc.nextInt();

						switch (option2) {
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
							print2(neighbours, node.peerIP);
							break;
						case 6:
							printFiles(allFile, node.peerIP);
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
	 * Works for 2-3 nodes only
	 * 
	 * @throws RemoteException
	 * @throws Exception
	 */
	private static void leave() throws RemoteException, Exception {
		// if two peers in network
		if (neighbours.size() == 1) {
			String n_ip = neighbours.get(0).peerIP;
			Registry leaveObj = LocateRegistry.getRegistry(n_ip, 9898);
			PeerInterface leavePI = (PeerInterface) leaveObj.lookup("peer");
			leavePI.setLX(0);
			leavePI.setLY(0);
			leavePI.setUX(10);
			leavePI.setUY(10);
			leavePI.removeNiebhor(neighbours.get(0));

			Iterator it = allFile.entrySet().iterator();
			while (it.hasNext()) {
				HashMap.Entry pair = (HashMap.Entry) it.next();
				File temps1 = (File) pair.getKey();
				leavePI.addFile(temps1, (byte[]) pair.getValue());
			}
			ArrayList<NodeInfo> abc = new ArrayList<NodeInfo>();
			leavePI.setNeighbor(abc);

			System.out.println("Leaving. thanks");
			System.exit(0);
		} else {
			if (neighbours.size() == 2) {
				if (node.ux - node.lx >= node.uy - node.ly) {
					// square
					// System.out.println("SQUARE");
					double centreX = node.ux + node.lx / 2;
					double centreY = node.uy + node.ly / 2;
					NodeInfo t = routeThrough1(neighbours, centreX, centreY);
					Registry leaveObj1 = LocateRegistry.getRegistry(t.peerIP,
							9898);
					PeerInterface leavePI1 = (PeerInterface) leaveObj1
							.lookup("peer");
					NodeInfo tempOr = leavePI1.getNodeInfo();
					if (node.ly > tempOr.ly) {
						leavePI1.setUY(node.uy);
						leavePI1.setUX(node.ux);
					} else {
						leavePI1.setLX(tempOr.lx);
						leavePI1.setLY(tempOr.ly);
					}
					// files
					tempOr = leavePI1.getNodeInfo();
					Iterator it = allFile.entrySet().iterator();
					while (it.hasNext()) {
						HashMap.Entry pair = (HashMap.Entry) it.next();
						File temps1 = (File) pair.getKey();
						leavePI1.addFile(temps1, (byte[]) pair.getValue());
					}
					// update neighbors

					if (neighbours.size() >= leavePI1.getNeibhor().size()) {
						leavePI1.setNeighbor(neighbours);
						leavePI1.removeNiebhor(leavePI1.getNodeInfo());
					}
					ArrayList<NodeInfo> tempList = leavePI1.getNeibhor();
					for (int i = 0; i < tempList.size(); i++) {
						NodeInfo x = tempList.get(i);
						Registry leaveObj2 = LocateRegistry.getRegistry(
								x.peerIP, 9898);
						PeerInterface leavePI2 = (PeerInterface) leaveObj2
								.lookup("peer");
						ArrayList<NodeInfo> tempList1 = leavePI2.getNeibhor();
						for (int j = 0; j < tempList1.size(); j++) {
							NodeInfo x1 = tempList1.get(j);
							if (tempList1.get(j).peerIP.equals(node.peerIP)) {
								leavePI2.removeNiebhor(x1);
							}
							if (tempList1.get(j).peerIP.equals(tempOr.peerIP)) {
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
					NodeInfo n1 = neighbours.get(0);
					NodeInfo n2 = neighbours.get(1);
					Registry o1 = LocateRegistry.getRegistry(n1.peerIP, 9898);
					PeerInterface p1 = (PeerInterface) o1.lookup("peer");
					Registry o2 = LocateRegistry.getRegistry(n2.peerIP, 9898);
					PeerInterface p2 = (PeerInterface) o2.lookup("peer");
					NodeInfo x1 = p1.getNodeInfo();
					NodeInfo x2 = p2.getNodeInfo();
					if (node.uy == x1.uy) {
						// up
						p2.setUX(x1.ux);
						p2.setUY(x1.uy);
						p1.setLX(node.lx);
						p1.setLY(node.ly);
						p1.setUY(node.uy);
						p1.setUX(node.ux);
						
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
						p1.setUX(x2.ux);
						p1.setUY(x2.uy);
						p2.setLX(node.lx);
						p2.setLY(node.ly);
						p2.setUY(node.uy);
						p2.setUX(node.ux);
						
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
					ArrayList<NodeInfo> first = new ArrayList<NodeInfo>();
					ArrayList<NodeInfo> sec = new ArrayList<NodeInfo>();
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
			Registry printObj = LocateRegistry.getRegistry(IP, 9898);
			PeerInterface printPI = (PeerInterface) printObj.lookup("peer");
			NodeInfo t = printPI.getNodeInfo();
			System.out
					.println("\nPeer IP:" + t.peerIP + " LowerX:" + t.lx
							+ " LowerY:" + t.ly + " UpperX:" + t.ux
							+ " UpperY:" + t.uy);
			print2(printPI.getNeibhor(), t.peerIP);
			printFiles(printPI.getFileList(), t.peerIP);

		} catch (Exception e) {
			System.out.println("Exception e " + e);
			e.printStackTrace();
		}
	}

	/**
	 * @throws Exception
	 */
	public static void joinNetwork() throws Exception {
		// TODO Auto-generated method stub

		System.out.println("Enter BootStrap Id");
		Scanner sc = new Scanner(System.in);
		String newIP = sc.nextLine();
		try {
			Peer p = new Peer();
			Registry regi = LocateRegistry.getRegistry(newIP, 9898);
			BootStrapInterface bsObj = (BootStrapInterface) regi
					.lookup("BootStrap");
			String bootStrapIP = bsObj.emitBootStrapIP();
			if (bootStrapIP == null) {
				node.lx = 0;
				node.ly = 0;
				node.ux = 10;
				node.uy = 10;
				bsObj.setNewBootStrapIP(node.peerIP);
				Registry reg = LocateRegistry.createRegistry(9898);
				reg.rebind("peer", p);
			} else {
				Registry regNew = LocateRegistry.getRegistry(bootStrapIP, 9898);
				PeerInterface bsObjNew = (PeerInterface) regNew.lookup("peer");
				int rx = (int) (1 + (Math.random() * ((9 - 1) + 1)));
				int ry = (int) (1 + (Math.random() * ((9 - 1) + 1)));
				Registry regclient = LocateRegistry.createRegistry(9898);
				regclient.rebind("peer", p);
				bsObjNew.routing(rx, ry, node.peerIP);
			}
		} catch (Exception e) {
			System.out.println("Exception err " + e.getMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see PeerInterface#routing(int, int, java.lang.String)
	 */
	@Override
	public void routing(int rx, int ry, String ip) throws RemoteException {

		// check if within the lower x and greater x
		if (node.ux >= rx && node.lx <= rx) {
			// check if within bounds of lower y and greater y
			if (node.uy >= ry && node.ly <= ry) {
				splitZone(ip);
			}
		} else {
			NodeInfo temp = routeThrough(neighbours, rx, ry);
			try {
				Registry tempObj = LocateRegistry
						.getRegistry(temp.peerIP, 9898);
				PeerInterface PI = (PeerInterface) tempObj.lookup("peer");
				PI.routing(rx, ry, ip);
			} catch (Exception e) {
				System.out.println("Expcetion" + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param tempNeigbhor
	 * @param tempNode
	 * @param toSplitPeer
	 * @param d
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void changeNeighborsH(ArrayList<NodeInfo> tempNeigbhor,
			NodeInfo tempNode, PeerInterface toSplitPeer, double d)
			throws RemoteException, NotBoundException {

		if (tempNeigbhor.isEmpty() == true) {
			NodeInfo addNodeTemp = new NodeInfo();
			NodeInfo addNodeTemp2 = new NodeInfo();
			addNodeTemp = tempNode;
			neighbours.add(addNodeTemp);
			addNodeTemp2 = node;
			toSplitPeer.addNieghbor(addNodeTemp2);
		} else {
			ArrayList<NodeInfo> oldPeerList_down = new ArrayList<NodeInfo>();
			ArrayList<NodeInfo> newPeerList_up = new ArrayList<NodeInfo>();
			NodeInfo tmp1 = new NodeInfo();
			NodeInfo tmp2 = new NodeInfo();
			for (int i = 0; i < tempNeigbhor.size(); i++) {
				// when below
				if (tempNeigbhor.get(i).uy <= d && tempNeigbhor.get(i).ly <= d) {
					oldPeerList_down.add(tempNeigbhor.get(i));
					Registry neigbhorReg5 = LocateRegistry.getRegistry(
							tempNeigbhor.get(i).peerIP, 9898);
					PeerInterface pObj5 = (PeerInterface) neigbhorReg5
							.lookup("peer");
					ArrayList<NodeInfo> tempList = pObj5.getNeibhor();
					NodeInfo old_temp = new NodeInfo();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).peerIP.equals(node.peerIP)) {
							pObj5.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj5.addNieghbor(old_temp);

				}
				// when above
				else if (tempNeigbhor.get(i).uy >= d
						&& tempNeigbhor.get(i).ly >= d) {
					newPeerList_up.add(tempNeigbhor.get(i));
					Registry neigbhorReg6 = LocateRegistry.getRegistry(
							tempNeigbhor.get(i).peerIP, 9898);
					PeerInterface pObj6 = (PeerInterface) neigbhorReg6
							.lookup("peer");
					ArrayList<NodeInfo> tempList = pObj6.getNeibhor();
					NodeInfo old_temp = new NodeInfo();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).peerIP.equals(node.peerIP)) {
							pObj6.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj6.addNieghbor(old_temp);
				}
				// when above/below
				else {
					oldPeerList_down.add(tempNeigbhor.get(i));
					newPeerList_up.add(tempNeigbhor.get(i));
					Registry neigbhorReg7 = LocateRegistry.getRegistry(
							tempNeigbhor.get(i).peerIP, 9898);
					PeerInterface pObj7 = (PeerInterface) neigbhorReg7
							.lookup("peer");
					ArrayList<NodeInfo> tempList = pObj7.getNeibhor();
					NodeInfo old_temp = new NodeInfo();
					NodeInfo new_temp = new NodeInfo();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).peerIP.equals(node.peerIP)) {
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

	// done
	/**
	 * @param tempNeigbhor
	 * @param tempNode
	 * @param toSplitPeer
	 * @param d
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void changeNeighborsV(ArrayList<NodeInfo> tempNeigbhor,
			NodeInfo tempNode, PeerInterface toSplitPeer, double d)
			throws RemoteException, NotBoundException {
		if (tempNeigbhor.isEmpty() == true) {
			NodeInfo addNodeTemp = new NodeInfo();
			NodeInfo addNodeTemp2 = new NodeInfo();
			addNodeTemp = tempNode;
			neighbours.add(addNodeTemp);
			addNodeTemp2 = node;
			toSplitPeer.addNieghbor(addNodeTemp2);
		} else {
			ArrayList<NodeInfo> oldPeerList_left = new ArrayList<NodeInfo>();
			ArrayList<NodeInfo> newPeerList_right = new ArrayList<NodeInfo>();
			NodeInfo tmp3 = new NodeInfo();
			NodeInfo tmp4 = new NodeInfo();
			for (int i = 0; i < tempNeigbhor.size(); i++) {
				// when left
				if (tempNeigbhor.get(i).lx <= d && tempNeigbhor.get(i).ux <= d) {
					oldPeerList_left.add(tempNeigbhor.get(i));
					Registry neigbhorReg1 = LocateRegistry.getRegistry(
							tempNeigbhor.get(i).peerIP, 9898);
					PeerInterface pObj1 = (PeerInterface) neigbhorReg1
							.lookup("peer");
					ArrayList<NodeInfo> tempList = pObj1.getNeibhor();
					NodeInfo old_temp = new NodeInfo();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).peerIP.equals(node.peerIP)) {
							pObj1.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj1.addNieghbor(old_temp);
				}
				// when right
				else if (tempNeigbhor.get(i).lx >= d
						&& tempNeigbhor.get(i).ux >= d) {
					newPeerList_right.add(tempNeigbhor.get(i));
					Registry neigbhorReg2 = LocateRegistry.getRegistry(
							tempNeigbhor.get(i).peerIP, 9898);
					PeerInterface pObj2 = (PeerInterface) neigbhorReg2
							.lookup("peer");
					ArrayList<NodeInfo> tempList = pObj2.getNeibhor();
					NodeInfo old_temp = new NodeInfo();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).peerIP.equals(node.peerIP)) {
							pObj2.removeNiebhor(tempList.get(j));
						}
					}
					old_temp = node;
					pObj2.addNieghbor(old_temp);
				}
				// when above/below
				else {
					oldPeerList_left.add(tempNeigbhor.get(i));
					newPeerList_right.add(tempNeigbhor.get(i));
					Registry neigbhorReg3 = LocateRegistry.getRegistry(
							tempNeigbhor.get(i).peerIP, 9898);
					PeerInterface pObj4 = (PeerInterface) neigbhorReg3
							.lookup("peer");
					ArrayList<NodeInfo> tempList = pObj4.getNeibhor();
					NodeInfo old_temp = new NodeInfo();
					NodeInfo new_temp = new NodeInfo();
					for (int j = 0; j < tempList.size(); j++) {
						if (tempList.get(j).peerIP.equals(node.peerIP)) {
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
	public static void print2(ArrayList<NodeInfo> list, String ip) {
		System.out.println("\nNeighbors of Peer: " + ip);
		System.out.println("Peer IP \t lx \t ly \t ux \t uy");
		for (int i = 0; i < list.size(); i++) {
			NodeInfo tmp = list.get(i);
			System.out.println(tmp.peerIP + "\t" + tmp.lx + "\t" + tmp.ly
					+ "\t" + tmp.ux + "\t" + tmp.uy);
		}
		System.out.println();
	}

	/**
	 * @param toSplitPeer
	 * @param tempNode
	 * @throws RemoteException
	 */
	public void updateFiles(PeerInterface toSplitPeer, NodeInfo tempNode)
			throws RemoteException {
		Iterator it = allFile.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) it.next();
			File temps1 = (File) pair.getKey();
			String temps = temps1.getName();
			double hx = getHashX(temps);
			double hy = getHashY(temps);
			if (tempNode.lx <= hx && tempNode.ux >= hx && tempNode.ly <= hy
					&& tempNode.uy >= hy) {
				it.remove();
				toSplitPeer.addFile(temps1, (byte[]) pair.getValue());
			}
		}
	}

	/**
	 * @param peerIP
	 */
	public void splitZone(String peerIP) {

		ArrayList<NodeInfo> tempNeigbhor = new ArrayList<NodeInfo>();
		tempNeigbhor = neighbours;
		try {
			// to split vertically
			Registry toSplitObj = LocateRegistry.getRegistry(peerIP, 9898);
			PeerInterface toSplitPeer = (PeerInterface) toSplitObj
					.lookup("peer");
			NodeInfo tempNode = toSplitPeer.getNodeInfo();
			if (node.ux - node.lx >= node.uy - node.ly) {
				// old peer stays on left
				double tempX = node.ux;
				node.ux = (node.ux + node.lx) / 2;
				tempNode.setLowerX(node.ux);
				tempNode.setLowerY(node.ly);
				tempNode.setUpperX(tempX);
				tempNode.setUpperY(node.uy);
				double neighborX = node.ux;
				toSplitPeer.updateNode(tempNode);
				updateFiles(toSplitPeer, tempNode);
				changeNeighborsV(tempNeigbhor, tempNode, toSplitPeer, neighborX);
			} else {// old peer stays below
				double tempY = node.uy;
				node.uy = (node.uy + node.ly) / 2;
				tempNode.setLowerX(node.lx);
				tempNode.setLowerY(node.uy);
				tempNode.setUpperX(node.ux);
				tempNode.setUpperY(tempY);
				toSplitPeer.updateNode(tempNode);
				double neigbhorY = node.uy;
				updateFiles(toSplitPeer, tempNode);
				changeNeighborsH(tempNeigbhor, tempNode, toSplitPeer, neigbhorY);
			}
		} catch (Exception e) {
			System.out.println("Exception " + e);
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see PeerInterface#getNodeInfo()
	 */
	@Override
	public NodeInfo getNodeInfo() throws RemoteException {
		return node;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#addNieghbor(NodeInfo)
	 */
	@Override
	public void addNieghbor(NodeInfo _n) throws RemoteException {
		neighbours.add(_n);
	}

	/* (non-Javadoc)
	 * @see PeerInterface#getNeibhor()
	 */
	@Override
	public ArrayList<NodeInfo> getNeibhor() throws RemoteException {
		return neighbours;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#removeNiebhor(NodeInfo)
	 */
	@Override
	public void removeNiebhor(NodeInfo _n) throws RemoteException {
		for (int i = 0; i < neighbours.size(); i++) {
			if (neighbours.get(i).peerIP.equals(_n.peerIP))
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
		node.lx = _lx;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setUX(double)
	 */
	public void setUX(double _ux) {
		node.ux = _ux;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setLY(double)
	 */
	public void setLY(double _ly) {
		node.ly = _ly;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#setUY(double)
	 */
	public void setUY(double _uy) {
		node.uy = _uy;
	}

	/* (non-Javadoc)
	 * @see PeerInterface#updateNode(NodeInfo)
	 */
	@Override
	public void updateNode(NodeInfo _n) throws RemoteException {
		node = _n;
	}

	/**
	 * @throws IOException
	 */
	public static void insertFile() throws IOException {

		System.out.println("Enter the file name");
		Scanner sc1 = new Scanner(System.in);
		String fileName = sc1.nextLine();
		File file = new File(fileName);
		byte buffer[] = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(buffer);
		in.close();
		double hash_x = getHashX(file);
		double hash_y = getHashY(file);

		if (node.lx <= hash_x && node.ux >= hash_x && node.ly <= hash_y
				&& node.uy >= hash_y) {
			System.out.println("\nFile inserted in SAME Peer: " + node.peerIP);
			allFile.put(file, buffer);
		} else {
			NodeInfo temp = routeThrough(neighbours, hash_x, hash_y);
			try {
				Registry tempObj_insert = LocateRegistry.getRegistry(
						temp.peerIP, 9898);
				PeerInterface peerInsert = (PeerInterface) tempObj_insert
						.lookup("peer");
				System.out.println("Route from Peer to Destination: \n"
						+ node.peerIP + "\n" + temp.peerIP);
				peerInsert.insertRoute(hash_x, hash_y, file, buffer,
						node.peerIP);
			} catch (Exception e) {
				System.out.println("Expcetion" + e);
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 */
	private static void searchFile() {
		Scanner sc2 = new Scanner(System.in);
		System.out.println("Enter File to search:");
		String searchFile = sc2.nextLine();
		File fileSearch = new File(searchFile);
		double hash_x = getHashX(searchFile);
		double hash_y = getHashY(searchFile);

		if (node.lx <= hash_x && node.ux >= hash_x && node.ly <= hash_y
				&& node.uy >= hash_y) {
			if (allFile.containsKey(fileSearch)) {
				System.out.println("\nFile found in SAME Peer: " + node.peerIP);
				System.out.println("File " + searchFile + " found.");
			} else {
				System.out.println("\nFile not found");
			}
		} else {
			NodeInfo temp = routeThrough(neighbours, hash_x, hash_y);
			try {
				Registry tempObj_search = LocateRegistry.getRegistry(
						temp.peerIP, 9898);
				PeerInterface peerSearch = (PeerInterface) tempObj_search
						.lookup("peer");
				System.out.println("Route from Peer to Destination: \n"
						+ node.peerIP + "\n" + temp.peerIP);
				peerSearch.searchRoute(hash_x, hash_y, fileSearch, node.peerIP);
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

		if (node.lx <= hx && node.ux >= hx && node.ly <= hy && node.uy >= hy) {
			allFile.put(fileName, buffer);
			// allFiles.add(fileName);
		} else {
			NodeInfo temp = routeThrough(neighbours, hx, hy);
			try {
				Registry tempObj_insertRoute = LocateRegistry.getRegistry(
						temp.peerIP, 9898);
				PeerInterface peerInsertRoute = (PeerInterface) tempObj_insertRoute
						.lookup("peer");
				peerInsertRoute.printRouteSingle(temp.peerIP);
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

		if (node.lx <= hx && node.ux >= hx && node.ly <= hy && node.uy >= hy) {
			try {
				Registry finalReg = LocateRegistry.getRegistry(ip, 9898);
				PeerInterface finalPI = (PeerInterface) finalReg.lookup("peer");
				if (allFile.containsKey(searchName)) {
					finalPI.printRoute(searchName, ip);
				} else {
					finalPI.printRouteNot(searchName, ip);
				}
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

		} else {
			NodeInfo temp = routeThrough(neighbours, hx, hy);
			try {
				Registry tempObj_searchRoute = LocateRegistry.getRegistry(
						temp.peerIP, 9898);
				PeerInterface peerSearchRoute = (PeerInterface) tempObj_searchRoute
						.lookup("peer");
				Registry tempObj_searchRoute1 = LocateRegistry.getRegistry(ip,
						9898);
				PeerInterface peerSearchRoute1 = (PeerInterface) tempObj_searchRoute1
						.lookup("peer");
				peerSearchRoute1.printRouteSingle(temp.peerIP);
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
	public static NodeInfo routeThrough(ArrayList<NodeInfo> neigbhours,
			double hx, double hy) {
		double centreX = (neighbours.get(0).lx + neighbours.get(0).ux) / 2;
		double centreY = (neighbours.get(0).ly + neighbours.get(0).uy) / 2;
		double shortDist = Math.sqrt(Math.pow(centreX - hx, 2)
				+ Math.pow(centreY - hy, 2));
		NodeInfo temp = new NodeInfo();
		for (int i = 0; i < neighbours.size(); i++) {
			double centreX_temp = (neighbours.get(i).lx + neighbours.get(i).ux) / 2;
			double centreY_temp = (neighbours.get(i).ly + neighbours.get(i).uy) / 2;
			double dist = Math.sqrt(Math.pow(centreX_temp - hx, 2)
					+ Math.pow(centreY_temp - hy, 2));
			if (dist <= shortDist) {
				shortDist = dist;
				temp = neighbours.get(i);
			}
		}
		return temp;
	}

	/**
	 * @param neigbhours
	 * @param hx
	 * @param hy
	 * @return
	 */
	public static NodeInfo routeThrough1(ArrayList<NodeInfo> neigbhours,
			double hx, double hy) {
		double centreX = (neighbours.get(0).lx + neighbours.get(0).ux) / 2;
		double centreY = (neighbours.get(0).ly + neighbours.get(0).uy) / 2;
		double shortDist = Math.sqrt(Math.pow(centreX - hx, 2)
				+ Math.pow(centreY - hy, 2));
		NodeInfo temp = new NodeInfo();
		for (int i = 0; i < neighbours.size(); i++) {
			if (neigbhours.get(i).ly <= node.ly
					|| neigbhours.get(i).uy >= node.uy) {
				double centreX_temp = (neighbours.get(i).lx + neighbours.get(i).ux) / 2;
				double centreY_temp = (neighbours.get(i).ly + neighbours.get(i).uy) / 2;
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
	public void setNeighbor(ArrayList<NodeInfo> list) throws RemoteException {
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
