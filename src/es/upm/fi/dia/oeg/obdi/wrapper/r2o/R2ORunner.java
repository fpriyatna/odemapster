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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.materializer.NTripleMaterializer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.materializer.RDFXMLMaterializer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2MappingTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2SQLTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;

public class R2ORunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(R2ORunner.class);
	private static final String PRIMITIVE_OPERATIONS_FILE = "primitiveOperations.cfg";
	public static R2OConfigurationProperties configurationProperties;
	public static R2OPrimitiveOperationsProperties primitiveOperationsProperties;
	private R2ODataTranslator dataTranslator;
	
	public R2ORunner() {
		this.dataTranslator = new R2ODefaultDataTranslator();
	}

	public R2ORunner(R2ODataTranslator dataTranslator) {
		this.dataTranslator = dataTranslator;
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
		
		String rdfLanguage = R2ORunner.configurationProperties.getRdfLanguage();
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
		for(R2OMappingDocument translationResultMappingDocument : translationResultMappingDocuments) {
			this.dataTranslator.processMappingDocument(translationResultMappingDocument);
		}
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
	
	public String run(String mappingDirectory, String r2oConfigurationFile) throws Exception {
		String status = null;
		
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
				
				
				logger.debug("No of rewriting query result = " + queries.size());
				logger.debug("queries = " + queries);
			}			
			
			
			//translate sparql into mappings
//			SPARQL2MappingTranslator translator = 
//				new SPARQL2MappingTranslator(originalMappingDocument);
			
			//translate sparql into sql
			SPARQL2SQLTranslator translator = new SPARQL2SQLTranslator(originalMappingDocument);
			translator.setOptimizeTripleBlock(this.configurationProperties.isOptimizeTB());
			translator.setSubQueryElimination(this.configurationProperties.isSubQueryElimination());
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
					String sparql2SQLResult = translator.translate(query).toString();
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

	public void setDataTranslator(R2ODataTranslator dataTranslator) {
		this.dataTranslator = dataTranslator;
	}
}
