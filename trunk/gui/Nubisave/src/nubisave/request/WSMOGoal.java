package nubisave.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * This class is used for generate WSMO-Goals (e.g. used as Java Bean).
 * 
 * @author Bastian Buder
 *
 */
public class WSMOGoal implements Serializable {

	/**
	 * UID.
	 */
	private static final long serialVersionUID = -2110065926209344191L;
	
	/**
	 * Name of the goal.
	 */
	private String name;
	
	/**
	 * WSML variant.
	 */
	private String wsmlVariant;
	
	/**
	 * Used Namespaces.
	 */
	private Map<String, String> namespaces;
	
	/**
	 * List of used goalontologies.
	 */
	private List<String> goalOntologies;
	
	/**
	 * List of used ontologies.
	 */
	private List<String> ontologies;
	
	/**
	 * Capability of goal.
	 */
	private String capability;
	
	/**
	 * Map of WSML instances.
	 */
	private Map<String, WSMOInstance> instances;		

	
	/**
	 * Constructor
	 */
	public WSMOGoal(){
		namespaces = new HashMap<String, String>();
		goalOntologies = new ArrayList<String>();
		ontologies = new ArrayList<String>();
		instances = new HashMap<String, WSMOInstance>();
		
		this.wsmlVariant = "http://www.wsmo.org/wsml/wsml-syntax/wsml-rule";
		this.name="Goal";
	}
	
	
	/**
	 * Get WSML-Variant
	 * 
	 * @return WSML-Variant as String (e.g. WSML-Rule)
	 */
	public String getWsmlVariant() {
		return wsmlVariant;
	}

	
	/**
	 * Set WSML-Variant
	 * 
	 * @param wsmlVariant WSML variant
	 */
	public void setWsmlVariant(String wsmlVariant) {
		this.wsmlVariant = wsmlVariant;
	}

	
	/**
	 * Get Namespaces of this Goal.
	 * 
	 * @return Map of used Namespaces
	 */
	public Map<String, String> getNamespaces() {
		return namespaces;
	}	
	
	
	/**
	 * Set Namespaces
	 * 
	 * @param namespaces Map of Namespaces
	 */
	public void setNamespaces(HashMap<String, String> namespaces) {
		this.namespaces = namespaces;
	}
	
	
	/**
	 * Add a new Namespace
	 * 
	 * @param id Name, "own" for Namespace of this Goal.
	 * @param namespace Namespace (e.g. URL)
	 */
	public void addNameSpace(String id, String namespace) {
		if (id=="") {
			id="own";
		}
		namespaces.put(id, namespace);
	}
	
	
	/**
	 * Clear all Namespaces.
	 */
	public void clearNameSpaces(){
		this.namespaces.clear();
	}

	
	/**
	 * Set the name of a Goal.
	 * 
	 * @param name Name of Goal
	 */
	public void setName(String name){
		this.name=name;
	}
	
	
	/**
	 * Get the Name of Goal.
	 * 
	 * @return Name of Goal
	 */
	public String getName() {
		return this.name;
	}
	
	
	/**
	 * Add a GoalOntologie.
	 * 
	 * @param ontologie Namespace of ontologie
	 */	
	public void addGoalOntologie(String ontologie) {
		goalOntologies.add(ontologie);	
	}
	
	
	/**
	 * Get a List of all GoalOntologies.
	 * 
	 * @return List of GoalOntologies
	 */
	public List<String> getGoalOntologies() {
		return goalOntologies;
	}
	
	
	/**
	 * Set GoalOntologies
	 * 
	 * @param ontologies List of GoalOntologies
	 */
	public void setGoalOntologies(List<String> ontologies) {
		this.goalOntologies = ontologies;
	}
	

	/**
	 * Clear all GoalOntologies.
	 */
	public void clearGoalOntologies() {
		this.goalOntologies.clear();		
	}
	
		
	/**
	 * Get Capability of goal.
	 * 
	 * @return Capability as String
	 */
	public String getCapability() {
		return capability;
	}
	

	/**
	 * Set Capability of goal.
	 * 
	 * @param capability Capability as String
	 */
	public void setCapability(String capability) {
		this.capability = capability;
	}


