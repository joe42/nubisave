package conqo.xml.types.discovery;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about web service interfaces.
 * 
 * @author Bastian Buder
 *
 */
public class InterfaceInfo {
	
	/**
	 * IRI of the interface.
	 */
	private String iri = "";
	
	/**
	 * List of goalRequirements.
	 */
	private List<Requirement> goalRequirements = new ArrayList<Requirement>();
	
	/**
	 * List of environment requirements.
	 */
	private List<Requirement> envRequirements = new ArrayList<Requirement>();
	
	/**
	 * Ranking of the interface.
	 */
	private Double ranking = 0.0;
	
	/**
	 * Score of the interface.
	 */
	private Double score = 0.0;
	
	/**
	 * LocalName of the corresponding web service.
	 */
	private String serviceLocalName = "";
	
	/**
	 * LocalName of the interface.
	 */
	private String interfaceLocalName = "";
	
	/**
	 * True, if an mandatory requirement fails.
	 */
	private String mandatoryFails = "";
	
	/**
	 * URL of the SLA.
	 */
	private String slaPath = "";
	
	
	/**
	 * URL of the WSDL.
	 */
	private String wsdlPath = "";
	
	
	/**
	 * URL of the WSML.
	 */
	private String wsmlPath = "";
	
	
	/**
	 * TemplateID of the SLA.
	 */
	private String slaTemplateID = "";
	
	/**
	 * ID of the monitor.
	 */
    private String monitorID = "";
    
    /**
     * Endpoint of the monitor.
     */
	private String monitorEndpoint = "";
    
    
    /**
     * Constructor.
     */
	public InterfaceInfo() {		
	}	
	
	
	/**
	 * Get the IRI of the interface.
	 * 
	 * @return IRI of the interface
	 */
	public String getIri() {
		return iri;
	}

	
	/**
	 * Set the IRI of the interface.
	 * 
	 * @param iri IRI of the interface
	 */
	public void setIri(String iri) {
		this.iri = iri;
	}
	
	
	/**
	 * Get the environment requirements.
	 * 
	 * @return Environment requirements
	 */
	public List<Requirement> getEnvRequirements() {
		return envRequirements;
	}
	
	
	/**
	 * Add an environment requirement.
	 * 
	 * @param envRequirement Environment requirement
	 */
	public void addEnvRequirement(Requirement envRequirement) {
		this.envRequirements.add(envRequirement);
	}
	
	
	/**
	 * Set the environment requirements.
	 * 
	 * @param envRequirements environment requirements
	 */
	public void setEnvRequirements(List<Requirement> envRequirements) {
		this.envRequirements = envRequirements;
	}
	
	
	/**
	 * Get the goal requirements.
	 * 
	 * @return GoalRequirements goal requirements
	 */
	public List<Requirement> getGoalRequirements() {
		return goalRequirements;
	}
	
	
	/**
	 * Set the goal requirements.
	 * 
	 * @param goalRequirements Goal requirements
	 */
	public void setGoalRequirements(List<Requirement> goalRequirements) {
		this.goalRequirements = goalRequirements;
	}
	
	
	/**
	 * Add a goal requirement.
	 * 
	 * @param goalRequirement Goal requirement
	 */
	public void addGoalRequirement(Requirement goalRequirement) {
		this.goalRequirements.add(goalRequirement);
	}
	
	
	/**
	 * Get the ranking of the interface.
	 * 
	 * @return Ranking of interface
	 */
	public Double getRanking() {
		return ranking;
	}
	
	
	/**
	 * Set the ranking of the interface.
	 * 
	 * @param ranking Ranking of interface
	 */
	public void setRanking(Double ranking) {
		this.ranking = ranking;
	}
	
	
	/**
	 * Get the score of the interface.
	 * 
	 * @return Score of interface
	 */
	public Double getScore() {
		return score;
	}
	
	
	/**
	 * Set the score of the interface.
	 * 
	 * @param score Score of interface
	 */
	public void setScore(Double score) {
		this.score = score;
	}
	
	
	/**
	 * Get the localName of the interface.
	 * 
	 * @return LocalName of the interface
	 */
	public String getInterfaceLocalName() {
		return interfaceLocalName;
	}
	
	
	/**
	 * Set the localName of the interface.
	 * 
	 * @param interfaceLocalName LocalName of the interface
	 */
	public void setInterfaceLocalName(String interfaceLocalName) {
		this.interfaceLocalName = interfaceLocalName;
	}
	
	
	/**
	 * Get the localName of the service.
	 * 
	 * @return Localname of the service.
	 */
	public String getServiceLocalName() {
		return serviceLocalName;
	}
	
	
	/**
	 * Set the LocalName of the service.
	 * 
	 * @param serviceLocalName LocalName of service
	 */
	public void setServiceLocalName(String serviceLocalName) {
		this.serviceLocalName = serviceLocalName;
	}
	
	
	/**
	 * Get the name of the failed mandatory parameter.
	 * 
	 * @return Name of failed mandatory parameter
	 */
	public String getMandatoryFails() {
		return mandatoryFails;
	}
	
	
	/**
	 * Set the name of the failed mandatory parameter.
	 * 
	 * @param mandatoryFails Name of failed mandatory parameter
	 */
	public void setMandatoryFails(String mandatoryFails) {
		this.mandatoryFails = mandatoryFails;
	}
	
	
	/**
	 * Get the URL of the SLA.
	 * 
	 * @return URL of the SLA.
	 */
	public String getSlaPath() {
		return slaPath;
	}
	
	
	/**
	 * Set the URL of the SLA.
	 * 
	 * @param slaPath URL of the SLA 
	 */
	public void setSlaPath(String slaPath) {
		this.slaPath = slaPath;
	}
	

	
	/**
	 * Get the Content of the SLA.
	 * 
	 * @return Content of the SLA.
	 */
	/*	
	public String getSlaContent() {
		return slaContent;
	}
	*/
	
