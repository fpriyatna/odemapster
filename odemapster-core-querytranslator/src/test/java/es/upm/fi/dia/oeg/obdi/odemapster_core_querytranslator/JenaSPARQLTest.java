package es.upm.fi.dia.oeg.obdi.odemapster_core_querytranslator;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterPlacement;

import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryRewritter;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public class JenaSPARQLTest {
	private static Logger logger = Logger.getLogger(JenaSPARQLTest.class);
	
	@Test
	public void testReadFromFile() throws Exception {
		//String queryFilePath = "C:/Users/Freddy/Dropbox/Documents/oeg/pig-script/bsbm04.sparql";
		String queryFilePath = "C:/Users/Freddy/Dropbox/Documents/oeg/odemapster2/mappings/r2rml-mappings/r2rml-bsbm-mysql-250k/bsbm01.sparql";
		logger.info("Query file : " + queryFilePath);		
		
		Query sparqlQuery = QueryFactory.read(queryFilePath);
		logger.info("sparqlQuery = \n" + sparqlQuery);
		Op opSparqlQuery = Algebra.compile(sparqlQuery) ;
		logger.info("opSparqlQuery = \n" + opSparqlQuery);
		
		
		Op opSparqlQuery2 = new QueryRewritter().rewrite(opSparqlQuery);
		logger.info("opSparqlQuery2 = \n" + opSparqlQuery2);
		
		assert(true);
	}
}
