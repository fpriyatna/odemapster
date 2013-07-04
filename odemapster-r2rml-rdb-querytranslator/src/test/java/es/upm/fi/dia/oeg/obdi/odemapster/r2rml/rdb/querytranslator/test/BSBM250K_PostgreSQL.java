package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import Zql.ZStatement;
import Zql.ZqlParser;
import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.dia.oeg.obdi.core.ScriptRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.sql.IQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator.R2RMLQueryTranslator;

public class BSBM250K_PostgreSQL {
	private static Logger logger = Logger.getLogger(BSBM250K_PostgreSQL.class);
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-bsbm-postgresql-250k/";
	private String mappingDocumentFile = configurationDirectory + "bsbm.ttl";
	private static IQueryTranslator queryTranslatorFreddy = null;
	
	private String url = "jdbc:postgresql://localhost:5432/bsbm250k-original";
	private String username = "fpriyatna";
	private String password = "password";
	private String databaseType = Constants.DATABASE_POSTGRESQL;
	private String databaseName = "bsbm250k-original"; 

	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	public IQueryTranslator getQueryTranslatorFreddy() throws Exception {
		if(queryTranslatorFreddy != null) {
			queryTranslatorFreddy = R2RMLQueryTranslator.getQueryTranslatorFreddy(
					url, username, password, databaseType, databaseName, mappingDocumentFile);	
		}
		
		return queryTranslatorFreddy;
	}
	
	
	public void runChebotko(String testName) {
		logger.info("------ Running " + testName + " Chebotko ------");
		String configurationFile = testName + ".r2rml.properties";
		String queryFilePath = configurationDirectory + testName + ".sparql";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunnerChebotko(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = runner.getQueryTranslator();
			IQuery query = queryTranslator.translateFromQueryFile(queryFilePath);
			logger.info("sql query = \n" + query + "\n");
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			logger.info("------" + testName + " Chebotko DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}

	public void runChebotkoView(String testName) {
		logger.info("------ Running " + testName + " Chebotko View ------");
		String configurationFile = testName + ".r2rml.properties";
		String queryFilePath = configurationDirectory + testName + ".sparql";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = runner.getQueryTranslator();
			IQuery query = queryTranslator.translateFromQueryFile(queryFilePath);
			logger.info("query = \n" + query + "\n");
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			logger.info("------" + testName + " Chebotko View DONE------\n\n");
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
		long start = System.currentTimeMillis();
		
		try {
			AbstractRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = runner.getQueryTranslator();
			Connection conn = runner.getConnection();
			runner.run();
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			logger.info("------" + testName + " Freddy DONE------");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}

	public void runFreddyView(String testName) {
		logger.info("------ Running " + testName + " Freddy View ------");
		String configurationFile = testName + ".r2rml.properties";
		String queryFilePath = configurationDirectory + testName + ".sparql";

		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = runner.getQueryTranslator();
			IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
			queryTranslationOptimizer.setSelfJoinElimination(true);
			queryTranslationOptimizer.setUnionQueryReduction(true);
			queryTranslationOptimizer.setSubQueryAsView(true);
			queryTranslator.setOptimizer(queryTranslationOptimizer);
			IQuery query = queryTranslator.translateFromQueryFile(queryFilePath);
			logger.info("query = \n" + query + "\n");
			Connection conn = runner.getConnection();
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			logger.info("------" + testName + " Freddy View DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	

	
	@Test
	public void testBSBM() throws Exception {
		String testName = "bsbm";
		logger.info("------" + testName + " STARTED------");
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		try {
			R2RMLRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			R2RMLMappingDocument md = (R2RMLMappingDocument) runner.getMappingDocument();
			md.accept(new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile));
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}


	@Test
	public void testBSBM01Chebotko() throws Exception {
		String testName = "bsbm01";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM01ChebotkoView() throws Exception {
		String testName = "bsbm01";
		this.runChebotkoView(testName);
	}

	@Test
	public void testBSBM01Freddy() throws Exception {
		String queryFile = configurationDirectory + "bsbm01.sparql";
		try {
			IQueryTranslator queryTranslatorFreddy = R2RMLQueryTranslator.getQueryTranslatorFreddy(
					url, username, password, databaseType, databaseName, mappingDocumentFile);	
			IQuery sqlQuery = queryTranslatorFreddy.translateFromQueryFile(queryFile);
			logger.info("sqlQuery = " + sqlQuery);
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		logger.info("Done");
	}

	@Test
	public void testBSBM02Chebotko() throws Exception {
		String testName = "bsbm02";
		this.runChebotko(testName);
	}	

	@Test
	public void testBSBM02ChebotkoView() throws Exception {
		String testName = "bsbm02";
		this.runChebotkoView(testName);
	}	

	@Test
	public void testBSBM02Freddy() throws Exception {
		String queryFile = configurationDirectory + "bsbm02.sparql";
		try {
			IQueryTranslator queryTranslatorFreddy = R2RMLQueryTranslator.getQueryTranslatorFreddy(
					url, username, password, databaseType, databaseName, mappingDocumentFile);	
			IQuery sqlQuery = queryTranslatorFreddy.translateFromQueryFile(queryFile);
			logger.info("sqlQuery = \n" + sqlQuery);
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
	}



	@Test
	public void testBSBM03Chebotko() throws Exception {
		String testName = "bsbm03";
		this.runChebotko(testName);
	}	

	@Test
	public void testBSBM03ChebotkoView() throws Exception {
		String testName = "bsbm03";
		this.runChebotkoView(testName);
	}	

	@Test
	public void testBSBM03Freddy() throws Exception {
		String testName = "bsbm03";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM03FreddyView() throws Exception {
		String testName = "bsbm03";
		this.runFreddyView(testName);
	}

	@Test
	public void testBSBM04Chebotko() throws Exception {
		String testName = "bsbm04";
		this.runChebotko(testName);
	}	

	@Test
	public void testBSBM04ChebotkoView() throws Exception {
		String testName = "bsbm04";
		this.runChebotkoView(testName);
	}	

	@Test
	public void testBSBM04Freddy() throws Exception {
		String testName = "bsbm04";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM04FreddyView() throws Exception {
		String testName = "bsbm04";
		this.runFreddyView(testName);
	}

	@Test
	public void testBSBM05Chebotko() throws Exception {
		String testName = "bsbm05";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM05ChebotkoView() throws Exception {
		String testName = "bsbm05";
		this.runChebotkoView(testName);
	}

	@Test
	public void testBSBM05Freddy() throws Exception {
		String testName = "bsbm05";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM05FreddyView() throws Exception {
		String testName = "bsbm05";
		this.runFreddyView(testName);
	}

	@Test
	public void testBSBM06Chebotko() throws Exception {
		String testName = "bsbm06";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM06ChebotkoView() throws Exception {
		String testName = "bsbm06";
		this.runChebotkoView(testName);
	}

	@Test
	public void testBSBM06Freddy() throws Exception {
		String testName = "bsbm06";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM06FreddyView() throws Exception {
		String testName = "bsbm06";
		this.runFreddyView(testName);
	}

	@Test
	public void testBSBM07Chebotko() throws Exception {
		String testName = "bsbm07";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM07ChebotkoView() throws Exception {
		String testName = "bsbm07";
		this.runChebotkoView(testName);
	}

	@Test
	public void testBSBM07Freddy() throws Exception {
		String testName = "bsbm07";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM07FreddyView() throws Exception {
		String testName = "bsbm07";
		this.runFreddyView(testName);
	}

	@Test
	public void testBSBM08Chebotko() throws Exception {
		String testName = "bsbm08";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM08ChebotkoView() throws Exception {
		String testName = "bsbm08";
		this.runChebotkoView(testName);
	}

	@Test
	public void testBSBM08Freddy() throws Exception {
		String testName = "bsbm08";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM08FreddyView() throws Exception {
		String testName = "bsbm08";
		this.runFreddyView(testName);
	}

	@Test
	public void testBSBM10Chebotko() throws Exception {
		String testName = "bsbm10";
		this.runChebotko(testName);
	}	

	@Test
	public void testBSBM10ChebotkoView() throws Exception {
		String testName = "bsbm10";
		this.runChebotkoView(testName);
	}	

	@Test
	public void testBSBM10Freddy() throws Exception {
		String testName = "bsbm10";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM10FreddyView() throws Exception {
		String testName = "bsbm10";
		this.runFreddyView(testName);
	}


	@Test
	public void testBSBM11Chebotko() throws Exception {
		String testName = "bsbm11";
		this.runChebotko(testName);
	}
	
	@Test
	public void testBSBM11Freddy() throws Exception {
		String testName = "bsbm11";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM12Freddy() throws Exception {
		String testName = "bsbm12";
		this.runFreddy(testName);
	}
}
