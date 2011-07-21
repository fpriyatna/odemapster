package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.d2rq.ModelD2RQ;
import es.upm.fi.dia.oeg.obdi.Utility;

public class D2RQTest {
	private String mappingFile;
	private String queryFile;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		//PropertyConfigurator.configure("log4j.properties");

		org.apache.log4j.Logger.getLogger(
        "de.fuberlin.wiwiss.d2rq").setLevel(
                org.apache.log4j.Level.ALL);
		D2RQTest test = new D2RQTest();
		test.testStudentSport();
		
		ModelD2RQ bsbm_model_mysql = new ModelD2RQ(test.mappingFile);
		Query q = QueryFactory.create(test.queryFile); 
		long start = System.currentTimeMillis();
		ResultSet rs = QueryExecutionFactory.create(q, bsbm_model_mysql).execSelect();
		int rowNumber = rs.getRowNumber();
		
		long end = System.currentTimeMillis();
		System.out.println("SQL execution time was "+(end-start)+" ms.");		
		while (rs.hasNext()) {
		    QuerySolution row = rs.nextSolution();
		    rowNumber = rs.getRowNumber();
		}
		System.out.println("rowNumber =  " + rowNumber);
		System.out.println("done");
		
	}

	public void testStudentSport() throws Exception {
		String mappingDirectory = "C:/Users/Freddy/Dropbox/jose-mora/freddy/mapping-collection/r2o-mappings/studentsport/";
		this.mappingFile = mappingDirectory + "studentsport.n3";
		this.queryFile = Utility.readFileAsString(mappingDirectory + "studentsport.sparql");
	}
	
	public void testPSSA() throws Exception {
		this.mappingFile = "C:/Users/Freddy/Dropbox/oeg/pssa/mapping.n3";
		this.queryFile = Utility.readFileAsString("C:/Users/Freddy/Dropbox/oeg/pssa/SSA SPARQL Queries/query1.rq");
	}
}
