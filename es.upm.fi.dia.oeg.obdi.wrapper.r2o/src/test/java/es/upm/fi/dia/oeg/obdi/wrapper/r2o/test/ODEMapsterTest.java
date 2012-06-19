package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;
//package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractDataTranslator;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;


public class ODEMapsterTest {
	private static Logger logger = Logger.getLogger(ODEMapsterTest.class);
	private static AbstractDataTranslator dataTranslator;
	
public static void setDataTranslator(AbstractDataTranslator dataTranslator) {
		ODEMapsterTest.dataTranslator = dataTranslator;
	}

	//	private static String MAPPING_DIRECTORY = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
//	private static String MAPPING_DIRECTORY = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
	
	public static void main(String args[]) throws Exception {
		ODEMapsterTest test = new ODEMapsterTest();
		
		//BSBMSQLServerTest bsbmSqlServerTest = new BSBMSQLServerTest();
//		bsbmSqlServerTest.testBSBMQuery01SqlServer();
//		bsbmSqlServerTest.testBSBMQuery01TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery02SqlServer();
//		bsbmSqlServerTest.testBSBMQuery02TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery02ReorderedSqlServer();
//		bsbmSqlServerTest.testBSBMQuery03SqlServer();
//		bsbmSqlServerTest.testBSBMQuery03TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery03ReorderedSqlServer();
//		bsbmSqlServerTest.testBSBMQuery04SqlServer();
//		bsbmSqlServerTest.testBSBMQuery04TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery05SqlServer();
//		bsbmSqlServerTest.testBSBMQuery05TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery05ReorderedSqlServer();
//		bsbmSqlServerTest.testBSBMQuery06SqlServer();
//		bsbmSqlServerTest.testBSBMQuery06TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery07SqlServer();
//		bsbmSqlServerTest.testBSBMQuery07TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery07ReorderedSqlServer();
//		bsbmSqlServerTest.testBSBMQuery08SqlServer();
//		bsbmSqlServerTest.testBSBMQuery08TBSqlServer();
//		bsbmSqlServerTest.testBSBMQuery10SqlServer();
//		bsbmSqlServerTest.testBSBMQuery10TBSqlServer();
		
		
//		ODEMapsterTest.testBSBMQuery01Mysql();
//		ODEMapsterTest.testBSBMQuery01TBMysql();
//		ODEMapsterTest.testBSBMQuery02Mysql();
//		ODEMapsterTest.testBSBMQuery02TBMysql();
//		ODEMapsterTest.testBSBMQuery03Mysql();
//		ODEMapsterTest.testBSBMQuery03TBMysql();
//		ODEMapsterTest.testBSBMQuery04Mysql();
//		ODEMapsterTest.testBSBMQuery04TBMysql();
//		ODEMapsterTest.testBSBMQuery05Mysql();
//		ODEMapsterTest.testBSBMQuery05TBMysql();
//		ODEMapsterTest.testBSBMQuery05ReorderedMysql();
//		ODEMapsterTest.testBSBMQuery06Mysql();
//		ODEMapsterTest.testBSBMQuery06TBMysql();
//		ODEMapsterTest.testBSBMQuery07Mysql();
//		ODEMapsterTest.testBSBMQuery07TBMysql();
//		ODEMapsterTest.testBSBMQuery07ReorderedMysql();
//		ODEMapsterTest.testBSBMQuery08Mysql();
//		ODEMapsterTest.testBSBMQuery08TBMysql();
//		ODEMapsterTest.testBSBMQuery10Mysql();
//		ODEMapsterTest.testBSBMQuery10TBMysql();
		
		
	}



	private static void testMcsuarez01() throws Exception {
		String dir = "C:/Users/Freddy/Dropbox/oeg/odemapster2/mappings/mcsuarez01/";
//		String dir = "/home/fpriyatna/Dropbox/oeg/odemapster2/mappings/mcsuarez01/";
		String r2oConfigurationFile = "mcsuarez01.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}


	public void testEsraa() throws Exception {
		String dir = TestUtility.getMappingDirectoryByOS() + "esraa/";
//		String dir = "/home/fpriyatna/Dropbox/oeg/odemapster2/mappings/mcsuarez01/";
		String r2oConfigurationFile = "esraa.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void test42CanalIndividualsAttributes() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "CanalIndividualsAttributes.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	
	private static void test42CorrienteFluvialIndividualsAttributes() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "CorrienteFluvialAttributes.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void test42Nomgeo10Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo10Individuals.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void test42Nomgeo3Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo3Individuals.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void test42Nomgeo5Individuals() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "Nomgeo5Individuals.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void test42NomgeoIndividuals() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividuals.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}
	
	private static void test42NomgeoIndividualsAttributes() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase42/odemapster2/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase42/odemapster2/";
		String r2oConfigurationFile = dir + "NomgeoIndividualsAttributes.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void test43Playa() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase43/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}

	private static void test44Playa() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase44/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
		//this.runMapsterTest("test42", "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/mapster.cfg", "base-result.rdf");
	}
	
