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
import es.upm.fi.dia.oeg.obdi.core.ScriptRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test.R2RMLRunnerFreddy;

public class BSBM250K_MYSQL {
	private static Logger logger = Logger.getLogger(BSBM250K_MYSQL.class);
	
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-mappings/r2rml-bsbm-mysql-250k/";
	private String mappingDocumentFile = configurationDirectory + "bsbm.ttl";
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
//	private R2RMLQueryTranslator getQueryTranslator(String testName, 
//			boolean optimizeTripleBlock, boolean subqueryAsView) 
//					throws Exception {
//		
//		String configurationFile = testName + ".r2rml.properties";
//		
//		R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
////		R2RMLElementDataTranslateVisitor dataTranslator = 
////				new R2RMLElementDataTranslateVisitor(configurationDirectory, configurationFile); 
////		md.accept(dataTranslator);
//		R2RMLElementUnfoldVisitor unfolder = new R2RMLElementUnfoldVisitor(
//				configurationDirectory, configurationFile);
//		String queryFilePath = configurationDirectory + testName + ".sparql"; 
//		R2RMLQueryTranslator queryTranslator = new R2RMLQueryTranslator(md, unfolder);
//		queryTranslator.setIgnoreRDFTypeStatement(true);
//		queryTranslator.setOptimizeTripleBlock(optimizeTripleBlock);
//		queryTranslator.setSubqueryAsView(subqueryAsView);
//		queryTranslator.setQueryFilePath(queryFilePath);
//		return queryTranslator;
//	}
	
