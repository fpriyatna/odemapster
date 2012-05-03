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
	private String mappingDirectory = ODEMapsterTest.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml-mappings/sameas/";
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	private void runBatch(String configurationDirectory, String configurationFile, String mappingDocumentFile, String testName) {
		try {
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			R2RMLElementUnfoldVisitor unfolder = new R2RMLElementUnfoldVisitor(
					configurationDirectory, configurationFile);
			R2RMLElementVisitor dataTranslateVisitor = new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile, unfolder); 
			md.accept(dataTranslateVisitor);
			
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			//e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void testSameAsBatch() throws Exception {
		String testName = "sameas";
		String configurationDirectory = mappingDirectory + "r2rml-mappings/sameas/";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + "sameas2.ttl";
		this.runBatch(configurationDirectory, configurationFile, mappingDocumentFile, testName);
	}
}
