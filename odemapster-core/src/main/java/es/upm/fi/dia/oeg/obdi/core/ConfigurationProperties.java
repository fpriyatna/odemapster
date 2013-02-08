package es.upm.fi.dia.oeg.obdi.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractQueryEvaluator;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractQueryResultWriter;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;

public class ConfigurationProperties extends Properties {
	private Logger logger = Logger.getLogger(ConfigurationProperties.class);

	private Connection conn;
	private String ontologyFilePath;
	private String mappingDocumentFilePath;
	private String outputFilePath;
	private String queryFilePath;
	private String rdfLanguage;
	private String jenaMode;
	private String databaseType;
	
	//query translator
	private boolean selfJoinElimination;
	private boolean subQueryElimination;
	private boolean subQueryAsView;
	private String queryTranslatorClassName;
	private IQueryTranslator queryTranslator;
	private String queryEvaluatorClassName;
	private AbstractQueryEvaluator queryEvaluator;
	private String queryTranslatorOutputWriterClassName;
	private AbstractQueryResultWriter queryResultWriter;
	
	//batch upgrade
	private boolean literalRemoveStrangeChars;
	private boolean encodeUnsafeChars;
	private boolean encodeReservedChars;
	
	//database
	private int noOfDatabase;
	private String databaseDriver; 
	private String databaseURL;
	private String databaseName;
	private String databaseUser;
	private String databasePassword;
	private int databaseTimeout = 0;
	
	public boolean isSelfJoinElimination() {
		return selfJoinElimination;
	}

	public ConfigurationProperties() {}
	
	public ConfigurationProperties(String configurationAbsoluteFilePath) throws IOException {
		File file = new File(configurationAbsoluteFilePath);
		logger.info("file.getAbsolutePath() = " + file.getAbsolutePath());
		logger.info("file.getName() = " + file.getName());
		logger.info("file.getCanonicalPath() = " + file.getCanonicalPath());
		logger.info("file.getParent() = " + file.getParent());
		logger.info("file.getPath() = " + file.getPath());
	}
	
	public ConfigurationProperties(
			String configurationDirectory, String configurationFile) 
	throws Exception 
	{
		String absoluteR2OConfigurationFile = configurationFile;
		if(configurationDirectory != null) {
			if(!configurationDirectory.endsWith("/")) {
				configurationDirectory = configurationDirectory + "/";
			}
			absoluteR2OConfigurationFile = configurationDirectory + configurationFile; 
		}

		try {
			this.load(new FileInputStream(absoluteR2OConfigurationFile));
		} catch (FileNotFoundException e) {
			String errorMessage = "Configuration file " + absoluteR2OConfigurationFile + " is not found!";
			logger.error(errorMessage);
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			String errorMessage = "Error reading configuration file " + absoluteR2OConfigurationFile;
			logger.error(errorMessage);
			e.printStackTrace();
			throw e;
		}

		this.readConfigurationFile(configurationDirectory);

	}
	
	public String getOutputFilePath() {
		return outputFilePath;
	}

	public String getMappingDocumentFilePath() {
		return mappingDocumentFilePath;
	}

