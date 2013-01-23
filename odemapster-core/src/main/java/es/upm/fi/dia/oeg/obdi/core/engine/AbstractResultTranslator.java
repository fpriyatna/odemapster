package es.upm.fi.dia.oeg.obdi.core.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public abstract class AbstractResultTranslator {
	private static Logger logger = Logger.getLogger(AbstractResultTranslator.class);
	AbstractRunner runner;
	
	public AbstractResultTranslator(AbstractRunner runner) {
		this.runner = runner;
	}
	
	protected Document generateSPARQLQueryResultXMLFile(
			Collection<SQLQuery> queries, String outputFileName) throws Exception {
		Document xmlDoc = XMLUtility.createNewXMLDocument();
		
		Element rootElement = null;
		Element headElement = null;
		Element resultsElement = null;
		List<Element> headElements = null;
		List<String> headElementsString = null;
		
		for(SQLQuery query : queries) {
			try {

				//create root
				if(rootElement == null) {
					String rootString = "sparql";
					rootElement = xmlDoc.createElement(rootString);
					xmlDoc.appendChild(rootElement);
				}

				//create header
				if(headElement == null) {
					String headString = "head";
					headElement = xmlDoc.createElement(headString);
					rootElement.appendChild(headElement);
					headElements = this.runner.createHeadElementFromColumnNames(query, xmlDoc);
					headElementsString = new ArrayList<String>();
					for(Element element : headElements) {
						headElement.appendChild(element);
						headElementsString.add(element.getAttribute("name"));
					}						
				}

				//create results
				if(resultsElement == null) {
					String resultsString = "results";
					resultsElement = xmlDoc.createElement(resultsString);
					rootElement.appendChild(resultsElement);						
				}

				
				this.generateResultBindingElement(query, xmlDoc, resultsElement, headElementsString);
				//translationResultMappingDocuments.add(translator.processQuery(query));
			} catch(Exception e) {
				e.printStackTrace();
				logger.error("error processing query, error message = " + e.getMessage());
				throw e;
			}

		}
		
		return xmlDoc;
	}
	
	protected abstract void generateResultBindingElement(SQLQuery query, Document xmlDoc
			, Element resultsElement, List<String> headElementsString) throws Exception;
}
