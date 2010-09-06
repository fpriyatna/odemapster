package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;

public class SparqlTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String query01URL = "/home/fpriyatna/Dropbox/bsbm/queries/query01.sparql";
		String query01URL = "D:/Users/fpriyatna/My Dropbox/bsbm/queries/query01.sparql";
		Query query = QueryFactory.read(query01URL) ;
		
		Op op = Algebra.compile(query) ;
		Op op2 = Algebra.toQuadForm(op);
		
		System.out.println("op = " + op);
		System.out.println("op2 = " + op2);

		
		
	}

}
