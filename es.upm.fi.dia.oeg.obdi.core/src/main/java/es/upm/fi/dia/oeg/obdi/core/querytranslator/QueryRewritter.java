package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.algebra.optimize.Rewrite;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import com.hp.hpl.jena.sparql.expr.ExprList;

public class QueryRewritter implements Rewrite {
	private static Logger logger = Logger.getLogger(QueryRewritter.class);

	@Override
	public Op rewrite(Op op) {
		if(op instanceof OpBGP) { //triple or bgp pattern
			OpBGP bgp = (OpBGP) op; 
			BasicPattern basicPattern = bgp.getPattern();
			ReorderTransformation reorderTransformation = new ReorderSubject();
			BasicPattern basicPattern2 = reorderTransformation.reorder(basicPattern);
			OpBGP bgp2 = new OpBGP(basicPattern2);
			return bgp2;
		} else if(op instanceof OpJoin) { // AND pattern
			OpJoin opJoin = (OpJoin) op;
			Op leftChild = opJoin.getLeft();
			Op rightChild = opJoin.getRight();
			Op leftChildRewritten = this.rewrite(leftChild);
			Op rightChildRewritten = this.rewrite(rightChild);
			if(leftChildRewritten instanceof OpBGP && rightChildRewritten instanceof OpBGP) {
				OpBGP leftChildRewrittenBGP = (OpBGP) leftChildRewritten;
				OpBGP rightChildRewrittenBGP = (OpBGP) rightChildRewritten;
				leftChildRewrittenBGP.getPattern().addAll(rightChildRewrittenBGP.getPattern());
				return leftChildRewrittenBGP;
			} else {
				return OpJoin.create(leftChildRewritten, rightChildRewritten);
			}

		} else if(op instanceof OpLeftJoin) { //OPT pattern
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Op leftChild = opLeftJoin.getLeft();
			Op rightChild = opLeftJoin.getRight();
			Op leftChildRewritten = this.rewrite(leftChild);
			Op rightChildRewritten = this.rewrite(rightChild);
			
			if(leftChildRewritten instanceof OpBGP && rightChildRewritten instanceof OpBGP) {
				OpBGP leftChildRewrittenBGP = (OpBGP) leftChildRewritten;
				OpBGP rightChildRewrittenBGP = (OpBGP) rightChildRewritten;
				BasicPattern rightBasicPattern = rightChildRewrittenBGP.getPattern();
				int rightBasicPatternSize = rightBasicPattern.size();
				if(rightBasicPatternSize == 1) {
					logger.debug("Optional pattern with only one triple pattern.");
					
					Triple rightTp = rightChildRewrittenBGP.getPattern().get(0);
					Node rightTpSubject = rightTp.getSubject();
					Node rightTpObject = rightTp.getObject();
					
					Collection<Triple> leftChildTriples = leftChildRewrittenBGP.getPattern().getList();
					Collection<Node> leftChildSubjects = QueryTranslatorUtility.getSubjects(leftChildTriples);
					Collection<Node> leftChildObjects = QueryTranslatorUtility.getObjects(leftChildTriples);
					
					if(leftChildSubjects.contains(rightTpSubject) && !leftChildObjects.contains(rightTpObject)) {
						ExtendedTriple rightEtp = new ExtendedTriple(rightTp.getSubject(), rightTp.getPredicate(), rightTp.getObject());
						rightEtp.setSingleTripleFromTripleBlock(true);
						BasicPattern leftChildRewrittenPattern = leftChildRewrittenBGP.getPattern();
						leftChildRewrittenPattern.add(rightEtp);
						logger.debug("leftChildRewrittenPattern = " + leftChildRewrittenPattern);
						
						
						return leftChildRewrittenBGP;
					} 					
					

					//List<Triple> leftChildTriplesList = leftChildRewrittenBGP.getPattern().getList();
					//SortedSet<Triple> leftChildTriplesListSorted = new TreeSet<Triple>(leftChildTriplesList);

					
				} 
			} 

			
			ExprList exprList = opLeftJoin.getExprs();
			return OpLeftJoin.create(leftChildRewritten, rightChildRewritten, exprList);
		} else if(op instanceof OpUnion) { //UNION pattern
			OpUnion opUnion = (OpUnion) op;
			Op leftChild = opUnion.getLeft();
			Op rightChild = opUnion.getRight();
			Op leftChildRewritten = this.rewrite(leftChild);
			Op rightChildRewritten = this.rewrite(rightChild);
			return new OpUnion(leftChildRewritten, rightChildRewritten);
		} else if(op instanceof OpFilter) { //FILTER pattern
			OpFilter opFilter = (OpFilter) op;
			Op subOp = opFilter.getSubOp();
			Op subOpRewritten = this.rewrite(subOp);
			return OpFilter.filter(opFilter.getExprs(), subOpRewritten);
		} else if(op instanceof OpProject) {
			//			logger.debug("op instanceof OpProject/OpSlice/OpDistinct");
			OpProject opProject = (OpProject) op;
			Op subOp = opProject.getSubOp();
			Op subOpRewritten = this.rewrite(subOp);
			return new OpProject(subOpRewritten, opProject.getVars());
		} else if(op instanceof OpSlice) {
			OpSlice opSlice = (OpSlice) op;
			Op subOp = opSlice.getSubOp();
			Op subOpRewritten = this.rewrite(subOp);
			return new OpSlice(subOpRewritten, opSlice.getStart(), opSlice.getLength());
		} else if(op instanceof OpDistinct) {
			OpDistinct opDistinct = (OpDistinct) op;
			Op subOp = opDistinct.getSubOp();
			Op subOpRewritten = this.rewrite(subOp);
			return new OpDistinct(subOpRewritten);
		} else if(op instanceof OpOrder) {
			OpOrder opOrder = (OpOrder) op;
			Op subOp = opOrder.getSubOp();
			Op subOpRewritten = this.rewrite(subOp);
			return new OpOrder(subOpRewritten, opOrder.getConditions());
		} else {
			return op;
		}

	}

}
