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

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//PropertyConfigurator.configure("log4j.properties");

		org.apache.log4j.Logger.getLogger(
        "de.fuberlin.wiwiss.d2rq").setLevel(
                org.apache.log4j.Level.ALL);
		
		String d2rMapping = "C:/Users/Freddy/Dropbox/oeg/pssa/mapping.n3";
//		String d2rMapping = "C:/software/d2r-server-0.7/d2r-server-0.7/mapping.n3";
		
//		String query = Utility.readFileAsString("C:/Users/Freddy/Dropbox/jose-mora/freddy/mapping-collection/r2o/bsbm01/bsbm01.sparql");
//		String query = Utility.readFileAsString("C:/Users/Freddy/Dropbox/jose-mora/freddy/mapping-collection/r2o/pssa02/pssa02.sparql");
		//String query = Utility.readFileAsString("C:/Users/Freddy/Dropbox/jose-mora/freddy/mapping-collection/r2o/studentsport/studentsport.sparql");
		String query = Utility.readFileAsString("C:/Users/Freddy/Dropbox/oeg/pssa/SSA SPARQL Queries/query1.rq");
		
		// Set up the ModelD2RQ using a mapping file
//		ModelD2RQ m = new ModelD2RQ("/home/fpriyatna/Downloads/mapping-iswc.n3");
		ModelD2RQ bsbm_model_mysql = new ModelD2RQ(d2rMapping);
		//ModelD2RQ bsbm_model_monetdb = new ModelD2RQ("/home/fpriyatna/Dropbox/bsbm/bsbm-d2r-mapping/d2r-mapping(monetdb).n3");
//		String sparql = Utility.readFileAsString("/home/fpriyatna/Documents/iswc.sparql");
//		String bsbm_query00 = Utility.readFileAsString("C:/Users/fpriyatna/My Dropbox/bsbm/queries1m/query00.sparql");
		
//		String bsbm_query02 = Utility.readFileAsString("/home/fpriyatna/Dropbox/bsbm/queries1m/query02.sparql");
//		String bsbm_query03 = Utility.readFileAsString("/home/fpriyatna/Dropbox/bsbm/queries1m/query03.sparql");
//		String bsbm_query04 = Utility.readFileAsString("/home/fpriyatna/Dropbox/bsbm/query2mapping/query04.sparql");
		
		Query q = QueryFactory.create(query); 
		long start = System.currentTimeMillis();
		ResultSet rs = QueryExecutionFactory.create(q, bsbm_model_mysql).execSelect();
		int rowNumber = rs.getRowNumber();
		
		long end = System.currentTimeMillis();
		System.out.println("SQL execution time was "+(end-start)+" ms.");		
		while (rs.hasNext()) {
		    QuerySolution row = rs.nextSolution();
		    /*
		    System.out.println("paperTitle: " + row.get("paperTitle"));
		    System.out.println("authorName: " + row.get("authorName"));
		    */
		    rowNumber = rs.getRowNumber();
			
		}
		System.out.println("rowNumber =  " + rowNumber);
		System.out.println("done");
		
	}

}
