package nubisave.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * An instance of this class represent a WSML-Instance from goal. 
 * 
 * @author Bastian Buder
 *
 */
public class WSMOInstance {
	
	/**
	 * Name of instance.
	 */
	private String name;
	
	/**
	 * List of concepts.
	 */
	private List<String> concepts;
	
	/**
	 * Map with names and values of parameters.
	 */
	private Map<String, String> params;
	
	
	/**
	 * Constructor.
	 * 
	 * @param name Goalname as String
	 */
	public WSMOInstance(String name) {
		this.name=name;
		concepts = new ArrayList<String>();
		params = new HashMap<String, String>();
	}
	
	
	/**
	 * Set the name of the goal.
	 * 
	 * @param name Goalname as String
	 */
	public void setName(String name) {
		this.name=name;
	}
	
	
	/**
	 * Adds a concept.
	 * 
	 * @param concept Conceptname as String
	 */
	public void addConcept(String concept){
		concepts.add(concept);
	}
	
	
	/**
	 * Adds a parameter to an instance.
	 * 
	 * @param concept name of used concept
	 * @param value value of parameter
	 */
	public void addParam(String concept, String value) {
		params.put(concept, value);		
	}
	
	
	/**
	 * This method generate the instance and return the instance as String.
	 * 
	 * @return Instance as String
	 */
	public String getWSMOInstance() {
		String result="";
		result+="instance "+name+" memberOf ";
		if (concepts.size()>1)
			result+="{ ";
		
		Iterator<String> cIt = concepts.iterator();
		while (cIt.hasNext()){
			result+=cIt.next();
			if (cIt.hasNext()){
				result+=", ";
			}
		}		
		if (concepts.size()>1)
			result+=" }";
		result+=" \n";
		
		Iterator<Map.Entry<String, String>> pIt = params.entrySet().iterator();
		while (pIt.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) pIt.next();
			result+=" "+pair.getKey()+" hasValue "+pair.getValue()+"\n";
		}
		
		return result;		
	}
	
}