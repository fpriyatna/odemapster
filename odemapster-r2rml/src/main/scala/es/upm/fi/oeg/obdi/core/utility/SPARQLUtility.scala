package es.upm.fi.oeg.obdi.core.utility

import com.hp.hpl.jena.graph.Node
import com.hp.hpl.jena.sparql.algebra.Op
import com.hp.hpl.jena.graph.Triple
import com.hp.hpl.jena.sparql.algebra.op.OpBGP
import scala.collection.JavaConversions._
import com.hp.hpl.jena.sparql.algebra.op.OpJoin
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin
import com.hp.hpl.jena.sparql.algebra.op.OpUnion
import com.hp.hpl.jena.sparql.algebra.op.OpFilter
import com.hp.hpl.jena.sparql.algebra.op.OpProject
import com.hp.hpl.jena.sparql.algebra.op.OpSlice
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct
import com.hp.hpl.jena.sparql.algebra.op.OpOrder


class SPARQLUtility {
	def isNodeInSubjectTriple(node : Node, tp: Triple) : Boolean = {
	  tp.getSubject() == node
	}
	
	def isNodeInSubjectGraph(node : Node, op : Op) : Boolean = {
	  val found = op match {
	    case tp: Triple => {
	      this.isNodeInSubjectTriple(node, tp)
	    }
	    case bgp: OpBGP => {
	      this.isNodeInSubjectBGP(node, bgp.getPattern().toList);
	    }
	    case join: OpJoin => {
	      this.isNodeInSubjectGraphs(node,join.getLeft(), join.getRight());
	    }
	    case leftJoin: OpLeftJoin => {
	      this.isNodeInSubjectGraphs(node,leftJoin.getLeft(), leftJoin.getRight());
	    }	    
	    case union: OpUnion => {
	      this.isNodeInSubjectGraphs(node,union.getLeft(), union.getRight());
	    }
	    case filter: OpFilter=> {
	      this.isNodeInSubjectGraph(node,filter.getSubOp());
	    }
	    case project: OpProject=> {
	      this.isNodeInSubjectGraph(node,project.getSubOp());
	    }	    
	    case slice: OpSlice=> {
	      this.isNodeInSubjectGraph(node,slice.getSubOp());
	    }
	    case distinct: OpDistinct=> {
	      this.isNodeInSubjectGraph(node,distinct.getSubOp());
	    }	    
	    case order: OpOrder=> {
	      this.isNodeInSubjectGraph(node,order.getSubOp());
	    }
	    case _ => false
	  }
	  
	  found;
	}
	
	def isNodeInSubjectBGP(node : Node, bgpList : List[Triple]) : Boolean = {
	  val isInHead = isNodeInSubjectTriple(node, bgpList.head);
	  var found = isInHead;
	  if(!found && !bgpList.tail.isEmpty) {
	    found = isNodeInSubjectBGP(node, bgpList.tail);  
	  }
	  found;
	}
	
	def isNodeInSubjectGraphs(node : Node, opLeft: Op, opRight: Op) : Boolean = {
	  val isInLeft = isNodeInSubjectGraph(node, opLeft);
	  var found = isInLeft;
	  if(!found) {
	    found = isNodeInSubjectGraph(node, opRight);
	  }
	  found;
	}	

	
}