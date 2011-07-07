package conqo.xml.types.discovery;

import java.util.ArrayList;

/**
 * Instance of this class contains information about a monitoring component.
 * 
 * @author Bastian Buder
 *
 */
public class MonitorInfo {

	/**
	 * Endpoint of the monitor.
	 */
	private String endpoint = "";
	
	/**
	 * Target namespace of the monitor.
	 */
	private String targetNamespace = "";
	
	/**
	 * Password for the monitor.
	 */
	private String password = "";
	
	/**
	 * ID of the monitor in ConQo.
	 */
	private int id = -1;
	
	/**
	 * List of service level agreement references.
	 */
 	private ArrayList<SLARef> slaRefs = new ArrayList<SLARef>();
	
 	
 	/**
 	 * Constructor to create an instance of MonitorInfo.
 	 * 
 	 * @param id ID of the monitor in Conqo
 	 * @param endpoint Endpoint of the monitor
 	 * @param targetNamespace Target namespace of the monitor
 	 * @param password Password of the monitor
 	 */
	public MonitorInfo(int id, String endpoint, String targetNamespace, String password) {
		this.endpoint = endpoint;
		this.targetNamespace = targetNamespace;
		this.password = password;
		this.id = id;
	 	this.slaRefs = new ArrayList<SLARef>();
	}
	
	
	/**
	 * Constructor to create an instance of MonitorInfo.
	 */
	public MonitorInfo() {
		
	}
	
	
	/**
	 * Get the ID of the monitor.
	 * 
	 * @return ID of the monitor
	 */
	public int getId() {
		return id;
	}
	
	
	/**
	 * Set the ID of the monitor.
	 * 
	 * @param id ID of the monitor
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	
	/**
	 * Get the password of the monitor.
	 * 
	 * @return Password of the monitor
	 */
	public String getPassword() {
		return password;
	}
	
	
	/**
	 * Set the password of the monitor.
	 * 
	 * @param password Password of the monitor
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	/**
	 * Get the endpoint of the monitor.
	 * 
	 * @return Endpoint of the monitor
	 */
	public String getEndpoint() {
		return endpoint;
	}
	
	
	/**
	 * Set the enpoint of the monitor.
	 * 
	 * @param endpoint Endpoint of the monitor
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	
	/**
	 * Get the target namespace  of the monitor.
	 * 
	 * @return Target namespace of the monitor
	 */
	public String getTargetNamespace() {
		return targetNamespace;
	}
	
	
	/**
	 * Set the target namespace  of the monitor.
	 * 
	 * @param targetNamespace Target namespace of the monitor
	 */
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}
	
	
	/**
	 * Get a List of corresponding service level agreements. 
	 * 
	 * @return References of corresponding service level agreements
	 */
	public ArrayList<SLARef> getSlaRefs() {
		return slaRefs;
	}
	
	
	/**
	 * Set a List of corresponding service level agreements. 
	 * 
	 * @param slaRefs References of corresponding service level agreements
	 */
	public void setSlaRefs(ArrayList<SLARef> slaRefs) {
		this.slaRefs = slaRefs;
	}
	
	
	/**
	 * Add a reference of a corresponding service level agreement.
	 * 
	 * @param slaRef Reference of a corresponding service level agreement
	 */
	public void addSLARef(SLARef slaRef) {
		this.slaRefs.add(slaRef);
	}
	
}
