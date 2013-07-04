package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.sql.IQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;

public class FusionTablesTest {
	private static Logger logger = Logger.getLogger(FusionTablesTest.class);
	
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory 
			+ "r2rml-mappings/r2rml-fusiontable/";
	private String mappingDocumentFile = configurationDirectory + "worldgdp.ttl";
	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	
	private void runFreddy(String testName) {
		logger.info("------ Running " + testName + " Freddy ------");
		String configurationFile = testName + ".r2rml.properties";
		String queryFilePath = configurationDirectory + testName + ".sparql";
		
		try {
			long start = System.currentTimeMillis();
			AbstractRunner runner = new R2RMLRunnerFreddy(configurationDirectory, configurationFile);
			IQueryTranslator queryTranslator = runner.getQueryTranslator();
			IQuery query = queryTranslator.translateFromQueryFile(queryFilePath);
			logger.info("query = \n" + query + "\n");
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			logger.info("------" + testName + " Freddy DONE------");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void testFusionTable() throws Exception {
		String testName = "worldgdp";
		this.runFreddy(testName);
	}	
}
