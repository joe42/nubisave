package conqo.xml.parser.discovery;

import conqo.xml.types.discovery.XMLRespData;

/**
 * Parser for XML standard response messages.
 * 
 * @author Bastian Buder
 *
 */
public class XMLStandardResponseParser extends XMLResponseParser {
	
	
	/**
	 * Constructor for standard response parser.
	 * 
	 * @param xml XML as String
	 * @throws Exception
	 */
	public XMLStandardResponseParser(String xml) throws Exception {
		super(xml);
	}

	@Override
	public XMLRespData parse() {
		XMLRespData resp = new XMLRespData();
		resp.setCallname(this.callName);
		resp.setDescription(this.description);
		resp.setErrorCode(this.errorCode);
		resp.setStatus(this.status);
		resp.setTimeStamp(this.timeStamp);
		return resp;
	}
	
}
