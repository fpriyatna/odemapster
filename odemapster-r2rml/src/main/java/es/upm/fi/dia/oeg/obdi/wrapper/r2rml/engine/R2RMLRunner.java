package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap.TermMapType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator;

public class R2RMLRunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(R2RMLRunner.class);
	private static final String QUERY_TRANSLATOR_CLASS_NAME = "es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator";
	private Map<String, Matcher> mapTemplateMatcher = new HashMap<String, Matcher>(); 
	private Map<String, Collection<String>> mapTemplateAttributes = new HashMap<String, Collection<String>>();
	
	public static void main(String args[]) {
		try {
			
			
			if(args == null || args.length == 0 || args.length != 2) {
				logger.info("usage R2RMLRunner propertiesDirectory propertiesFile");
				System.exit(-1);
			}
			
			//String configurationDirectory = System.getProperty("user.dir");
			String configurationDirectory = args[0];
			logger.info("propertiesDirectory = " + configurationDirectory);
			
			String configurationFile = args[1];
			logger.info("propertiesFile = " + configurationFile);
			
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
		} catch(Exception e) {
			logger.error("Exception occured!");
			logger.error("Error message = " + e.getMessage());
		}
	}
	
	public R2RMLRunner(String configurationDirectory, String configurationFile) 
			throws Exception {
		this.parser = new R2RMLParser();
		
		try {
			R2RMLRunner.configurationProperties = new ConfigurationProperties(configurationDirectory, configurationFile);
			String mappingDocumentFile = R2RMLRunner.configurationProperties.getMappingDocumentFilePath();
			String queryFilePath = R2RMLRunner.configurationProperties.getQueryFilePath();
			
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			R2RMLElementUnfoldVisitor unfolder = new R2RMLElementUnfoldVisitor(
					R2RMLRunner.configurationProperties);

			this.queryTranslator = super.createQueryTranslator(
					R2RMLRunner.QUERY_TRANSLATOR_CLASS_NAME, md, unfolder);
			this.queryTranslator.setQueryFilePath(queryFilePath);
			this.dataTranslator = new R2RMLElementDataTranslateVisitor(
					R2RMLRunner.configurationProperties, unfolder);
			
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

//	public static void runFreddy(String configurationDirectory, String testName) {
//		logger.info("------ Running " + testName + " Freddy ------");
//		String configurationFile = testName + ".r2rml.properties";
//		String queryFilePath = configurationDirectory + testName + ".sparql";
//		
//		try {
//			long start = System.currentTimeMillis();
//			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
//			IQueryTranslator queryTranslator = runner.getQueryTranslator();
//			queryTranslator.setOptimizeTripleBlock(true);
//			queryTranslator.setSubQueryElimination(true);
//			queryTranslator.setSubqueryAsView(false);
//			queryTranslator.setQueryFilePath(queryFilePath);
//			
//			SQLQuery query = queryTranslator.translateFromFile();
//			logger.info("query = \n" + query + "\n");
//			Connection conn = AbstractRunner.getConnection();
//
//			long end = System.currentTimeMillis();
//			logger.info("test execution time was "+(end-start)+" ms.");
//			logger.info("------" + testName + " Freddy DONE------");
//		} catch(Exception e) {
//			e.printStackTrace();
//			logger.error("Error : " + e.getMessage());
//			logger.info("------" + testName + " FAILED------\n\n");
//			assertTrue(e.getMessage(), false);
//		}
//	}	
//
//	public static void runFreddyView(String configurationDirectory, String testName) {
//		logger.info("------ Running " + testName + " Freddy View ------");
//		String configurationFile = testName + ".r2rml.properties";
//		String queryFilePath = configurationDirectory + testName + ".sparql";
//
//		try {
//			long start = System.currentTimeMillis();
//			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
//			IQueryTranslator queryTranslator = runner.getQueryTranslator();
//			queryTranslator.setOptimizeTripleBlock(true);
//			queryTranslator.setSubqueryAsView(true);
//			queryTranslator.setQueryFilePath(queryFilePath);
//			
////			boolean optimizeTripleBlock = true;
////			boolean subqueryAsView = true;
////			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName, optimizeTripleBlock, subqueryAsView);
////			
//			SQLQuery query = queryTranslator.translateFromFile();
//			logger.info("query = \n" + query + "\n");
//			Connection conn = AbstractRunner.getConnection();
//
////			ResultSet rs = DBUtility.executeQuery(conn, query.toString());
////			int noOfRows = DBUtility.getRowCount(rs);
////			logger.info("noOfRows = " + noOfRows);
//
//			
//			long end = System.currentTimeMillis();
//			logger.info("test execution time was "+(end-start)+" ms.");
//			logger.info("------" + testName + " Freddy View DONE------\n\n");
//		} catch(Exception e) {
//			e.printStackTrace();
//			logger.error("Error : " + e.getMessage());
//			logger.info("------" + testName + " FAILED------\n\n");
//			assertTrue(e.getMessage(), false);
//		}
//	}

	
	protected String translateResultSet(String columnLabel, String dbValue) {
		String result = dbValue;
		
		try {
			if(dbValue != null) {
				R2RMLQueryTranslator r2rmlQueryTranslator = (R2RMLQueryTranslator) this.queryTranslator;
				Map<String, R2RMLTermMap> mapNodeMapping = r2rmlQueryTranslator.getMapVarMapping(); 
				R2RMLTermMap termMap = mapNodeMapping.get(columnLabel);
				if(termMap != null) {
					if(termMap.getTermMapType() == TermMapType.TEMPLATE) {
						String template = termMap.getTemplate();
						Matcher matcher = this.mapTemplateMatcher.get(template);
						if(matcher == null) {
							Pattern pattern = Pattern.compile(R2RMLConstants.R2RML_TEMPLATE_PATTERN);
							matcher = pattern.matcher(template);
							this.mapTemplateMatcher.put(template, matcher);
						}
						Collection<String> attributes = this.mapTemplateAttributes.get(template);
						if(attributes == null) {
							attributes = R2RMLUtility.getAttributesFromStringTemplate(template);
							this.mapTemplateAttributes.put(template, attributes);
						}
						
						Map<String, String> replacements = new HashMap<String, String>();
						replacements.put(attributes.iterator().next(), dbValue);
						result = R2RMLUtility.replaceTokens(template, replacements);
					}					
				}
			}			
		} catch(Exception e) {
			logger.error("Error occured while translating result set!");
		}
		
		return result;
	}
}
