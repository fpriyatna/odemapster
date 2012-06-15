package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;

public class TestCity {

	private static Logger logger = Logger.getLogger(TestCity.class);
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	@Test
	public void testCityBatch() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/city/";
			String configurationFile = "city.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			
			logger.info("------testCityBatch DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testCityBatch() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 

	}
}
