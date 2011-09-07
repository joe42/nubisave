package conqo.xml.types.discovery;

import java.util.HashSet;

/**
 * Information about web services.
 * 
 * @author Bastian Buder
 *
 */
public class XMLGetAllServicesRespData extends XMLRespData {
	
	/**
	 * Set of WSInfos.
	 */
	private HashSet<WSInfo> webServices = null;
	
	
	/**
	 * Constructor
	 */
	public XMLGetAllServicesRespData() {
		super();
		webServices = new HashSet<WSInfo>();
		
	}
	
	
	/**
	 * Get web service infos.
	 * 
	 * @return Web service infos
	 */
	public HashSet<WSInfo> getWebServices() {
		return webServices;
	}
	
	
	/**
	 * Set web service infos.
	 * 
	 * @param webServices Web service infos
	 */
	public void setWebServices(HashSet<WSInfo> webServices) {
		this.webServices = webServices;
	}
	
	
	/**
	 * Add a web service info.
	 * 
	 * @param wsInfo Web service info
	 */
	public void addWebService(WSInfo wsInfo) {
		webServices.add(wsInfo);
	}

}
