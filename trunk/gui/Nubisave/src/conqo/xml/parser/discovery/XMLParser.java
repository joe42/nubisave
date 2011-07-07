package conqo.xml.parser.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import conqo.xml.parser.exception.ParserException;
import conqo.xml.types.discovery.XMLData;


/**
 * Abstract Class for parsing XML-Streams.
 * 
 * @author Bastian Buder
 *
 */
public abstract class XMLParser {
	
	/**
	 * This abstract method converts XML-Elements in Java objects. 
	 *  
	 * @return Data from XML
	 * @throws ParserException
	 */
	public abstract XMLData parse() throws ParserException;
	
	/**
	 * The XML-Document.
	 */
	protected Document doc = null;
	
	/**
	 * Constructor for instantiate new Parser. 
	 * 
	 * @param xml XML as String
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public XMLParser(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		byte[] stringBytes = xml.getBytes();
		ByteArrayInputStream bais = new ByteArrayInputStream(stringBytes);
		this.doc = db.parse(bais);		
		// BASE64Encoder enc = new BASE64Encoder();
	}	
	
}
