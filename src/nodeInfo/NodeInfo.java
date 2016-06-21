package nodeInfo;
import java.io.Serializable;

/**
 * @author karan
 *
 */
public class NodeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double lx;
	public double ly;
	public double ux;
	public double uy;
	public String peerIP = null;

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
