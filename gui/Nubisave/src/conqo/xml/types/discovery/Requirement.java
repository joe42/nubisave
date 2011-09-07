package conqo.xml.types.discovery;


/**
 * Class represent a requirement of a goal or web service.
 * 
 * @author Bastian Buder
 *
 */
public class Requirement {

	/**
	 * Name of the SLA parameter.
	 */
	private String slaParameter = "";
	
	
	/**
	 * Name of the WSML parameter.
	 */
	private String parameter = "";
	
	
	/**
	 * Required value.
	 */
	private Double value = -1.0;
	
	
	/**
	 * Measured value.
	 */
	private Double measuredValue = -1.0;
	
	
	/**
	 * Unit of the parameter.
	 */
	private String unit = "";
	
	
	/**
	 * True of requirement is satisfied.
	 */
	private boolean satisfied = false;
	
	
	/**
	 * State of the parameter.
	 */
	private String measuredState = "";
	
	
	/**
	 * Timestamp of last update.
	 */
	private String updated = "";
	
	
	/**
	 * Number of bad states.
	 */
	private int badStates = 0;
	
	
	/**
	 * Constructor
	 */
	public Requirement() {
		
	}
	
	
	/**
	 * Get parameter name.
	 * 
	 * @return parameter name
	 */
	public String getParameter() {
		return parameter;
	}
	
	
	/**
	 * Set parameter name.
	 * 
	 * @param parameter Parameter name.
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
	
	/**
	 * Get value of the measured parameter.
	 * 
	 * @return Value of the measured parameter
	 */
	public Double getMeasuredValue() {
		return measuredValue;
	}
	
	
	/**
	 * Set value of the measured parameter.
	 * 
	 * @param measuredValue Value of the measured parameter
	 */
	public void setMeasuredValue(Double measuredValue) {
		this.measuredValue = measuredValue;
	}
	
	
	/**
	 * Get value of the parameter.
	 * 
	 * @return Value of the parameter
	 */
	public Double getValue() {
		return value;
	}
	
	
	/**
	 * Set value of the parameter.
	 * 
	 * @param value Value of the parameter
	 */
	public void setValue(Double value) {
		this.value = value;
	}
	
	
	/**
	 * Get unit of the parameter.
	 * 
	 * @return Unit of the parameter
	 */
	public String getUnit() {
		return unit;
	}
	
	
	/**
	 * Set unit of the parameter.
	 * 
	 * @param unit Unit of the parameter
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	
	/**
	 * True, if requirement is satisfied.
	 * 
	 * @param satisfied Set true, if requirement is satisfied
	 */
	public void setSatisfied(boolean satisfied) {
		this.satisfied = satisfied;
	}
	
	
	/**
	 * True, if requirement is satisfied.
	 * 
	 * @return True, if requirement is satisfied
	 */
	public boolean isSatisfied() {
		return satisfied;
	}
	
	
	/**
	 * Get state of the measured value.
	 * 
	 * @return State of the measured value
	 */
	public String getMeasuredState() {
		return measuredState;
	}
	
	
	/**
	 * Set state of the measured value.
	 * 
	 * @param measuredState State of the measured value
	 */
	public void setMeasuredState(String measuredState) {
		this.measuredState = measuredState;
	}
	
	
	/**
	 * Get number of bad states.
	 * 
	 * @return Number of bad states
	 */
	public int getBadStates() {
		return badStates;
	}
	
	
	/**
	 * Set number of bad states.
	 * 
	 * @param badStates Number of bad states
	 */
	public void setBadStates(int badStates) {
		this.badStates = badStates;
	}
	
	
	/**
	 * Get timestamp of last update.
	 * 
	 * @return timestamp of last update
	 */
	public String getUpdated() {
		return updated;
	}
	
	
	/**
	 * Set timestamp of last update.
	 * 
	 * @param updated Timestamp of last update
	 */
	public void setUpdated(String updated) {
		this.updated = updated;
	}
	
	
	/**
	 * Get the name of the corresponding SLA parameter.
	 * 
	 * @return Name of the corresponding SLA parameter
	 */
	public String getSlaParameter() {
		return slaParameter;
	}
	
	
	/**
	 * Set the name of the corresponding SLA parameter.
	 * 
	 * @param slaParameter Name of the corresponding SLA parameter
	 */
	public void setSlaParameter(String slaParameter) {
		this.slaParameter = slaParameter;
	}
}
