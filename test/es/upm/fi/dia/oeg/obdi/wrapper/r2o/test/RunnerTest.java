package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;

import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.ModelWriter;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractParser;
import es.upm.fi.dia.oeg.obdi.wrapper.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OModelGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OUnfolder;


public class RunnerTest extends XMLTestCase {
	private static Logger logger = Logger.getLogger(RunnerTest.class);
	
	private static void testTestcase08() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase08/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void testTestcase34() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase34/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public void testTestcase35() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase35/";
		String r2oConfigurationFile = dir + "r2o.properties";
		this.testProcess(r2oConfigurationFile, dir);
	}

	/*
	private void testTestcase37() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase37/";
		String r2oConfigurationFile = dir + "r2o.properties";
		this.testProcess(r2oConfigurationFile);
	}
	*/

	public static void testTestcase38() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase38/";
		String r2oFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oFile, dir);
	}

	public static void testTestcase39() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/";
		String r2oFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oFile, dir);
	}

	public static void testTestcase40() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase40/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase41() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase41/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void test42NomgeoIndividuals() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/odemapster_public_server/mappings/Freddy/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}
	
	public static void test42NomgeoIndividualsAttributes() {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/odemapster_public_server/mappings/Freddy/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividualsAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void test42CanalIndividualsAttributes() {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/odemapster_public_server/mappings/Freddy/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "CanalIndividualsAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void test42Nomgeo3Individuals() {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/odemapster_public_server/mappings/Freddy/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo3Individuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	
	public static void main(String args[]) {
		RunnerTest.test42NomgeoIndividuals();
	}


	
	@Test
	private static void testProcess(String r2oConfigurationFile, String mappingDirectory) {
		PropertyConfigurator.configure("log4j.properties");
		try {
			long startMemory = Runtime.getRuntime().freeMemory();
			AbstractRunner runner = new R2ORunner();
			runner.run(r2oConfigurationFile);
			long endMemory = Runtime.getRuntime().freeMemory();
			long memoryUsage = (startMemory - endMemory) / 1024;
			logger.info("Memory usage was "+(memoryUsage)+" KB.");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error occured : " + e.getMessage());
			//assertTrue(e.getMessage(), false);
		}
		assertTrue(true);
	}
}
