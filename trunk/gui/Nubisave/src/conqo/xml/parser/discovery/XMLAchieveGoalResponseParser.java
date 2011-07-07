package conqo.xml.parser.discovery;

import org.w3c.dom.NodeList;

import conqo.xml.parser.exception.ParserException;
import conqo.xml.types.discovery.InterfaceInfo;
import conqo.xml.types.discovery.Requirement;
import conqo.xml.types.discovery.XMLAchieveGoalRespData;


/**
 * Parser for XML response from AchieveGoal method.
 * 
 * @author Bastian Buder
 *
 */
public class XMLAchieveGoalResponseParser extends XMLResponseParser {

	/**
	 * Constructor
	 * 
	 * @param xml XML as String
	 * @throws Exception
	 */
	public XMLAchieveGoalResponseParser(String xml) throws Exception {
		super(xml);
	}

	@Override
	public XMLAchieveGoalRespData parse() throws ParserException {
		XMLAchieveGoalRespData resp = new XMLAchieveGoalRespData();
		
		resp.setCallname(this.callName);
		resp.setDescription(this.description);
		resp.setErrorCode(this.errorCode);
		resp.setStatus(this.status);
		resp.setTimeStamp(this.timeStamp);
		
		// now parse result values
		if (this.status.toLowerCase().equals("success")) {
			
			try {
			
				NodeList nl;
				int i;
				nl = doc.getElementsByTagName("tns:ServicesTotal");
				
				try {
					int servicesTotal = Integer.valueOf(nl.item(0).getTextContent());
					resp.setServicesTotal(servicesTotal);
				} catch(Exception e) {
					
				}
				
				nl = doc.getElementsByTagName("tns:RankedServices");
				
				try {
					int rankedServices = Integer.valueOf(nl.item(0).getTextContent());
					resp.setRankedServices(rankedServices);
				} catch(Exception e) {
					
				}
				
				nl = doc.getElementsByTagName("tns:TakenTime").item(0).getChildNodes();
				
				
				
				for(i=0; i<nl.getLength(); i++) {
					if (nl.item(i).getNodeName().equals("tns:FunctionalDiscovery")) {
						try {
							int funcDiscTime = Integer.valueOf(nl.item(i).getTextContent());
							resp.setFuncDiscTime(funcDiscTime);
						} catch(Exception e) {
							
						}
					}
					else if (nl.item(i).getNodeName().equals("tns:NonFunctionalDiscovery")) {
						try {
							int nonFuncDiscTime = Integer.valueOf(nl.item(i).getTextContent());
							resp.setNonFuncDiscTime(nonFuncDiscTime);
						} catch(Exception e) {
							
						}
					}
					else if (nl.item(i).getNodeName().equals("tns:Ranking")) {
						try {
							int rankingTime = Integer.valueOf(nl.item(i).getTextContent());
							resp.setRankingTime(rankingTime);
						} catch(Exception e) {
							
						}
					}
					
				}
				
				nl = doc.getElementsByTagName("tns:CountStepRejecting").item(0).getChildNodes();
				
				for(i=0; i<nl.getLength(); i++) {
					if (nl.item(i).getNodeName().equals("tns:FunctionalDiscovery")) {
						try {
							int funcRejected = Integer.valueOf(nl.item(i).getTextContent());
							resp.setFuncRejected(funcRejected);
						} catch(Exception e) {
							
						}
					}
					else if (nl.item(i).getNodeName().equals("tns:NonFunctionalDiscovery")) {
						try {
							int nonFuncRejected = Integer.valueOf(nl.item(i).getTextContent());
							resp.setNonFuncRejected(nonFuncRejected);
						} catch(Exception e) {
							
						}
					}					
				}
				
				
				nl = doc.getElementsByTagName("tns:RankingResult").item(0).getChildNodes();
				
				for(i=0; i<nl.getLength(); i++) {
					
					InterfaceInfo iInfo = new InterfaceInfo();
					NodeList rsnl=nl.item(i).getChildNodes();
					int l;
					
					for(l=0; l<rsnl.getLength(); l++) {
						
					
					if (rsnl.item(l).getNodeName().equals("tns:IRI")) {
						iInfo.setIri(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:SLA")) {
						iInfo.setSlaPath(rsnl.item(l).getTextContent());
					}
					/*
					else if (rsnl.item(l).getNodeName().equals("tns:SLAContent")) {
						iInfo.setSlaContent(rsnl.item(l).getTextContent());
					}
					*/
					else if (rsnl.item(l).getNodeName().equals("tns:SLATemplateID")) {
						iInfo.setSlaTemplateID(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:MonitorID")) {
						iInfo.setMonitorID(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:MonitorEndpoint")) {
						iInfo.setMonitorEndpoint(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:WSDL")) {
						iInfo.setWsdlPath(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:WSML")) {
						iInfo.setWsmlPath(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:ServiceLocalName")) {
						iInfo.setServiceLocalName(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:InterfaceLocalName")) {
						iInfo.setInterfaceLocalName(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:GoalRequirements")) {
						NodeList grnl;
						grnl = rsnl.item(l).getChildNodes();
						int k;

						
						
						for(k=0; k<grnl.getLength(); k++) {
							
							Requirement goalRequirement = new Requirement();
							
							NodeList grdnl=grnl.item(k).getChildNodes();
							
							int m;
							for (m=0; m<grdnl.getLength(); m++) {
								
							
							
								if (grdnl.item(m).getNodeName().equals("tns:Parameter")) {
									goalRequirement.setParameter(grdnl.item(m).getTextContent());
								}
								else if (grdnl.item(m).getNodeName().equals("tns:Value")) {
									try {
										Double value = Double.valueOf(grdnl.item(m).getTextContent().replace(',', '.'));
										goalRequirement.setValue(value);
									} catch(Exception e) {
									
									}
								}
								else if (grdnl.item(m).getNodeName().equals("tns:SLAParameter")) {
									goalRequirement.setSlaParameter(grdnl.item(m).getTextContent());
								}
								else if (grdnl.item(m).getNodeName().equals("tns:Unit")) {
									goalRequirement.setUnit(grdnl.item(m).getTextContent());
								}
								else if (grdnl.item(m).getNodeName().equals("tns:Satisfied")) {
									if (grdnl.item(m).getTextContent().toLowerCase().equals("yes"))
										goalRequirement.setSatisfied(true);
									else 
										goalRequirement.setSatisfied(false);
								}
								else if (grdnl.item(m).getNodeName().equals("tns:MeasuredValue")) {
									try {
										Double mValue = Double.valueOf(grdnl.item(m).getTextContent().replace(',', '.'));
										goalRequirement.setMeasuredValue(mValue);
									} catch(Exception e) {
										
									}
								}
								else if (grdnl.item(m).getNodeName().equals("tns:MeasureState")) {
									goalRequirement.setMeasuredState(grdnl.item(m).getTextContent());
								}
								else if (grdnl.item(m).getNodeName().equals("tns:Updated")) {
									goalRequirement.setUpdated(grdnl.item(m).getTextContent());
								}
								else if (grdnl.item(m).getNodeName().equals("tns:NoBadStates")) {
									try {
										int badStates = Integer.valueOf(grdnl.item(m).getTextContent());
										goalRequirement.setBadStates(badStates);
									} catch(Exception ex) {
										
									}									
								}								
								
							}
							
							iInfo.addGoalRequirement(goalRequirement);
							
						}
						
					} else if (rsnl.item(l).getNodeName().equals("tns:EnvironmentRequirements")) {
						NodeList ernl;
						ernl = rsnl.item(l).getChildNodes();
						int k;
						
						for(k=0; k<ernl.getLength(); k++) {
							Requirement envRequirement = new Requirement();
							
							NodeList erdnl=ernl.item(k).getChildNodes();
							
							int m;
							for (m=0; m<erdnl.getLength(); m++) {
								if (erdnl.item(m).getNodeName().equals("tns:Parameter")) {
									envRequirement.setParameter(erdnl.item(m).getTextContent());
								}
								else if (erdnl.item(m).getNodeName().equals("tns:Satisfied")) {
									if (erdnl.item(m).getTextContent().toLowerCase().equals("yes"))
										envRequirement.setSatisfied(true);
									else 
										envRequirement.setSatisfied(false);
								}
							}
							
							iInfo.addEnvRequirement(envRequirement);
							
						}
						
					}
					else if (rsnl.item(l).getNodeName().equals("tns:Ranking")) {
						try {
							Double ranking = Double.valueOf(rsnl.item(l).getTextContent().replace(',', '.'));
							iInfo.setRanking(ranking);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
					else if (rsnl.item(l).getNodeName().equals("tns:Score")) {
						try {
							Double score = Double.valueOf(rsnl.item(l).getTextContent().replace(',', '.'));
							iInfo.setScore(score);
						} catch(Exception e) {
							
						}
					}
					
					}
					
					resp.addRankedInterface(iInfo);
					
					
					
					
					
				}
				
				nl = doc.getElementsByTagName("tns:NFRejectedServices").item(0).getChildNodes();
				
				for(i=0; i<nl.getLength(); i++) {
										
					InterfaceInfo iInfo = new InterfaceInfo();
					NodeList rsnl=nl.item(i).getChildNodes();
					int l;
					
					for(l=0; l<rsnl.getLength(); l++) {
						
					
					if (rsnl.item(l).getNodeName().equals("tns:IRI")) {
						iInfo.setIri(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:ServiceLocalName")) {
						iInfo.setServiceLocalName(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:InterfaceLocalName")) {
						iInfo.setInterfaceLocalName(rsnl.item(l).getTextContent());
					}
					else if (rsnl.item(l).getNodeName().equals("tns:GoalRequirements")) {
						NodeList grnl;
						grnl = rsnl.item(l).getChildNodes();
						int k;

						
						
						for(k=0; k<grnl.getLength(); k++) {
							
							Requirement goalRequirement = new Requirement();
							
							NodeList grdnl=grnl.item(k).getChildNodes();
							
							int m;
							for (m=0; m<grdnl.getLength(); m++) {
								
							
							
								if (grdnl.item(m).getNodeName().equals("tns:Parameter")) {
									goalRequirement.setParameter(grdnl.item(m).getTextContent());
								}
								else if (grdnl.item(m).getNodeName().equals("tns:Value")) {
									try {
										Double value = Double.valueOf(grdnl.item(m).getTextContent().replace(',', '.'));
										goalRequirement.setValue(value);
									} catch(Exception e) {
									
									}
								}
								else if (grdnl.item(m).getNodeName().equals("tns:Unit")) {
									goalRequirement.setUnit(grdnl.item(m).getTextContent());
								}
								else if (grdnl.item(m).getNodeName().equals("tns:Satisfied")) {
									if (grdnl.item(m).getTextContent().toLowerCase().equals("yes"))
										goalRequirement.setSatisfied(true);
									else 
										goalRequirement.setSatisfied(false);
								}
								else if (grdnl.item(m).getNodeName().equals("tns:MeasuredValue")) {
									try {
										Double mValue = Double.valueOf(grdnl.item(m).getTextContent().replace(',', '.'));
										goalRequirement.setMeasuredValue(mValue);
									} catch(Exception e) {
										
									}
								}
								else if (grdnl.item(m).getNodeName().equals("tns:MeasureState")) {
									goalRequirement.setMeasuredState(grdnl.item(m).getTextContent());
								}
								
							}
							
							iInfo.addGoalRequirement(goalRequirement);
							
						}
						
					} else if (rsnl.item(l).getNodeName().equals("tns:EnvironmentRequirements")) {
						NodeList ernl;
						ernl = rsnl.item(l).getChildNodes();
						int k;
						
						for(k=0; k<ernl.getLength(); k++) {
							Requirement envRequirement = new Requirement();
							
							NodeList erdnl=ernl.item(k).getChildNodes();
							
							int m;
							for (m=0; m<erdnl.getLength(); m++) {
								if (erdnl.item(m).getNodeName().equals("tns:Parameter")) {
									envRequirement.setParameter(erdnl.item(m).getTextContent());
								}
								else if (erdnl.item(m).getNodeName().equals("tns:Satisfied")) {
									if (erdnl.item(m).getTextContent().toLowerCase().equals("yes"))
										envRequirement.setSatisfied(true);
									else 
										envRequirement.setSatisfied(false);
								}
							}
							
							iInfo.addEnvRequirement(envRequirement);
							
						}
						
					}
					else if (rsnl.item(l).getNodeName().equals("tns:MandatoryFails")) {
						iInfo.setMandatoryFails(rsnl.item(l).getTextContent().replace(',', '.'));
					}
					
					}
					
					resp.addNFFailInterface(iInfo);
				}
				
			} catch(Exception ex) {

			}
		}	
		return resp;
	}

}
