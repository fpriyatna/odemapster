package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;

public class R2RML_Kuwait_Clinic {

	private static Logger logger = Logger.getLogger(R2RMLTC.class);
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-mappings/kuwaitclinic/";
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void runBatch(String configurationDirectory, String configurationFile, String mappingDocumentFile, String testName) {
		try {
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			R2RMLElementVisitor dataTranslateVisitor = new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile); 
			md.accept(dataTranslateVisitor);
			
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			//e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}

	public void runChebotko(String testName) {
		logger.info("------ Running " + testName + " Chebotko ------");
		String configurationFile = testName + ".r2rml.properties";
		String queryFilePath = configurationDirectory + testName + ".sparql";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = runner.getQueryTranslator();
			//queryTranslator.setOptimizeTripleBlock(false);
			queryTranslator.setQueryFilePath(queryFilePath);

//			boolean optimizeTripleBlock = false;
//			boolean subqueryAsView = false;			
//			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName, optimizeTripleBlock, subqueryAsView);
			
			SQLQuery query = queryTranslator.translateFromPropertyFile();
			logger.info("sql query = \n" + query + "\n");
			Connection conn = runner.getConnection();
			ResultSet rs = DBUtility.executeQuery(conn, query.toString(), 0);
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			
			int noOfRows = DBUtility.getRowCount(rs);
			logger.info("noOfRows = " + noOfRows);
			logger.info("------" + testName + " Chebotko DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	public void runFreddy(String testName) {
		logger.info("------ Running " + testName + " Freddy ------");
		String configurationFile = testName + ".r2rml.properties";
		String queryFilePath = configurationDirectory + testName + ".sparql";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = runner.getQueryTranslator();
			SQLQuery query = queryTranslator.translateFromPropertyFile();
			logger.info("query = \n" + query + "\n");
			Connection conn = runner.getConnection();
			ResultSet rs = DBUtility.executeQuery(conn, query.toString(), 0);
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			int noOfRows = DBUtility.getRowCount(rs);
			logger.info("noOfRows = " + noOfRows);
			logger.info("------" + testName + " Freddy DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void testKuwaitClinic02Batch() throws Exception {
		String testName = "kuwaitclinic02";
		String configurationDirectory = mappingDirectory + "r2rml-mappings/kuwaitclinic/";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + "kuwaitclinic.ttl";
		this.runBatch(configurationDirectory, configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testKuwaitClinic02Chebotko() throws Exception {
		String testName = "kuwaitclinic02";
		this.runChebotko(testName);
	}
	
	@Test
	public void testKuwaitClinic02Freddy() throws Exception {
		String testName = "kuwaitclinic02";
		this.runFreddy(testName);
	}

}
