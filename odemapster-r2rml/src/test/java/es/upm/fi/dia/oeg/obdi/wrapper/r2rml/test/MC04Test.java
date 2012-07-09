package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;

public class MC04Test {
	private static Logger logger = Logger.getLogger(MC04Test.class);
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void testMc04() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/mcsuarez04/";
			String configurationFile = "upmlod.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testMc04 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testMc04() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

}
