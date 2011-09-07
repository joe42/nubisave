package conqo.xml.parser.discovery;

import org.w3c.dom.NodeList;

import conqo.xml.parser.exception.ParserException;
import conqo.xml.types.discovery.XMLGetSearchesRespData;


/**
 * Class for parsing XML response of method GetSearches.
 * 
 * @author Bastian Buder
 *
 */
public class XMLGetSearchesResponseParser extends XMLResponseParser {

	/**
	 * Constructor of parser.
	 * 
	 * @param xml XML message as String
	 * @throws Exception
	 */
	public XMLGetSearchesResponseParser(String xml) throws Exception {
		super(xml);
	}

	@Override
	public XMLGetSearchesRespData parse() throws ParserException {
		XMLGetSearchesRespData resp = new XMLGetSearchesRespData();
		
		resp.setCallname(this.callName);
		resp.setDescription(this.description);
		resp.setErrorCode(this.errorCode);
		resp.setStatus(this.status);
		resp.setTimeStamp(this.timeStamp);
		
		// now parse result values
		if (this.status.toLowerCase().equals("success")) {
			
			try {
				NodeList nl;
				nl = doc.getElementsByTagName("tns:GoalContent");
			
				int i;
				for (i=0; i<nl.getLength(); i++) {
					resp.addGoal(nl.item(i).getTextContent());
				}
			} catch(Exception ex) {

			}
		}	
		return resp;
	}

}
