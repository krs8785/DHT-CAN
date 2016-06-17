package routingServer.routingServerImpl;
import java.rmi.*;

/**
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