	private void readConfigurationFile(String configurationDir) 
			throws Exception {

		this.noOfDatabase = this.readInteger(Constants.NO_OF_DATABASE_NAME_PROP_NAME, 0); 
		if(this.noOfDatabase != 0 && this.noOfDatabase != 1) {
			throw new InvalidConfigurationPropertiesException("Only zero or one database is supported.");
		}

		this.conn = null;
		for(int i=0; i<noOfDatabase;i++) {
			String propertyDatabaseDriver = Constants.DATABASE_DRIVER_PROP_NAME + "[" + i + "]";
			this.databaseDriver = this.getProperty(propertyDatabaseDriver);

			String propertyDatabaseURL = Constants.DATABASE_URL_PROP_NAME + "[" + i + "]";
			this.databaseURL = this.getProperty(propertyDatabaseURL);

			String propertyDatabaseName= Constants.DATABASE_NAME_PROP_NAME + "[" + i + "]";
			this.databaseName = this.getProperty(propertyDatabaseName);

			String propertyDatabaseUser = Constants.DATABASE_USER_PROP_NAME + "[" + i + "]";
			this.databaseUser = this.getProperty(propertyDatabaseUser);

			String propertyDatabasePassword = Constants.DATABASE_PWD_PROP_NAME  + "[" + i + "]";
			this.databasePassword = this.getProperty(propertyDatabasePassword);

			String propertyDatabaseType = Constants.DATABASE_TYPE_PROP_NAME  + "[" + i + "]";
			this.databaseType = this.getProperty(propertyDatabaseType);

			String propertyDatabaseTimeout = Constants.DATABASE_TIMEOUT_PROP_NAME  + "[" + i + "]";
			String timeoutPropertyString = this.getProperty(propertyDatabaseTimeout);
			if(timeoutPropertyString != null && !timeoutPropertyString.equals("")) {
				this.databaseTimeout = Integer.parseInt(timeoutPropertyString.trim());
			}
			
			logger.info("Getting database connection...");
			try {
				this.conn = DBUtility.getLocalConnection(
						databaseUser, databaseName, databasePassword, 
						databaseDriver, 
						databaseURL, "Configuration Properties");
			} catch (SQLException e) {
				String errorMessage = "Error loading database, error message = " + e.getMessage();
				logger.error(errorMessage);
				//e.printStackTrace();
				throw e;
			}
		}

		this.mappingDocumentFilePath = this.getProperty(Constants.MAPPINGDOCUMENT_FILE_PATH);
		this.ontologyFilePath = this.getProperty(Constants.ONTOFILE_PROP_NAME);
		this.outputFilePath = this.getProperty(Constants.OUTPUTFILE_PROP_NAME);
		this.queryFilePath = this.getProperty(Constants.QUERYFILE_PROP_NAME);
		

		if(configurationDir != null) {
			this.mappingDocumentFilePath = configurationDir + mappingDocumentFilePath;
			this.outputFilePath = configurationDir + outputFilePath;
			if(this.queryFilePath != null && !this.queryFilePath.equals("")) {
				this.queryFilePath = configurationDir + queryFilePath;
			}
			if(this.ontologyFilePath != null && !this.ontologyFilePath.equals("")) {
				this.ontologyFilePath = configurationDir + ontologyFilePath;
			}
			
		}

		this.rdfLanguage = this.readString(Constants.OUTPUTFILE_RDF_LANGUAGE, Constants.OUTPUT_FORMAT_NTRIPLE);
		logger.debug("rdf language = " + this.rdfLanguage);

		this.jenaMode = this.readString(Constants.JENA_MODE_TYPE, Constants.JENA_MODE_TYPE_MEMORY);
		logger.debug("Jena mode = " + jenaMode);

		this.selfJoinElimination = this.readBoolean(Constants.OPTIMIZE_TB, true);
		logger.debug("Self join elimination = " + this.selfJoinElimination);

		this.subQueryElimination = this.readBoolean(Constants.SUBQUERY_ELIMINATION, true);
		logger.debug("Subquery elimination = " + this.subQueryElimination);

		this.subQueryAsView = this.readBoolean(Constants.SUBQUERY_AS_VIEW, false);
		logger.debug("Subquery as view = " + this.subQueryAsView);

		this.queryTranslatorClassName = this.readString(
				Constants.QUERY_TRANSLATOR_CLASSNAME, Constants.QUERY_TRANSLATOR_CLASSNAME_DEFAULT);
		this.queryTranslator = (IQueryTranslator) Class.forName(this.queryTranslatorClassName).newInstance();
		
		this.queryEvaluatorClassName = this.readString(
				Constants.QUERY_EVALUATOR_CLASSNAME, Constants.QUERY_EVALUATOR_CLASSNAME_DEFAULT);
		this.queryEvaluator = (AbstractQueryEvaluator) Class.forName(this.queryEvaluatorClassName).newInstance();
		
		this.queryTranslatorOutputWriterClassName = this.readString(
				Constants.QUERY_RESULT_WRITER_CLASSNAME, Constants.QUERY_RESULT_WRITER_CLASSNAME_DEFAULT);
		this.queryResultWriter = (AbstractQueryResultWriter)
				Class.forName(this.queryTranslatorOutputWriterClassName).newInstance();
		
		this.literalRemoveStrangeChars = this.readBoolean(Constants.REMOVE_STRANGE_CHARS_FROM_LITERAL, true);
		logger.debug("Remove Strange Chars From Literal Column = " + this.literalRemoveStrangeChars);

		this.encodeUnsafeChars = this.readBoolean(Constants.ENCODE_UNSAFE_CHARS_IN_URI_COLUMN, true);
		logger.debug("Encode Unsafe Chars From URI Column = " + this.encodeUnsafeChars);

		this.encodeReservedChars = this.readBoolean(Constants.ENCODE_RESERVED_CHARS_IN_URI_COLUMN, true);
		logger.debug("Encode Reserved Chars From URI Column = " + this.encodeReservedChars);

	}

