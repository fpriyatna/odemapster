package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.syntax.Element;

import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryRewritter;

public class JenaTest {
	private static Logger logger = Logger.getLogger(JenaTest.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//process SPARQL file
		String queryFilePath = "C:/Users/Freddy/Dropbox/Documents/oeg/odemapster2/mappings/r2rml-mappings/r2rml-bsbm-mysql-100m/bsbm07.sparql";
		logger.info("Parsing query file : " + queryFilePath);
		Query sparqlQuery = QueryFactory.read(queryFilePath);
		logger.info("sparqlQuery = " + sparqlQuery);
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);
		logger.info("opQueryPattern = " + opQueryPattern);

	}

}
