package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;

public class BSBMTest {
	private static Logger logger = Logger.getLogger(BSBMTest.class);
	private String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
			+ "bsbm-r2rml-mappings/";

	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testBSBM(String configurationFile) throws Exception {
		try {
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------q01 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void q01() throws Exception {
		String configurationFile = "bsbm01.r2rml.properties";
		this.testBSBM(configurationFile);
	}		
	
	@Test
	public void q02() throws Exception {
		String configurationFile = "bsbm02.r2rml.properties";
		this.testBSBM(configurationFile);
	}	
	
	@Test
	public void q03() throws Exception {
		String configurationFile = "bsbm03.r2rml.properties";
		this.testBSBM(configurationFile);
	}
	
	@Test
	public void q04() throws Exception {
		String configurationFile = "bsbm04.r2rml.properties";
		this.testBSBM(configurationFile);
	}
	
	@Test
	public void q05() throws Exception {
		String configurationFile = "bsbm05.r2rml.properties";
		this.testBSBM(configurationFile);
	}	

	@Test
	public void q06() throws Exception {
		String configurationFile = "bsbm06.r2rml.properties";
		this.testBSBM(configurationFile);
	}
	
	@Test
	public void q07() throws Exception {
		String configurationFile = "bsbm07.r2rml.properties";
		this.testBSBM(configurationFile);
	}
	
	@Test
	public void q08() throws Exception {
		String configurationFile = "bsbm08.r2rml.properties";
		this.testBSBM(configurationFile);
	}	
	
	@Test
	public void q10() throws Exception {
		String configurationFile = "bsbm10.r2rml.properties";
		this.testBSBM(configurationFile);
	}	
	
	@Test
	public void q11() throws Exception {
		String configurationFile = "bsbm11.r2rml.properties";
		this.testBSBM(configurationFile);
	}	
}
