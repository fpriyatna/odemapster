package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;

public class TestLuis {
	private static Logger logger = Logger.getLogger(TestLuis.class);
	String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
			+ "r2rml-mappings/luis/";
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testbicyMappingsLuis() throws Exception {
		try {
			String configurationFile = "bicyMappingsLuis.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testbicyMappingsLuis DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testbicyMappingsLuis FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testbicyMappings_v2() throws Exception {
		try {
			String configurationFile = "bicyMappings_v2.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testbicyMappings_v2 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testbicyMappings_v2 FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testbicyMappingsEdna() throws Exception {
		try {
			String configurationFile = "bicyMappingsEdna.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testbicyMappingsEdna DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testbicyMappingsEdna FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void test_bicyMappingsSys() throws Exception {
		try {
			String configurationFile = "bicyMappingsSys.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------test_bicyMappingsSys DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------test_bicyMappingsSys FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void test_bicyMappingsZ() throws Exception {
		try {
			String configurationFile = "bicyMappingsZ.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------test_bicyMappingsZ DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------test_bicyMappingsZ FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
}
