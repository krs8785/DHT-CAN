package routingServer;

import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import routingServer.routingServerInterface.BootStrapInterface;

/**
 * @author karan
 *
 */
public class BootStrapServer extends UnicastRemoteObject implements
		BootStrapInterface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
			Registry reg = LocateRegistry.createRegistry(9898);
			reg.rebind("BootStrap", obj);
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