	public void runSQL(String testName) {
		logger.info("------ Running " + testName + " SQL ------");
		String configurationFile = testName + ".r2rml.properties";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			String queryFilePath = configurationDirectory + testName + "mysql.sql";
			FileInputStream fis = new FileInputStream(queryFilePath);
			ZqlParser zqlParser = new ZqlParser(fis);
			ZStatement statement = zqlParser.readStatement();
			
			String query = statement.toString();
			
			logger.info("query = \n" + query + "\n");
			Connection conn = runner.getConnection();
//			ResultSet rs = DBUtility.executeQuery(conn, query.toString());
//			int noOfRows = DBUtility.getRowCount(rs);
//			logger.info("noOfRows = " + noOfRows);

			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			
			
			logger.info("------" + testName + " SQL DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	public void runSQL2(String testName) {
		logger.info("------ Running " + testName + " SQL ------");
		String configurationFile = testName + ".r2rml.properties";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			String queryFilePath = configurationDirectory + testName + "mysql.sql";
			FileInputStream fis = new FileInputStream(queryFilePath);
			ZqlParser zqlParser = new ZqlParser(fis);
			ZStatement statement = zqlParser.readStatement();
			
			String query = statement.toString();
			
			logger.info("query = \n" + query + "\n");
			Connection conn = runner.getConnection();
			ScriptRunner scriprunner = 
					new ScriptRunner(conn, false, true);
			scriprunner.runScript(new BufferedReader(new FileReader(queryFilePath)));

					
//			ResultSet rs = DBUtility.executeQuery(conn, query.toString());
//			int noOfRows = DBUtility.getRowCount(rs);
//			logger.info("noOfRows = " + noOfRows);

			
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			
			
			logger.info("------" + testName + " SQL DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	public void runSQL3(String testName, String query) {
		logger.info("------ Running " + testName + " SQL ------");
		String configurationFile = testName + ".r2rml.properties";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			
			logger.info("query = \n" + query + "\n");
			Connection conn = runner.getConnection();
			
//			ResultSet rs = DBUtility.executeQuery(conn, query.toString());
//			int noOfRows = DBUtility.getRowCount(rs);
//			logger.info("noOfRows = " + noOfRows);

			
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			
			
			logger.info("------" + testName + " SQL DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
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
			
//			ResultSet rs = DBUtility.executeQuery(conn, query.toString());
//			int noOfRows = DBUtility.getRowCount(rs);
//			logger.info("noOfRows = " + noOfRows);

			
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
			//queryTranslator.setOptimizeTripleBlock(false);
			queryTranslator.setQueryFilePath(queryFilePath);
			

			
//			boolean optimizeTripleBlock = false;
//			boolean subqueryAsView = true;
//			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName, optimizeTripleBlock, subqueryAsView);
//			
			SQLQuery query = queryTranslator.translateFromPropertyFile();
			logger.info("query = \n" + query + "\n");
			Connection conn = runner.getConnection();
			
//			ResultSet rs = DBUtility.executeQuery(conn, query.toString());
//			int noOfRows = DBUtility.getRowCount(rs);
//			logger.info("noOfRows = " + noOfRows);

			
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
//			SQLQuery query = queryTranslator.translateFromPropertyFile();
//			logger.debug("query = \n" + query + "\n");
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
			queryTranslator.setQueryFilePath(queryFilePath);
			
			IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
			queryTranslationOptimizer.setSelfJoinElimination(true);
			queryTranslationOptimizer.setUnionQueryReduction(true);
			queryTranslationOptimizer.setSubQueryAsView(true);
			queryTranslator.setOptimizer(queryTranslationOptimizer);

//			boolean optimizeTripleBlock = true;
//			boolean subqueryAsView = true;
//			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName, optimizeTripleBlock, subqueryAsView);
//			
			SQLQuery query = queryTranslator.translateFromPropertyFile();
			logger.info("query = \n" + query + "\n");
			Connection conn = runner.getConnection();

//			ResultSet rs = DBUtility.executeQuery(conn, query.toString());
//			int noOfRows = DBUtility.getRowCount(rs);
//			logger.info("noOfRows = " + noOfRows);

			
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
	public void testBSBM01SQL() throws Exception {
		String testName = "bsbm01";
		this.runSQL(testName);
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
		String testName = "bsbm01";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM01FreddyView() throws Exception {
		String testName = "bsbm01";
		this.runFreddyView(testName);
	}

	@Test
	public void testBSBM02SQL() throws Exception {
		String testName = "bsbm02";
		this.runSQL(testName);
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
		String testName = "bsbm02";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM02FreddyView() throws Exception {
		String testName = "bsbm02";
		this.runFreddyView(testName);
	}



	@Test
	public void testBSBM03SQL() throws Exception {
		String testName = "bsbm03";
		this.runSQL(testName);
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
	public void testBSBM04SQL() throws Exception {
		String testName = "bsbm04";
		this.runSQL(testName);
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
	public void testBSBM05SQL() throws Exception {
		String testName = "bsbm05";
		String query = "SELECT DISTINCT p.nr, p.label " + "\n"
				+ "FROM product p, product po, "  + "\n"
				+ " (SELECT DISTINCT pfp1.product FROM productfeatureproduct pfp1, "  + "\n"
				+ " (SELECT productFeature FROM productfeatureproduct WHERE product=2) pfp2 WHERE pfp2.productFeature=pfp1.productFeature) pfp "  + "\n"
				+ " WHERE p.nr=pfp.product AND po.nr=2 AND p.nr!=po.nr "  + "\n"
				+ " AND p.propertyNum1<(po.propertyNum1+120) AND p.propertyNum1>(po.propertyNum1-120) "  + "\n"
				+ " AND p.propertyNum2<(po.propertyNum2+170) AND p.propertyNum2>(po.propertyNum2-170) "  + "\n"
				;
		this.runSQL3(testName, query);
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
	public void testBSBM06SQL() throws Exception {
		String testName = "bsbm06";
		this.runSQL(testName);
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
	public void testBSBM07SQL() throws Exception {
		String testName = "bsbm05";
		String query = "SELECT * " + "\n"
				+ "FROM (select label from product where nr=2) p LEFT JOIN "  + "\n"
				+ " ((select o.nr as onr, o.price, v.nr as vnr, v.label from offer o, vendor v where 2=o.product AND "  + "\n"
				+ " o.vendor=v.nr AND v.country='GB' AND o.validTo>'2008-07-01') ov right join "  + "\n"
				+ " (select r.nr as rnr, r.title, pn.nr as pnnr, pn.name, r.rating1, r.rating2 from review r, person pn where r.product=2 AND "  + "\n"
				+ " r.person=pn.nr) rpn on (1=1)) on (1=1) "  + "\n"
				;
		this.runSQL3(testName, query);
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
	public void testBSBM08SQL() throws Exception {
		String testName = "bsbm08";
		this.runSQL(testName);
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
	public void testBSBM10SQL() throws Exception {
		String testName = "bsbm10";
		this.runSQL(testName);
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
