package nodeInfo;
import java.io.Serializable;

/**
 * The NodeInfo class is a simulation of a real world server. This class contains different information
 * about the server. The server typically has an associated DNS domain name, and that this
 * resolves to the IP address of one or more server bootstrap nodes. I have made small tweaking here.
 * Instead of servers maintaining the IP address we are required to provide the bootstrapServer IP to it.
 * 
 * @author karan
 *
 */
public class NodeInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//The cooridnates of the server on the 2 dimensional space 
	public double lx;
	public double ly;
	public double ux;
	public double uy;
	
	//BootStrapServer Ip address
	public String peerIP = null;
	
	/*
	* @Constructor
	*/
	public NodeInfo(){
		
	}

	/**
	 * @param _lx
	 */
	public void setLowerX(double _lx) {
		this.lx = _lx;
	}

	/**
	 * @param _ly
	 */
	public void setLowerY(double _ly) {
		this.ly = _ly;
	}

	/**
	 * @param _ux
	 */
	public void setUpperX(double _ux) {
		this.ux = _ux;
	}

	/**
	 * @param _uy
	 */
	public void setUpperY(double _uy) {
		this.uy = _uy;
	}

	/**
	 * @param _ip
	 */
	public void setIP(String _ip) {
		this.peerIP = _ip;
	}

	/**
	 * @return
	 */
	public String getIP() {
		return this.peerIP;
	}
}
