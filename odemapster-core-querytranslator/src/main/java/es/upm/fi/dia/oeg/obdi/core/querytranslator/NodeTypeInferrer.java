package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;

public class NodeTypeInferrer {
	private static Logger logger = Logger.getLogger(NodeTypeInferrer.class);
	private AbstractMappingDocument mappingDocument;
	
	
	public NodeTypeInferrer(AbstractMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

//	private void addToInferredTypes(Map<Node, Set<AbstractConceptMapping>> mapNodeTypes, Node node, AbstractConceptMapping cm) {
//		Set<AbstractConceptMapping> types = mapNodeTypes.get(node);
//		if(types == null) { 
//			types = new HashSet<AbstractConceptMapping>();
//			mapNodeTypes.put(node, types);
//		} else {
//			if(types.contains(cm)) {
//				types.retainAll(new Hacm);
//			}
//		}
//		types.add(cm);
//	}

	private void addToInferredTypes(Map<Node, Set<AbstractConceptMapping>> mapNodeTypes, Node node, Set<AbstractConceptMapping> cms) {
		if(cms != null) {
			for(AbstractConceptMapping cm : cms) {
				Set<AbstractConceptMapping> types = mapNodeTypes.get(node);
				if(types == null) { 
					//types = new HashSet<AbstractConceptMapping>();
					mapNodeTypes.put(node, cms);
				} else {
					Set<AbstractConceptMapping> intersection = new HashSet<AbstractConceptMapping>();
					intersection.addAll(types);
					intersection.retainAll(cms);
					mapNodeTypes.put(node, intersection);
				}
			}
		}
	}

	
	public Map<Node, Set<AbstractConceptMapping>> infer(Op opQueryPattern) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypesByRdfType = this.inferByRDFType(opQueryPattern);
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypesByPredicateUri = this.inferByPredicateURI(opQueryPattern);
		
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = NodeTypeInferrer.mergeMaps(mapNodeTypesByRdfType, mapNodeTypesByPredicateUri);
		return mapNodeTypes;
	}
	
	protected Map<Node, Set<AbstractConceptMapping>> inferByRDFType(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				Node subject = tp.getSubject();
				Node predicate = tp.getPredicate();
				String predicateURI = predicate.getURI();
				Node object = tp.getObject();

				if(RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
					String subjectType = object.getURI();
					Set<AbstractConceptMapping> cms = 
							this.mappingDocument.getConceptMappingsByConceptName(subjectType);
					if(cms != null && cms.size() > 0) {
						this.addToInferredTypes(mapNodeTypes, subject, cms);
						//this.mapNodeConceptMapping.put(subject, cm);

					}
				} 
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opLeftJoin.getRight());
			mapNodeTypes = NodeTypeInferrer.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opUnion.getRight());
			mapNodeTypes = NodeTypeInferrer.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opJoin.getRight());
			mapNodeTypes = NodeTypeInferrer.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferByRDFType(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}

	private Map<Node, Set<AbstractConceptMapping>> inferByPredicateURI(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				//System.out.println("tp = " + tp);
				Node subject = tp.getSubject();
				Node tpPredicate = tp.getPredicate();
				String predicateURI = tpPredicate.getURI();
				if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
					Set<AbstractConceptMapping> cms = this.mappingDocument.getConceptMappingByPropertyUri(predicateURI);
					if(cms != null && cms.size() > 0) {
						this.addToInferredTypes(mapNodeTypes, subject, cms);
					}
				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByPredicateURI(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByPredicateURI(opLeftJoin.getRight());
			mapNodeTypes = NodeTypeInferrer.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByPredicateURI(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByPredicateURI(opUnion.getRight());
			mapNodeTypes = NodeTypeInferrer.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByPredicateURI(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByPredicateURI(opJoin.getRight());
			mapNodeTypes = NodeTypeInferrer.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferByPredicateURI(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}

	
	private static Map<Node, Set<AbstractConceptMapping>> mergeMaps(
			Map<Node, Set<AbstractConceptMapping>> map1, 
			Map<Node, Set<AbstractConceptMapping>> map2) {
		Map<Node, Set<AbstractConceptMapping>> result = new HashMap<Node, Set<AbstractConceptMapping>>();
		result.putAll(map1);
		
		Set<Node> map2Key = map2.keySet();
		for(Node map2KeyNode : map2Key) {
			Set<AbstractConceptMapping> map2Values = map2.get(map2KeyNode);
			if(result.containsKey(map2KeyNode)) {
				Set<AbstractConceptMapping> map1Values = map1.get(map2KeyNode);
				Set<AbstractConceptMapping> intersection = new HashSet<AbstractConceptMapping>();
				intersection.addAll(map1Values);
				intersection.retainAll(map2Values); 
				result.put(map2KeyNode, intersection);
			} else {
				result.put(map2KeyNode, map2Values);
			}
		}
		
		return result;
	}
	
}
