package es.upm.fi.dia.oeg.obdi.core.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class ConfigurationProperties extends Properties {
	private Logger logger = Logger.getLogger(ConfigurationProperties.class);

	private Connection conn;
	private String ontologyFilePath;
	private String r2oFilePath;
	private String outputFilePath;
	private String queryFilePath;
	private String rdfLanguage;
	private String jenaMode;
	private String databaseType;
	private boolean optimizeTB;
	private boolean subQueryElimination;
	private boolean literalRemoveStrangeChars;
	
	private String databaseDriver; 
	private String databaseURL;
	private String databaseName;
	private String databaseUser;
	private String databasePassword;
	
	public boolean isOptimizeTB() {
		return optimizeTB;
	}

	public ConfigurationProperties(
			String r2oConfigurationDir, String r2oConfigurationFile) 
	throws IOException, InvalidConfigurationPropertiesException, SQLException 
	{
		String absoluteR2OConfigurationFile = r2oConfigurationFile;
		if(r2oConfigurationDir != null) {
			absoluteR2OConfigurationFile = r2oConfigurationDir + r2oConfigurationFile; 
		}

		try {
			this.load(new FileInputStream(absoluteR2OConfigurationFile));
		} catch (FileNotFoundException e) {
			String errorMessage = "R2O configuration file " + absoluteR2OConfigurationFile + " is not found!";
			logger.error(errorMessage);
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			String errorMessage = "Error reading R2O configuration file " + absoluteR2OConfigurationFile;
			logger.error(errorMessage);
			e.printStackTrace();
			throw e;
		}

		this.readConfigurationFile(r2oConfigurationDir);

	}
	
	public String getOutputFilePath() {
		return outputFilePath;
	}

	public String getR2oFilePath() {
		return r2oFilePath;
	}

	private void readConfigurationFile(String r2oConfigurationDir) throws InvalidConfigurationPropertiesException, SQLException {

		int noOfDatabase = Integer.parseInt(this.getProperty("no_of_database"));
		if(noOfDatabase != 1) {
			throw new InvalidConfigurationPropertiesException("Only one database is supported.");
		}

		this.conn = null;
		for(int i=0; i<noOfDatabase;i++) {
			String propertyDatabaseDriver = R2OConstants.DATABASE_DRIVER_PROP_NAME + "[" + i + "]";
			this.databaseDriver = this.getProperty(propertyDatabaseDriver);

			String propertyDatabaseURL = R2OConstants.DATABASE_URL_PROP_NAME + "[" + i + "]";
			this.databaseURL = this.getProperty(propertyDatabaseURL);

			String propertyDatabaseName= R2OConstants.DATABASE_NAME_PROP_NAME + "[" + i + "]";
			this.databaseName = this.getProperty(propertyDatabaseName);

			String propertyDatabaseUser = R2OConstants.DATABASE_USER_PROP_NAME + "[" + i + "]";
			this.databaseUser = this.getProperty(propertyDatabaseUser);

			String propertyDatabasePassword = R2OConstants.DATABASE_PWD_PROP_NAME  + "[" + i + "]";
			this.databasePassword = this.getProperty(propertyDatabasePassword);

			String propertyDatabaseType = R2OConstants.DATABASE_TYPE_PROP_NAME  + "[" + i + "]";
			this.databaseType = this.getProperty(propertyDatabaseType);

			try {
				this.conn = Utility.getLocalConnection(
						databaseUser, databaseName, databasePassword, 
						databaseDriver, 
						databaseURL, "R2ORunner");
			} catch (SQLException e) {
				String errorMessage = "Error loading database, error message = " + e.getMessage();
				logger.error(errorMessage);
				//e.printStackTrace();
				throw e;
			}
		}

		this.r2oFilePath = this.getProperty(R2OConstants.R2OFILE_PROP_NAME);
		this.ontologyFilePath = this.getProperty(R2OConstants.ONTOFILE_PROP_NAME);
		this.outputFilePath = this.getProperty(R2OConstants.OUTPUTFILE_PROP_NAME);
		this.queryFilePath = this.getProperty(R2OConstants.QUERYFILE_PROP_NAME);
		

		if(r2oConfigurationDir != null) {
			this.r2oFilePath = r2oConfigurationDir + r2oFilePath;
			this.outputFilePath = r2oConfigurationDir + outputFilePath;
			if(this.queryFilePath != null && !this.queryFilePath.equals("")) {
				this.queryFilePath = r2oConfigurationDir + queryFilePath;
			}
			if(this.ontologyFilePath != null && !this.ontologyFilePath.equals("")) {
				this.ontologyFilePath = r2oConfigurationDir + ontologyFilePath;
			}
			
		}

		this.rdfLanguage = this.getProperty(R2OConstants.OUTPUTFILE_RDF_LANGUAGE);
		logger.debug("rdf language = " + this.rdfLanguage);

		this.jenaMode = this.getProperty(R2OConstants.JENA_MODE_TYPE);
		logger.debug("Jena mode = " + jenaMode);

		//boolean splitOutput = false;
		String splitOutputPerConcept = this.getProperty(R2OConstants.SPLIT_OUTPUT_PER_CONCEPT);
		if(splitOutputPerConcept != null) {
			if(splitOutputPerConcept.equalsIgnoreCase("yes") || splitOutputPerConcept.equalsIgnoreCase("true")) {
				logger.warn("Split output is not supported anymore");
			}
		}

		String optimizeTBString = this.getProperty(R2OConstants.OPTIMIZE_TB);
		if(optimizeTBString != null) {
			if(optimizeTBString.equalsIgnoreCase("yes") || optimizeTBString.equalsIgnoreCase("true")) {
				this.optimizeTB = true;
			}
		}
		logger.debug("Self join elimination = " + this.optimizeTB);

		String subQueryEliminationString = this.getProperty(R2OConstants.SUBQUERY_ELIMINATION);
		if(subQueryEliminationString != null) {
			if(subQueryEliminationString.equalsIgnoreCase("yes") || subQueryEliminationString.equalsIgnoreCase("true")) {
				this.subQueryElimination = true;
			}
		}
		logger.debug("Subquery elimination = " + this.subQueryElimination);

		String removeStrangeCharsFromLiteral = this.getProperty(R2OConstants.REMOVE_STRANGE_CHARS_FROM_LITERAL);
		if(removeStrangeCharsFromLiteral != null) {
			if(removeStrangeCharsFromLiteral.equalsIgnoreCase("yes") || removeStrangeCharsFromLiteral.equalsIgnoreCase("true")) {
				this.literalRemoveStrangeChars = true;
			}
		}
		logger.debug("Self join elimination = " + this.optimizeTB);

	}

	public Connection getConn() {
		return conn;
	}

	public String getJenaMode() {
		return jenaMode;
	}

	public String getRdfLanguage() {
		if(this.rdfLanguage == null) {
			this.rdfLanguage = R2OConstants.OUTPUT_FORMAT_NTRIPLE;
		}
		return rdfLanguage;
	}

	public String getDatabaseType() {
		if(this.databaseType == null) {
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
			this.conn = Utility.getLocalConnection(
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
	
	

}
