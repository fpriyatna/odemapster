package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

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

public class PSSA_MYSQL {
	private static Logger logger = Logger.getLogger(PSSA_MYSQL.class);
	
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-mappings/r2rml-mysql-pssa/";
	private String mappingDocumentFile = configurationDirectory + "pssa.ttl";
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	private IQueryTranslator getQueryTranslator(String testName) throws Exception {
		logger.info("------ Running " + testName + " ------");
		String configurationFile = testName + ".r2rml.properties";
		
		R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
//		R2RMLElementDataTranslateVisitor dataTranslator = 
//				new R2RMLElementDataTranslateVisitor(configurationDirectory, configurationFile); 
//		md.accept(dataTranslator);
		R2RMLElementUnfoldVisitor unfolder = 
				new R2RMLElementUnfoldVisitor();
		String queryFilePath = configurationDirectory + testName + ".sparql"; 
		//R2RMLQueryTranslator queryTranslator = new R2RMLQueryTranslator(md, unfolder);
		Class queryTranslatorClass = Class.forName("es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator");
		IQueryTranslator queryTranslator = (IQueryTranslator) queryTranslatorClass.newInstance();
		queryTranslator.setMappingDocument(md);
		queryTranslator.setUnfolder(unfolder);

		
		queryTranslator.setIgnoreRDFTypeStatement(true);
		return queryTranslator;
	}
	
	public void runChebotko(String testName) {
		String configurationFile = testName + ".r2rml.properties";
		
		try {
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			
			IQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);

			String queryFilePath = configurationDirectory + testName + ".sparql";
			SQLQuery query = queryTranslator.translateFromQueryFile(queryFilePath);
			logger.info("query = \n" + query + "\n");
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	public void runFreddy(String testName) {
		String configurationFile = testName + ".r2rml.properties";
		
		try {
			AbstractRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			String queryFilePath = configurationDirectory + testName + ".sparql";
			SQLQuery query = queryTranslator.translateFromQueryFile(queryFilePath);
			logger.info("query = \n" + query + "\n");
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}


	
	@Test
	public void testPSSA01() throws Exception {
		String testName = "pssa01";
		this.runChebotko(testName);
	}
	
	@Test
	public void testPSSA01TB() throws Exception {
		String testName = "pssa01";
		this.runFreddy(testName);
	}
	
	@Test
	public void testPSSA02() throws Exception {
		String testName = "pssa02";
		this.runChebotko(testName);
	}
	
	@Test
	public void testPSSA02TB() throws Exception {
		String testName = "pssa02";
		this.runFreddy(testName);
	}

	@Test
	public void testPSSA03() throws Exception {
		String testName = "pssa03";
		this.runChebotko(testName);
	}
	
	@Test
	public void testPSSA03TB() throws Exception {
		String testName = "pssa03";
		this.runFreddy(testName);
	}

	@Test
	public void testPSSA05() throws Exception {
		String testName = "pssa05";
		this.runChebotko(testName);
	}

	@Test
	public void testPSSA05TB() throws Exception {
		String testName = "pssa05";
		this.runFreddy(testName);
	}

	@Test
	public void testPSSA06() throws Exception {
		String testName = "pssa06";
		this.runChebotko(testName);
	}
	
	@Test
	public void testPSSA06TB() throws Exception {
		String testName = "pssa06";
		this.runFreddy(testName);
	}
	
}
