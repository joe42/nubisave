package conqo.xml.types.discovery;

import java.util.ArrayList;

/**
 * Information about WSML Goals.
 * 
 * @author Bastian Buder
 *
 */
public class XMLGetSearchesRespData extends XMLRespData {

	/*
	 * List of user requests.
	 */
	private ArrayList<String> goals = null;
	
	/*
	 * Constructor.
	 */
	public XMLGetSearchesRespData() {
		super();
		goals = new ArrayList<String>();
	}
	
	
	/**
	 * Get the goals.
	 *  
	 * @return List of Goals.
	 */
	public ArrayList<String> getGoals() {
		return goals;
	}
	
	
	/**
	 * Add goal.
	 * 
	 * @param goal User Request (WSML Goal).
	 */
	public void addGoal(String goal) {
		goals.add(goal);
	}
	
}