	public Connection getConn() {
		return conn;
	}

	public String getJenaMode() {
		return jenaMode;
	}

	public String getRdfLanguage() {
		if(this.rdfLanguage == null) {
			this.rdfLanguage = Constants.OUTPUT_FORMAT_NTRIPLE;
		}
		return rdfLanguage;
	}

	public String getDatabaseType() {
		if(this == null || this.databaseType == null) {
			this.databaseType = Constants.DATABASE_MYSQL;
		}
		return databaseType;
	}

	public String getQueryFilePath() {
		return queryFilePath;
	}

	public String getOntologyFilePath() {
		return ontologyFilePath;
	}

	public boolean isSubQueryElimination() {
		return subQueryElimination;
	}

	public boolean isLiteralRemoveStrangeChars() {
		return literalRemoveStrangeChars;
	}
	
	public Connection openConnection() throws SQLException {
		try {
			this.conn = DBUtility.getLocalConnection(
					databaseUser, databaseName, databasePassword, 
					databaseDriver, 
					databaseURL, "R2ORunner");
			return this.conn;
		} catch (SQLException e) {
			String errorMessage = "Error loading database, error message = " + e.getMessage();
			logger.error(errorMessage);
			//e.printStackTrace();
			throw e;
		}
	}

	public int getDatabaseTimeout() {
		return databaseTimeout;
	}

	public boolean isEncodeUnsafeChars() {
		return encodeUnsafeChars;
	}

	public boolean isEncodeReservedChars() {
		return encodeReservedChars;
	}

	public boolean isSubQueryAsView() {
		return subQueryAsView;
	}

	public void setSubQueryAsView(boolean subQueryAsView) {
		this.subQueryAsView = subQueryAsView;
	}

	private boolean readBoolean(String property, boolean defaultValue) {
		boolean result = defaultValue;

		String propertyString = this.getProperty(property);
		if(propertyString != null) {
			if(propertyString.equalsIgnoreCase("yes") || propertyString.equalsIgnoreCase("true")) {
				result = true;
			} else if(propertyString.equalsIgnoreCase("no") || propertyString.equalsIgnoreCase("false")) {
				result = false;
			}
		}

		return result;
	}
	
	private int readInteger(String property, int defaultValue) {
		int result = defaultValue;

		String propertyString = this.getProperty(property);
		if(propertyString != null && !propertyString.equals("")) {
			result = Integer.parseInt(propertyString);
		} 
		
		return result;
	}
	
	private String readString(String property, String defaultValue) {
		String result = defaultValue;
		
		String propertyString = this.getProperty(property);
		if(propertyString != null && !propertyString.equals("")) {
			result = propertyString;
		}
		return result;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public String getDatabaseDriver() {
		return databaseDriver;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public int getNoOfDatabase() {
		return noOfDatabase;
	}

	public AbstractQueryEvaluator getQueryEvaluator() {
		return queryEvaluator;
	}

	public IQueryTranslator getQueryTranslator() {
		return queryTranslator;
	}

	public AbstractQueryResultWriter getQueryResultWriter() {
		return queryResultWriter;
	}

	
	

}
