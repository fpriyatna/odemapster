package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;

public class BVilla01Test {

	private static Logger logger = Logger.getLogger(BVilla01Test.class);
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	@Test
	public void testBVilla01() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/bvilla01/";
			String configurationFile = "bvilla01.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			
			logger.info("------testBVilla01 DONE------");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testSameAsBatch() FAILED------");
			assertTrue(e.getMessage(), false);
		} 

	}
}
