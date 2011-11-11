package es.upm.fi.dia.oeg.obdi.core.engine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Zql.ZUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.newrqr.MappingsExtractor;
import es.upm.fi.dia.oeg.newrqr.RewriterWrapper;
import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.materializer.NTripleMaterializer;
import es.upm.fi.dia.oeg.obdi.core.materializer.RDFXMLMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.R2OQueryTranslator;

public abstract class AbstractRunner {
	private static Logger logger = Logger.getLogger(AbstractRunner.class);
	
	protected AbstractDataTranslator dataTranslator;
	protected AbstractParser parser;
	protected AbstractQueryTranslator queryTranslator;
	public static ConfigurationProperties configurationProperties;
	

	public AbstractRunner() {}

	public AbstractRunner(AbstractDataTranslator dataTranslator,
			AbstractParser parser, AbstractQueryTranslator queryTranslator) {
		super();
		this.dataTranslator = dataTranslator;
		this.parser = parser;
		this.queryTranslator = queryTranslator;
	}

	public String run(String mappingDirectory, String configurationFile)
			throws Exception {
		String status = null;
		
		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("substring", 3);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		ZUtils.addCustomFunction("lower", 1);
		

		//Loading configuration file
		AbstractRunner.configurationProperties = 
			this.loadConfigurationFile(mappingDirectory, configurationFile);

		//loading ontology file
		String ontologyFilePath = configurationProperties.getOntologyFilePath();
		
		//parsing mapping document
		String mappingDocumentPath = configurationProperties.getR2oFilePath();
		AbstractMappingDocument originalMappingDocument = 
			parser.parse(mappingDocumentPath);

		//set output file
		String outputFileName = configurationProperties.getOutputFilePath();

		//parsing sparql file path
		String queryFilePath = configurationProperties.getQueryFilePath();
//		R2OMappingDocument translationResultMappingDocument = null;

		if(queryFilePath == null || queryFilePath.equals("")) {
			this.materializeMappingDocuments(outputFileName, originalMappingDocument);
		} else {
			//process SPARQL file
			logger.info("Parsing query file : " + queryFilePath);
			Query originalQuery = QueryFactory.read(queryFilePath);
			
			//rewrite the SPARQL query if necessary
			List<Query> queries = new ArrayList<Query>();
			if(ontologyFilePath == null || ontologyFilePath.equals("")) {
				queries.add(originalQuery);
			} else {
				//rewrite the query based on the mappings and ontology
				logger.info("Rewriting query...");
				Collection <String> mappedOntologyElements = 
					MappingsExtractor.getMappedPredicatesFromR2O(mappingDocumentPath);
				String rewritterWrapperMode = RewriterWrapper.fullMode;
				//RewriterWrapper rewritterWapper = new RewriterWrapper(ontologyFilePath, rewritterWrapperMode, mappedOntologyElements);
				//queries = rewritterWapper.rewrite(originalQuery);
				queries = RewriterWrapper.rewrite(originalQuery, ontologyFilePath, RewriterWrapper.fullMode, mappedOntologyElements, RewriterWrapper.globalMatchMode);
				
				
				logger.debug("No of rewriting query result = " + queries.size());
				logger.debug("queries = " + queries);
			}			
			
			
			//translate sparql into mappings
//			SPARQL2MappingTranslator translator = 
//				new SPARQL2MappingTranslator(originalMappingDocument);
			
			//translate sparql into sql
			this.queryTranslator.setOptimizeTripleBlock(this.configurationProperties.isOptimizeTB());
			this.queryTranslator.setSubQueryElimination(this.configurationProperties.isSubQueryElimination());
			Document xmlDoc = XMLUtility.createNewXMLDocument();
			Connection conn = this.configurationProperties.getConn();

			Element rootElement = null;
			Element headElement = null;
			Element resultsElement = null;
			List<Element> headElements = null;
			List<String> headElementsString = null;
			

			
			for(Query query : queries) {
//				translationResultMappingDocument = translator.processQuery(query);
				try {
					
					
					logger.debug("query(i) = " + query);
					String sparql2SQLResult = 
							this.queryTranslator.translate(query).toString();
					logger.debug("sparql2sql = \n" + sparql2SQLResult);

					ResultSet rs = Utility.executeQuery(conn, sparql2SQLResult);
					ResultSetMetaData rsmd = rs.getMetaData();


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
						headElements = this.createHeadElementFromColumnNames(rsmd, xmlDoc);
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

					
					String resultString = "result";
					String bindingString = "binding";
					int i=0;
					while(rs.next()) {
						Element resultElement = xmlDoc.createElement(resultString);
						resultsElement.appendChild(resultElement);
						Iterator<String> headElementsStringIterator = headElementsString.iterator(); 
						while(headElementsStringIterator.hasNext()) {
							String columnLabel = headElementsStringIterator.next();
							Element bindingElement = xmlDoc.createElement(bindingString);
							bindingElement.setAttribute("name", columnLabel);
							bindingElement.setTextContent(rs.getString(columnLabel));
							resultElement.appendChild(bindingElement);
						}
						i++;
					}
					status = i  + " instance(s) retrieved ";
					logger.info(status);
					//translationResultMappingDocuments.add(translator.processQuery(query));
				} catch(Exception e) {
					//e.printStackTrace();
					logger.error("error processing query, error message = " + e.getMessage());
					throw e;
				}

			}
			XMLUtility.saveXMLDocument(xmlDoc, outputFileName);
			//logger.debug("translationResult = " + translationResultMappingDocument);			 
		}
		
		logger.info("**********************DONE****************************\n\n");
		return status;

	}

