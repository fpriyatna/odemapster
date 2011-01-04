package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.translator.SPARQL2MappingTranslator;

public class SPARQLToMappingTranslatorTest {
	private static Logger logger = Logger.getLogger(SPARQL2MappingTranslator.class);
//	private static String MAPPING_DIRECTORY = "/home/fpriyatna/Dropbox/bsbm/bsbm-r2o-mapping/";
		private static String MAPPING_DIRECTORY = "C:/Users/fpriyatna/My Dropbox/bsbm/bsbm-r2o-mapping/";
//	private static String QUERY_DIRECTORY = "/home/fpriyatna/Dropbox/bsbm/query2mapping/";
		private static String QUERY_DIRECTORY = "C:/Users/fpriyatna/My Dropbox/bsbm/query2mapping/";

	@Test
	public void testQuery00()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query00.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery00==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
	}

	public void testQuery01()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query01.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery01==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
	}

	public void testQuery02()
	{
		PropertyConfigurator.configure("log4j.properties");
		logger.info("\n\n==========================Starting testQuery02==========================");

		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query02.sparql";

		try {
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);


			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();

		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
	}


	public void testQuery03()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query03.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery03==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
	}

	public void testQuery04()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query04.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery04==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
		logger.info("Finished.");
	}

	public void testQuery05()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query05.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery05==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
		logger.info("Finished.");
	}

	public void testQuery07()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query07.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery07==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
		logger.info("Finished.");
	}

	public void testQuery08()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query08.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery08==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
		logger.info("Finished.");
	}	

	public void testQuery10()
	{
		String mappingURL = MAPPING_DIRECTORY + "query.r2o.xml";
		String queryURL = QUERY_DIRECTORY + "query10.sparql";

		try {
			PropertyConfigurator.configure("log4j.properties");
			logger.info("\n\n==========================Starting testQuery10==========================");
			R2OMappingDocument r2oMappingDocument = 
				(R2OMappingDocument) new R2OParser().parse(mappingURL);
			Query query = QueryFactory.read(queryURL) ;
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(r2oMappingDocument, query);
			translator.processQuery();
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception " + e.getMessage());
		}
		logger.info("Finished.");
	}
}
