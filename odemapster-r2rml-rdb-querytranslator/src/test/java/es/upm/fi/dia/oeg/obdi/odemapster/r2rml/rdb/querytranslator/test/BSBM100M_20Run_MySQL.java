package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator.R2RMLQueryTranslator;

public class BSBM100M_20Run_MySQL {
	private static Logger logger = Logger.getLogger(BSBM100M_MYSQL.class);

	private static String mappingDirectory = TestUtility.getMappingDirectoryByOS();
	private static String configurationDirectory = mappingDirectory + 
			"r2rml-mappings/r2rml-bsbm-mysql-100m-20run/";
	private static String mappingDocumentFile = configurationDirectory + "bsbm.ttl";

	private static IQueryTranslator queryTranslatorFreddy = null;
	private static IQueryTranslator queryTranslatorChebotko = null;

	private int MODE_WARM = 1;
	private int MODE_COLD = 2;
	private int NO_OF_QUERIES = 20;
	
	static {
		PropertyConfigurator.configure("log4j.properties");

		try {
			queryTranslatorFreddy = R2RMLQueryTranslator.createQueryTranslator(mappingDocumentFile); 
			IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
			queryTranslationOptimizer.setSelfJoinElimination(true);
			queryTranslationOptimizer.setUnionQueryReduction(true);
			queryTranslationOptimizer.setSubQueryElimination(true);
			queryTranslationOptimizer.setSubQueryAsView(false);
			queryTranslatorFreddy.setOptimizer(queryTranslationOptimizer);
			
			queryTranslatorChebotko = R2RMLQueryTranslator.createQueryTranslator(mappingDocumentFile); 
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	private void testBSBM(String testName, String queryFile, int mode, 
			IQueryTranslator queryTranslator, Map<String, String> mapVarFile, String outputFileName) 
					throws Exception {

		
		logger.info("------" + testName + " Freddy STARTED------\n");

		String outputString = "";
		outputString += "SET PROFILING= 1;\n\n";

		
		String queryTemplate = ODEMapsterUtility.readFileAsString(queryFile);
		
		Collection<String> mapVarFileKeyset = mapVarFile.keySet();
		Map<String, List<String>> mapVarLines = new HashMap<String, List<String>>();
		
		for(String varName : mapVarFileKeyset) {
			String fileName = mapVarFile.get(varName);
			List<String> fileLines = ODEMapsterUtility.readFileAsLines(fileName);
			mapVarLines.put(varName, fileLines);
		}
		
		for(int i=0; i<NO_OF_QUERIES; i++) {
			outputString += "-- run " + (i+1) + " --\n";
			if(mode == MODE_COLD) {
				outputString += "RESET QUERY CACHE;\n";
			}
			
			String queryString = queryTemplate;
			Collection<String> mapVarLinesKeyset = mapVarLines.keySet();
			for(String mapVarKey : mapVarLinesKeyset) {
				List<String> lines =  mapVarLines.get(mapVarKey);
				String replacement = lines.get(i);
				queryString = queryString.replaceAll(mapVarKey, replacement);
			}

			long start = System.currentTimeMillis();
			SQLQuery query = queryTranslator.translateFromString(queryString);
			long end = System.currentTimeMillis();
			long duration = end-start;
			logger.info("test execution time was "+(end-start)+" ms.");
			outputString += "-- Query translation time was " + duration + " ms.\n";
			outputString += query + ";" + "\n\n";
			
			if((i+1) % 5 == 0) {
				outputString += "SHOW PROFILES;\n";
			}
		}

		

		
		if(mode == MODE_COLD) {
			outputFileName += "_cold";
		} else if(mode == MODE_WARM) {
			outputFileName += "_warm";
		}
		outputFileName += ".sql";
		FileWriter fstream = new FileWriter(outputFileName);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(outputString);
		//Close the output stream
		out.close();

		logger.info("------" + testName + " Freddy DONE------\n");
	}

	@Test
	public void testBSBM01FreddyWarm() throws Exception {
		this.testBSBM01(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM01FreddyCold() throws Exception {
		this.testBSBM01(MODE_COLD, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM01ChebotkoWarm() throws Exception {
		this.testBSBM01(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM01ChebotkoCold() throws Exception {
		this.testBSBM01(MODE_COLD, queryTranslatorChebotko);
	}
	
	private void testBSBM01(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm01";
		String queryFile = configurationDirectory + "bsbm01_template.sparql";
		String outputFileName = null;
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q01";
		} else {
			outputFileName = "mysql_chebotko_100m_q01";
		}
		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("@ProductType@", configurationDirectory + "q1_producttypeid.txt");
		mapVarFile.put("@x@", configurationDirectory + "q1_propertynum1.txt");
		mapVarFile.put("@ProductFeature1@", configurationDirectory + "q1_productFeatureID1.txt");
		mapVarFile.put("@ProductFeature2@", configurationDirectory + "q1_productFeatureID2.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	private void testBSBM02(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm02";
		String queryFile = configurationDirectory + "bsbm02_template.sparql";
		String outputFileName = "mysql_freddy_100m_q02";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q02";
		} else {
			outputFileName = "mysql_chebotko_100m_q02";
		}
		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%ProductXYZ%", configurationDirectory + "q2_pt.productID.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	

	@Test
	public void testBSBM02FreddyWarm() throws Exception {
		this.testBSBM02(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM02FreddyCold() throws Exception {
		this.testBSBM02(MODE_COLD, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM02ChebotkoWarm() throws Exception {
		this.testBSBM02(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM02ChebotkoCold() throws Exception {
		this.testBSBM02(MODE_COLD, queryTranslatorChebotko);
	}

	
	private void testBSBM03(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm03";
		String queryFile = configurationDirectory + "bsbm03_template.sparql";
		String outputFileName = "mysql_freddy_100m_q03";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q03";
		} else {
			outputFileName = "mysql_chebotko_100m_q03";
		}
		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%ProductType%", configurationDirectory + "q3_productTypeID.txt");
		mapVarFile.put("%x%", configurationDirectory + "q3_propertyNum1.txt");
		mapVarFile.put("%y%", configurationDirectory + "q3_propertyNum3.txt");
		mapVarFile.put("%ProductFeature1%", configurationDirectory + "q3_in.txt");
		mapVarFile.put("%ProductFeature2%", configurationDirectory + "q3_not.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM03FreddyWarm() throws Exception {
		this.testBSBM03(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM03FreddyCold() throws Exception {
		this.testBSBM03(MODE_COLD, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM03ChebotkoWarm() throws Exception {
		this.testBSBM03(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM03ChebotkoCold() throws Exception {
		this.testBSBM03(MODE_COLD, queryTranslatorChebotko);
	}

	private void testBSBM04(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm04";
		String queryFile = configurationDirectory + "bsbm04_template.sparql";
		String outputFileName = "mysql_freddy_100m_q04";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q04";
		} else {
			outputFileName = "mysql_chebotko_100m_q04";
		}		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%ProductType%", configurationDirectory + "q4_productTypeID.txt");
		mapVarFile.put("%ProductFeature1%", configurationDirectory + "q4_productFeatureID.txt");
		mapVarFile.put("%x%", configurationDirectory + "q4_x.txt");
		mapVarFile.put("%ProductFeature2%", configurationDirectory + "q4_productFeature2.txt");
		mapVarFile.put("%y%", configurationDirectory + "q4_y.txt");
		mapVarFile.put("%ProductFeature3%", configurationDirectory + "q4_productFeature3.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM04FreddyWarm() throws Exception {
		this.testBSBM04(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM04FreddyCold() throws Exception {
		this.testBSBM04(MODE_COLD, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM04ChebotkoWarm() throws Exception {
		this.testBSBM04(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM04ChebotkoCold() throws Exception {
		this.testBSBM04(MODE_COLD, queryTranslatorChebotko);
	}
	private void testBSBM05(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm05";
		String queryFile = configurationDirectory + "bsbm05_template.sparql";
		String outputFileName = "mysql_freddy_100m_q05";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q05";
		} else {
			outputFileName = "mysql_chebotko_100m_q05";
		}
		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%ProductXYZ%", configurationDirectory + "q5_productID.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM05FreddyWarm() throws Exception {
		this.testBSBM05(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM05FreddyCold() throws Exception {
		this.testBSBM05(MODE_COLD, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM05ChebotkoWarm() throws Exception {
		this.testBSBM05(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM05ChebotkoCold() throws Exception {
		this.testBSBM05(MODE_COLD, queryTranslatorChebotko);
	}

	private void testBSBM06(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm06";
		String queryFile = configurationDirectory + "bsbm06_template.sparql";
		String outputFileName = "mysql_freddy_100m_q06";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q06";
		} else {
			outputFileName = "mysql_chebotko_100m_q06";
		}
		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%word1%", configurationDirectory + "q6_label.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM06FreddyWarm() throws Exception {
		this.testBSBM06(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM06FreddyCold() throws Exception {
		this.testBSBM06(MODE_COLD, queryTranslatorFreddy);
	}
	
	@Test
	public void testBSBM06ChebotkoWarm() throws Exception {
		this.testBSBM06(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM06ChebotkoCold() throws Exception {
		this.testBSBM06(MODE_COLD, queryTranslatorChebotko);
	}

	private void testBSBM07(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm07";
		String queryFile = configurationDirectory + "bsbm07_template.sparql";
		String outputFileName = "mysql_freddy_100m_q07";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q07";
		} else {
			outputFileName = "mysql_chebotko_100m_q07";
		}		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%ProductXYZ%", configurationDirectory + "q7_productID.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM07ChebotkoWarm() throws Exception {
		this.testBSBM07(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM07ChebotkoCold() throws Exception {
		this.testBSBM07(MODE_COLD, queryTranslatorChebotko);
	}
	
	@Test
	public void testBSBM07FreddyWarm() throws Exception {
		this.testBSBM07(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM07FreddyCold() throws Exception {
		this.testBSBM07(MODE_COLD, queryTranslatorFreddy);
	}

	private void testBSBM08(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm08";
		String queryFile = configurationDirectory + "bsbm08_template.sparql";
		String outputFileName = "mysql_freddy_100m_q06";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q10";
		} else {
			outputFileName = "mysql_chebotko_100m_q10";
		}		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%ProductXYZ%", configurationDirectory + "q8_productID.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM08ChebotkoWarm() throws Exception {
		this.testBSBM08(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM08ChebotkoCold() throws Exception {
		this.testBSBM08(MODE_COLD, queryTranslatorChebotko);
	}
	
	@Test
	public void testBSBM08FreddyWarm() throws Exception {
		this.testBSBM08(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM08FreddyCold() throws Exception {
		this.testBSBM08(MODE_COLD, queryTranslatorFreddy);
	}
	
	private void testBSBM10(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm10";
		String queryFile = configurationDirectory + "bsbm10_template.sparql";
		String outputFileName = "mysql_freddy_100m_q10";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q10";
		} else {
			outputFileName = "mysql_chebotko_100m_q10";
		}
		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%ProductXYZ%", configurationDirectory + "q10_productID.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM10ChebotkoWarm() throws Exception {
		this.testBSBM10(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM10ChebotkoCold() throws Exception {
		this.testBSBM10(MODE_COLD, queryTranslatorChebotko);
	}
	
	@Test
	public void testBSBM10FreddyWarm() throws Exception {
		this.testBSBM10(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM10FreddyCold() throws Exception {
		this.testBSBM10(MODE_COLD, queryTranslatorFreddy);
	}

	private void testBSBM11(int mode, IQueryTranslator queryTranslator) throws Exception {
		String testName = "bsbm11";
		String queryFile = configurationDirectory + "bsbm11_template.sparql";
		String outputFileName = "mysql_freddy_100m_q11";
		if(queryTranslator == queryTranslatorFreddy) {
			outputFileName = "mysql_freddy_100m_q11";
		} else {
			outputFileName = "mysql_chebotko_100m_q11";
		}		
		Map<String, String> mapVarFile = new HashMap<String, String>();
		mapVarFile.put("%OfferXYZ%", configurationDirectory + "q11_offerID.txt");
		this.testBSBM(testName, queryFile, mode, queryTranslator, mapVarFile, outputFileName);
	}
	
	@Test
	public void testBSBM11FreddyWarm() throws Exception {
		this.testBSBM11(MODE_WARM, queryTranslatorFreddy);
	}

	@Test
	public void testBSBM11FreddyCold() throws Exception {
		this.testBSBM11(MODE_COLD, queryTranslatorFreddy);
	}	
	
	@Test
	public void testBSBM11ChebotkoWarm() throws Exception {
		this.testBSBM11(MODE_WARM, queryTranslatorChebotko);
	}

	@Test
	public void testBSBM11ChebotkoCold() throws Exception {
		this.testBSBM11(MODE_COLD, queryTranslatorChebotko);
	}	
}
