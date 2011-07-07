package conqo.xml.parser.exception;

/**
 * This exeption is thrown if a stream can't be parsed.
 * 
 * @author Bastian Buder
 *
 */
public class ParserException extends Exception {


	/**
	 * UID
	 */
	private static final long serialVersionUID = -3709964437922606926L;

	
	/**
	 * Constructor 
	 */
	public ParserException() {
		super();
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param message Errormessage
	 */
	public ParserException(String message) {
		super(message);
	}
	
}
