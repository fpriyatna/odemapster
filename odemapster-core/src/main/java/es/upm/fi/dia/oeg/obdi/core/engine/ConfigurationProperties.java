package es.upm.fi.dia.oeg.obdi.core.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
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
	
	//batch upgrade
	private boolean literalRemoveStrangeChars;
	private boolean encodeUnsafeChars;
	private boolean encodeReservedChars;
	
	//database
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
	throws IOException, es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException, SQLException 
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

	private void readConfigurationFile(String r2oConfigurationDir) throws InvalidConfigurationPropertiesException, SQLException {

		int noOfDatabase = Integer.parseInt(this.getProperty("no_of_database"));
		if(noOfDatabase != 1) {
			throw new InvalidConfigurationPropertiesException("Only one database is supported.");
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
			
//			logger.info("Getting database connection...");
//			try {
//				this.conn = Utility.getLocalConnection(
//						databaseUser, databaseName, databasePassword, 
//						databaseDriver, databaseURL, "Runner");
//			} catch (SQLException e) {
//				String errorMessage = "Error loading database, error message = " + e.getMessage();
//				logger.error(errorMessage);
//				//e.printStackTrace();
//				throw e;
//			}
		}

		this.mappingDocumentFilePath = this.getProperty(Constants.MAPPINGDOCUMENT_FILE_PATH);
		this.ontologyFilePath = this.getProperty(Constants.ONTOFILE_PROP_NAME);
		this.outputFilePath = this.getProperty(Constants.OUTPUTFILE_PROP_NAME);
		this.queryFilePath = this.getProperty(Constants.QUERYFILE_PROP_NAME);
		

		if(r2oConfigurationDir != null) {
			this.mappingDocumentFilePath = r2oConfigurationDir + mappingDocumentFilePath;
			this.outputFilePath = r2oConfigurationDir + outputFilePath;
			if(this.queryFilePath != null && !this.queryFilePath.equals("")) {
				this.queryFilePath = r2oConfigurationDir + queryFilePath;
			}
			if(this.ontologyFilePath != null && !this.ontologyFilePath.equals("")) {
				this.ontologyFilePath = r2oConfigurationDir + ontologyFilePath;
			}
			
		}

		this.rdfLanguage = this.getProperty(Constants.OUTPUTFILE_RDF_LANGUAGE);
		logger.debug("rdf language = " + this.rdfLanguage);

		this.jenaMode = this.getProperty(Constants.JENA_MODE_TYPE);
		logger.debug("Jena mode = " + jenaMode);

		//boolean splitOutput = false;
		String splitOutputPerConcept = this.getProperty(Constants.SPLIT_OUTPUT_PER_CONCEPT);
		if(splitOutputPerConcept != null) {
			if(splitOutputPerConcept.equalsIgnoreCase("yes") || splitOutputPerConcept.equalsIgnoreCase("true")) {
				logger.warn("Split output is not supported anymore");
			}
		}

		String optimizeTBString = this.getProperty(Constants.OPTIMIZE_TB);
		if(optimizeTBString != null) {
			if(optimizeTBString.equalsIgnoreCase("yes") || optimizeTBString.equalsIgnoreCase("true")) {
				this.selfJoinElimination = true;
			}
		}
		logger.debug("Self join elimination = " + this.selfJoinElimination);

		String subQueryEliminationString = this.getProperty(Constants.SUBQUERY_ELIMINATION);
		if(subQueryEliminationString != null) {
			if(subQueryEliminationString.equalsIgnoreCase("yes") || subQueryEliminationString.equalsIgnoreCase("true")) {
				this.subQueryElimination = true;
			}
		}
		logger.debug("Subquery elimination = " + this.subQueryElimination);

		this.subQueryAsView = this.readBoolean(Constants.SUBQUERY_AS_VIEW, false);
		logger.debug("Subquery as view = " + this.subQueryAsView);

		String queryTranslatorDefaultClass = "es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator";
		this.queryTranslatorClassName = this.readString(Constants.QUERY_TRANSLATOR_CLASSNAME, queryTranslatorDefaultClass);
		
		String removeStrangeCharsFromLiteral = this.getProperty(Constants.REMOVE_STRANGE_CHARS_FROM_LITERAL);
		if(removeStrangeCharsFromLiteral != null) {
			if(removeStrangeCharsFromLiteral.equalsIgnoreCase("yes") || removeStrangeCharsFromLiteral.equalsIgnoreCase("true")) {
				this.literalRemoveStrangeChars = true;
			}
		}
		logger.debug("Remove Strange Chars From Literal Column = " + this.literalRemoveStrangeChars);

		String encodeUnsafeCharsString = this.getProperty(Constants.ENCODE_UNSAFE_CHARS_IN_URI_COLUMN);
		if(encodeUnsafeCharsString != null) {
			if(encodeUnsafeCharsString.equalsIgnoreCase("yes") || encodeUnsafeCharsString.equalsIgnoreCase("true")) {
				this.encodeUnsafeChars = true;
			}
		}
		logger.debug("Encode Unsafe Chars From URI Column = " + this.encodeUnsafeChars);

		String encodeReservedCharsString = this.getProperty(Constants.ENCODE_RESERVED_CHARS_IN_URI_COLUMN);
		if(encodeReservedCharsString != null) {
			if(encodeReservedCharsString.equalsIgnoreCase("yes") || encodeReservedCharsString.equalsIgnoreCase("true")) {
				this.encodeReservedChars = true;
			}
		}
		logger.debug("Encode Reserved Chars From URI Column = " + this.encodeReservedChars);

	}

	private Connection getConn() {
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
	
	

}
