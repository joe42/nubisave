package conqo.xml.parser.discovery;

import org.w3c.dom.NodeList;

import conqo.xml.parser.exception.ParserException;
import conqo.xml.types.discovery.XMLRespData;


/**
 * Parser for XML response messages.
 * 
 * @author Bastian Buder
 *
 */
public abstract class XMLResponseParser extends XMLParser {
	
	/**
	 * Callname of remote procedure.
	 */
	protected String callName = "";
	
	/**
	 * Error or success description.
	 */
	protected String description = "";
	
	/**
	 * Time of response.
	 */
	protected String timeStamp = "";
	
	/**
	 * Status of execution.
	 */
	protected String status = "";
	
	/**
	 * Errorcode.
	 */
	protected int errorCode = 0;

	
	/**
	 * Abstract method for parsing xml stream.
	 */
	public abstract XMLRespData parse() throws ParserException;
	
	
	/**
	 * Constructor
	 * 
	 * @param xml XML as String
	 * @throws Exception
	 */
	public XMLResponseParser(String xml) throws Exception{
		super(xml);
		
		getConQoMonResp();
	}
	
	
	/**
	 * Convert xml data into java objects.
	 * 
	 * @throws ParserException
	 */
	protected void getConQoMonResp() throws ParserException {
		
		NodeList nl;
		int i;
		
		try {
			nl = doc.getElementsByTagName("tns:CallName");
			this.callName=nl.item(0).getFirstChild().getTextContent();
			
			nl = doc.getElementsByTagName("tns:Status");
			this.status=nl.item(0).getFirstChild().getTextContent();
			
			nl = doc.getElementsByTagName("tns:ResponseMessage").item(0).getChildNodes().item(0).getChildNodes();
			

			for (i=0; i<nl.getLength(); i++) {
				if(nl.item(i).getNodeName().equals("tns:Description")) {
					this.description=nl.item(i).getTextContent();
				}
				else if(nl.item(i).getNodeName().equals("tns:TimeStamp")) {
					this.timeStamp=nl.item(i).getTextContent();
				}
				else if(nl.item(i).getNodeName().equals("tns:ErrorCode")) {
					try {
						this.errorCode=Integer.valueOf(nl.item(i).getTextContent());
					} catch(Exception e) {
						
					}					
				}
			}
			
		} catch(Exception ex) {
			
		}
		
		if(this.callName.equals(""))
			throw new ParserException("missing CallName");
		if(this.status.equals(""))
			throw new ParserException("missing Status");		
	}
}
