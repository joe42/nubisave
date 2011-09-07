package conqo.xml.types.discovery;

import java.util.ArrayList;

/**
 * Information about monitors.
 * 
 * @author Bastian Buder
 *
 */
public class XMLGetAllMonitorsRespData extends XMLRespData {

	/**
	 * List of MonitorInfos.
	 */
	private ArrayList<MonitorInfo> monitorInfos = new ArrayList<MonitorInfo>();
	
	
	/**
	 * Constructor
	 */
	public XMLGetAllMonitorsRespData() {
		super();
	}
	
	
	/**
	 * Get all monitors.
	 * 
	 * @return MonitorInfos
	 */
	public ArrayList<MonitorInfo> getMonitorInfos() {
		return monitorInfos;
	}
	
	
	/**
	 * Set MonitorInfos.
	 * 
	 * @param monitorInfos MonitorInfos
	 */
	public void setMonitorInfos(ArrayList<MonitorInfo> monitorInfos) {
		this.monitorInfos = monitorInfos;
	}
	
	
	/**
	 * Add a MonitorInfo.
	 * 
	 * @param mInfo MonitorInfo
	 */
	public void addMonitorInfo(MonitorInfo mInfo) {
		monitorInfos.add(mInfo);
	}
}
