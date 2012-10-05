package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;

public class R2RML_Edna {

	private static Logger logger = Logger.getLogger(R2RML_Edna.class);
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	@Test
	public void testEdna01() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/edna/";
			String configurationFile = "batch01.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testEdna01 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testEdna01() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
	
	@Test
	public void testEdna02() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/edna02/";
			String configurationFile = "batch01.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testEdna02 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testEdna02() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
	
	@Test
	public void testEdna03() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/edna03/";
			String configurationFile = "batch01.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testEdna03 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testEdna02() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
	
	@Test
	public void testEdna04() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/edna04/";
			String configurationFile = "batch01.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testEdna04 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testEdna04() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
}
