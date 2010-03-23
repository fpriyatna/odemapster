package es.upm.fi.dia.oeg.obdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtility {
	private static Logger logger = Logger.getLogger(XMLUtility.class);
	
	public static void main(String args[]) throws Exception, ParserConfigurationException, TransformerException
	{
		PropertyConfigurator.configure("log4j.properties");
		logger.debug("logger initialized");
	
		/*
		String folder = "D:\\home\\freddy\\My Dropbox\\oeg\\odemapster++\\from_luis\\20091014(mappings)\\nconciso\\";
		String filename = "mappingsR2Onconciso.r2o";
		String filePath = folder + "\\" +filename;  
		
		Document document = XMLUtility.loadXMLFile(filePath);
		removeFromRootElement(document, "dbschema-desc", "name", "mysql");
		StringWriter writer = new StringWriter();
		*/
		
		//test3
		
	}
	
	public static Document removeFromRootElement(Document document, String nodeName, String attributeName, String attributeValue) {
		try
		{
			Element rootElement = document.getDocumentElement();
			
			NodeList nl = document.getElementsByTagName(nodeName);
			Vector<Node> deletedNodes = new Vector<Node>();
			
			for(int i=0; i<nl.getLength(); i++) {
				Node node = nl.item(i);
				String noteAttributeValue = node.getAttributes().getNamedItem(attributeName).getNodeValue();

				if(noteAttributeValue.equals(attributeValue))
				{
					deletedNodes.add(node);
				}
				
				
			}
			
			for(Node deletedNode : deletedNodes) {
				rootElement.removeChild(deletedNode);
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}

		return document;
	}

	public static Document createNewXMLDocument() throws ParserConfigurationException, TransformerException, FileNotFoundException
	{
		DocumentBuilderFactory documentBuilderFactory = 
			DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = 
			documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		return document;
	}

	public static Document loadXMLFile(String fileAbsolutePath) throws ParserConfigurationException, SAXException, IOException
	{
		
		DocumentBuilderFactory documentBuilderFactory = 
			DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = 
			documentBuilderFactory.newDocumentBuilder();
		//Document xmlDocument = docBuilder.parse(fileAbsolutePath);
		Document xmlDocument = docBuilder.parse(new InputSource(new InputStreamReader(new FileInputStream(fileAbsolutePath), "UTF8")));
		return xmlDocument;
	}

	public static Element getRootElement(String fileLocation) throws ParserConfigurationException, SAXException, IOException {
		return XMLUtility.loadXMLFile(fileLocation).getDocumentElement();
	}
	
	public static void appendChilds(Element element, Vector<Element> childElements) {
		if(element != null && childElements != null) {
			for(Element childElement : childElements) {
				element.appendChild(childElement);
			}			
		}
	}
	
	public static Vector<Element> toVectorElements(Element element) {
		Vector<Element> elements = new Vector<Element>();
		elements.add(element);
		return elements;
	}

	public static Element getFirstElement(Element element) {
		Element result = null;
		NodeList nl = element.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			if(nl.item(i) instanceof Element) {
				result = (Element) nl.item(i);
			}
		}
		return result;
	}

	public static Element getFirstElementByTagName(Element element, String name) {
		NodeList nl = element.getElementsByTagName(name);
		return (Element) nl.item(0);
	}
	
	public static void printXMLDocument(
			Document document, Writer writer, 
			boolean indenting, boolean omitXMLDeclaration) throws TransformerException, IOException
	{
		OutputFormat format = new OutputFormat(document);
		format.setIndenting(indenting);
		format.setOmitXMLDeclaration(omitXMLDeclaration);

		XMLSerializer serializer = new XMLSerializer(writer, format);
		serializer.serialize(document);
	}
	/**
	 * This method uses Xerces specific classes
	 * prints the XML document to file.
     */
	public static void saveXMLDocument(Document document, String filename){

		try
		{
			//print
			OutputFormat format = new OutputFormat(document);
			format.setIndenting(true);

			//to generate output to console use this serializer
			//XMLSerializer serializer = new XMLSerializer(System.out, format);


			//to generate a file output use fileoutputstream instead of system.out
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(filename)), format);
			
			serializer.serialize(document);

		} catch(IOException ie) {
		    ie.printStackTrace();
		}
	}

	/**
	 * @param docBuilder
	 *          the parser
	 * @param parent
	 *          node to add fragment to
	 * @param fragment
	 *          a well formed XML fragment
	 * @throws ParserConfigurationException 
	 */
	public static void appendXmlFragment(Node parent, String fragment) throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory dbf =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc = parent.getOwnerDocument();
		Node fragmentNode = db.parse(
				new InputSource(new StringReader(fragment)))
				.getDocumentElement();
		fragmentNode = doc.importNode(fragmentNode, true);
		parent.appendChild(fragmentNode);
	}
}

