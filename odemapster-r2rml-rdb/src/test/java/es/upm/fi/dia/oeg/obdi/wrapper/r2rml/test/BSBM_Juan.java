package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;

public class BSBM_Juan {
	private static Logger logger = Logger.getLogger(BSBM_Juan.class);
	
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-mappings/r2rml-bsbm-juan/";
	private String mappingDocumentFile = configurationDirectory + "bsbm.ttl";
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	private IQueryTranslator getQueryTranslator(String testName) throws Exception {
		logger.info("------ Running " + testName + " ------");
		String configurationFile = testName + ".r2rml.properties";
		
		AbstractMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
//		R2RMLElementDataTranslateVisitor dataTranslator = 
//				new R2RMLElementDataTranslateVisitor(configurationDirectory, configurationFile); 
//		md.accept(dataTranslator);
		AbstractUnfolder unfolder = new R2RMLElementUnfoldVisitor();
		String queryFilePath = configurationDirectory + testName + ".sparql"; 
		//R2RMLQueryTranslator queryTranslator = new R2RMLQueryTranslator(md, unfolder);
		Class queryTranslatorClass = Class.forName("es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator");
		IQueryTranslator queryTranslator = (IQueryTranslator) queryTranslatorClass.newInstance();
		queryTranslator.setMappingDocument(md);
		queryTranslator.setUnfolder(unfolder);
		
		//queryTranslator.setOptimizeTripleBlock(false);
		queryTranslator.setIgnoreRDFTypeStatement(true);
		
		IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
		queryTranslationOptimizer.setSelfJoinElimination(false);
		queryTranslator.setOptimizer(queryTranslationOptimizer);

		return queryTranslator;
	}
	
	public void runChebotko(String testName) {
		try {
			IQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);
			//queryTranslator.setOptimizeTripleBlock(false);
			
			
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
		try {
			IQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);
			//queryTranslator.setOptimizeTripleBlock(true);
			
			IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
			queryTranslationOptimizer.setSelfJoinElimination(true);
			queryTranslationOptimizer.setUnionQueryReduction(true);
			queryTranslator.setOptimizer(queryTranslationOptimizer);
			
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
	public void testBSBM() throws Exception {
		String testName = "bsbm";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		try {
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
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
	public void testBSBM01Freddy() throws Exception {
		String testName = "bsbm01";
		this.runFreddy(testName);
	}
	
	@Test
	public void testBSBM02Chebotko() throws Exception {
		String testName = "bsbm02";
		this.runChebotko(testName);
	}	

	@Test
	public void testBSBM02Freddy() throws Exception {
		String testName = "bsbm02";
		this.runFreddy(testName);
	}



	@Test
	public void testBSBM03Chebotko() throws Exception {
		String testName = "bsbm03";
		this.runChebotko(testName);
	}	

	@Test
	public void testBSBM03Freddy() throws Exception {
		String testName = "bsbm03";
		this.runFreddy(testName);
	}
	
	@Test
	public void testBSBM04Chebotko() throws Exception {
		String testName = "bsbm04";
		this.runChebotko(testName);
	}	
	
	@Test
	public void testBSBM04Freddy() throws Exception {
		String testName = "bsbm04";
		this.runFreddy(testName);
	}
	
	@Test
	public void testBSBM05Chebotko() throws Exception {
		String testName = "bsbm05";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM05Freddy() throws Exception {
		String testName = "bsbm05";
		this.runFreddy(testName);
	}
	

	
	@Test
	public void testBSBM06Chebotko() throws Exception {
		String testName = "bsbm06";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM06Freddy() throws Exception {
		String testName = "bsbm06";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM07() throws Exception {
		String testName = "bsbm07";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM07Freddy() throws Exception {
		String testName = "bsbm07";
		this.runFreddy(testName);
	}


	
	@Test
	public void testBSBM08Chebotko() throws Exception {
		String testName = "bsbm08";
		this.runChebotko(testName);
	}

	@Test
	public void testBSBM08Freddy() throws Exception {
		String testName = "bsbm08";
		this.runFreddy(testName);
	}

	@Test
	public void testBSBM10Chebotko() throws Exception {
		String testName = "bsbm10";
		this.runChebotko(testName);
	}	

	@Test
	public void testBSBM10Freddy() throws Exception {
		String testName = "bsbm10";
		this.runFreddy(testName);
	}
	

	
}
