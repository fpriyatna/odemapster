package es.upm.fi.dia.oeg.obdi.odemapster_r2rml_querytranslator;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator;

public class ZaragozaTest {
	private static Logger logger = Logger.getLogger(ZaragozaTest.class);
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-mappings/zaragoza02/";
	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void testZaragozaBatch01() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/zaragoza01/";
			String configurationFile = "zaragoza.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testZaragoza01 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testZaragoza01() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testZaragozaBatch02() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/zaragoza02/";
			String configurationFile = "zaragoza.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testZaragoza02 DONE------");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testZaragoza02() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testZaragozaQuery01Freddy() throws Exception {
		String testName = "zaragoza01";
		//R2RMLRunner.runFreddy(configurationDirectory, testName);

		String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
				+ "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza01.r2rml.properties";
		logger.info("configurationDirectory = " + configurationDirectory);
		logger.info("configurationFile = " + configurationDirectory + configurationFile);
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
		
	}

	@Test
	public void testZaragozaQuery01FreddyView() throws Exception {
		String testName = "zaragoza06";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza06.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.getQueryTranslator().getOptimizer().setSubQueryAsView(true);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}

	@Test
	public void testZaragozaQuery02Freddy() throws Exception {
		String testName = "zaragoza02";
		//R2RMLRunner.runFreddy(configurationDirectory, testName);

		String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
				+ "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza02.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
		
	}

	@Test
	public void testZaragozaQuery02FreddyView() throws Exception {
		String testName = "zaragoza06";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza06.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.getQueryTranslator().getOptimizer().setSubQueryAsView(true);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}
	
	@Test
	public void testZaragozaQuery03Freddy() throws Exception {
		String testName = "zaragoza03";
		//R2RMLRunner.runFreddy(configurationDirectory, testName);

		String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
				+ "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza03.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}

	@Test
	public void testZaragozaQuery03FreddyView() throws Exception {
		String testName = "zaragoza06";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza06.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.getQueryTranslator().getOptimizer().setSubQueryAsView(true);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}

	@Test
	public void testZaragozaQuery04Freddy() throws Exception {
		String testName = "zaragoza04";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza04.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}

	@Test
	public void testZaragozaQuery04FreddyView() throws Exception {
		String testName = "zaragoza04";
		//R2RMLRunner.runFreddy(configurationDirectory, testName);

		String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
				+ "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza04.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.getQueryTranslator().getOptimizer().setSubQueryAsView(true);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}
	
	@Test
	public void testZaragozaQuery05aFreddy() throws Exception {
		String testName = "zaragoza05a";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza05a.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}

	@Test
	public void testZaragozaQuery05aFreddyView() throws Exception {
		String testName = "zaragoza05a";
		String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
				+ "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza05a.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.getQueryTranslator().getOptimizer().setSubQueryAsView(true);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}	
	
	@Test
	public void testZaragozaQuery05bFreddy() throws Exception {
		String testName = "zaragoza05b";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza05b.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}

	@Test
	public void testZaragozaQuery05bFreddyView() throws Exception {
		String testName = "zaragoza05b";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza05b.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.getQueryTranslator().getOptimizer().setSubQueryAsView(true);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}
	
	@Test
	public void testZaragozaQuery06Freddy() throws Exception {
		String testName = "zaragoza06";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza06.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}

	@Test
	public void testZaragozaQuery06FreddyView() throws Exception {
		String testName = "zaragoza06";
		String configurationDirectory = 
				TestUtility.getMappingDirectoryByOS() + "r2rml-mappings/zaragoza02/";
		String configurationFile = "zaragoza05b.r2rml.properties";
		AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
		runner.getQueryTranslator().getOptimizer().setSubQueryAsView(true);
		runner.run();
		logger.info("------ " + testName + " DONE------");
		assertTrue(true);
	}
		
}
