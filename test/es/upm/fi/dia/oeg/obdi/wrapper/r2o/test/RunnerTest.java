package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

//package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPostProcessor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;


public class RunnerTest extends XMLTestCase {
	private static Logger logger = Logger.getLogger(RunnerTest.class);
	private static String MAPPING_DIRECTORY = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
//	private static String MAPPING_DIRECTORY = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
	
	public static void main(String args[]) throws Exception {
		RunnerTest.testSole01();
	}

	public static void testSole04() throws Exception {
		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole04/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	
	public static void testSole03() throws Exception {
		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testSole02() throws Exception {
		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole02/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole02/";
		String r2oConfigurationFile = "upmlod.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testSole01() throws Exception {
		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole01/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole01/";
		String r2oConfigurationFile = "upmlod.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	public static void testR2RMLTC0008() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/office/";
		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/R2RMLTC0008/";
		String r2oConfigurationFile = "R2RMLTC0008.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	public static void testOffice() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/office/";
		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/office/";
		String r2oConfigurationFile = "office.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	@Test
	public static void testHospital() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/hospital/";
		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/hospital/";
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String r2oConfigurationFile = "dot.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	@Test
	public static void testSPARQL2Mapping() throws Exception {
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String r2oConfigurationFile = "bsbm.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	@Test
	public static void testUniversidades() throws Exception {
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String r2oConfigurationFile = "inv.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/universidades/");
	}
	
	@Test
	public static void testBSBMQuery10() throws Exception {
		String r2oConfigurationFile = "query10.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}

	
	@Test
	public static void testBSBMQuery08() throws Exception {
		String r2oConfigurationFile = "query08.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}

	@Test
	public static void testBSBMQuery07() throws Exception {
		String r2oConfigurationFile = "query07.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	@Test
	public static void testBSBMQuery00() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
//		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "query00.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testBSBMQuery04() throws Exception {
		String r2oConfigurationFile = "query04.r2o.properties";
//		String r2oConfigurationFile = "query02(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	public static void testBSBMQuery03() throws Exception {
		String r2oConfigurationFile = "query03.r2o.properties";
//		String r2oConfigurationFile = "query02(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	public static void testBSBMQuery02() throws Exception {
//		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "query02.r2o.properties";
//		String r2oConfigurationFile = "query02(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	@Test	
	public static void testBSBMQuery01() throws Exception {
		String r2oConfigurationFile = "query01.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	public static void testBSBMReview2() throws Exception {
		//String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "review2(monetdb).r2o.properties";
//		String r2oConfigurationFile = "review2.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testBSBMOffer() throws Exception {
		//String dir = "/home/fpriyatna/Dropbox/bsbm/mapping-r2o/";
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
//		String r2oConfigurationFile = "offer(monetdb).r2o.properties";
		String r2oConfigurationFile = "offer.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testPSSA2() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/pssa2/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/bsbm/mapping-r2o/";
		String r2oConfigurationFile = "pssa.r2o.properties";
		//String r2oConfigurationFile = "product(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	


	
	public static void testVOC() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/voc/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = "voc.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase07() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/temp/mappings/FAO/testcase07/good/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase56() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase56/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase55() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase55/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}


	
	public static void testBSBMReview() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/bsbm/mapping-r2o/";
		String r2oConfigurationFile = dir + "review.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase54() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase54/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	


	


	public static void testTestcase53() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		String dir = "/home/fpriyatna/Dropbox/oeg/odemapster/testcases/testcase53/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void testTestcase52() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		//String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase51/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase51() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/oeg/odemapster/testcases/testcase51/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase08() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase08/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void testTestcase34() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase34/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void testTestcase35() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase35/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	/*
	private void testTestcase37() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase37/";
		String r2oConfigurationFile = dir + "r2o.properties";
		this.testProcess(r2oConfigurationFile);
	}
	*/

	public static void testTestcase38() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase38/";
		String r2oFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oFile, dir);
	}

	public static void testTestcase39() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/";
		String r2oFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oFile, dir);
	}

	public static void testTestcase40() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase40/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase41() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase41/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void test42NomgeoIndividuals() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}
	
	public static void test42NomgeoIndividualsAttributes() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase42/odemapster2/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividualsAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void test42CanalIndividualsAttributes() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "CanalIndividualsAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void test42CorrienteFluvialIndividualsAttributes() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "CorrienteFluvialAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void test42Nomgeo3Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo3Individuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void test42Nomgeo5Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo5Individuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void test42Nomgeo10Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo10Individuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void test43Playa() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase43/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}

	public static void test44Playa() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase44/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}

	public static void testTestcase45() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase45/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void testTestcase46() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase46/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	public static void testTestcase47() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase47/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void testTestcase50() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase50/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	
	@Test
	private static void testProcess(String r2oConfigurationFile, String mappingDirectory) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		logger.info("==========================Starting==========================");
		String absoluteR2OConfigurationFile = r2oConfigurationFile;
		if(absoluteR2OConfigurationFile != null) {
			absoluteR2OConfigurationFile = mappingDirectory + r2oConfigurationFile;
		}
		try {
			long startMemory = Runtime.getRuntime().freeMemory();
			R2ORunner runner = new R2ORunner();
			runner.run(mappingDirectory, r2oConfigurationFile);
			long endMemory = Runtime.getRuntime().freeMemory();
			long memoryUsage = (startMemory - endMemory) / 1024;
			logger.info("Memory usage was "+(memoryUsage)+" KB.\n\n");
		} catch(SQLException e) {
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile;
			logger.error(errorMessage);
			throw e;
		} catch(RelationMappingUnfolderException e) {
			e.printStackTrace();
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile;
			logger.error(errorMessage);
			throw e;
		} catch(Exception e) {
			//e.printStackTrace();
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile; 
			logger.error(errorMessage);
			logger.error("Error message = " + e.getMessage());
			throw e;
		}
		assertTrue(true);
	}
}