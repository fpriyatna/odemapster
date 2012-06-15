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
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;

public class TypeInferrer {
	private static Logger logger = Logger.getLogger(TypeInferrer.class);
	protected AbstractMappingDocument mappingDocument;
	protected Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes = 
			new HashMap<Node, Collection<AbstractConceptMapping>>();
	protected boolean change = false;

	public TypeInferrer(AbstractMappingDocument mappingDocument) {
		this.mappingDocument = mappingDocument;
	}

	public Map<Node, Collection<AbstractConceptMapping>> infer(Op opQueryPattern) {
		this.mapInferredTypes = new HashMap<Node, Collection<AbstractConceptMapping>>();

		//1. by explicit rdf type
		this.inferByRDFType(opQueryPattern);

		//2. by well defined URIs
		//this.inferByWellDefinedURIs(opQueryPattern);

		//5. subject and object type detection of predicate
		this.inferByPredicateURI(opQueryPattern);

		//
		//		
		//		int mapNodeCMSizeBeforeIteration = this.mapInferredTypes.size();
		//		int mapNodeCMSizeAfterIteration = this.mapInferredTypes.size();
		do {
			change=false;
			//			mapNodeCMSizeBeforeIteration = this.mapInferredTypes.size();
			//3. object type detection with defined subject
			this.inferByDefinedSubject(opQueryPattern);
			//
			//4. subject type detection with defined object
			this.inferByDefinedObject(opQueryPattern);
			//
			//

			this.inferByFilterExpression(opQueryPattern);
			//			
			//			mapNodeCMSizeAfterIteration = this.mapInferredTypes.size();
		} while(change==true);

		logger.debug("Types = " + this.printInferredTypes());
		
		return this.mapInferredTypes;
	}	
	protected void inferByRDFType(Op op) {
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
					Collection<AbstractConceptMapping> cms = 
							this.mappingDocument.getConceptMappingsByConceptName(subjectType);
					if(cms != null && cms.size() > 0) {
						AbstractConceptMapping cm = cms.iterator().next();

						logger.debug("Type of : " + subject + " = " + cm.getConceptName());
						this.addToInferredTypes(subject, cm);
						//this.mapNodeConceptMapping.put(subject, cm);

					}
				} 
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.inferByRDFType(opLeftJoin.getLeft());
			this.inferByRDFType(opLeftJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.inferByRDFType(opUnion.getLeft());
			this.inferByRDFType(opUnion.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.inferByRDFType(opJoin.getLeft());
			this.inferByRDFType(opJoin.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.inferByRDFType(opFilter.getSubOp());
		}
	}

	private void addToInferredTypes(Node node, AbstractConceptMapping cm) {
		Collection<AbstractConceptMapping> types = this.mapInferredTypes.get(node);
		if(types == null) { 
			types = new HashSet<AbstractConceptMapping>();
			this.mapInferredTypes.put(node, types);
		}
		types.add(cm);
	}

	private void addToInferredTypes(Node node, Collection<AbstractConceptMapping> cms) {
		Collection<AbstractConceptMapping> types = this.mapInferredTypes.get(node);
		if(types == null) { 
			types = new HashSet<AbstractConceptMapping>();
			this.mapInferredTypes.put(node, types);
		}
		types.addAll(cms);
	}

	public String printInferredTypes() {
		String result = "";
		if(this.mapInferredTypes != null) {
			Set<Node> keySet = this.mapInferredTypes.keySet();
			for(Node key : keySet) {
				String subResult = key + ":";
				Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(key);
				for(AbstractConceptMapping abstractConceptMapping : cms) {
					AbstractConceptMapping cm = abstractConceptMapping; 
					subResult += cm.getId() + ",";
				}
				result += subResult + "\n";
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return this.printInferredTypes();
	}

	protected void inferByFilterExpression(Op op) {
		if(op instanceof OpBGP) {
			//do nothing
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.inferByFilterExpression(opLeftJoin.getLeft());
			this.inferByFilterExpression(opLeftJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.inferByFilterExpression(opUnion.getLeft());
			this.inferByFilterExpression(opUnion.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.inferByFilterExpression(opJoin.getLeft());
			this.inferByFilterExpression(opJoin.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			Op opFilterSubOp = opFilter.getSubOp();
			ExprList exprList = opFilter.getExprs();
			this.inferByFilterExpression(opFilterSubOp);
			this.inferByExprList(exprList);

		}
	}

	private void inferByExprList(ExprList exprList) {
		List<Expr> exprs = exprList.getList();
		for(Expr expr : exprs) {
			this.inferByExpr(expr);
		}
	}

	private void inferByExpr(Expr expr) {
		if(expr.isFunction()) {
			ExprFunction exprFunction = expr.getFunction();
			List<Expr> args = exprFunction.getArgs();
			if((exprFunction instanceof E_Equals) || (exprFunction instanceof E_NotEquals) 
					|| (exprFunction instanceof E_GreaterThan) || (exprFunction instanceof E_GreaterThanOrEqual) 
					|| (exprFunction instanceof E_LessThan) || (exprFunction instanceof E_LessThanOrEqual)
				) {
				Expr arg0 = args.get(0);
				Node arg0Node = this.asNode(arg0);
				Collection<AbstractConceptMapping> arg0Types = this.mapInferredTypes.get(arg0Node);
				if(arg0Types != null && arg0Types.size() == 0) {
					arg0Types = null;
				}
				
				Expr arg1 = args.get(1);
				Node arg1Node = this.asNode(arg1);
				Collection<AbstractConceptMapping> arg1Types = this.mapInferredTypes.get(arg1Node);
				if(arg1Types != null && arg1Types.size() == 0) {
					arg1Types = null;
				}

				if(arg0Types != null && arg1Types == null) {
					arg1Types = new HashSet<AbstractConceptMapping>();
					arg1Types.addAll(arg0Types);
					this.mapInferredTypes.put(arg1Node, arg1Types);
					this.change = true;
				} else if(arg0Types == null && arg1Types != null) {
					arg0Types = new HashSet<AbstractConceptMapping>();
					arg0Types.addAll(arg1Types);
					this.mapInferredTypes.put(arg0Node, arg0Types);
					this.change = true;
				} else if(arg0Types != null && arg1Types != null) {
					if(arg0Types.containsAll(arg1Types) && arg1Types.containsAll(arg0Types)) {
						//do nothing
					} else {
						Collection<AbstractConceptMapping> typesIntersection = 
								new HashSet<AbstractConceptMapping>(arg0Types);
						typesIntersection.retainAll(arg1Types);
						this.mapInferredTypes.put(arg0Node, typesIntersection);
						this.mapInferredTypes.put(arg1Node, typesIntersection);
						this.change = true;
					}
				}
			} 
		}
	}

	private Node asNode(Expr arg0) {
		Node arg0Node = null;
		
		//boolean isIRI = arg0.getConstant().isIRI();
		
		
		if(arg0.isVariable()) {
			arg0Node = arg0.asVar().asNode();
		} else if(arg0.isConstant()) {
			boolean isIRI = arg0.getConstant().getNode().isURI();
			if(isIRI) {
				arg0Node = arg0.getConstant().asNode();
			}
			
		}

		return arg0Node;
	}

	protected void inferByDefinedSubject(Op op) {
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;

			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				Node tpObject = tp.getObject();
				Collection<AbstractConceptMapping> inferredObjectTypes = this.mapInferredTypes.get(tpObject);
				if(inferredObjectTypes == null || inferredObjectTypes.size() != 1) {
					Node tpPredicate = tp.getPredicate();
					Set<AbstractConceptMapping> possibleObjectTypes = new HashSet<AbstractConceptMapping>();

					String predicateURI = tpPredicate.getURI();
					if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
						Node tpSubject = tp.getSubject();
						Collection<AbstractConceptMapping> inferredSubjectTypes = this.mapInferredTypes.get(tpSubject);
						if(inferredSubjectTypes != null && inferredSubjectTypes.size() > 0) {
							for(AbstractConceptMapping inferredSubjectType : inferredSubjectTypes) {
								//							Collection<R2OConceptMapping> cmObjects = this.mapInferredTypes.get(object);
								//							R2OConceptMapping cmObject = null;
								//							if(cmObjects != null) {
								//								cmObject = cmObjects.iterator().next();
								//							}

								Collection<AbstractPropertyMapping> pms = inferredSubjectType.getPropertyMappings(predicateURI);

								if(pms != null && pms.size() != 0) {
									if(pms.size() > 1) {
										logger.warn("Multiple defined property mappings : " + predicateURI);
										logger.warn("Only the first one obtained is being used.");
										//throw new R2OTranslationException("Multiple defined property mappings : " + predicateURI);
									}

									AbstractPropertyMapping pm = pms.iterator().next();
									if(pm.isObjectPropertyMapping()) {
										//this.mapPredicatePropertyMapping.put(predicate, pm);

										IRelationMapping rm = (IRelationMapping) pm;
										String rangeConceptMappingID = rm.getRangeClassMapping();
										if(rangeConceptMappingID != null) {
											AbstractConceptMapping rangeConceptMapping = 
													this.mappingDocument.getConceptMappingByMappingId(rangeConceptMappingID);
											possibleObjectTypes.add(rangeConceptMapping);
											
										}


										if(inferredObjectTypes == null) {
											this.mapInferredTypes.put(tpObject, possibleObjectTypes);
											this.change = true;
										} else {
											if(possibleObjectTypes.containsAll(inferredObjectTypes) && inferredObjectTypes.containsAll(possibleObjectTypes)) {
												//nothing new
											} else {
												Set<AbstractConceptMapping> objectTypesIntersection = 
														new HashSet<AbstractConceptMapping>(possibleObjectTypes);
												objectTypesIntersection.retainAll(inferredObjectTypes);
												this.mapInferredTypes.put(tpObject, objectTypesIntersection);
												this.change = true;
											}
										}


										//										logger.debug("Type of : " + object + " = " + rangeConceptMapping.getConceptName());
										//										this.addToInferredTypes(object, rangeConceptMapping);
										//										change=true;
									}
								}


							}
						}
					}
				}



			}



		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.inferByDefinedSubject(opLeftJoin.getLeft());
			this.inferByDefinedSubject(opLeftJoin.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.inferByDefinedSubject(opJoin.getLeft());
			this.inferByDefinedSubject(opJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.inferByDefinedSubject(opUnion.getLeft());
			this.inferByDefinedSubject(opUnion.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.inferByDefinedSubject(opFilter.getSubOp());
		}
	}


	protected void inferByDefinedObject(Op op) {
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				//System.out.println("tp = " + tp);
				Node tpPredicate = tp.getPredicate();
				String predicateURI = tpPredicate.getURI();
				if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
					Node tpSubject = tp.getSubject();
					Collection<AbstractConceptMapping> inferredSubjectTypes = this.mapInferredTypes.get(tpSubject);
					if(inferredSubjectTypes == null || inferredSubjectTypes.size() != 1) {
						Node tpObject = tp.getObject();
						Collection<AbstractConceptMapping> inferedObjectTypes = this.mapInferredTypes.get(tpObject);

						if(inferedObjectTypes != null) { //known object type only
							for(AbstractConceptMapping abstractConceptMapping : inferedObjectTypes) {
								AbstractConceptMapping inferedObjectType = (AbstractConceptMapping) abstractConceptMapping; 
								String rangeConceptName = inferedObjectType.getConceptName();
								Collection<IRelationMapping> rms = 
										this.mappingDocument.getRelationMappingsByPropertyAndRange(predicateURI, rangeConceptName);

								if(rms != null && rms.size() > 0 ) {
									if(rms.size() > 1) {
										logger.debug("Multiple defined property mappings : " + predicateURI + " with range : " + rangeConceptName);
										//logger.warn("Only the first one obtained is being used.");
										//throw new R2OTranslationException("Multiple defined property mappings : " + predicateURI);
									}

									for(IRelationMapping rm : rms) {
										Set<AbstractConceptMapping> subjectTypes = new HashSet<AbstractConceptMapping>();
										AbstractConceptMapping cmDomain = rm.getParent();
										subjectTypes.add(cmDomain);

										
										if(inferredSubjectTypes == null) {
											this.mapInferredTypes.put(tpSubject, subjectTypes);
											this.change = true;
										} else {
											if(inferredSubjectTypes.containsAll(subjectTypes) && subjectTypes.containsAll(inferredSubjectTypes)) {
												//do nothing
											} else {
												Set<AbstractConceptMapping> subjectTypesIntersection = new HashSet<AbstractConceptMapping>(subjectTypes);
												subjectTypesIntersection.retainAll(inferredSubjectTypes);
												this.mapInferredTypes.put(tpSubject, subjectTypesIntersection);
												this.change = true;

											}
										}

									}
								}
							}
						}
					}


				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.inferByDefinedObject(opLeftJoin.getLeft());
			this.inferByDefinedObject(opLeftJoin.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.inferByDefinedObject(opJoin.getLeft());
			this.inferByDefinedObject(opJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.inferByDefinedObject(opUnion.getLeft());
			this.inferByDefinedObject(opUnion.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.inferByDefinedObject(opFilter.getSubOp());
		}

		//logger.info("translator.mapPropertyMappings = " + this.getMapPropertyMappings());	
	}

	private void inferByPredicateURI(Op op) {

		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				//System.out.println("tp = " + tp);
				Node tpPredicate = tp.getPredicate();
				String predicateURI = tpPredicate.getURI();
				if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
					Node tpSubject = tp.getSubject();
					//Collection<AbstractConceptMapping> cmSubjects = this.mapInferredTypes.get(tpSubject);
					Collection<AbstractConceptMapping> inferredSubjectTypes = this.mapInferredTypes.get(tpSubject);
					AbstractConceptMapping inferredSubjectType = null;
					if(inferredSubjectTypes != null && inferredSubjectTypes.size() == 1) { 
						inferredSubjectType = inferredSubjectTypes.iterator().next(); 
					}

					Node tpObject = tp.getObject();
					//Collection<AbstractConceptMapping> cmObjects = this.mapInferredTypes.get(tpObject);
					Collection<AbstractConceptMapping> inferredObjectTypes = this.mapInferredTypes.get(tpObject);
					AbstractConceptMapping inferredObjectType = null;
					if(inferredObjectTypes != null && inferredObjectTypes.size() == 1) { 
						inferredObjectType = inferredObjectTypes.iterator().next(); 
					}

					
					//if(cmSubjects == null && cmObjects == null) //unknown subject nor object 
					//{
					Collection<AbstractPropertyMapping> pms = 
							this.mappingDocument.getPropertyMappingsByPropertyURI(predicateURI);
					if(pms != null && pms.size() > 0) {
						Set<AbstractConceptMapping> possibleSubjectTypes = null;
						Set<AbstractConceptMapping> possibleObjectTypes = null;
						for(AbstractPropertyMapping pm : pms) {
							AbstractConceptMapping parentPropertyMapping = pm.getParent();

							//this.addToInferredTypes(subject, subjectConceptMapping);
							if(possibleSubjectTypes == null) {possibleSubjectTypes = new HashSet<AbstractConceptMapping>();}
							possibleSubjectTypes.add(parentPropertyMapping);

							if(pm.isObjectPropertyMapping()) {
								IRelationMapping rm = (IRelationMapping) pm;
								String rangeConceptMappingID = rm.getRangeClassMapping();
								if(rangeConceptMappingID != null) {
									AbstractConceptMapping rangeConceptMapping = 
											this.mappingDocument.getConceptMappingByMappingId(rangeConceptMappingID);

									//this.addToInferredTypes(object, rangeConceptMapping);
									if(possibleObjectTypes == null) { possibleObjectTypes = new HashSet<AbstractConceptMapping>(); } 
									possibleObjectTypes.add(rangeConceptMapping);
									
								}
							}
						}	


						if(possibleSubjectTypes != null && inferredSubjectType == null) {


							if(inferredSubjectTypes == null) {
								this.mapInferredTypes.put(tpSubject, possibleSubjectTypes);
								this.change = true;
							} else {
								if(inferredSubjectTypes.containsAll(possibleSubjectTypes) && possibleSubjectTypes.containsAll(inferredSubjectTypes)) {
									//do nothing
								} else {
									Set<AbstractConceptMapping> subjectTypesIntersection = 
											new HashSet<AbstractConceptMapping>(possibleSubjectTypes);
									subjectTypesIntersection.retainAll(inferredSubjectTypes);
									this.mapInferredTypes.put(tpSubject, subjectTypesIntersection);
									this.change = true;
								}
							}
						}

						if(possibleObjectTypes != null && inferredObjectType == null) {
							if(inferredObjectTypes == null) {
								this.mapInferredTypes.put(tpObject, possibleObjectTypes);
								this.change = true;
							} else {
								if(possibleObjectTypes.containsAll(inferredObjectTypes) && inferredObjectTypes.containsAll(possibleObjectTypes)) {
									//do nothing
								} else {
									Set<AbstractConceptMapping> objectTypesIntersection = new HashSet<AbstractConceptMapping>(possibleObjectTypes);
									objectTypesIntersection.retainAll(inferredObjectTypes);
									this.mapInferredTypes.put(tpObject, objectTypesIntersection);
									this.change = true;

								}
							}
						}
					}




					//}
				}
			}
			//logger.info("translator.mapPropertyMappings = " + this.getMapPropertyMappings());
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.inferByPredicateURI(opLeftJoin.getLeft());
			this.inferByPredicateURI(opLeftJoin.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.inferByPredicateURI(opJoin.getLeft());
			this.inferByPredicateURI(opJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.inferByPredicateURI(opUnion.getLeft());
			this.inferByPredicateURI(opUnion.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.inferByPredicateURI(opFilter.getSubOp());
		}
	}

}
