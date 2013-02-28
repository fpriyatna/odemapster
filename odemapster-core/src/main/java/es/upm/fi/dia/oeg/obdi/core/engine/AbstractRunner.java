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
import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public abstract class AbstractRunner {
	private static Logger logger = Logger.getLogger(AbstractRunner.class);
	public ConfigurationProperties configurationProperties;
	protected Connection conn;
	//protected AbstractParser parser;
	protected AbstractDataTranslator dataTranslator;
	private String queryTranslatorClassName = null;
	private IQueryTranslator queryTranslator;
	protected DefaultResultProcessor resultProcessor;
	protected AbstractMappingDocument mappingDocument;
	protected Query sparqQuery = null;
	private String queryResultWriterClassName = null;
	private AbstractQueryResultWriter queryResultWriter = null;
	private String dataSourceReaderClassName = null;
	private AbstractDataSourceReader dataSourceReader = null;
	private Object queryResultWriterOutput = null;
	
	public AbstractRunner() {
		
	}
	
	public AbstractRunner(String configurationDirectory, String configurationFile) 
			throws Exception {
		this.configurationProperties = new ConfigurationProperties
				(configurationDirectory, configurationFile);

		//mapping document
		String mappingDocumentFile = 
				this.configurationProperties.getMappingDocumentFilePath();
		if(mappingDocumentFile != null) {
			this.readMappingDocumentFile(mappingDocumentFile);
			logger.info("mapping document = " + this.mappingDocument);
		}

		//sparql query
		String queryFilePath = this.configurationProperties.getQueryFilePath();
		if(queryFilePath != null && !queryFilePath.equals("") ) {
			logger.info("Parsing query file : " + queryFilePath);
			this.sparqQuery = QueryFactory.read(queryFilePath);
		}

		if(this.sparqQuery == null) {
			//data translator
			this.createDataTranslator(this.configurationProperties);
		} else {
			//query translator
			this.queryTranslatorClassName = 
					this.configurationProperties.getQueryTranslatorClassName();
			if(this.queryTranslatorClassName != null) {
				this.buildQueryTranslator();	
			}

			//query writer
			this.queryResultWriterClassName = 
					this.configurationProperties.getQueryResultWriterClassName();
			if(this.queryResultWriterClassName != null) {
				this.buildQueryResultWriter();
			}

			//query evaluator
			this.dataSourceReaderClassName = 
					this.configurationProperties.getQueryEvaluatorClassName();
			if(this.dataSourceReaderClassName != null) {
				this.buildDataSourceReader();
			}

			//result processor
			this.resultProcessor = new DefaultResultProcessor(
					dataSourceReader, queryResultWriter);				

		}
	}

	public String run()
			throws Exception {
		String status = null;

		if(this.sparqQuery == null) {
			//set output file
			String outputFileName = configurationProperties.getOutputFilePath();
			this.materializeMappingDocuments(outputFileName, mappingDocument);
		} else {
			logger.info("sparql query = " + this.sparqQuery);

			//query translator
			if(this.queryTranslator == null) {
				if(this.queryTranslatorClassName == null) {
					this.queryTranslatorClassName = Constants.QUERY_TRANSLATOR_CLASSNAME_DEFAULT;					
				}
				this.buildQueryTranslator();
			}

			//query result writer
			if(this.queryResultWriter == null) {
				if(this.queryResultWriterClassName == null) {
					this.queryResultWriterClassName = Constants.QUERY_RESULT_WRITER_CLASSNAME_DEFAULT;					
				}
				this.buildQueryResultWriter();
			}

			//query evaluator
			if(this.dataSourceReader == null) {
				if(this.dataSourceReaderClassName == null) {
					this.dataSourceReaderClassName = Constants.QUERY_EVALUATOR_CLASSNAME_DEFAULT;
				}
				this.buildDataSourceReader();
			}

			//result processor
			this.resultProcessor = new DefaultResultProcessor(
					dataSourceReader, queryResultWriter);				

			//loading ontology file
			String ontologyFilePath = null;
			if(this.configurationProperties != null) {
				ontologyFilePath = this.configurationProperties.getOntologyFilePath();
			}
			

			//rewrite the SPARQL query if necessary
			List<Query> queries = new ArrayList<Query>();
			if(ontologyFilePath == null || ontologyFilePath.equals("")) {
				queries.add(this.sparqQuery);
			} else {
				//rewrite the query based on the mappings and ontology
				logger.info("Rewriting query...");
				String mappingDocumentFile = 
						this.configurationProperties.getMappingDocumentFilePath();
				Collection <String> mappedOntologyElements = 
						MappingsExtractor.getMappedPredicatesFromR2O(mappingDocumentFile);
				String rewritterWrapperMode = RewriterWrapper.fullMode;
				//RewriterWrapper rewritterWapper = new RewriterWrapper(ontologyFilePath, rewritterWrapperMode, mappedOntologyElements);
				//queries = rewritterWapper.rewrite(originalQuery);
				queries = RewriterWrapper.rewrite(this.sparqQuery, ontologyFilePath, RewriterWrapper.fullMode, mappedOntologyElements, RewriterWrapper.globalMatchMode);

				logger.debug("No of rewriting query result = " + queries.size());
				logger.debug("queries = " + queries);
			}			


			//translate sparql queries into sql queries
			Collection<SQLQuery> sqlQueries = 
					this.translateSPARQLQueriesIntoSQLQueries(queries);
			
			//translate result
			this.resultProcessor.translateResult(sqlQueries);
		}

		logger.info("**********************DONE****************************");
		return status;

	}

	protected abstract void createDataTranslator(
			ConfigurationProperties configurationProperties);

	protected abstract IQueryTranslationOptimizer buildQueryTranslationOptimizer();

	protected abstract AbstractUnfolder createUnfolder();

	public abstract void readMappingDocumentFile(String mappingDocumentFile) throws Exception;

	public ConfigurationProperties getConfigurationProperties() {
		if(this.configurationProperties == null) {
			this.configurationProperties = new ConfigurationProperties();
		}
		return this.configurationProperties;
	}

	public ConfigurationProperties getConfigurationProperties2() {
		return this.configurationProperties;
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

	public AbstractDataTranslator getDataTranslator() {
		return dataTranslator;
	}

	public IQueryTranslator getQueryTranslator() {
		return this.queryTranslator;
	}

	protected void buildQueryTranslator() throws Exception {
		this.queryTranslator = (IQueryTranslator) 
				Class.forName(this.queryTranslatorClassName).newInstance();					
		this.queryTranslator.setConfigurationProperties(configurationProperties);
		
		if(this.mappingDocument == null) {
			String mappingDocumentFilePath = this.getMappingDocumentPath();
			if(mappingDocumentFilePath == null) {
				throw new Exception("Mapping document is not set yet!");
			}
			this.readMappingDocumentFile(mappingDocumentFilePath);
		}
		this.queryTranslator.setMappingDocument(this.mappingDocument);

		//query translation optimizer
		IQueryTranslationOptimizer queryTranslationOptimizer = this.buildQueryTranslationOptimizer();
		boolean optimizeTriplesSameSubject = this.isSelfJoinElimination();
		queryTranslationOptimizer.setSelfJoinElimination(optimizeTriplesSameSubject);
		boolean eliminateSubQuery = this.isSubQueryElimination();
		queryTranslationOptimizer.setSubQueryElimination(eliminateSubQuery);
		boolean subQueryAsView = this.isSubQueryAsView();
		queryTranslationOptimizer.setSubQueryAsView(subQueryAsView);
		this.queryTranslator.setOptimizer(queryTranslationOptimizer);

		logger.info("query translator = " + this.queryTranslator);
	}

	private boolean isSelfJoinElimination() {
		boolean result = true;
		if(this.configurationProperties != null) {
			result = this.configurationProperties.isSelfJoinElimination();
		}
		return result;
	}
	
	private boolean isSubQueryElimination() {
		boolean result = true;
		if(this.configurationProperties != null) {
			result = this.configurationProperties.isSubQueryElimination();
		}
		return result;
	}

	private boolean isSubQueryAsView() {
		boolean result = false;
		if(this.configurationProperties != null) {
			result = this.configurationProperties.isSubQueryAsView();
		}
		return result;
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

	private void materializeMappingDocuments(String outputFileName
			, AbstractMappingDocument translationResultMappingDocument) throws Exception {
		long start = System.currentTimeMillis();

		String rdfLanguage = this.configurationProperties.getRdfLanguage();
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
		this.configurationProperties = this.loadConfigurationFile(mappingDirectory, configurationFile);
		return this.run();
	}

	private void setDataTranslator(AbstractDataTranslator dataTranslator) {
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

	public void readSPARQLFile(String sparqQueryFileURL) {
		this.sparqQuery = QueryFactory.read(sparqQueryFileURL);
	}

	
	public String getMappingDocumentPath() {
		return this.configurationProperties.getMappingDocumentFilePath();
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

	public void setQueryTranslatorClassName(String queryTranslatorClassName) throws Exception {
		this.queryTranslatorClassName = queryTranslatorClassName;
		//this.buildQueryTranslator();
	}

	public void setQueryResultWriterClassName(String queryResultWriterClassName) throws Exception {
		this.queryResultWriterClassName = queryResultWriterClassName;
		//this.buildQueryResultWriter();
	}

	public void setQueryResultWriterOutput(Object output) throws Exception {
		this.queryResultWriterOutput = output;
	}

	public AbstractQueryResultWriter getQueryResultWriter() {
		return this.queryResultWriter;
	}

	private void buildQueryResultWriter() throws Exception {
		this.queryResultWriter = (AbstractQueryResultWriter) 
				Class.forName(this.queryResultWriterClassName).newInstance();

		if(this.queryTranslator == null) {
			throw new Exception("Query Translator is not set yet!");
		}
		this.queryResultWriter.setQueryTranslator(this.queryTranslator);

		if(queryResultWriter instanceof XMLWriter && this.queryResultWriterOutput == null) {
			//set output file
			String outputFileName = null;
			if(this.configurationProperties != null) {
				outputFileName = this.configurationProperties.getOutputFilePath();
			}
			if(outputFileName == null) {
				outputFileName = Constants.QUERY_RESULT_XMLWRITER_OUTPUT_DEFAULT; 
			}
			this.queryResultWriterOutput = outputFileName;
		}
		queryResultWriter.setOutput(this.queryResultWriterOutput);

		logger.info("query result writer = " + this.queryResultWriter);
	}

	private void buildDataSourceReader() throws Exception {
		this.dataSourceReader = (AbstractDataSourceReader)
				Class.forName(this.dataSourceReaderClassName).newInstance();
		if(dataSourceReader instanceof RDBReader) {
			//database connection
			if(this.configurationProperties != null) {
				if(this.configurationProperties.getNoOfDatabase() > 1) {
					try { conn = this.getConnection(); } catch(Exception e) { }				
				}
				((RDBReader) dataSourceReader).setConnection(conn);
				int timeout = this.configurationProperties.getDatabaseTimeout();
				((RDBReader) dataSourceReader).setTimeout(timeout);
			}
		}

		logger.info("query evaluator = " + this.dataSourceReader);
	}

	public void setDataSourceReaderClassName(String queryEvaluatorClassName) {
		this.dataSourceReaderClassName = queryEvaluatorClassName;
	}
	

}
