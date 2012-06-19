package es.upm.fi.dia.oeg.obdi.core.test;


import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;

public class SparqlTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String query01URL = "/home/fpriyatna/Dropbox/bsbm/queries/query01.sparql";
		//String query01URL = "C:/Users/fpriyatna/My Dropbox/bsbm/queries/query01.sparql";
		Query query = QueryFactory.read(query01URL) ;
		System.out.println("Query = " + query);
		
		Op op = Algebra.compile(query) ;
		if(op instanceof Op0) { System.out.println("OP0 ");}
		if(op instanceof Op1) { System.out.println("OP1 ");}
		if(op instanceof OpModifier) { System.out.println("OpModifier ");}
		if(op instanceof OpSlice) { System.out.println("OpSlice ");}
		if(op instanceof Op2) { System.out.println("OP2 ");}
		if(op instanceof OpExt) { System.out.println("OPExt ");}
		if(op instanceof OpN) { System.out.println("OPN");}
		if(op instanceof OpProject) { System.out.println("OpProject");}
		
		if(op instanceof OpProject) {
			OpProject opProject = (OpProject) op;
			Op opProjectSubOp = opProject.getSubOp();

			if(opProjectSubOp instanceof OpBGP) {
				OpBGP opbg = (OpBGP) opProjectSubOp;
				BasicPattern bp = opbg.getPattern();
				List<Triple> bpTriples = bp.getList();
				for(Triple tp : bpTriples) {
					System.out.println("tp = " + tp);
					Node subject = tp.getSubject();
					Node predicate = tp.getPredicate();
					
				}
			}
		}

		
		
		
	}

}
