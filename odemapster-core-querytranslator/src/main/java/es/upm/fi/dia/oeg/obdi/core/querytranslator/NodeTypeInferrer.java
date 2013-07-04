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
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesByRdfType = 
					this.inferByRDFType(opQueryPattern);
			logger.info("mapNodeTypesByRdfType = " + mapNodeTypesByRdfType);

//			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesBySubject = 
//					this.inferBySubject(opQueryPattern);
//			logger.info("mapNodeTypesBySubject = " + mapNodeTypesBySubject);

			Map<Node, Set<AbstractConceptMapping>> mapSubjectTypesByPredicatesURIs = 
					this.inferSubjectTypesByPredicatesURIs(opQueryPattern);
			logger.info("mapSubjectTypesByPredicatesURIs = " + mapSubjectTypesByPredicatesURIs);

			Map<Node, Set<AbstractConceptMapping>> mapSubjectTypesBySubjectUri = 
					this.inferSubjectTypesBySubjectURI(opQueryPattern);
			logger.info("mapSubjectTypesBySubjectUri = " + mapSubjectTypesBySubjectUri);

			Map<Node, Set<AbstractConceptMapping>> mapObjectTypesByObjectsURIs = 
					this.inferObjectTypesByObjectURI(opQueryPattern);
			logger.info("mapObjectTypesByObjectsURIs = " + mapObjectTypesByObjectsURIs);
			for(Node mapNodeTypesByUriKey : mapObjectTypesByObjectsURIs.keySet()) {
				Set<AbstractConceptMapping> mapNodeTypesByUriValue = mapObjectTypesByObjectsURIs.get(mapNodeTypesByUriKey);
				Set<AbstractConceptMapping> setConceptMappingsWithoutClassURI = new HashSet<AbstractConceptMapping>();
				for(AbstractConceptMapping cm : mapNodeTypesByUriValue) {
					Collection<String> mappedClassURIs = cm.getMappedClassURIs(); 
					if(mappedClassURIs == null || mappedClassURIs.isEmpty()) {
						setConceptMappingsWithoutClassURI.add(cm);
					}
				}
				mapNodeTypesByUriValue.removeAll(setConceptMappingsWithoutClassURI);
			}
			logger.info("mapObjectTypesByObjectsURIs updated = " + mapObjectTypesByObjectsURIs);

			Map<Node, Set<AbstractConceptMapping>> mapObjectTypesByPredicateURI = 
			this.inferObjectTypesByPredicateURI(opQueryPattern);
			logger.info("mapObjectTypesByPredicateURI = " + mapObjectTypesByPredicateURI);

			
			List<Map<Node, Set<AbstractConceptMapping>>> listMapNodes = new Vector<Map<Node,Set<AbstractConceptMapping>>>();
			listMapNodes.add(mapNodeTypesByRdfType);
