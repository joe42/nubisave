package conqo.xml.parser.discovery;

import org.w3c.dom.NodeList;

import conqo.xml.parser.exception.ParserException;
import conqo.xml.types.discovery.WSInfo;
import conqo.xml.types.discovery.XMLGetAllServicesRespData;


/**
 * Class for parsing XML response of method GetAllServices.
 * 
 * @author Bastian Buder
 * 
 */
public class XMLGetAllServicesResponseParser extends XMLResponseParser {

	/**
	 * Constructor of parser.
	 * 
	 * @param xml
	 *            XML message as String
	 * @throws Exception
	 */
	public XMLGetAllServicesResponseParser(String xml) throws Exception {		
		super(xml);
		// System.out.println("############ XML: " + xml);
	}

	@Override
	public XMLGetAllServicesRespData parse() throws ParserException {
		XMLGetAllServicesRespData resp = new XMLGetAllServicesRespData();

		resp.setCallname(this.callName);
		resp.setDescription(this.description);
		resp.setErrorCode(this.errorCode);
		resp.setStatus(this.status);
		resp.setTimeStamp(this.timeStamp);

		// now parse result values
		if (this.status.toLowerCase().equals("success")) {

			try {
				NodeList nl;
				nl = doc.getElementsByTagName("tns:Service");

				int i;
				for (i = 0; i < nl.getLength(); i++) {

					NodeList snl = nl.item(i).getChildNodes();
					WSInfo wsInfo = new WSInfo();

					// get IRIs
					int k;
					for (k = 0; k < snl.getLength(); k++) {

						if (snl.item(k).getNodeName().equals("tns:ServiceIRI")) {
							wsInfo.setIri(snl.item(k).getTextContent());
						} else if (snl.item(k).getNodeName().equals(
								"tns:ImageURL")) {
							wsInfo.setImageURL(snl.item(k).getTextContent());
						} else if (snl.item(k).getNodeName().equals(
								"tns:UserName")) {
							wsInfo.setUsername(snl.item(k).getTextContent());
						} else if (snl.item(k).getNodeName().equals(
								"tns:ServiceName")) {
							wsInfo.setLocalName(snl.item(k).getTextContent());
						} else if (snl.item(k).getNodeName().equals(
								"tns:Interfaces")) {
							NodeList inl = snl.item(k).getChildNodes();

							int n;
							for (n = 0; n < inl.getLength(); n++) {

								String localName = "";
								String iIRI = "";
								int m;
								NodeList ifacenl = inl.item(n).getChildNodes();

								for (m = 0; m < ifacenl.getLength(); m++) {
									if (ifacenl.item(m).getNodeName().equals("tns:InterfaceIRI")) {
										iIRI = ifacenl.item(m).getTextContent();
									} else if (ifacenl.item(m).getNodeName().equals("tns:InterfaceName")) {
										localName = ifacenl.item(m).getTextContent();
									} else if (ifacenl.item(m).getNodeName().equals("tns:InterfaceDocs")) {
										NodeList idsnl = ifacenl.item(m).getChildNodes();
										// System.out.println("############ InterfaceDocs: " + idsnl.getLength());
										
										int o;
										for (o = 0; o < idsnl.getLength(); o++) {
											NodeList idnl = idsnl.item(o).getChildNodes();
											
											// System.out.println("############ InterfaceParent: " + o + "_" + idsnl.item(o).getNodeName());
											
											String type = null;
											String url = null;
										
											int p;
											NodeList idenl = idsnl.item(o).getChildNodes();

											for (p = 0; p < idenl.getLength(); p++) {
												// System.out.println("############ Interface: " + p + "_" + idenl.item(p).getNodeName());
												if (idenl.item(p).getNodeName().equals("tns:InterfaceDocType")) {
													type = idenl.item(p).getTextContent();
													// System.out.println("############ Type: " + type);
												} else if (idenl.item(p).getNodeName().equals("tns:InterfaceDocURL")) {
													url = idenl.item(p).getTextContent();
													// System.out.println("############ URL: " + url);
												} 													
											}
										
											if (type!=null && url != null)
												wsInfo.addInterfaceDoc(iIRI, type, url);
																					
										}
									}
									
								}
								wsInfo.addInterfaceIRI(iIRI, localName);								
							}

						}
					}
					resp.addWebService(wsInfo);
				}

			} catch (Exception ex) {

			}
		}
		return resp;
	}

}