	private static void testBSBMOffer() throws Exception {
		//String dir = "/home/fpriyatna/Dropbox/bsbm/mapping-r2o/";
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
//		String r2oConfigurationFile = "offer(monetdb).r2o.properties";
		String r2oConfigurationFile = "offer.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testBSBMQuery00() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
//		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "query00.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testBSBMQuery01MonetDB() throws Exception {
		String testcaseName = "bsbm01";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testBSBMQuery02MonetDB() throws Exception {
		String testcaseName = "bsbm02";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testBSBMQuery03MonetDB() throws Exception {
		String testcaseName = "bsbm03";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testBSBMQuery04MonetDB() throws Exception {
		String testcaseName = "bsbm04";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testBSBMQuery05MonetDB() throws Exception {
		String testcaseName = "bsbm05";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testBSBMQuery07MonetDB() throws Exception {
		String testcaseName = "bsbm07";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}


	private static void testBSBMQuery08MonetDB() throws Exception {
		String testcaseName = "bsbm08";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testBSBMQuery10MonetDB() throws Exception {
		String testcaseName = "bsbm10";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testBSBMQuery11MonetDB() throws Exception {
		String testcaseName = "bsbm11";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testBSBMQuery11Mysql() throws Exception {
		String testcaseName = "bsbm11";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testBSBMReview() throws Exception {
		String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		//String dir = "D:/Users/fpriyatna/My Dropbox/bsbm/mapping-r2o/";
		String r2oConfigurationFile = dir + "review.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testBSBMReview2() throws Exception {
		//String dir = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		String dir = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
		String r2oConfigurationFile = "review2(monetdb).r2o.properties";
//		String r2oConfigurationFile = "review2.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testCbobed01() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "cbobed01/";
		String r2oConfigurationFile = "cbobed01.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testCristina02() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "cristina02/";
		String r2oConfigurationFile = "bsbm.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testHospital() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "hospital/";
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String r2oConfigurationFile = "dot(email).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testmn_relationship() throws Exception {
//		String mappingDirectory = "C:/Users/Freddy/Dropbox/oeg/odemapster/odemapster2/testcases/R2RMLTC0008/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "odemapster_m-n_relationship_example/";
//		String r2oConfigurationFile = "R2RMLTC0008(mysql).r2o.properties";
		String r2oConfigurationFile = "R2RMLTC0008(sqlserver).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testOCorcho01() throws Exception {
		String testcaseName = "ocorcho01";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testOffice() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/office/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "office/";
		String r2oConfigurationFile = "office.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	static void testProcess(String r2oConfigurationFile, String mappingDirectory) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		logger.info("==========================STARTING==========================");
		String absoluteR2OConfigurationFile = r2oConfigurationFile;
		if(absoluteR2OConfigurationFile != null) {
			absoluteR2OConfigurationFile = mappingDirectory + r2oConfigurationFile;
		}
		try {
			long startMemory = Runtime.getRuntime().freeMemory();
			if(ODEMapsterTest.dataTranslator == null) {
				ODEMapsterTest.dataTranslator = new R2ODataTranslator();
			}
			//R2ODataTranslator postProcessor = new R2ODefaultDataTranslator();
			//R2ODataTranslator postProcessor = new R2OFreddyPostProcessor();
			R2ORunner runner = new R2ORunner(ODEMapsterTest.dataTranslator);
			
			
			String status = runner.run(mappingDirectory, r2oConfigurationFile);
			long endMemory = Runtime.getRuntime().freeMemory();
			long memoryUsage = (startMemory - endMemory) / 1024;
			logger.debug("Memory usage was "+(memoryUsage)+" KB.\n\n");
			assertTrue(status, true);
		} catch(SQLException e) {
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile;
			logger.error(errorMessage);
			assertTrue(e.getMessage(), false);
			throw e;
		} catch(RelationMappingUnfolderException e) {
			e.printStackTrace();
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile;
			logger.error(errorMessage);
			assertTrue(false);
			throw e;
		} catch(Exception e) {
			e.printStackTrace();
			String errorMessage = "Error processing mapping file : " + absoluteR2OConfigurationFile; 
			logger.error(errorMessage);
			logger.error("Error message = " + e.getMessage());
			assertTrue(false);
			throw e;
		}
		
	}
	
	private static void testPSSA01Mysql() throws Exception {
		String testcaseName = "pssa01";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testPSSA02Mysql() throws Exception {
		String testcaseName = "pssa02";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testPSSA03Mysql() throws Exception {
		String testcaseName = "pssa03";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testPSSA05Mysql() throws Exception {
		String testcaseName = "pssa05";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testPSSA06Mysql() throws Exception {
		String testcaseName = "pssa06";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testR2RMLTC0008() throws Exception {
//		String mappingDirectory = "C:/Users/Freddy/Dropbox/oeg/odemapster/odemapster2/testcases/R2RMLTC0008/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "R2RMLTC0008/";
		String r2oConfigurationFile = "R2RMLTC0008(mysql).r2o.properties";
//		String r2oConfigurationFile = "R2RMLTC0008(sqlserver).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testRoberto01() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "roberto01/";
		String r2oConfigurationFile = "TC0009(sqlserver).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testRoberto02() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "roberto02/";
		String r2oConfigurationFile = "TC0009(sqlserver).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testSole01() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole01/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole01/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "sole01/";
		String r2oConfigurationFile = "upmlod.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testSole02() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole02/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole02/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "sole02/";
		String r2oConfigurationFile = "upmlod.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testSole03() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "sole03/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testSole04() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole04/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole03/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "sole04/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testSole05() throws Exception {
//		String mappingDirectory = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
//		String mappingDirectory = "/home/fpriyatna/Dropbox/oeg/odemapster/odemapster2/testcases/sole05/";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "sole05/";
		String r2oConfigurationFile = "upmlod_2.26.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testSPARQL2Mapping() throws Exception {
		String mappingDirectory = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String r2oConfigurationFile = "bsbm.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testSparql2SQL() throws Exception {
		String testcaseName = "TCSparql2SQL";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	
	private static void testStudentSport() throws Exception {
		String testcaseName = "studentsport";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + ".r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0000() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0000/";
		String r2oConfigurationFile = "TC0000(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0001() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0001/";
		String r2oConfigurationFile = "TC0001(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	

	private static void testTC0002() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0002/";
		String r2oConfigurationFile = "TC0002(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0003() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0003/";
		String r2oConfigurationFile = "TC0003(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0004() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0004/";
		String r2oConfigurationFile = "TC0004(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0005() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0005/";
		String r2oConfigurationFile = "TC0005(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testTC0005AppliesIfMySql() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0005AppliesIf/";
		String r2oConfigurationFile = "TC0005(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0005AppliesIfSqlServer() throws Exception {
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "TC0005AppliesIf/";
		String r2oConfigurationFile = "TC0005(sqlserver).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	

	private static void testTC0006() throws Exception {
		String testcaseName = "TC0006";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testTC0007() throws Exception {
		String testcaseName = "TC0007";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0008() throws Exception {
		String testcaseName = "TC0008";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testTC0009() throws Exception {
		String testcaseName = "TC0009";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(mysql).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testTC0009SqlServer() throws Exception {
		String testcaseName = "TC0009";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + testcaseName + "/";
		String r2oConfigurationFile = testcaseName + "(sqlserver).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}

	private static void testTestcase07() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/temp/mappings/FAO/testcase07/good/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	/*
	private void testTestcase37() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase37/";
		String r2oConfigurationFile = dir + "r2o.properties";
		this.testProcess(r2oConfigurationFile);
	}
	*/

	private static void testTestcase08() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase08/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase34() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase34/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase35() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase35/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase38() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase38/";
		String r2oFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oFile, dir);
	}
	
	private static void testTestcase39() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/";
		String r2oFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oFile, dir);
	}
	
	private static void testTestcase40() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase40/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase41() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase41/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase45() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase45/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase46() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase46/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase47() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase47/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase50() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase50/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase51() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase41/";
		String dir = "/home/fpriyatna/Dropbox/oeg/odemapster/testcases/testcase51/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase52() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		//String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase51/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase53() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		String dir = "/home/fpriyatna/Dropbox/oeg/odemapster/testcases/testcase53/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase54() throws Exception {
		//String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase52/";
		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase54/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	private static void testTestcase55() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase55/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	private static void testTestcase56() throws Exception {
		String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/testcase56/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = dir + "r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

	
	private static void testUniversidades() throws Exception {
		//String r2oConfigurationFile = "universidades.r2o.properties";
		String mappingDirectory = TestUtility.getMappingDirectoryByOS() + "universidades/";
		String r2oConfigurationFile = "inv.r2o.properties";
//		String r2oConfigurationFile = "query01(monetdb).r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, mappingDirectory);
	}
	
	private static void testVOC() throws Exception {
		String dir = "C:/Users/fpriyatna/My Dropbox/oeg/odemapster/testcases/voc/";
//		String dir = "/Users/freddy_priyatna/Dropbox/oeg/odemapster/testcases/testcase55/";
		String r2oConfigurationFile = "voc.r2o.properties";
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
}