	protected void materializeMappingDocuments(String outputFileName
			, AbstractMappingDocument translationResultMappingDocument) throws Exception {
		long start = System.currentTimeMillis();
		
		String rdfLanguage = AbstractRunner.configurationProperties.getRdfLanguage();
		if(rdfLanguage == null) {
			rdfLanguage = R2OConstants.OUTPUT_FORMAT_RDFXML;
		}
		
		//preparing output file
	    OutputStream fileOut = new FileOutputStream (outputFileName);
	    Writer out = new OutputStreamWriter (fileOut, "UTF-8");
	    String jenaModel = configurationProperties.getJenaMode();
		Model model = Utility.createJenaModel(jenaModel);

		AbstractMaterializer materializer;
		if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
			materializer = new NTripleMaterializer(out);
		} else if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_RDFXML)) {
			materializer = new RDFXMLMaterializer(out, model);
		} else {
			materializer = new NTripleMaterializer(out);
		}
		this.dataTranslator.setMaterializer(materializer);
		
		//materializing model
		long startGeneratingModel = System.currentTimeMillis();
		this.dataTranslator.processMappingDocument(translationResultMappingDocument);
		if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_RDFXML)) {
			if(model == null) {
				logger.warn("Model was empty!");
			} else {
				ModelWriter.writeModelStream(model, configurationProperties.getOutputFilePath(), configurationProperties.getRdfLanguage());
				model.close();				
			}
		}
		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Materializing Mapping Document time was "+(durationGeneratingModel)+" s.");

		//cleaning up
		try {
			out.flush(); out.close();
			fileOut.flush(); fileOut.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
		}

		Utility.closeConnection(configurationProperties.getConn(), "r2o wrapper");
		long end = System.currentTimeMillis();
		long duration = (end-start) / 1000;
		logger.info("Execution time was "+(duration)+" s.");
	}



	protected ConfigurationProperties loadConfigurationFile(
			String mappingDirectory, String configurationFile) 
	throws IOException, SQLException, InvalidConfigurationPropertiesException {
		logger.debug("Active Directory = " + mappingDirectory);
		logger.debug("Loading configuration file : " + configurationFile);
		
		try {
			ConfigurationProperties configurationProperties = 
				new ConfigurationProperties(mappingDirectory, configurationFile);
			return configurationProperties;
		} catch(FileNotFoundException e) {
			logger.error("Can't find properties file : " + configurationFile);
			throw e;
		}
	}

	public void setQueryTranslator(AbstractQueryTranslator queryTranslator) {
		this.queryTranslator = queryTranslator;
	}
	
	protected List<Element> createHeadElementFromColumnNames(ResultSetMetaData rsmd, Document xmlDoc) throws SQLException {
		List<Element> result = new ArrayList<Element>();
		
		for(int i=0; i<rsmd.getColumnCount(); i++) {
			Element element = xmlDoc.createElement("variable");
			element.setAttribute("name", rsmd.getColumnLabel(i+1));
			result.add(element);
		}
		
		return result;
	}

	public void setDataTranslator(AbstractDataTranslator dataTranslator) {
		this.dataTranslator = dataTranslator;
	}

	public void setParser(AbstractParser parser) {
		this.parser = parser;
	}
}
