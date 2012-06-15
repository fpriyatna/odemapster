package test.r2rml;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import test.r2o.ODEMapsterTest;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;

public class R2RML_SameAs {
	private static Logger logger = Logger.getLogger(R2RMLTC.class);
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	@Test
	public void testSameAsBatch() throws Exception {
		try {
			String configurationDirectory = ODEMapsterTest.getMappingDirectoryByOS() 
					+ "r2rml-mappings/sameas/";
			String configurationFile = "sameas.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			
			logger.info("------testSameAsBatch DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testSameAsBatch() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 

	}
}
