package es.upm.fi.dia.oeg.obdi.core.engine;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.XMLUtility;

public class XMLWriter extends AbstractQueryResultWriter {

	private static Logger logger = Logger.getLogger(XMLWriter.class);
	private Document xmlDoc;
	private Element resultsElement;
	private String outputFileName;
	

	public void initialize() throws Exception {
		this.xmlDoc = XMLUtility.createNewXMLDocument();
	}

	public void preProcess() throws Exception {
		//create root element
		Element rootElement = xmlDoc.createElement("sparql");
		xmlDoc.appendChild(rootElement);

		//create head element
		Element headElement = xmlDoc.createElement("head");
		rootElement.appendChild(headElement);
		Collection<String> columnNames = super.abstractResultSet.getColumnNames();
		for(String columnName : columnNames) {
			Element variableElement = xmlDoc.createElement("variable");
			variableElement.setAttribute("name", columnName);
			headElement.appendChild(variableElement);
		}
		
		//create results element
		this.resultsElement = xmlDoc.createElement("results");
		rootElement.appendChild(resultsElement);
	}

	public void process() throws Exception {
		int i=0;
		while(this.abstractResultSet.next()) {
			Element resultElement = xmlDoc.createElement("result");
			resultsElement.appendChild(resultElement);
			Collection<String> columnNames = this.abstractResultSet.getColumnNames(); 
			for(String columnName : columnNames) {
				Element bindingElement = xmlDoc.createElement("binding");
				bindingElement.setAttribute("name", columnName);
				String columnValue = this.abstractResultSet.getString(columnName);
				String translatedColumnValue = super.queryTranslator.translateResultSet(
						columnName, columnValue);
				bindingElement.setTextContent(translatedColumnValue);
				resultElement.appendChild(bindingElement);
			}
			i++;
		}
		String status = i  + " instance(s) retrieved ";
		logger.info(status);
		
	}

	public void postProcess() throws Exception {
		logger.info("Writing result to " + outputFileName);
		XMLUtility.saveXMLDocument(xmlDoc, outputFileName);
	}


	@Override
	public Object getOutput() throws Exception {
		return this.xmlDoc;
	}

	@Override
	public void setOutput(Object output) throws Exception {
		this.outputFileName = (String) output;
	}

	
}
