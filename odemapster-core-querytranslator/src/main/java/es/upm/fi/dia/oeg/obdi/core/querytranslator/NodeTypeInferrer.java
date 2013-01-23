package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;

public class NodeTypeInferrer {
	private Map<Node, Set<AbstractConceptMapping>> mapInferredTypes = null;
	private static Logger logger = Logger.getLogger(NodeTypeInferrer.class);
	private AbstractMappingDocument mappingDocument;
	private Query query;
	
	public void setQuery(Query query) {
		this.query = query;
	}

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

	public Map<Node, Set<AbstractConceptMapping>> infer() {
		if(this.mapInferredTypes == null) {
			this.mapInferredTypes = this.infer(query); 
		}
		
		return this.mapInferredTypes;
	}
	
	public Map<Node, Set<AbstractConceptMapping>> infer(Query query) {
		if(this.mapInferredTypes == null) {
			Element queryPattern = query.getQueryPattern();
			Op opQueryPattern = Algebra.compile(queryPattern);
			this.mapInferredTypes = this.infer(opQueryPattern);			
		}
		
		return this.mapInferredTypes;
	}
	
	private Map<Node, Set<AbstractConceptMapping>> infer(Op opQueryPattern) {
		if(this.mapInferredTypes == null) {
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesByRdfType = this.inferByRDFType(opQueryPattern);
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesBySubject = this.inferBySubject(opQueryPattern);
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesByUri = this.inferByURI(opQueryPattern);
			
			List<Map<Node, Set<AbstractConceptMapping>>> listMapNodes = new Vector<Map<Node,Set<AbstractConceptMapping>>>();
			listMapNodes.add(mapNodeTypesByRdfType);
			listMapNodes.add(mapNodeTypesBySubject);
			listMapNodes.add(mapNodeTypesByUri);
			
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = 
					QueryTranslatorUtility.mergeMaps(listMapNodes);
			this.mapInferredTypes = mapNodeTypes;			
		}
		
		return this.mapInferredTypes;
	}
	
	private Map<Node, Set<AbstractConceptMapping>> inferByRDFType(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				Node subject = tp.getSubject();
				Node predicate = tp.getPredicate();
				if(predicate.isURI()) {
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
 
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opLeftJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opUnion.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferByRDFType(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}

	private Map<Node, Set<AbstractConceptMapping>> inferBySubject(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		Map<Node,List<Set<AbstractConceptMapping>>> mapNodeListNodeTypes = new HashMap<Node, List<Set<AbstractConceptMapping>>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				Node subject = tp.getSubject();
				Node predicate = tp.getPredicate();
				if(predicate.isURI() && !RDF.type.getURI().equalsIgnoreCase(predicate.getURI())) {
					String predicateURI = predicate.getURI();
					Set<AbstractConceptMapping> conceptMappingsByPredicate = 
							this.mappingDocument.getConceptMappingByPropertyUri(predicateURI);
					List<Set<AbstractConceptMapping>> listAbstractConceptMappings = 
							mapNodeListNodeTypes.get(subject);
					if(listAbstractConceptMappings == null) {
						listAbstractConceptMappings = new Vector<Set<AbstractConceptMapping>>();
						mapNodeListNodeTypes.put(subject, listAbstractConceptMappings);
					}
					listAbstractConceptMappings.add(conceptMappingsByPredicate);
				}
 			}
			
			for(Node key : mapNodeListNodeTypes.keySet()) {
				List<Set<AbstractConceptMapping>> listCMS = mapNodeListNodeTypes.get(key);
				Set<AbstractConceptMapping> cms = QueryTranslatorUtility.setsIntersection(listCMS);	
				Set<AbstractConceptMapping> nodeTypes = mapNodeTypes.get(key);
				if(nodeTypes == null) {
					nodeTypes = new HashSet<AbstractConceptMapping>();
					mapNodeTypes.put(key, nodeTypes);
				}
				nodeTypes.addAll(cms);
			}
			
			
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opLeftJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opUnion.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferByRDFType(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}

	
	private Set<AbstractConceptMapping> inferByPredicateURI(String predicateURI) {
		Set<AbstractConceptMapping> result = new HashSet<AbstractConceptMapping>();
		
		if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
			Set<AbstractConceptMapping> cms = this.mappingDocument.getConceptMappingByPropertyUri(predicateURI);
			if(cms != null && cms.size() > 0) {
				result.addAll(cms);
			}
		}
		
		return result;
	}
	
	private Map<Node, Set<AbstractConceptMapping>> inferByURI(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				Node subject = tp.getSubject();
				Node tpPredicate = tp.getPredicate();
				
//				if(subject.isURI()) {
//					String subjectURI = subject.getURI();
//					Set<AbstractConceptMapping> nodeTypes = this.inferBySubjectURI(subjectURI);
//					if(nodeTypes != null && nodeTypes.size() > 0) {
//						this.addToInferredTypes(mapNodeTypes, subject, nodeTypes);
//					}
//					
//				}
				
				
				if(tpPredicate.isURI()) {
					String predicateURI = tpPredicate.getURI();
					Set<AbstractConceptMapping> nodeTypes = this.inferByPredicateURI(predicateURI);
					if(nodeTypes != null && nodeTypes.size() > 0) {
						this.addToInferredTypes(mapNodeTypes, subject, nodeTypes);
					}
					
//					if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
//						Set<AbstractConceptMapping> cms = this.mappingDocument.getConceptMappingByPropertyUri(predicateURI);
//						if(cms != null && cms.size() > 0) {
//							this.addToInferredTypes(mapNodeTypes, subject, cms);
//						}
//					}					
				}
				
//				Node tpObject = tp.getObject();
//				if(tpObject.isURI()) {
//					String objectURI = tpObject.getURI();
//					Set<AbstractConceptMapping> nodeTypes = this.inferBySubjectURI(objectURI);
//					if(nodeTypes != null && nodeTypes.size() > 0) {
//						this.addToInferredTypes(mapNodeTypes, tpObject, nodeTypes);
//					}
//				}				
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByURI(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByURI(opLeftJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByURI(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByURI(opUnion.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByURI(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByURI(opJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferByURI(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}

	


	

	
	public Set<AbstractConceptMapping> getTypes(Node node) {
		return this.mapInferredTypes.get(node);
	}
	
	public Set<AbstractConceptMapping> inferBySubjectURI(String uri) {
		Set<AbstractConceptMapping> result = new HashSet<AbstractConceptMapping>();
		
		Collection<AbstractConceptMapping> cms = this.mappingDocument.getConceptMappings();
		for(AbstractConceptMapping cm : cms) {
			try {
				if(cm.isPossibleInstance(uri)) {
					result.add(cm);
				}				
			} catch(Exception e) {
				logger.warn(e.getMessage());
			}
		}
		
		return result;
	}

}
