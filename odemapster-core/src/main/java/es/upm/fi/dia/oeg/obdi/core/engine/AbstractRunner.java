package es.upm.fi.dia.oeg.obdi.core.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Zql.ZUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import es.upm.fi.dia.oeg.newrqr.MappingsExtractor;
import es.upm.fi.dia.oeg.newrqr.RewriterWrapper;
import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.Utility;
import es.upm.fi.dia.oeg.obdi.core.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;

public abstract class AbstractRunner {
	private static Logger logger = Logger.getLogger(AbstractRunner.class);
	private Connection conn;
	protected AbstractParser parser;
	protected AbstractDataTranslator dataTranslator;
	protected IQueryTranslator queryTranslator;
	private AbstractMappingDocument mappingDocument;
	protected Query sparqQuery = null;
	public static ConfigurationProperties configurationProperties;
	
	public AbstractRunner(String configurationDirectory, String configurationFile) 
			throws Exception {
		try {
			AbstractRunner.configurationProperties = new ConfigurationProperties(configurationDirectory, configurationFile);
			String mappingDocumentFile = AbstractRunner.configurationProperties.getMappingDocumentFilePath();
			String queryFilePath = AbstractRunner.configurationProperties.getQueryFilePath();
			
			//mapping document
			this.mappingDocument = this.createMappingDocument(mappingDocumentFile);
			
			//unfolder
			AbstractUnfolder unfolder = this.createUnfolder();

			//query translator
			this.queryTranslator = this.createQueryTranslator(this.mappingDocument, unfolder);
			this.queryTranslator.setQueryFilePath(queryFilePath);
			this.queryTranslator.setMappingDocument(this.mappingDocument);
			this.queryTranslator.setUnfolder(unfolder);
			
			
			//query translation optimizer
			IQueryTranslationOptimizer queryTranslationOptimizer = this.createQueryTranslationOptimizer();
			boolean optimizeTriplesSameSubject = AbstractRunner.configurationProperties.isSelfJoinElimination();
			queryTranslationOptimizer.setSelfJoinElimination(optimizeTriplesSameSubject);
			boolean eliminateSubQuery = AbstractRunner.configurationProperties.isSubQueryElimination();
			queryTranslationOptimizer.setSubQueryElimination(eliminateSubQuery);
			boolean subQueryAsView = AbstractRunner.configurationProperties.isSubQueryAsView();
			queryTranslationOptimizer.setSubQueryAsView(subQueryAsView);
			this.queryTranslator.setOptimizer(queryTranslationOptimizer);
			
			//data translator
			this.dataTranslator = this.createDataTranslator(AbstractRunner.configurationProperties, unfolder);
			
		} catch (IOException e) {
			logger.error("IO error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw e;
		} catch (InvalidConfigurationPropertiesException e) {
			logger.error("invalid configuration error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			logger.error("Database error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw e;
		} catch(Exception e) {
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
			throw e;
			
		}		
	}
	
	protected abstract AbstractDataTranslator createDataTranslator(
			ConfigurationProperties configurationProperties,
			AbstractUnfolder unfolder);

	protected abstract IQueryTranslator createQueryTranslator(
			AbstractMappingDocument mappingDocument, AbstractUnfolder unfolder);

	protected abstract IQueryTranslationOptimizer createQueryTranslationOptimizer();

	protected abstract AbstractUnfolder createUnfolder();

	protected abstract AbstractMappingDocument createMappingDocument(String mappingDocumentFile) throws Exception;

	public static ConfigurationProperties getConfigurationProperties() {
		if(configurationProperties == null) {
			configurationProperties = new ConfigurationProperties();
		}
		return configurationProperties;
	}
	
	public Connection getConnection() throws SQLException {
		if(this.conn == null) {
			String databaseUser = this.configurationProperties.getDatabaseUser();
			String databaseName = this.configurationProperties.getDatabaseName();
			String databasePassword = this.configurationProperties.getDatabasePassword();
			String databaseDriver = this.configurationProperties.getDatabaseDriver();
			String databaseURL = this.configurationProperties.getDatabaseURL();

			try {
				this.conn = Utility.getLocalConnection(
						databaseUser, databaseName, databasePassword, 
						databaseDriver, 
						databaseURL, "Runner");
			} catch (SQLException e) {
				String errorMessage = "Error loading database, error message = " + e.getMessage();
				logger.error(errorMessage);
				//e.printStackTrace();
				throw e;
			}			
		}
		
		return this.conn;
	}

//	public AbstractRunner() {}
//
////	public AbstractRunner(AbstractDataTranslator dataTranslator,
////			AbstractParser parser, AbstractQueryTranslator queryTranslator) {
////		super();
////		this.dataTranslator = dataTranslator;
////		this.parser = parser;
////		this.queryTranslator = queryTranslator;
////	}

	protected List<Element> createHeadElementFromColumnNames(ResultSetMetaData rsmd, Document xmlDoc) throws SQLException {
		List<Element> result = new ArrayList<Element>();
		
		for(int i=0; i<rsmd.getColumnCount(); i++) {
			Element element = xmlDoc.createElement("variable");
			element.setAttribute("name", rsmd.getColumnLabel(i+1));
			result.add(element);
		}
		
		return result;
	}

//	public IQueryTranslator createQueryTranslator(String queryTranslatorClassName, AbstractMappingDocument md, AbstractUnfolder unfolder) throws Exception {
//		Class queryTranslatorClass = Class.forName(queryTranslatorClassName);
//		IQueryTranslator queryTranslator = (IQueryTranslator) queryTranslatorClass.newInstance();
//		queryTranslator.setMappingDocument(md);
//		queryTranslator.setUnfolder(unfolder);
//		boolean optimizeTriplesSameSubject = AbstractRunner.configurationProperties.isOptimizeTB();
//		queryTranslator.setOptimizeTripleBlock(optimizeTriplesSameSubject);
//		boolean eliminateSubQuery = AbstractRunner.configurationProperties.isSubQueryElimination();
//		queryTranslator.setSubQueryElimination(eliminateSubQuery);
//		boolean subQueryAsView = AbstractRunner.configurationProperties.isSubQueryAsView();
//		queryTranslator.setSubqueryAsView(subQueryAsView);
//		
//		return queryTranslator;
//	}



	public AbstractDataTranslator getDataTranslator() {
		return dataTranslator;
	}

	public IQueryTranslator getQueryTranslator() {
		return queryTranslator;
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

	protected void materializeMappingDocuments(String outputFileName
			, AbstractMappingDocument translationResultMappingDocument) throws Exception {
		long start = System.currentTimeMillis();
		
		String rdfLanguage = AbstractRunner.configurationProperties.getRdfLanguage();
		if(rdfLanguage == null) {
			rdfLanguage = Constants.OUTPUT_FORMAT_RDFXML;
		}
		
		//preparing output file
	    //OutputStream fileOut = new FileOutputStream (outputFileName);
	    //Writer out = new OutputStreamWriter (fileOut, "UTF-8");
	    String jenaMode = configurationProperties.getJenaMode();
		AbstractMaterializer materializer = AbstractMaterializer.create(rdfLanguage, outputFileName, jenaMode);
		Map<String, String> mappingDocumentPrefixMap = this.mappingDocument.getMappingDocumentPrefixMap(); 
		if(mappingDocumentPrefixMap != null) {
			materializer.setModelPrefixMap(mappingDocumentPrefixMap);
		}
		this.dataTranslator.setMaterializer(materializer);
		
		//materializing model
		long startGeneratingModel = System.currentTimeMillis();
		this.dataTranslator.translateData(translationResultMappingDocument);
		this.dataTranslator.materializer.materialize();
		
//		if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_RDFXML)) {
//			if(model == null) {
//				logger.warn("Model was empty!");
//			} else {
//				ModelWriter.writeModelStream(model, configurationProperties.getOutputFilePath(), configurationProperties.getRdfLanguage());
//				model.close();				
//			}
//		}
		
		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Materializing Mapping Document time was "+(durationGeneratingModel)+" s.");

		//cleaning up
		try {
			//out.flush(); out.close();
			//fileOut.flush(); fileOut.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
		}

		DBUtility.closeConnection(this.conn, "AbstractRunner");
		long end = System.currentTimeMillis();
		long duration = (end-start) / 1000;
		logger.info("Execution time was "+(duration)+" s.");
	}
	
	public String run()
			throws Exception {
		String status = null;
		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("substring", 3);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		ZUtils.addCustomFunction("lower", 1);
		
		//loading ontology file
		String ontologyFilePath = configurationProperties.getOntologyFilePath();
		
		//parsing mapping document
		String mappingDocumentPath = configurationProperties.getMappingDocumentFilePath();
		mappingDocument = parser.parse(mappingDocumentPath);

		//set output file
		String outputFileName = configurationProperties.getOutputFilePath();

		//process SPARQL file
		if(this.sparqQuery == null) {
			String queryFilePath = configurationProperties.getQueryFilePath();
			if(queryFilePath != null && !queryFilePath.equals("") ) {
				logger.info("Parsing query file : " + queryFilePath);
				this.sparqQuery = QueryFactory.read(queryFilePath);
			}
		}

		if(this.sparqQuery == null) {
			this.materializeMappingDocuments(outputFileName, mappingDocument);
		} else {
			//rewrite the SPARQL query if necessary
			List<Query> queries = new ArrayList<Query>();
			if(ontologyFilePath == null || ontologyFilePath.equals("")) {
				queries.add(this.sparqQuery);
			} else {
				//rewrite the query based on the mappings and ontology
				logger.info("Rewriting query...");
				Collection <String> mappedOntologyElements = 
					MappingsExtractor.getMappedPredicatesFromR2O(mappingDocumentPath);
				String rewritterWrapperMode = RewriterWrapper.fullMode;
				//RewriterWrapper rewritterWapper = new RewriterWrapper(ontologyFilePath, rewritterWrapperMode, mappedOntologyElements);
				//queries = rewritterWapper.rewrite(originalQuery);
				queries = RewriterWrapper.rewrite(this.sparqQuery, ontologyFilePath, RewriterWrapper.fullMode, mappedOntologyElements, RewriterWrapper.globalMatchMode);
				
				logger.debug("No of rewriting query result = " + queries.size());
				logger.debug("queries = " + queries);
			}			
			
			
			//translate sparql queries into sql queries
			Collection<String> sqlQueries = this.translateSPARQLQueriesIntoSQLQueries(queries);
			this.generateSPARQLXMLBindingDocument(sqlQueries, outputFileName);
		}
		
		logger.info("**********************DONE****************************");
		return status;

	}

	private Collection<String> translateSPARQLQueriesIntoSQLQueries(Collection<Query> queries) throws Exception {
		Collection<String> sparqlQueries = new Vector<String>();
		for(Query query : queries) {
			logger.debug("SPARQL Query = \n" + query);
			String sparql2SQLResult = 
					this.queryTranslator.translate(query).toString();
			logger.info("SQL Query = \n" + sparql2SQLResult);
			sparqlQueries.add(sparql2SQLResult);
		}
		
		return sparqlQueries;
	}
	
	private void generateSPARQLXMLBindingDocument(Collection<String> queries, String outputFileName) throws Exception {
		Document xmlDoc = XMLUtility.createNewXMLDocument();
		Connection conn = this.conn;
		int timeout = this.configurationProperties.getDatabaseTimeout();
		
		Element rootElement = null;
		Element headElement = null;
		Element resultsElement = null;
		List<Element> headElements = null;
		List<String> headElementsString = null;
		

		
		for(String query : queries) {
			try {
				ResultSet rs = DBUtility.executeQuery(conn, query, timeout);
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
						String dbValue = rs.getString(columnLabel);
						dbValue = this.translateResultSet(columnLabel, dbValue);
						bindingElement.setTextContent(dbValue);
						resultElement.appendChild(bindingElement);
					}
					i++;
				}
				String status = i  + " instance(s) retrieved ";
				logger.info(status);
				//translationResultMappingDocuments.add(translator.processQuery(query));
			} catch(Exception e) {
				e.printStackTrace();
				logger.error("error processing query, error message = " + e.getMessage());
				throw e;
			}

		}
		XMLUtility.saveXMLDocument(xmlDoc, outputFileName);
	}
	
	protected abstract String translateResultSet(String columnLabel, String dbValue);
	
	public String run(String mappingDirectory, String configurationFile) throws Exception {
		AbstractRunner.configurationProperties = this.loadConfigurationFile(mappingDirectory, configurationFile);
		return this.run();
	}

	public void setDataTranslator(AbstractDataTranslator dataTranslator) {
		this.dataTranslator = dataTranslator;
	}

	public void setParser(AbstractParser parser) {
		this.parser = parser;
	}

	public void setQueryTranslator(IQueryTranslator queryTranslator) {
		this.queryTranslator = queryTranslator;
	}
	
	public void setSparqQuery(Query sparqQuery) {
		this.sparqQuery = sparqQuery;
	}
	
	public void setSparqQuery(String sparqQuery) {
		this.sparqQuery = QueryFactory.create(sparqQuery);
	}

	public AbstractMappingDocument getMappingDocument() {
		return mappingDocument;
	}
}
