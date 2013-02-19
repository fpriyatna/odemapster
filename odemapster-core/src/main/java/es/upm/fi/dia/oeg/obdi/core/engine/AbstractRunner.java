package es.upm.fi.dia.oeg.obdi.core.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import es.upm.fi.dia.oeg.newrqr.MappingsExtractor;
import es.upm.fi.dia.oeg.newrqr.RewriterWrapper;
import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public abstract class AbstractRunner {
	private static Logger logger = Logger.getLogger(AbstractRunner.class);
	public static ConfigurationProperties configurationProperties;
	protected Connection conn;
	//protected AbstractParser parser;
	protected AbstractDataTranslator dataTranslator;
	private String queryTranslatorClassName = null;
	private IQueryTranslator queryTranslator;
	protected DefaultResultProcessor resultProcessor;
	private AbstractMappingDocument mappingDocument;
	protected Query sparqQuery = null;
	private String queryResultWriterClassName = null;



	public AbstractRunner(String configurationDirectory, String configurationFile) 
			throws Exception {
		AbstractRunner.configurationProperties = new ConfigurationProperties(
				configurationDirectory, configurationFile);
	}

	protected abstract AbstractDataTranslator createDataTranslator(
			ConfigurationProperties configurationProperties);

	//	protected abstract IQueryTranslator createQueryTranslator(
	//			AbstractMappingDocument mappingDocument) throws Exception;

	protected abstract IQueryTranslationOptimizer createQueryTranslationOptimizer();

	protected abstract AbstractUnfolder createUnfolder();

	protected abstract AbstractMappingDocument createMappingDocument(String mappingDocumentFile) throws Exception;

	public static ConfigurationProperties getConfigurationProperties() {
		if(configurationProperties == null) {
			configurationProperties = new ConfigurationProperties();
		}
		return configurationProperties;
	}

	public ConfigurationProperties getConfigurationProperties2() {
		return AbstractRunner.configurationProperties;
	}

	public Connection getConnection() throws SQLException {
		if(this.configurationProperties.getNoOfDatabase() > 0 && 
				this.conn == null) {
			String databaseUser = this.configurationProperties.getDatabaseUser();
			String databaseName = this.configurationProperties.getDatabaseName();
			String databasePassword = this.configurationProperties.getDatabasePassword();
			String databaseDriver = this.configurationProperties.getDatabaseDriver();
			String databaseURL = this.configurationProperties.getDatabaseURL();

			try {
				this.conn = DBUtility.getLocalConnection(
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



	public AbstractDataTranslator getDataTranslator() {
		return dataTranslator;
	}

	public IQueryTranslator getQueryTranslator() {
		return this.queryTranslator;
	}

	public void buildQueryTranslator() throws Exception {
		if(this.queryTranslatorClassName == null) {
			this.queryTranslator = 
					AbstractRunner.configurationProperties.getQueryTranslator();
		} else {
			try {
				this.queryTranslator = (IQueryTranslator) 
						Class.forName(this.queryTranslatorClassName).newInstance();					
			} catch(Exception e) {
				logger.error("error creating an instance of query translator!");
			}
		}
		
		if(this.mappingDocument == null) {
			String mappingDocumentFilePath = this.getMappingDocumentPath();
			this.mappingDocument = this.createMappingDocument(mappingDocumentFilePath);
		}
		this.queryTranslator.setMappingDocument(this.mappingDocument);
	}

	protected ConfigurationProperties loadConfigurationFile(
			String mappingDirectory, String configurationFile) 
					throws Exception {
		logger.debug("Active Directory = " + mappingDirectory);
		logger.debug("Loading configuration file : " + configurationFile);

		try {
			ConfigurationProperties configurationProperties = 
					new ConfigurationProperties(mappingDirectory, configurationFile);
			return configurationProperties;
		} catch(Exception e) {
			logger.error("Error while loding properties file : " + configurationFile);
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

		DBUtility.closeConnection(this.conn, this.getClass().getName());
		long end = System.currentTimeMillis();
		long duration = (end-start) / 1000;
		logger.info("Execution time was "+(duration)+" s.");
	}

	public String run()
			throws Exception {
		String status = null;

		//database connection
		if(AbstractRunner.configurationProperties.getNoOfDatabase() > 1) {
			try { conn = this.getConnection(); } catch(Exception e) { }				
		}

		//mapping document
		String mappingDocumentFile = AbstractRunner.configurationProperties
				.getMappingDocumentFilePath();
		this.mappingDocument = this.createMappingDocument(mappingDocumentFile);

		//query translator if exists sparql query
		if(this.sparqQuery == null) {
			String queryFilePath = AbstractRunner.configurationProperties.getQueryFilePath();
			if(queryFilePath != null && !queryFilePath.equals("") ) {
				logger.info("Parsing query file : " + queryFilePath);
				this.sparqQuery = QueryFactory.read(queryFilePath);
			}
		}

		if(this.sparqQuery != null) {
			this.buildQueryTranslator();
			
			//query translation optimizer
			IQueryTranslationOptimizer queryTranslationOptimizer = this.createQueryTranslationOptimizer();
			boolean optimizeTriplesSameSubject = AbstractRunner.configurationProperties.isSelfJoinElimination();
			queryTranslationOptimizer.setSelfJoinElimination(optimizeTriplesSameSubject);
			boolean eliminateSubQuery = AbstractRunner.configurationProperties.isSubQueryElimination();
			queryTranslationOptimizer.setSubQueryElimination(eliminateSubQuery);
			boolean subQueryAsView = AbstractRunner.configurationProperties.isSubQueryAsView();
			queryTranslationOptimizer.setSubQueryAsView(subQueryAsView);
			this.queryTranslator.setOptimizer(queryTranslationOptimizer);
		}

		//data translator
		this.dataTranslator = this.createDataTranslator(AbstractRunner.configurationProperties);

		//loading ontology file
		String ontologyFilePath = this.configurationProperties.getOntologyFilePath();

		//		//parsing mapping document
		String mappingDocumentPath = configurationProperties.getMappingDocumentFilePath();
		//		mappingDocument = parser.parse(mappingDocumentPath);

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
			Collection<SQLQuery> sqlQueries = this.translateSPARQLQueriesIntoSQLQueries(queries);
			//this.generateSPARQLXMLBindingDocument(sqlQueries, outputFileName);

			AbstractQueryEvaluator queryEvaluator = this.configurationProperties.getQueryEvaluator();		
			if(queryEvaluator instanceof RDBQueryEvaluator) {
				((RDBQueryEvaluator) queryEvaluator).setConnection(conn);
				int timeout = this.configurationProperties.getDatabaseTimeout();
				((RDBQueryEvaluator) queryEvaluator).setTimeout(timeout);
			}

			AbstractQueryResultWriter queryResultWriter = null;
			if(this.queryResultWriterClassName == null) {
				queryResultWriter = 
						this.configurationProperties.getQueryResultWriter();
			} else {
				queryResultWriter = (AbstractQueryResultWriter) 
						Class.forName(this.queryResultWriterClassName).newInstance();
			}
			queryResultWriter.setQueryTranslator(queryTranslator);
			if(queryResultWriter instanceof XMLWriter) {
				((XMLWriter) queryResultWriter).setOutputFileName(outputFileName);
			}

			this.resultProcessor = new DefaultResultProcessor(
					this, queryEvaluator, queryResultWriter);

			//			Document xmlDoc = this.resultTranslator.generateSPARQLQueryResultXMLFile(
			//					sqlQueries, outputFileName);
			//			XMLUtility.saveXMLDocument(xmlDoc, outputFileName);
			//			this.resultTranslator.generateSPARQLQueryResultXMLFile(
			//					sqlQueries, outputFileName);
			this.resultProcessor.translateResult(sqlQueries);
		}

		logger.info("**********************DONE****************************");
		return status;

	}

	private Collection<SQLQuery> translateSPARQLQueriesIntoSQLQueries(
			Collection<Query> sparqlQueries) throws Exception {
		Collection<SQLQuery> sqlQueries = new Vector<SQLQuery>();
		for(Query sparqlQuery : sparqlQueries) {
			logger.debug("SPARQL Query = \n" + sparqlQuery);
			SQLQuery sqlQuery = 
					this.queryTranslator.translate(sparqlQuery);
			logger.debug("SQL Query = \n" + sqlQuery);
			sqlQueries.add(sqlQuery);
		}

		return sqlQueries;
	}

	public String run(String mappingDirectory, String configurationFile) throws Exception {
		AbstractRunner.configurationProperties = this.loadConfigurationFile(mappingDirectory, configurationFile);
		return this.run();
	}

	public void setDataTranslator(AbstractDataTranslator dataTranslator) {
		this.dataTranslator = dataTranslator;
	}

	//	public void setParser(AbstractParser parser) {
	//		this.parser = parser;
	//	}

	public void setQueryTranslator(IQueryTranslator queryTranslator) {
		this.queryTranslator = queryTranslator;
	}

	public void setSparqQuery(Query sparqQuery) {
		this.sparqQuery = sparqQuery;
	}

	public void setSparqQuery(String sparqQuery) {
		this.sparqQuery = QueryFactory.create(sparqQuery);
	}

	public String getMappingDocumentPath() {
		return AbstractRunner.configurationProperties.getMappingDocumentFilePath();
	}
	
	public AbstractMappingDocument getMappingDocument() {
		return mappingDocument;
	}

	//	public abstract String getQueryTranslatorClassName();

	public void setResultTranslator(DefaultResultProcessor resultTranslator) {
		this.resultProcessor = resultTranslator;
	}

	public String getOutputFilePath() {
		return this.configurationProperties.getOutputFilePath();
	}

	public void setQueryTranslatorClassName(String queryTranslatorClassName) {
		this.queryTranslatorClassName = queryTranslatorClassName;
	}

	public void setQueryResultWriterClassName(String queryResultWriterClassName) {
		this.queryResultWriterClassName = queryResultWriterClassName;
	}

	public AbstractQueryResultWriter getQueryResultWriter() {
		return this.resultProcessor.queryResultWriter;
	}
}
