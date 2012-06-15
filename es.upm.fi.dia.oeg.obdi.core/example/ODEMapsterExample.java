

import static org.junit.Assert.*;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;

public class ODEMapsterExample {
	private static Logger logger = Logger.getLogger(ODEMapsterExample.class);
	
	@Test
	public void testODEMapsterBatchMode01() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "batch01.r2o.properties";
		R2ODataTranslator postProcessor = new R2ODataTranslator();
		R2ORunner runner = new R2ORunner();
		runner.setDataTranslator(postProcessor);
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}
	
	@Test
	public void testODEMapsterQueryMode01File() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "query01.r2o.properties";
		R2ODataTranslator postProcessor = new R2ODataTranslator();
		R2ORunner runner = new R2ORunner();
		runner.setDataTranslator(postProcessor);
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public void testODEMapsterQueryMode01String() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "query01string.r2o.properties";
		String sparqlQuery = "";
		sparqlQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		sparqlQuery += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		sparqlQuery += "SELECT ?a ?e ?w \n";
		sparqlQuery += "WHERE { \n";
		sparqlQuery += "?a <http://mappingpedia.linkeddata.es/ontologies/email> ?e . \n";
		sparqlQuery += "OPTIONAL { ?a <http://mappingpedia.linkeddata.es/ontologies/webPage> ?w } \n";
		sparqlQuery += "} \n";
		
		
		R2ORunner runner = new R2ORunner();
		runner.setDataTranslator(new R2ODataTranslator());
		runner.setSparqQuery(sparqlQuery);
		
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}
	
	@Test
	public  void testODEMapsterQueryMode02() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "query02.r2o.properties";
		R2ORunner runner = new R2ORunner();
		runner.setDataTranslator(new R2ODataTranslator());

		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public  void testODEMapsterQueryMode03() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "query03.r2o.properties";
		R2ORunner runner = new R2ORunner();
		runner.setDataTranslator(new R2ODataTranslator());
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public  void testODEMapsterQueryMode04() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "query04.r2o.properties";
		R2ORunner runner = new R2ORunner();
		runner.setDataTranslator(new R2ODataTranslator());
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public  void testODEMapsterQueryMode05() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "query05.r2o.properties";
		R2ORunner runner = new R2ORunner();
		runner.setDataTranslator(new R2ODataTranslator());
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}


}
