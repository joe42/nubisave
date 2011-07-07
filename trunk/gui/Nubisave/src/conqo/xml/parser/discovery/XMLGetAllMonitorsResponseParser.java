package conqo.xml.parser.discovery;

import org.w3c.dom.NodeList;

import conqo.xml.parser.exception.ParserException;
import conqo.xml.types.discovery.MonitorInfo;
import conqo.xml.types.discovery.SLARef;
import conqo.xml.types.discovery.XMLGetAllMonitorsRespData;


/**
 * Class for parsing XML response of method getAllMonitors.
 * 
 * @author Bastian Buder
 *
 */
public class XMLGetAllMonitorsResponseParser extends XMLResponseParser {

	/**
	 * Constructor of parser.
	 * 
	 * @param xml XML message as String.
	 * @throws Exception
	 */
	public XMLGetAllMonitorsResponseParser(String xml) throws Exception {
		super(xml);
	}

	@Override
	public XMLGetAllMonitorsRespData parse() throws ParserException {
		XMLGetAllMonitorsRespData resp = new XMLGetAllMonitorsRespData();
		
		resp.setCallname(this.callName);
		resp.setDescription(this.description);
		resp.setErrorCode(this.errorCode);
		resp.setStatus(this.status);
		resp.setTimeStamp(this.timeStamp);
		
		// now parse result values
		if (this.status.toLowerCase().equals("success")) {
			
			try {
				NodeList nl;
				nl = doc.getElementsByTagName("tns:Monitor");
				
				int i;
				for (i=0; i<nl.getLength(); i++) {
					NodeList snl = nl.item(i).getChildNodes();
					MonitorInfo mInfo = new MonitorInfo();
				
					int k;
					for (k=0; k<snl.getLength(); k++) {
					
						if(snl.item(k).getNodeName().equals("tns:MonitorID")) {
							try {
								int monitorID = Integer.valueOf(snl.item(k).getTextContent());
								mInfo.setId(monitorID);
							} catch(Exception e) {
								
							}													
						}
						else if(snl.item(k).getNodeName().equals("tns:MonitorEndpoint")) {
							mInfo.setEndpoint(snl.item(k).getTextContent());
						}						
						else if(snl.item(k).getNodeName().equals("tns:TargetNamespace")) {
							mInfo.setTargetNamespace(snl.item(k).getTextContent());
						}					
						else if(snl.item(k).getNodeName().equals("tns:SLAs")) {
							
							NodeList inl = snl.item(k).getChildNodes();
						
							int n;
							
							for (n=0; n<inl.getLength(); n++) {
								SLARef slaRef = new SLARef();							
								int m;
								NodeList ifacenl = inl.item(n).getChildNodes();
							
								for (m=0; m<ifacenl.getLength(); m++) {
									if(ifacenl.item(m).getNodeName().equals("tns:TemplateID")) {
										try { 
											int templateID = Integer.valueOf(ifacenl.item(m).getTextContent());
											slaRef.setIdTemplate(templateID);
										} catch(Exception e) {
											
										}										
									}
									else if(ifacenl.item(m).getNodeName().equals("tns:URL")) {
										slaRef.setUrl(ifacenl.item(m).getTextContent());
									}
								}
								mInfo.addSLARef(slaRef);
							}
						
						}
					}
					resp.addMonitorInfo(mInfo);
				}
			
			} catch(Exception ex) {

			}
		}	
		return resp;
	}

}
