package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import static org.junit.Assert.*;

import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;

public class MCTest {
	private static Logger logger = Logger.getLogger(MCTest.class);
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void testURI() {
		try {
			String uri = "http://edu.linkeddata.es/investigacion/UPM/resource/Conference/Reunion%20de%20la%20Red%20Tematica%20%26#8220;Biotecnologia%20de%20las%20Interacciones%20Beneficiosas%20entre%20Plantas%20y%20Microorganismos%268221;%20%20";
			Model model = ModelFactory.createDefaultModel();
			Resource resource = model.createResource(uri);
			resource.addLiteral(RDFS.label, "test label");
			FileOutputStream fos = new FileOutputStream("output.rdf.xml");
			model.write(fos, "RDF/XML");
			fos.flush();fos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

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

	@Test
	public void testMc05() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/mcsuarez05/";
			String configurationFile = "upmlod.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testMc05 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testMc05() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
	
	@Test
	public void testMc06() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/mcsuarez06/";
			String configurationFile = "upmlod.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testMc06 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testMc06() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}
	
	@Test
	public void testMC07() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/mcsuarez07/";
			String configurationFile = "upmlod.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testMC07 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------testMC07() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testMC08() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/mcsuarez08/";
			String configurationFile = "upmlod.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testMC08 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testMC08() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

	@Test
	public void testMC09() throws Exception {
		try {
			String configurationDirectory = TestUtility.getMappingDirectoryByOS() 
					+ "r2rml-mappings/mcsuarez09/";
			String configurationFile = "upmlod.r2rml.properties";
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
			logger.info("------testMC09 DONE------\n\n");
			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testMC09() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}

}
