package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;

public class Example {
	private static Logger logger = Logger.getLogger(Example.class);
	private String configurationDirectory = System.getProperty("user.dir") + "/example";
	private String mappingDocumentFile = configurationDirectory + "example.ttl";

	@Test
	public void batch() {
		String configurationFile = "example.r2rml.properties";
		logger.info("configurationFile = " + configurationFile);

		try {
			R2RMLRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("Batch process DONE------\n\n");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("Batch process FAILED------\n\n");
			assertTrue(e.getMessage(), false);

		}
	}

}
