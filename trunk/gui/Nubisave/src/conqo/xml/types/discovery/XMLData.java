package conqo.xml.types.discovery;

/**
 * Data of every XML-Message.
 * 
 * @author Bastian Buder
 *
 */
public class XMLData {
	
	/**
	 * Called Method.
	 */
	private String callname = "";
	
	
	/**
	 * Constructor
	 */
	public XMLData() {
		
	}
	
	
	/**
	 * Get the name of the called method.
	 * 
	 * @return Name of method.
	 */
	public String getCallname() {
		return callname;
	}
	
	
	/**
	 * Set the name of the called method.
	 * 
	 * @param callname Name of method
	 */
	public void setCallname(String callname) {
		this.callname = callname;
	}
}
