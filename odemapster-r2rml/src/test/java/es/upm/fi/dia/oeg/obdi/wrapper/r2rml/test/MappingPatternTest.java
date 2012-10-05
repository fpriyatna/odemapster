package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;

public class MappingPatternTest {
	private static Logger logger = Logger.getLogger(MappingPatternTest.class);
	
	@Test
	public void testTermBasedRelation() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/reengineering-patterns/PR-NOR-TSTX-02/";
			String configurationFile = "mapping-example.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testTermBasedRelation DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testTermBasedRelation() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testTermBasedRecord() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/reengineering-patterns/";
			String configurationFile = "mappingpattern-termbased-record.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testTermBasedRecord DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testTermBasedRecord() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
	
	@Test
	public void testFlattened() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/reengineering-patterns/";
			String configurationFile = "mappingpattern-flattened.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testFlattened DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testFlattened() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}	
	
	@Test
	public void testPathEnumeration() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/reengineering-patterns/";
			String configurationFile = "mappingpattern-pathenumeration.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testPathEnumeration DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testPathEnumeration() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testAdjancencyList() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/reengineering-patterns/";
			String configurationFile = "mappingpattern-adjacencylist.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testAdjancencyList DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testAdjancencyList() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
	
	@Test
	public void testSnowflake() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/reengineering-patterns/";
			String configurationFile = "mappingpattern-snowflake.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testSnowflake DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testSnowflake() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}	
}