	/**
	 * Add an ontology for Import.
	 * 
	 * @param ontologie Namespace of ontology as String
	 */
	public void addOntologie(String ontologie) {
		ontologies.add(ontologie);	
	}
	
	
	/**
	 * Get a List with namespaces of all imported ontologies.
	 * 
	 * @return List of ontologies as List
	 */
	public List<String> getOntologies() {
		return ontologies;
	}
	
	
	/**
	 * Set ontologies as List.
	 * 
	 * @param ontologies List of namespaces of ontologies as List
	 */
	public void setOntologies(List<String> ontologies) {
		this.ontologies = ontologies;
	}
	

	/**
	 * Clear all imported ontologies.
	 */
	public void clearOntologies() {
		this.ontologies.clear();		
	}	
	
	
	
	/**
	 * Get a Map of Instances.
	 * 
	 * @return Map of WSML instances
	 */
	public Map<String, WSMOInstance> getInstances() {
		return instances;
	}
	
	
	/**
	 * Get Instance of a with a specific name.
	 * 
	 * @param name Name of Instance
	 * @return If available the WSMOInstance, else null
	 */
	public WSMOInstance getInstance(String name){
		if (instances.containsKey(name)) 
			return (WSMOInstance) instances.get(name);
		else 
			return null;
	}
	
	
	/**
	 * Set Instances of a Goal.
	 * 
	 * @param instances Map of Instances
	 */
	public void setInstances(Map<String, WSMOInstance> instances) {
		this.instances = instances;
	}
	
	
	/**
	 * Add a new Instance.
	 * 
	 * @param name Name of Instance
	 */
	public void addInstance(String name) {		
		instances.put(name, new WSMOInstance(name));
	}
	
	
	/**
	 * Clear all Instances.
	 */
	public void clearInstances(){
		this.instances.clear();
	}
		
	
	/**
	 * This Method generates the Goal.
	 * 
	 * @return Goal as String
	 */
	public String generateGoal(){
		String goal = "";
		// Variant
		goal+="wsmlVariant _\""+this.wsmlVariant+"\"\n\n";
		
		// namespaces
		Map<String, String> ns = new HashMap<String, String>();
		ns = this.namespaces;
		
		goal+="namespace { ";
		if (ns.containsKey("own")) {
			goal+="_\""+ns.get("own")+"\"";
			ns.remove("own");
		}
		Iterator<Map.Entry<String, String>> nsIt = ns.entrySet().iterator();
		while (nsIt.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>)nsIt.next();
			goal+=",\n\t"+pair.getKey()+" _\""+pair.getValue()+"\"";			
		}
		goal+="}\n\n";
		
		// Goal

		goal+="goal "+this.name+"\n";
		
		if (!goalOntologies.isEmpty()) {
			goal+="importsOntology { ";			
			Iterator<String> gOit = goalOntologies.iterator();
			while (gOit.hasNext()) {
				goal+="_\""+gOit.next()+"\"";
				if (gOit.hasNext()) {
					goal+="\n\t";
				}
			}				
			goal+=" }\n\n";	
		}
		
		goal+="capability GoalCapability\n "+this.capability+" .\n\n";
		
		goal+="interface GoalInterface\n\timportsOntology { GParam }\n\n";
		
		goal+="ontology GParam\n";
		if (!ontologies.isEmpty()) {
			goal+="importsOntology { ";			
			Iterator<String> oit = ontologies.iterator();
			while (oit.hasNext()) {
				goal+="_\""+oit.next()+"\"";
				if (oit.hasNext()) {
					goal+=",\n\t";
				}
			}				
			goal+=" }\n";	
		}			
		goal+="\n";	
		
		
		// add Instances
		if (instances.size()>0) {
			Map<String, WSMOInstance> ins = new HashMap<String, WSMOInstance>();
			ins = this.instances;
			
			Iterator<Map.Entry<String, WSMOInstance>> insIt = ins.entrySet().iterator();
			while (insIt.hasNext()) {
				Map.Entry<String, WSMOInstance> pair = (Map.Entry<String, WSMOInstance>)insIt.next();
				goal+=((WSMOInstance)pair.getValue()).getWSMOInstance();
				if (insIt.hasNext()) {
					goal+="\n";
				}			
			}			
		}		
		return goal;	
	}

}
