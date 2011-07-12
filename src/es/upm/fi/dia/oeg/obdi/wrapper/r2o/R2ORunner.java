package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractParser;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.ModelWriter;
import es.upm.fi.dia.oeg.obdi.wrapper.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODefaultDataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2MappingTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2SQLTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;

public class R2ORunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(R2ORunner.class);
	private static final String PRIMITIVE_OPERATIONS_FILE = "primitiveOperations.cfg";
	public static R2OConfigurationProperties configurationProperties;
	public static R2OPrimitiveOperationsProperties primitiveOperationsProperties;
	private R2ODataTranslator postProcessor; 	

	public R2ORunner() {
		this.postProcessor = new R2ODefaultDataTranslator();
	}

	public R2ORunner(R2ODataTranslator postProcessor) {
		this.postProcessor = postProcessor;
	}

	private R2OConfigurationProperties loadConfigurationFile(
			String mappingDirectory, String r2oConfigurationFile) 
	throws IOException, SQLException, InvalidConfigurationPropertiesException {
		logger.debug("Active Directory = " + mappingDirectory);
		logger.debug("Loading R2O configuration file : " + r2oConfigurationFile);
		
		try {
			R2OConfigurationProperties r2oProperties = 
				new R2OConfigurationProperties(mappingDirectory, r2oConfigurationFile);
			return r2oProperties;
		} catch(FileNotFoundException e) {
			logger.error("Can't find R2O properties file : " + r2oConfigurationFile);
			throw e;
		}
	}
	
	private R2OPrimitiveOperationsProperties loadPrimitiveOperationsFile(String primitiveOperationsFile) throws Exception{
		logger.debug("Loading R2O operations file : " + primitiveOperationsFile);
		try {
			R2OPrimitiveOperationsProperties primitiveOperationsProperties = 
				new R2OPrimitiveOperationsProperties(primitiveOperationsFile);
			return primitiveOperationsProperties;
		} catch(Exception e) {
			logger.error("Error loading primitive operations file : " + PRIMITIVE_OPERATIONS_FILE);
			throw e;
		}
	}

	private void materializeSPARQLSolution(String outputFileName, String sql) {
		try {

		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error in materializing sparql solution.");
		}
		
	}
	
	private void materializeMappingDocuments(String outputFileName, Collection<R2OMappingDocument> translationResultMappingDocuments) throws Exception {
		long start = System.currentTimeMillis();

		//preparing output file
//		FileWriter fileWriter = new FileWriter(outputFileName);
	    OutputStream fileOut = new FileOutputStream (outputFileName);
	    Writer out = new OutputStreamWriter (fileOut, "UTF-8");
	    
	    String jenaModel = configurationProperties.getJenaMode();
		Model model = Utility.createJenaModel(jenaModel);
		
		long startGeneratingModel = System.currentTimeMillis();
		R2OMappingDocumentMaterializer materializer = 
			new R2OMappingDocumentMaterializer(postProcessor, model, out);
		for(R2OMappingDocument translationResultMappingDocument : translationResultMappingDocuments) {
			materializer.materialize(translationResultMappingDocument);
		}
		
		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Materializing Mapping Document time was "+(durationGeneratingModel)+" s.");

		if(model == null) {
			logger.warn("Model was empty!");
		} else {
			if(configurationProperties.getRdfLanguage().equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
				//done
			} else {
				ModelWriter.writeModelStream(model, configurationProperties.getOutputFilePath(), configurationProperties.getRdfLanguage());
				model.close();				
			}
				
				
		}


		//cleaning up
		try {
//			fileWriter.flush();
//			fileWriter.close();
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
	
	public void run(String mappingDirectory, String r2oConfigurationFile) throws Exception {

		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		
		//loading operations file
		R2ORunner.primitiveOperationsProperties = 
			this.loadPrimitiveOperationsFile(PRIMITIVE_OPERATIONS_FILE);

		//Loading R2O configuration file
		R2ORunner.configurationProperties = 
			this.loadConfigurationFile(mappingDirectory, r2oConfigurationFile);

		//loading ontology file
		String ontologyFilePath = configurationProperties.getOntologyFilePath();
		
		//parsing r2o mapping document
		R2OParser parser = new R2OParser(); 
		String r2oMappingDocumentPath = configurationProperties.getR2oFilePath();
		R2OMappingDocument originalMappingDocument = 
			(R2OMappingDocument) parser.parse(r2oMappingDocumentPath);
		
		//test the parsing result
		parser.testParseResult(configurationProperties.getR2oFilePath(), originalMappingDocument);

		String outputFileName = configurationProperties.getOutputFilePath();

		
		//parsing sparql file path
		String queryFilePath = configurationProperties.getQueryFilePath();
//		R2OMappingDocument translationResultMappingDocument = null;
		Collection<R2OMappingDocument> translationResultMappingDocuments = new ArrayList<R2OMappingDocument>();
		
		if(queryFilePath == null || queryFilePath.equals("")) {
//			translationResultMappingDocument = originalMappingDocument;
			translationResultMappingDocuments.add(originalMappingDocument);
			this.materializeMappingDocuments(outputFileName, translationResultMappingDocuments);
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
					MappingsExtractor.getMappedPredicatesFromR2O(r2oMappingDocumentPath);
				String rewritterWrapperMode = RewriterWrapper.fullMode;
				//RewriterWrapper rewritterWapper = new RewriterWrapper(ontologyFilePath, rewritterWrapperMode, mappedOntologyElements);
				//queries = rewritterWapper.rewrite(originalQuery);
				queries = RewriterWrapper.rewrite(originalQuery, ontologyFilePath, RewriterWrapper.fullMode, mappedOntologyElements, RewriterWrapper.globalMatchMode);
				
				
				logger.info("No of rewriting query result = " + queries.size());
				logger.info("queries = " + queries);
			}			
			
			
			//translate sparql into mappings
//			SPARQL2MappingTranslator translator = 
//				new SPARQL2MappingTranslator(originalMappingDocument);
			
			//translate sparql into sql
			SPARQL2SQLTranslator sparql2sql = new SPARQL2SQLTranslator(originalMappingDocument);
			sparql2sql.setOptimizeTripleBlock(this.configurationProperties.isOptimizeTB());
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
					
					
					logger.info("query(i) = " + query);
					String sparql2SQLResult = sparql2sql.query2SQL(query).toString();
					

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
						
						
					}
					
					//translationResultMappingDocuments.add(translator.processQuery(query));
				} catch(Exception e) {
					e.printStackTrace();
					logger.error("error while processing query : " + query);
				}

			}
			XMLUtility.saveXMLDocument(xmlDoc, outputFileName);
			//logger.debug("translationResult = " + translationResultMappingDocument);			 
		}
		
		logger.info("done");


	}
	
	private List<Element> createHeadElementFromColumnNames(ResultSetMetaData rsmd, Document xmlDoc) throws SQLException {
		List<Element> result = new ArrayList<Element>();
		
		for(int i=0; i<rsmd.getColumnCount(); i++) {
			Element element = xmlDoc.createElement("variable");
			element.setAttribute("name", rsmd.getColumnLabel(i+1));
			result.add(element);
		}
		
		return result;
	}
	
	

	public static void main(String args[]) {
		try {
			R2ORunner runner = new R2ORunner();
			String r2oFile = null;
			if(args == null || args.length == 0) {
				r2oFile = "r2o.properties";
			} else {
				r2oFile = args[0];
			}
			runner.run(null, r2oFile);
		} catch(Exception e) {
			logger.error("Exception occured!");
			logger.error("Error message = " + e.getMessage());
		}


	}
}
