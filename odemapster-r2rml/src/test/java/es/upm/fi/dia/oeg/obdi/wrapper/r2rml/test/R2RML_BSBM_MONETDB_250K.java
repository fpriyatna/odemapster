package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

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

public class R2RML_BSBM_MONETDB_250K {
	private static Logger logger = Logger.getLogger(R2RML_BSBM_MONETDB_250K.class);
	
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-mappings/mysql-pssa/";
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
		Class queryTranslatorClass = Class.forName("es.pm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator");
		IQueryTranslator queryTranslator = (IQueryTranslator) queryTranslatorClass.newInstance();
		queryTranslator.setMappingDocument(md);
		queryTranslator.setUnfolder(unfolder);

		//queryTranslator.setOptimizeTripleBlock(false);
		queryTranslator.setIgnoreRDFTypeStatement(true);
		return queryTranslator;
	}
	
	public void run(String testName) {
		String configurationFile = testName + ".r2rml.properties";
		
		try {
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			
			IQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);
			//queryTranslator.setOptimizeTripleBlock(false);
			String queryFilePath = configurationDirectory + testName + ".sparql";
			SQLQuery query = queryTranslator.translateFromQueryFile(queryFilePath);
			logger.info("final query = \n" + query + "\n");
			//Connection conn = AbstractRunner.getConfigurationProperties().getConn();
			//ResultSet rs = Utility.executeQuery(conn, query.toString());
			
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
			logger.info("final query = \n" + query + "\n");
			//Connection conn = AbstractRunner.getConfigurationProperties().getConn();
			//ResultSet rs = Utility.executeQuery(conn, query.toString());

			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}


	
	@Test
	public void testBSBMBatch() throws Exception {
		String testName = "bsbm";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";

		try {
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			md.accept((R2RMLElementDataTranslateVisitor) runner.getDataTranslator());
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void testBSBM01() throws Exception {
		String testName = "bsbm01";
		this.run(testName);
	}
	
	@Test
	public void testBSBM01TB() throws Exception {
		String testName = "bsbm01";
		this.runFreddy(testName);
	}
	
	@Test
	public void testBSBM02() throws Exception {
		String testName = "bsbm02";
		this.run(testName);
	}	

	@Test
	public void testBSBM02TB() throws Exception {
		String testName = "bsbm02";
		this.runFreddy(testName);
	}
	

	
	@Test
	public void testBSBM03() throws Exception {
		String testName = "bsbm03";
		this.run(testName);
	}	

	@Test
	public void testBSBM03TB() throws Exception {
		String testName = "bsbm03";
		this.runFreddy(testName);
	}
	
	@Test
	public void testBSBM04() throws Exception {
		String testName = "bsbm04";
		this.run(testName);
	}	
	
	@Test
	public void testBSBM04TB() throws Exception {
		String testName = "bsbm04";
		this.runFreddy(testName);
	}
	
	@Test
	public void testBSBM05() throws Exception {
		String testName = "bsbm05";
		this.run(testName);
	}

	@Test
	public void testBSBM05TB() throws Exception {
		String testName = "bsbm05";
		this.runFreddy(testName);
	}
	

	
	@Test
	public void testBSBM06() throws Exception {
		String testName = "bsbm06";
		this.run(testName);
	}

	@Test
	public void testBSBM06TB() throws Exception {
		String testName = "bsbm06";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM07() throws Exception {
		String testName = "bsbm07";
		this.run(testName);
	}

	@Test
	public void testBSBM07TB() throws Exception {
		String testName = "bsbm07";
		this.runFreddy(testName);
	}
	


	@Test
	public void testBSBM08() throws Exception {
		String testName = "bsbm08";
		this.run(testName);
	}

	@Test
	public void testBSBM08TB() throws Exception {
		String testName = "bsbm08";
		this.runFreddy(testName);
	}



	@Test
	public void testBSBM10() throws Exception {
		String testName = "bsbm10";
		this.run(testName);
	}	

	@Test
	public void testBSBM10TB() throws Exception {
		String testName = "bsbm10";
		this.runFreddy(testName);
	}
	

	
}
