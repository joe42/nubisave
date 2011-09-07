package conqo.xml.types.discovery;

/**
 * Class represent data of every XML Response.
 * 
 * @author Bastian Buder
 *
 */
public class XMLRespData extends XMLData {
	
	/**
	 * State of the call.
	 */
	private String status = "";
	
	/**
	 * Error or success description.
	 */
	private String description = "";
	
	/**
	 * Time of the call.
	 */
	private String timeStamp = "";
	
	/**
	 * Errorcode.
	 */
	private int ErrorCode = -1;
	
	
	/**
	 * Constructor.
	 */
	public XMLRespData() {
		super();
	}
	
	
	/**
	 * Get state of the call.
	 * 
	 * @return State as string
	 */
	public String getStatus() {
		return status;
	}
	
	
	/**
	 * Set state of the call.
	 * 
	 * @param status State as string
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	/**
	 * Get Description of error or success.
	 * 
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}
	
	
	/**
	 * Set Description of error or success.
	 * 
	 * @param description Description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * Get the errorcode.
	 * 
	 * @return Errorcode
	 */
	public int getErrorCode() {
		return ErrorCode;
	}
	
	
	/**
	 * Set the errorcode.
	 * 
	 * @param errorCode Errorcode
	 */
	public void setErrorCode(int errorCode) {
		ErrorCode = errorCode;
	}
	
	
	/**
	 * Get the time of the execution.
	 * 
	 * @return Time of execution
	 */
	public String getTimeStamp() {
		return timeStamp;
	}
	
	
	/**
	 * Set the time of the execution.
	 * 
	 * @param timeStamp Time of execution.
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
}
