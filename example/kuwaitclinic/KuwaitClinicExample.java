package kuwaitclinic;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator;

import test.r2o.ODEMapsterTest;
import test.r2rml.R2RMLBSBM250K;

public class KuwaitClinicExample {
	private static Logger logger = Logger.getLogger(R2RMLBSBM250K.class);
	
	private String configurationDirectory = "example/kuwaitclinic/";
	private String mappingDocumentFile = configurationDirectory + "kuwaitclinic.ttl";
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	private R2RMLQueryTranslator getQueryTranslator(String testName) throws Exception {
		logger.info("------ Running " + testName + " ------");
		
		R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
		R2RMLQueryTranslator queryTranslator = new R2RMLQueryTranslator(md);
		queryTranslator.setOptimizeTripleBlock(false);
		queryTranslator.setIgnoreRDFTypeStatement(true);
		return queryTranslator;
	}
	
	private String translateQuery(String testName, String queryFile) {
		SQLQuery sqlQuery = null;
		try {
			R2RMLQueryTranslator queryTranslator = this.getQueryTranslator(testName);
			queryTranslator.setIgnoreRDFTypeStatement(true);
			queryTranslator.setOptimizeTripleBlock(true);
			String queryFilePath = configurationDirectory + queryFile;
			sqlQuery = queryTranslator.translate(queryFilePath);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
		return sqlQuery.toString();
	}
	
	@Test
	public void testBatchMode() throws Exception {
		String testName = "kuwaitclinic";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		try {
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			md.accept(new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile));
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void testQuery01_CheckReceptionistLogin() throws Exception {
		String testName = "kuwaitclinic";
		String queryFile = "Q01-CheckReceptionistLogin.sparql";
		String sqlQuery = this.translateQuery(testName, queryFile);
		logger.info("sqlQuery = \n" + sqlQuery + "\n");
		logger.info("------" + testName + " DONE------\n\n");
	}
	
	@Test
	public void testQuery02_ListPatients() throws Exception {
		String testName = "kuwaitclinic";
		String queryFile = "Q02-ListPatients.sparql";
		String sqlQuery = this.translateQuery(testName, queryFile);
		logger.info("sqlQuery = \n" + sqlQuery + "\n");
		logger.info("------" + testName + " DONE------\n\n");
	}

	@Test
	public void testQuery03_ListAppointments() throws Exception {
		String testName = "kuwaitclinic";
		String queryFile = "Q03-ListAppointments.sparql";
		String sqlQuery = this.translateQuery(testName, queryFile);
		logger.info("sqlQuery = \n" + sqlQuery + "\n");
		logger.info("------" + testName + " DONE------\n\n");
	}

	@Test
	public void testQuery04_ListWaitingList() throws Exception {
		String testName = "kuwaitclinic";
		String queryFile = "Q04-ListWaitingList.sparql";
		String sqlQuery = this.translateQuery(testName, queryFile);
		logger.info("sqlQuery = \n" + sqlQuery + "\n");
		logger.info("------" + testName + " DONE------\n\n");
	}
	
}
