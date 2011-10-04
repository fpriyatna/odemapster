

import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODefaultDataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;

public class ODEMapsterExample extends TestCase {
	private static Logger logger = Logger.getLogger(ODEMapsterExample.class);
	
	
	@Test
	public static void testODEMapsterExample01() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "example01.r2o.properties";
		R2ODataTranslator postProcessor = new R2ODefaultDataTranslator();
		R2ORunner runner = new R2ORunner(postProcessor);
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public static void testODEMapsterExample02() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "example02.r2o.properties";
		R2ODataTranslator postProcessor = new R2ODefaultDataTranslator();
		R2ORunner runner = new R2ORunner(postProcessor);
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public static void testODEMapsterExample03() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "example03.r2o.properties";
		R2ODataTranslator postProcessor = new R2ODefaultDataTranslator();
		R2ORunner runner = new R2ORunner(postProcessor);
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public static void testODEMapsterExample04() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "example04.r2o.properties";
		R2ODataTranslator postProcessor = new R2ODefaultDataTranslator();
		R2ORunner runner = new R2ORunner(postProcessor);
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}

	@Test
	public static void testODEMapsterExample05() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		String mappingDirectory = "example/";
		String r2oConfigurationFile = "example05.r2o.properties";
		R2ODataTranslator postProcessor = new R2ODefaultDataTranslator();
		R2ORunner runner = new R2ORunner(postProcessor);
		String status = runner.run(mappingDirectory, r2oConfigurationFile);
		assertTrue(status, true);
	}


}
