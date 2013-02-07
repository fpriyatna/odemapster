package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;

public class R2RML_Atlashp {
	private static Logger logger = Logger.getLogger(R2RML_Atlashp.class);
	private String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void run(String configurationDirectory, String configurationFile, String mappingDocumentFile, String testName) {
		try {
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			R2RMLElementVisitor dataTranslateVisitor = new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile); 
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
	public void testAtlashp() throws Exception {
		String testName = "atlashp";
		String configurationDirectory = mappingDirectory + "r2rml-mappings/r2rml-atlashp/";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + "atlashp.ttl";
		this.run(configurationDirectory, configurationFile, mappingDocumentFile, testName);
	}
}
