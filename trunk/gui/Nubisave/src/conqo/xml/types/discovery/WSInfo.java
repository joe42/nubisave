package conqo.xml.types.discovery;

import java.util.HashMap;
import java.util.Map;

/**
 * Information about web services.
 * 
 * @author Bastian Buder
 *
 */
public class WSInfo {

	/**
	 * IRI of the wsml service description.
	 */
	private String iri = "";
	
	/**
	 * URL of provider image.
	 */
	private String imageURL = "";
	
	/**
	 * Name of provider.
	 */
	private String username = "";
	
	/**
	 * Local name of the service.
	 */
	private String localName = "";
	
	/**
	 * Map of interfaces.
	 */
	private HashMap<String, String> interfaces = new HashMap<String, String>();
	
	/**
	 * Interface documents
	 */
	private HashMap<String, HashMap<String, String>> interfaceDocs = new HashMap<String, HashMap<String, String>>();
	
	
	/**
	 * Constructor.
	 */
	public WSInfo() {
		
	}
	
	/**
	 * Set IRI of the service.
	 * 
	 * @param iri IRI of the service
	 */
	public WSInfo(String iri)  {
		this.iri=iri;
	}
	
	
	/**
	 * Get IRI of the service.
	 * 
	 * @return IRI of the service
	 */
	public String getIri() {
		return iri;
	}
	
	
	/**
	 * Set IRI of the service.
	 * 
	 * @param iri IRI of the service
	 */	
	public void setIri(String iri) {
		this.iri = iri;
	}	
	
	
	/**
	 * Get local name of the service.
	 * 
	 * @return Local name of the service
	 */
	public String getLocalName() {
		return localName;
	}
	
	
	/**
	 * Set local name of the service.
	 * 
	 * @param localName Local name of the service
	 */
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	
	
	/**
	 * Get Map of interface IDs.
	 * 
	 * @return Interface IDs
	 */
	public HashMap<String, String> getInterfaces() {
		return interfaces;
	}
	
	
	/**
	 * Set Interface IDs.
	 * 
	 * @param interfaceIDs Interface IDs
	 */
	public void setInterfaces(HashMap<String, String> interfaceIDs) {
		this.interfaces = interfaceIDs;
	}
	
	
	/**
	 * Add an interface ID.
	 * 
	 * @param iIRI IRI of the interface
	 * @param localName Local name of the interface
	 */
	public void addInterfaceIRI(String iIRI, String localName) {
		interfaces.put(iIRI, localName);
	}

	/**
	 * Get provider service image URL.
	 * 
	 * @return provider service image URL.
	 */
	public String getImageURL() {
		return imageURL;
	}

	/**
	 * Set provider service image URL.
	 * 
	 * @param imageURL provider service image URL.
	 */
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	
	/**
	 * Set username
	 * 
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	
	/**
	 * Get username
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	
	/**
	 * Set Interface documents
	 * 
	 * @param interfaceDocs
	 */
	public void setInterfaceDocs(HashMap<String, HashMap<String, String>> interfaceDocs) {
		this.interfaceDocs = interfaceDocs;
	}

	
	/**
	 * Get Interface documents
	 * 
	 * @return interface Documents
	 */
	public HashMap<String, HashMap<String, String>> getInterfaceDocs() {
		return interfaceDocs;
	}
	
	
	/**
	 * Get Interface-Docs 
	 * 
	 * @param iri IRI of Interface
	 * @return 
	 */
	public Map<String, String> getInterfaceDocs(String iri){
		if (this.interfaceDocs.containsKey(iri)) 
			return this.interfaceDocs.get(iri);
		return new HashMap<String, String>();
	}
	
	
	/**
	 * Add document to an interface.
	 * 
	 * @param type
	 * @param url
	 */
	public void addInterfaceDoc(String iri, String type, String url)
	{
		System.out.println("############ Add Doc");
		
		if (type == null || url == null)
			return;
		
		if (interfaceDocs.containsKey(iri)){
			HashMap<String, String> docs = interfaceDocs.get(iri);
			if (docs.containsKey(type))
				return;
			
			docs.put(type, url);			
		} else
		{
			HashMap<String, String> docs = new HashMap<String, String>();
			docs.put(type, url);
			interfaceDocs.put(iri, docs);
		}
		
		System.out.println("DOCS: " + interfaceDocs.toString());
	}
}