	/**
	 * Set the Content of the SLA.
	 * 
	 * @param slaContent Content of the SLA 
	 */
	/*	
	public void setSlaContent(String slaContent) {
		this.slaContent = slaContent;
	}
	*/ 
	
	
	/**
	 * Get the URL to the WSDL.
	 * 
	 * @return URL to the WSDL
	 */
	public String getWsdlPath() {
		return wsdlPath;
	}
	
	
	/**
	 * Set the URL to the WSDL.
	 * 
	 * @param wsdlPath URL to the WSDL
	 */
	public void setWsdlPath(String wsdlPath) {
		this.wsdlPath = wsdlPath;
	}
	

	/**
	 * Get the URL to the WSML.
	 * 
	 * @return URL to the WSML
	 */
	public String getWsmlPath() {
		return wsmlPath;
	}
	
	
	/**
	 * Set the URL to the WSML.
	 * 
	 * @param wsmlPath URL to the WSML
	 */
	public void setWsmlPath(String wsmlPath) {
		this.wsmlPath = wsmlPath;
	}
	
	
	/**
	 * Get the TemplateID of the SLA.
	 * 
	 * @return TemplateID of the SLA
	 */
	public String getSlaTemplateID() {
		return slaTemplateID;
	}
	
	
	/**
	 * Set the TemplateID of the SLA.
	 * 
	 * @param slaTemplateID TemplateID of the SLA
	 */
	public void setSlaTemplateID(String slaTemplateID) {
		this.slaTemplateID = slaTemplateID;
	}
	
	
	/**
	 * Get the ID of the monitor.
	 * 
	 * @return ID of the monitor
	 */
	public String getMonitorID() {
		return monitorID;
	}
	
	
	/**
	 * Set the ID of the monitor.
	 * 
	 * @param monitorID ID of the monitor
	 */
	public void setMonitorID(String monitorID) {
		this.monitorID = monitorID;
	}
	
	
	/**
	 * Get the endpoint of the monitor.
	 * 
	 * @return Endpoint of the monitor
	 */
	public String getMonitorEndpoint() {
		return monitorEndpoint;
	}
	
	
	/**
	 * Set the endpoint of the monitor.
	 * 
	 * @param monitorEndpoint Endpoint of the monitor
	 */
	public void setMonitorEndpoint(String monitorEndpoint) {
		this.monitorEndpoint = monitorEndpoint;
	}
	
}
