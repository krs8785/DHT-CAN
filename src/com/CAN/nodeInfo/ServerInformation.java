package com.CAN.nodeInfo;
import java.io.Serializable;

/**
 * The ServerInfo class is a simulation of a real world server. This class contains different 
 * information  about the server. The server typically has an associated DNS domain name, and 
 * that this resolves to the IP address of one or more server bootstrap nodes. I have made small 
 * tweaking here. Instead of servers maintaining the IP address we are required to provide the 
 * bootstrapServer IP to it.
 * 
 * @author karan
 *
 */
public class ServerInformation implements Serializable {

	
	private static final long serialVersionUID = 1L;
	

	//The cooridnates of the server on the 2 dimensional space 
	public double lowerX;
	public double lowerY;
	public double upperX;
	public double upperY;
	
	//BootStrapServer Ip address
	public String peerIP = null;

	/**
	 * @param _lx
	 */
	public void setLowerX(double _lx) {
		this.lowerX = _lx;
	}
	
	/**
	 * @return
	 */
	public double getLowerX() {
		return lowerX;
	}

	/**
	 * @param _ly
	 */
	public void setLowerY(double _ly) {
		this.lowerY = _ly;
	}
	
	/**
	 * @return
	 */
	public double getLowerY() {
		return lowerY;
	}

	/**
	 * @param _ux
	 */
	public void setUpperX(double _ux) {
		this.upperX = _ux;
	}

	/**
	 * @return
	 */
	public double getUpperX() {
		return upperX;
	}
	
	/**
	 * @param _uy
	 */
	public void setUpperY(double _uy) {
		this.upperY = _uy;
	}

	/**
	 * @return
	 */
	public double getUpperY() {
		return upperY;
	}
	
	/**
	 * @param _ip
	 */
	public void setPeerIP(String _ip) {
		this.peerIP = _ip;
	}

	/**
	 * @return
	 */
	public String getPeerIP() {
		return this.peerIP;
	}
}
