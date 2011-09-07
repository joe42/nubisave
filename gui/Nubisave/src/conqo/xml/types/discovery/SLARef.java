package conqo.xml.types.discovery;


/**
 * Service Level agreement reference.
 * Contains information about a SLA.
 * 
 * @author Bastian Buder
 *
 */
public class SLARef {

	/**
	 * URL of the SLA.
	 */
	private String url = "";
	
	/**
	 * ID of the monitor.
	 */
	private int idMonitor = -1;	
	
	/**
	 * Type of the service level agreement.
	 */
	private String slaType = "";
	
	/**
	 * Template ID of the SLA.
	 */
	private int idTemplate = -1;
	
	/**
	 * TemplateUserID of the SLA.
	 */
	private int idTemplateUser = -1;
		
	
	/**
	 * Constructor
	 * 
	 * @param url URL of SLA
	 * @param idMonitor ID of the monitor
	 * @param slaType Type of the SLA
	 * @param idTemplate TemplateID of the SLA
	 * @param idTemplateUser TemplateUserID of the SLA
	 */
	public SLARef(String url, int idMonitor, String slaType, int idTemplate, int idTemplateUser) {
		this.url = url;
		this.idMonitor = idMonitor;
		this.slaType = slaType;
		this.idTemplate = idTemplate;
		this.idTemplateUser = idTemplateUser;	
	}
	
	
	/**
	 * Constructor
	 */
	public SLARef() {
		
	}
	
	
	/**
	 * Get the URL of the SLA.
	 * 
	 * @return URL of the SLA
	 */
	public String getUrl() {
		return url;
	}
	
	
	/**
	 * Set the URL of the SLA.
	 * 
	 * @param url URL of the SLA
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	/**
	 * Set the ID of the monitor.
	 * 
	 * @param idMonitor MonitorID
	 */
	public void setIdMonitor(int idMonitor) {
		this.idMonitor = idMonitor;
	}
	
	
	/**
	 * Get the ID of the monitor.
	 * 
	 * @return MonitorID
	 */
	public int getIdMonitor() {
		return idMonitor;
	}

	
	/**
	 * Get the type of the SLA.
	 * 
	 * @return Type of the SLA
	 */
	public String getSlaType() {
		return slaType;
	}
	
	
	/**
	 * Set the type of the SLA.
	 * 
	 * @param slaType Type of the SLA
	 */
	public void setSlaType(String slaType) {
		this.slaType = slaType;
	}
	
	
	/**
	 * Get TemplateID of the SLA.
	 * 
	 * @return TemplateID of the SLA.
	 */
	public int getIdTemplate() {
		return idTemplate;
	}
	
	
	/**
	 * Set TemplateID of the SLA.
	 * 
	 * @param idTemplate TemplateID of the SLA.
	 */
	public void setIdTemplate(int idTemplate) {
		this.idTemplate = idTemplate;
	}
	
	
	/**
	 * Get TemplateUserID of the SLA.
	 * 
	 * @return TemplateUserID of the SLA.
	 */
	public int getIdTemplateUser() {
		return idTemplateUser;
	}
	
	
	/**
	 * Set the TemplateUserID of the SLA.
	 * 
	 * @param idTemplateUser TemplateUserID of the SLA.
	 */
	public void setIdTemplateUser(int idTemplateUser) {
		this.idTemplateUser = idTemplateUser;
	}
		
}
