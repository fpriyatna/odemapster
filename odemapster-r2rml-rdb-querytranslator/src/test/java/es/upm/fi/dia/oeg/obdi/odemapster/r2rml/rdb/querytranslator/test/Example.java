package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;

public class Example {
	private static Logger logger = Logger.getLogger(Example.class);
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	private String configurationDirectory = System.getProperty("user.dir") + "/example";
	private String mappingDocumentFile = configurationDirectory + "example.ttl";

	@Test
	public void batch() {
		String configurationFile = "batch.r2rml.properties";
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

	@Test
	public void sparql01() {
		String configurationFile = "query01.r2rml.properties";
		try {
			String[] args = {configurationDirectory, configurationFile};
			R2RMLRunner.main(args);
			logger.info("Query process DONE------\n\n");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("Query process FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void sparql02() {
		String configurationFile = "query02.r2rml.properties";
		try {
			R2RMLRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			runner.run();
			logger.info("Query process DONE------\n\n");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("Query process FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}

	@Test
	public void sparql03() {
		String configurationFile = "query03.r2rml.properties";
		try {
			R2RMLRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			runner.run();
			logger.info("Query process DONE------\n\n");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("Query process FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}	
	
	@Test
	public void sparql04() {
		String configurationFile = "query04.r2rml.properties";
		try {
			R2RMLRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			runner.run();
			logger.info("Query process DONE------\n\n");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("Query process FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}	
	
	@Test
	public void sparql05() {
		String configurationFile = "query05.r2rml.properties";
		try {
			R2RMLRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			runner.run();
			logger.info("Query process DONE------\n\n");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("Query process FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}	
}
