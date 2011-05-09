package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

//package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;


public class RunnerTest extends TestCase {
	private static Logger logger = Logger.getLogger(RunnerTest.class);
//	private static String MAPPING_DIRECTORY = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
//	private static String MAPPING_DIRECTORY = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
	private static String MAPPING_DIRECTORY = "C:/Users/Freddy/Dropbox/oeg/odemapster2/testcases/";
//	private static String MAPPING_DIRECTORY = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/";
	
	
	public static void main(String args[]) throws Exception {
		RunnerTest.testmn_relationship();
	}

	private static void testCristina02() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
		String mappingDirectory = MAPPING_DIRECTORY + "cristina02/";
		String r2oConfigurationFile = "bsbm.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	public static void testSole05() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
		String mappingDirectory = MAPPING_DIRECTORY + "sole05/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	public static void testSole04() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole04/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
		String mappingDirectory = MAPPING_DIRECTORY + "sole04/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	
	private static void testSole03() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
		String mappingDirectory = MAPPING_DIRECTORY + "sole03/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testSole02() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole02/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole02/";
		String mappingDirectory = MAPPING_DIRECTORY + "sole02/";
		String r2oConfigurationFile = "upmlod.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testSole01() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole01/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole01/";
		String mappingDirectory = MAPPING_DIRECTORY + "sole01/";
		String r2oConfigurationFile = "upmlod.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	public static void testR2RMLTC0008() throws Exception {
//		String mappingDirectory = "C:/Users/Freddy/Dropbox/oeg/odemapster/odemapster2/testcases/R2RMLTC0008/";
		String mappingDirectory = MAPPING_DIRECTORY + "R2RMLTC0008/";
		String r2oConfigurationFile = "R2RMLTC0008.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	public static void testmn_relationship() throws Exception {
//		String mappingDirectory = "C:/Users/Freddy/Dropbox/oeg/odemapster/odemapster2/testcases/R2RMLTC0008/";
		String mappingDirectory = MAPPING_DIRECTORY + "odemapster_m-n_relationship_example/";
		String r2oConfigurationFile = "R2RMLTC0008.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	public static void testOffice() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/office/";
		String mappingDirectory = MAPPING_DIRECTORY + "office/";
		String r2oConfigurationFile = "office.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	@Test
	public static void testHospital() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/hospital/";
		String mappingDirectory = MAPPING_DIRECTORY + "hospital/";
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String r2oConfigurationFile = "dot.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	@Test
	private static void testSPARQL2Mapping() throws Exception {
		String mappingDirectory = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String r2oConfigurationFile = "bsbm.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	@Test
	private static void testUniversidades() throws Exception {
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String mappingDirectory = MAPPING_DIRECTORY + "universidades/";
		String r2oConfigurationFile = "inv.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	@Test
	private static void testBSBMQuery10() throws Exception {
		String r2oConfigurationFile = "query10.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}

	
	@Test
	private static void testBSBMQuery08() throws Exception {
		String r2oConfigurationFile = "query08.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}

	@Test
	private static void testBSBMQuery07() throws Exception {
		String r2oConfigurationFile = "query07.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	@Test
	private static void testBSBMQuery00() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
//		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "query00.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testBSBMQuery04() throws Exception {
		String r2oConfigurationFile = "query04.r2o.properties";
//		String r2oConfigurationFile = "query02(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	private static void testBSBMQuery03() throws Exception {
		String r2oConfigurationFile = "query03.r2o.properties";
//		String r2oConfigurationFile = "query02(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	private static void testBSBMQuery02() throws Exception {
//		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "query02.r2o.properties";
//		String r2oConfigurationFile = "query02(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	@Test	
	private static void testBSBMQuery01() throws Exception {
		String r2oConfigurationFile = "query01.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, MAPPING_DIRECTORY);
	}
	
	private static void testBSBMReview2() throws Exception {
		//String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "review2(monetdb).r2o.properties";
//		String r2oConfigurationFile = "review2.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testBSBMOffer() throws Exception {
		//String dir = "/home/fpriyatna/Dropbox/bsbm/mapping-r2o/";
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
//		String r2oConfigurationFile = "offer(monetdb).r2o.properties";
		String r2oConfigurationFile = "offer.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testPSSA2() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/pssa2/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/bsbm/mapping-r2o/";
		String r2oConfigurationFile = "pssa.r2o.properties";
		//String r2oConfigurationFile = "product(monetdb).r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	


	
	private static void testVOC() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/voc/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = "voc.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase07() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/temp/mappings/FAO/testcase07/good/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase56() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase56/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase55() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase55/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}


	
	private static void testBSBMReview() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/bsbm/mapping-r2o/";
		String r2oConfigurationFile = dir + "review.r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase54() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase54/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	


	


	private static void testTestcase53() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		String dir = "/home/fpriyatna/Dropbox/oeg/odemapster/testcases/testcase53/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase52() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		//String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase51/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase51() throws Exception {
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

	private static void testTestcase34() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase34/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase35() throws Exception {
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

	private static void testTestcase38() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase38/";
		String r2oFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oFile, dir);
	}

	private static void testTestcase39() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/";
		String r2oFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oFile, dir);
	}

	private static void testTestcase40() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase40/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase41() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase41/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void test42NomgeoIndividuals() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}
	
	private static void test42NomgeoIndividualsAttributes() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase42/odemapster2/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividualsAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void test42CanalIndividualsAttributes() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "CanalIndividualsAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void test42CorrienteFluvialIndividualsAttributes() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "CorrienteFluvialAttributes.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void test42Nomgeo3Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo3Individuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void test42Nomgeo5Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo5Individuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void test42Nomgeo10Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo10Individuals.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void test43Playa() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase43/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}

	private static void test44Playa() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase44/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}

	private static void testTestcase45() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase45/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase46() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase46/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase47() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase47/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase50() throws Exception {
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
			assertTrue(false);
			throw e;
		} catch(RelationMappingUnfolderException e) {
			e.printStackTrace();
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile;
			logger.error(errorMessage);
			assertTrue(false);
			throw e;
		} catch(Exception e) {
			//e.printStackTrace();
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile; 
			logger.error(errorMessage);
			logger.error("Error message = " + e.getMessage());
			assertTrue(false);
			throw e;
		}
		assertTrue(true);
	}
}