//			listMapNodes.add(mapNodeTypesBySubject);
			listMapNodes.add(mapSubjectTypesBySubjectUri);
			listMapNodes.add(mapSubjectTypesByPredicatesURIs);
			listMapNodes.add(mapObjectTypesByObjectsURIs);
			listMapNodes.add(mapObjectTypesByPredicateURI);
			
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
						} else {
							String errorMessage = "No rdf:type mapping for: " + subjectType;
							logger.debug(errorMessage);
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
			Op opJoinLeft = opJoin.getLeft();
			Op opJoinRight = opJoin.getRight();
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferByRDFType(opJoinLeft);
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferByRDFType(opJoinRight);
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

	
//	private Set<AbstractConceptMapping> inferSubjectTypesByPredicateURI(
//			String predicateURI) {
//		Set<AbstractConceptMapping> result = 
//				new HashSet<AbstractConceptMapping>();
//		
//		if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
//			Set<AbstractConceptMapping> cms = 
//					this.mappingDocument.getConceptMappingByPropertyUri(
//							predicateURI);
//			if(cms != null && cms.size() > 0) {
//				result.addAll(cms);
//				
//
//			}
//		}
//		
//		return result;
//	}
	
	private Map<Node, Set<AbstractConceptMapping>> inferSubjectsTypesByPredicateURIs(Map<Node, Set<Triple>> mapSubjectSTGs) {
		Map<Node, Set<AbstractConceptMapping>> result = new HashMap<Node, Set<AbstractConceptMapping>>();
		for(Node subject : mapSubjectSTGs.keySet()) {
			Set<Triple> stg = mapSubjectSTGs.get(subject);
			Set<String> predicateURIs = new HashSet<String>();
			for(Triple tp : stg) {
				Node tpPredicate = tp.getPredicate();
				if(tpPredicate.isURI()) {
					String predicateURI = tpPredicate.getURI();
					if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
						predicateURIs.add(predicateURI);
					}
				}
			}
			Set<AbstractConceptMapping> subjectTypes = 
					this.mappingDocument.getConceptMappingByPropertyURIs(
							predicateURIs);
			result.put(subject, subjectTypes);
		}
		
		return result;
	}
	
	private Map<Node, Set<Triple>> bgpToSTGs(List<Triple> triples) {
		Map<Node, Set<Triple>> result = new HashMap<Node, Set<Triple>>();
		
		for(Triple tp : triples) {
			Node tpSubject = tp.getSubject();
			Set<Triple> stg = result.get(tpSubject);
			if(stg == null) {
				stg = new HashSet<Triple>();
				result.put(tpSubject, stg);
			}
			stg.add(tp);
		}
		
		return result;
	}
	

	
	private Map<Node, Set<AbstractConceptMapping>> inferSubjectTypesBySubjectURI(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			Map<Node, Set<Triple>> mapSubjectSTGs = this.bgpToSTGs(bpTriples);
			
			
			for(Triple tp : bpTriples) {
				Node subject = tp.getSubject();
				
				if(subject.isURI()) {
					String subjectURI = subject.getURI();
					Set<AbstractConceptMapping> subjectTypes = 
							this.inferByURI(subjectURI);
					if(subjectTypes != null && subjectTypes.size() > 0) {
						this.addToInferredTypes(
								mapNodeTypes, subject, subjectTypes);
					}
				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferSubjectTypesBySubjectURI(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferSubjectTypesBySubjectURI(opLeftJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferSubjectTypesBySubjectURI(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferSubjectTypesBySubjectURI(opUnion.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferSubjectTypesBySubjectURI(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferSubjectTypesBySubjectURI(opJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferSubjectTypesBySubjectURI(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}

	private Map<Node, Set<AbstractConceptMapping>> inferObjectTypesByObjectURI(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			
			for(Triple tp : bgp.getPattern().getList()) {
				Node tpObject = tp.getObject();
				if(tpObject.isURI()) {
					String objectURI = tpObject.getURI();
					Set<AbstractConceptMapping> nodeTypes = 
							this.inferByURI(objectURI);
					if(nodeTypes != null && nodeTypes.size() > 0) {
						this.addToInferredTypes(mapNodeTypes, tpObject, nodeTypes);
					}
				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferObjectTypesByObjectURI(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferObjectTypesByObjectURI(opLeftJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferObjectTypesByObjectURI(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferObjectTypesByObjectURI(opUnion.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferObjectTypesByObjectURI(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferObjectTypesByObjectURI(opJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferObjectTypesByObjectURI(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}

	private Map<Node, Set<AbstractConceptMapping>> inferObjectTypesByPredicateURI(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			
			for(Triple tp : bgp.getPattern().getList()) {
				Node tpPredicate = tp.getPredicate();
				Node tpObject = tp.getObject();
				if(tpPredicate.isURI()) {
					String predicateURI = tpPredicate.getURI();
					if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
						Set<AbstractConceptMapping> nodeTypes = 
								this.mappingDocument.getPossibleRange(predicateURI);
						//this.addToInferredTypes(mapNodeTypes, tp.getObject(), nodeTypes);
						Set<AbstractConceptMapping> inferredTypes = mapNodeTypes.get(tpObject);
						if(inferredTypes == null) { 
							inferredTypes = new HashSet<AbstractConceptMapping>(); 
						}
						inferredTypes.addAll(nodeTypes);
						mapNodeTypes.put(tpObject, inferredTypes);
					}
				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferObjectTypesByPredicateURI(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferObjectTypesByPredicateURI(opLeftJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferObjectTypesByPredicateURI(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferObjectTypesByPredicateURI(opUnion.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferObjectTypesByPredicateURI(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferObjectTypesByPredicateURI(opJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferObjectTypesByPredicateURI(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}
	
	private Map<Node, Set<AbstractConceptMapping>> inferSubjectTypesByPredicatesURIs(Op op) {
		Map<Node, Set<AbstractConceptMapping>> mapNodeTypes = new HashMap<Node, Set<AbstractConceptMapping>>();
		
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			Map<Node, Set<Triple>> mapSubjectSTGs = this.bgpToSTGs(bpTriples);
			
			//get subject types by all the predicate URIs of the STGs
			Map<Node, Set<AbstractConceptMapping>> subjectsTypesByPredicateURIs = 
					this.inferSubjectsTypesByPredicateURIs(mapSubjectSTGs);
			for(Node subject : subjectsTypesByPredicateURIs.keySet()) {
				Set<AbstractConceptMapping> subjectTypes = subjectsTypesByPredicateURIs.get(subject);
				if(subjectTypes != null && subjectTypes.size() > 0) {
					this.addToInferredTypes(
							mapNodeTypes, subject, subjectTypes);
				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferSubjectTypesByPredicatesURIs(opLeftJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferSubjectTypesByPredicatesURIs(opLeftJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferSubjectTypesByPredicatesURIs(opUnion.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferSubjectTypesByPredicatesURIs(opUnion.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesLeft = this.inferSubjectTypesByPredicatesURIs(opJoin.getLeft());
			Map<Node, Set<AbstractConceptMapping>> mapNodeTypesRight = this.inferSubjectTypesByPredicatesURIs(opJoin.getRight());
			mapNodeTypes = QueryTranslatorUtility.mergeMaps(mapNodeTypesLeft, mapNodeTypesRight);
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			mapNodeTypes = this.inferSubjectTypesByPredicatesURIs(opFilter.getSubOp());
		}
		
		return mapNodeTypes;
	}
	


	

	
	public Set<AbstractConceptMapping> getTypes(Node node) {
		return this.mapInferredTypes.get(node);
	}
	
	public Set<AbstractConceptMapping> inferByURI(
			String uri) {
		Set<AbstractConceptMapping> result = new HashSet<AbstractConceptMapping>();
		
		Collection<AbstractConceptMapping> cms = 
				this.mappingDocument.getConceptMappings();
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

	public String printInferredTypes() {
		StringBuffer result = new StringBuffer();
		for(Node key : this.mapInferredTypes.keySet()) {
			result.append(key + " : " + this.mapInferredTypes.get(key));
			result.append("\n");
		}
		return result.toString();
	}
	

}
