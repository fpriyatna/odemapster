package test.r2rml;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator;

import test.r2o.ODEMapsterTest;

public class R2RMLBSBMJuan {
	private static Logger logger = Logger.getLogger(R2RMLBSBMJuan.class);
	
	private String mappingDirectory = ODEMapsterTest.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml/r2rml-bsbm-juan/";
	private String mappingDocumentFile = configurationDirectory + "bsbm.ttl";
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	private R2RMLQueryTranslator getQueryTranslator(String testName) throws Exception {
		logger.info("------ Running " + testName + " ------");
		String configurationFile = testName + ".r2rml.properties";
		
		R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
//		R2RMLElementDataTranslateVisitor dataTranslator = 
//				new R2RMLElementDataTranslateVisitor(configurationDirectory, configurationFile); 
//		md.accept(dataTranslator);
		R2RMLElementUnfoldVisitor unfolder = new R2RMLElementUnfoldVisitor(
				configurationDirectory, configurationFile);
		String queryFilePath = configurationDirectory + testName + ".sparql"; 
		R2RMLQueryTranslator queryTranslator = new R2RMLQueryTranslator(md, unfolder);
		queryTranslator.setOptimizeTripleBlock(false);
		queryTranslator.setIgnoreRDFTypeStatement(true);
		return queryTranslator;
	}
	
	public void run(String testName) {
		try {
			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);
			queryTranslator.setOptimizeTripleBlock(false);
			String queryFilePath = configurationDirectory + testName + ".sparql";
			SQLQuery query = queryTranslator.translateFromFile(queryFilePath);
			logger.info("query = \n" + query + "\n");
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	public void runTB(String testName) {
		try {
			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);
			queryTranslator.setOptimizeTripleBlock(true);
			String queryFilePath = configurationDirectory + testName + ".sparql";
			SQLQuery query = queryTranslator.translateFromFile(queryFilePath);
			logger.info("query = \n" + query + "\n");
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}

	public void runReorderedTB(String testName) {
		try {
			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);
			queryTranslator.setOptimizeTripleBlock(true);
			String queryFilePath = configurationDirectory + testName + "(reordered).sparql";
			SQLQuery query = queryTranslator.translateFromFile(queryFilePath);
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
			R2RMLElementUnfoldVisitor unfolder = new R2RMLElementUnfoldVisitor(
					configurationDirectory, configurationFile);
			md.accept(new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile, unfolder));
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
		this.runTB(testName);
	}
	
	@Test
	public void testBSBM02() throws Exception {
		String testName = "bsbm02";
		this.run(testName);
	}	

	@Test
	public void testBSBM02TB() throws Exception {
		String testName = "bsbm02";
		this.runTB(testName);
	}

	@Test
	public void testBSBM02ReorderedTB() throws Exception {
		String testName = "bsbm02";
		this.runReorderedTB(testName);
	}

	@Test
	public void testBSBM03() throws Exception {
		String testName = "bsbm03";
		this.run(testName);
	}	

	@Test
	public void testBSBM03TB() throws Exception {
		String testName = "bsbm03";
		this.runTB(testName);
	}
	
	@Test
	public void testBSBM04() throws Exception {
		String testName = "bsbm04";
		this.run(testName);
	}	
	
	@Test
	public void testBSBM04TB() throws Exception {
		String testName = "bsbm04";
		this.runTB(testName);
	}
	
	@Test
	public void testBSBM05() throws Exception {
		String testName = "bsbm05";
		this.run(testName);
	}

	@Test
	public void testBSBM05TB() throws Exception {
		String testName = "bsbm05";
		this.runTB(testName);
	}
	
	@Test
	public void testBSBM05ReorderedTB() throws Exception {
		String testName = "bsbm05";
		this.runReorderedTB(testName);
	}
	
	@Test
	public void testBSBM06() throws Exception {
		String testName = "bsbm06";
		this.run(testName);
	}

	@Test
	public void testBSBM06TB() throws Exception {
		String testName = "bsbm06";
		this.runTB(testName);
	}

	@Test
	public void testBSBM07() throws Exception {
		String testName = "bsbm07";
		this.run(testName);
	}

	@Test
	public void testBSBM07TB() throws Exception {
		String testName = "bsbm07";
		this.runTB(testName);
	}

	@Test
	public void testBSBM07ReorderedTB() throws Exception {
		String testName = "bsbm07";
		this.runReorderedTB(testName);
	}
	
	@Test
	public void testBSBM08() throws Exception {
		String testName = "bsbm08";
		this.run(testName);
	}

	@Test
	public void testBSBM08TB() throws Exception {
		String testName = "bsbm08";
		this.runTB(testName);
	}

	@Test
	public void testBSBM10() throws Exception {
		String testName = "bsbm10";
		this.run(testName);
	}	

	@Test
	public void testBSBM10TB() throws Exception {
		String testName = "bsbm10";
		this.runTB(testName);
	}
	
	@Test
	public void testBSBM10ReorderedTB() throws Exception {
		String testName = "bsbm10";
		this.runReorderedTB(testName);
	}
	
}
