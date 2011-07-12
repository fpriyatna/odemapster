package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExpression;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OJoin;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;

public class TranslatorUtility {
	private static Logger logger = Logger.getLogger(TranslatorUtility.class);
	private R2OMappingDocument mappingDocument;
	private Map<Node, R2OConceptMapping> mapNodeConceptMapping = new HashMap<Node, R2OConceptMapping>();

	public TranslatorUtility(R2OMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {



	}

	public Map<Node, R2OConceptMapping> initializeMapConceptMapping(Op opQueryPattern) throws R2OTranslationException {
		this.mapNodeConceptMapping = new HashMap<Node, R2OConceptMapping>();

		//1. by explicit rdf type
		this.initMapNodeConceptMappingByRDFType(opQueryPattern);

		int mapNodeCMSizeBeforeIteration = this.mapNodeConceptMapping.size();
		int mapNodeCMSizeAfterIteration = this.mapNodeConceptMapping.size();
		do {
			mapNodeCMSizeBeforeIteration = this.mapNodeConceptMapping.size();
			//2. object type detection with defined subject
			this.initMapNodeConceptMappingByDefinedSubject(opQueryPattern);

			//3. subject type detection with defined object
			this.initMapNodeConceptMappingByDefinedObject(opQueryPattern);
			mapNodeCMSizeAfterIteration = this.mapNodeConceptMapping.size();			
		} while(mapNodeCMSizeBeforeIteration != mapNodeCMSizeAfterIteration);

		//4. subject and object type detection of predicate
		this.initMapNodeConceptMappingByPredicateURI(opQueryPattern);

		return this.mapNodeConceptMapping;
	}

	private void initMapNodeConceptMappingByRDFType(Op op) throws R2OTranslationException {
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
					if(cms == null || cms.size() == 0) {
						throw new R2OTranslationException("Undefined concept mapping : " + subjectType);
					} if(cms.size() > 1) {
						throw new R2OTranslationException("Multiple defined concept mappings : " + subjectType);
					}
					R2OConceptMapping cm = (R2OConceptMapping) cms.iterator().next();

					logger.info("Type of : " + subject + " = " + cm.getConceptName());
					this.mapNodeConceptMapping.put(subject, cm);
				} 
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.initMapNodeConceptMappingByRDFType(opLeftJoin.getLeft());
			this.initMapNodeConceptMappingByRDFType(opLeftJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.initMapNodeConceptMappingByRDFType(opUnion.getLeft());
			this.initMapNodeConceptMappingByRDFType(opUnion.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.initMapNodeConceptMappingByRDFType(opJoin.getLeft());
			this.initMapNodeConceptMappingByRDFType(opJoin.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.initMapNodeConceptMappingByRDFType(opFilter.getSubOp());
		}


	}

	private void initMapNodeConceptMappingByDefinedSubject(Op op) throws R2OTranslationException {
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;

			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				//System.out.println("tp = " + tp);
				Node predicate = tp.getPredicate();
				String predicateURI = predicate.getURI();
				if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
					Node subject = tp.getSubject();
					R2OConceptMapping cmSubject = mapNodeConceptMapping.get(subject);

					Node object = tp.getObject();
					R2OConceptMapping cmObject = mapNodeConceptMapping.get(object);

					if(cmSubject != null && cmObject == null) //known subject type only
					{
						Collection<R2OPropertyMapping> pms = cmSubject.getPropertyMappings(predicateURI);
						if(pms == null || pms.size() == 0) {
							throw new R2OTranslationException("Undefined property mapping : " + predicateURI);
						} else if(pms.size() > 1) {
							logger.warn("Multiple defined property mappings : " + predicateURI);
							logger.warn("Only the first one obtained is being used.");
							//throw new R2OTranslationException("Multiple defined property mappings : " + predicateURI);
						}
						R2OPropertyMapping pm = pms.iterator().next();
						if(pm instanceof R2ORelationMapping) {
							//this.mapPredicatePropertyMapping.put(predicate, pm);

							R2ORelationMapping rm = (R2ORelationMapping) pm;
							String rangeConceptMappingID = rm.getToConcept();
							R2OConceptMapping rangeConceptMapping = this.mappingDocument.getConceptMappingByConceptMappingId(rangeConceptMappingID);

							logger.info("Type of : " + object + " = " + rangeConceptMapping.getConceptName());
							this.mapNodeConceptMapping.put(object, rangeConceptMapping);
						}
					}
				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.initMapNodeConceptMappingByDefinedSubject(opLeftJoin.getLeft());
			this.initMapNodeConceptMappingByDefinedSubject(opLeftJoin.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.initMapNodeConceptMappingByDefinedSubject(opJoin.getLeft());
			this.initMapNodeConceptMappingByDefinedSubject(opJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.initMapNodeConceptMappingByDefinedSubject(opUnion.getLeft());
			this.initMapNodeConceptMappingByDefinedSubject(opUnion.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.initMapNodeConceptMappingByDefinedSubject(opFilter.getSubOp());
		}

	}

	private void initMapNodeConceptMappingByDefinedObject(Op op)
	throws R2OTranslationException {
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				//System.out.println("tp = " + tp);
				Node predicate = tp.getPredicate();
				String predicateURI = predicate.getURI();
				if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
					Node subject = tp.getSubject();
					R2OConceptMapping cmSubject = mapNodeConceptMapping.get(subject);

					Node object = tp.getObject();
					R2OConceptMapping cmObject = mapNodeConceptMapping.get(object);

					if(cmSubject == null && cmObject != null) { //known object type only
						String rangeConcept = cmObject.getConceptName();
						Collection<R2ORelationMapping> rms = 
							this.mappingDocument.getRelationMappingsByPropertyURI(predicateURI);

						if(rms == null || rms.size() == 0) {
							throw new R2OTranslationException("Undefined relation mapping : " + predicateURI + " with domain " + rangeConcept);
						} if(rms.size() > 1) {
							logger.warn("Multiple defined property mappings : " + predicateURI + " with domain " + rangeConcept);
							logger.warn("Only the first one obtained is being used.");
							//throw new R2OTranslationException("Multiple defined property mappings : " + predicateURI);
						}
						R2OPropertyMapping rm = rms.iterator().next();
						R2OConceptMapping domainConceptMapping = rm.getParent();

						logger.info("Type of : " + subject + " = " + domainConceptMapping.getConceptName());
						this.mapNodeConceptMapping.put(subject, domainConceptMapping);

					}
				}
			}
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.initMapNodeConceptMappingByDefinedObject(opLeftJoin.getLeft());
			this.initMapNodeConceptMappingByDefinedObject(opLeftJoin.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.initMapNodeConceptMappingByDefinedObject(opJoin.getLeft());
			this.initMapNodeConceptMappingByDefinedObject(opJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.initMapNodeConceptMappingByDefinedObject(opUnion.getLeft());
			this.initMapNodeConceptMappingByDefinedObject(opUnion.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.initMapNodeConceptMappingByDefinedObject(opFilter.getSubOp());
		}

		//logger.info("translator.mapPropertyMappings = " + this.getMapPropertyMappings());	
	}

	private void initMapNodeConceptMappingByPredicateURI(Op op) throws R2OTranslationException {

		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			BasicPattern bp = bgp.getPattern();
			List<Triple> bpTriples = bp.getList();
			for(Triple tp : bpTriples) {
				//System.out.println("tp = " + tp);
				Node predicate = tp.getPredicate();
				String predicateURI = predicate.getURI();
				if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
					Node subject = tp.getSubject();
					R2OConceptMapping cmSubject = mapNodeConceptMapping.get(subject);

					Node object = tp.getObject();
					R2OConceptMapping cmObject = mapNodeConceptMapping.get(object);

					if(cmSubject == null && cmObject == null) //unknown subject nor object 
					{
						Collection<AbstractPropertyMapping> pms = this.mappingDocument.getPropertyMappingsByPropertyURI(predicateURI);
						if(pms == null || pms.size() == 0) {
							throw new R2OTranslationException("Undefined property mapping : " + predicateURI);
						} if(pms.size() > 1) {
							logger.warn("Multiple defined property mappings : " + predicateURI);
							logger.warn("Only the first one obtained is being used.");
							//throw new R2OTranslationException("Multiple defined property mappings : " + predicateURI);
						}
						R2OPropertyMapping pm = (R2OPropertyMapping) pms.iterator().next();
						R2OConceptMapping subjectConceptMapping = pm.getParent();

						logger.info("Type of : " + subject + " = " + subjectConceptMapping.getConceptName());
						this.mapNodeConceptMapping.put(subject, subjectConceptMapping);
						if(pm instanceof R2ORelationMapping) {
							R2ORelationMapping rm = (R2ORelationMapping) pm;
							String rangeConceptMappingID = rm.getToConcept();
							R2OConceptMapping rangeConceptMapping = 
								this.mappingDocument.getConceptMappingByConceptMappingId(rangeConceptMappingID);

							logger.info("Type of : " + object + " = " + rangeConceptMapping.getConceptName());
							this.mapNodeConceptMapping.put(object, rangeConceptMapping);
						}
					}
				}
			}
			//logger.info("translator.mapPropertyMappings = " + this.getMapPropertyMappings());
		} else if(op instanceof OpLeftJoin) {
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			this.initMapNodeConceptMappingByPredicateURI(opLeftJoin.getLeft());
			this.initMapNodeConceptMappingByPredicateURI(opLeftJoin.getRight());
		} else if(op instanceof OpJoin) {
			OpJoin opJoin = (OpJoin) op;
			this.initMapNodeConceptMappingByPredicateURI(opJoin.getLeft());
			this.initMapNodeConceptMappingByPredicateURI(opJoin.getRight());
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			this.initMapNodeConceptMappingByPredicateURI(opUnion.getLeft());
			this.initMapNodeConceptMappingByPredicateURI(opUnion.getRight());
		} else if(op instanceof OpFilter) {
			OpFilter opFilter = (OpFilter) op;
			this.initMapNodeConceptMappingByPredicateURI(opFilter.getSubOp());
		}
	}


	public static Collection<Node> terms(Op op) {
		Collection<Node> result = new HashSet<Node>();

		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			result = TranslatorUtility.terms(bgp);
		} else if(op instanceof OpLeftJoin ) {
			OpLeftJoin leftJoin = (OpLeftJoin) op;
			result.addAll(terms(leftJoin.getLeft()));
			result.addAll(terms(leftJoin.getRight()));
		} else if(op instanceof OpJoin ) {
			OpJoin opJoin = (OpJoin) op;
			result.addAll(terms(opJoin.getLeft()));
			result.addAll(terms(opJoin.getRight()));			
		} else if(op instanceof OpFilter) {
			OpFilter filter = (OpFilter) op;
			result.addAll(terms(filter.getSubOp()));
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			result.addAll(terms(opUnion.getLeft()));
			result.addAll(terms(opUnion.getRight()));
		}

		return result;
	}

	public static Collection<Node> terms(OpBGP bgp) {
		List<Triple> triples = bgp.getPattern().getList();
		return TranslatorUtility.terms(triples);
	}

	public static Collection<Node> terms(Collection<Triple> triples) {
		Collection<Node> result = new HashSet<Node>();

		for(Triple tp : triples) {
			result.addAll(TranslatorUtility.terms(tp));
		}

		return result;
	}

	public static Set<Node> terms(Triple tp) {
		Set<Node> result = new HashSet<Node>();
		Node subject = tp.getSubject();
		if(subject.isURI() || subject.isBlank() || subject.isLiteral() || subject.isVariable()) {
			result.add(subject);
		}

		Node predicate = tp.getPredicate();
		if(predicate.isURI() || predicate.isBlank() || predicate.isLiteral() || predicate.isVariable()) {
			result.add(predicate);
		}

		Node object = tp.getObject();
		if(object.isURI() || object.isBlank() || object.isLiteral() || object.isVariable()) {
			result.add(object);
		}

		return result;
	}


	public static ZSelectItem generateCoalesceSelectItem(ZSelectItem selectItem1, ZSelectItem selectItem2, String alias) {

		ZExpression expression = new ZExpression("coalesce");

		String selectItem1Alias = selectItem1.getAlias();
		selectItem1.setAlias("");
		expression.addOperand(new ZConstant(selectItem1.toString(), ZConstant.COLUMNNAME));
		if(selectItem1Alias != null) {selectItem1.setAlias(selectItem1Alias);}

		String selectItem2Alias = selectItem2.getAlias();
		selectItem2.setAlias("");
		expression.addOperand(new ZConstant(selectItem2.toString(), ZConstant.COLUMNNAME));
		if(selectItem2Alias != null) {selectItem2.setAlias(selectItem2Alias);}

		ZSelectItem result = new ZSelectItem();
		result.setExpression(expression);
		result.setAlias(alias);

		return result;
	}

	public static ZSelectItem generateCoalesceSelectItem(Node c, String r1, String r2, NameGenerator nameGenerator) {
		String nameC = nameGenerator.generateName(c);
		ZExpression expression = new ZExpression("coalesce");

		ZConstant operand1 = new ZConstant(r1 + "." + nameC, ZConstant.COLUMNNAME);
		expression.addOperand(operand1);
		ZConstant operand2 = new ZConstant(r2 + "." + nameC, ZConstant.COLUMNNAME);
		expression.addOperand(operand2);

		ZSelectItem result = new ZSelectItem();
		result.setExpression(expression);
		result.setAlias(nameC);

		return result;
	}

	public static boolean isTriplePattern(OpBGP op) {
		int triplesSize = ((OpBGP) op).getPattern().getList().size();
		if(triplesSize == 1) {
			return true;
		} 
		return false;
	}
	
	public static boolean isTripleBlock(List<Triple> triples) {
		if(triples.size() <= 1) {
			return false;
		} else {
			String prevSubject = triples.get(0).getSubject().toString();
			String currSubject;
			for(int i=1; i<triples.size(); i++) {
				currSubject = triples.get(i).getSubject().toString();
				if(!prevSubject.equals(currSubject)) {
					return false;
				} else {
					prevSubject = triples.get(i).getSubject().toString();;
				}
			}
			return true;
		}
		
	}
	
	public static boolean isTripleBlock(OpBGP bgp) {
		List<Triple> triples = bgp.getPattern().getList();
		return TranslatorUtility.isTripleBlock(triples);
	}
	
	public static boolean isTripleBlock(Op op) {
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			return TranslatorUtility.isTripleBlock(bgp);
		} else {
			return false;
		}
	}
	
	public static int getFirstTBEndIndex(List<Triple> triples) {
		int result = 1;
		for(int i=0; i<triples.size(); i++) {
			List<Triple> sublist = triples.subList(0, i);
			if(TranslatorUtility.isTripleBlock(sublist)) {
				result = i;
			}
		}
		
		return result;
	}
	
	public static R2ORelationMapping processObjectPredicateObjectURI(
			Node object , R2ORelationMapping rm, R2OMappingDocument md) 
	throws R2OTranslationException
	{
		String toConcept = rm.getToConcept();
		R2OConceptMapping rangeConceptMapping = 
			(R2OConceptMapping) md.getConceptMappingByConceptMappingId(toConcept);
		R2OTransformationExpression rangeUriAs = rangeConceptMapping.getURIAs();
		R2OTransformationRestriction tr = new R2OTransformationRestriction(rangeUriAs);
		R2OArgumentRestriction rangeUriAsArgumentRestriction = new R2OArgumentRestriction(tr);

		R2OConstantRestriction cr = new R2OConstantRestriction();
		cr.setConstantValue(object.getURI());
		R2OArgumentRestriction objectAsArgumentRestriction = new R2OArgumentRestriction(cr);

		List<R2OArgumentRestriction> argRestrictions = new ArrayList<R2OArgumentRestriction>();
		argRestrictions.add(rangeUriAsArgumentRestriction);
		argRestrictions.add(objectAsArgumentRestriction);
		R2OCondition condition = new R2OCondition(R2OConstants.CONDITION_TAG
				, argRestrictions, R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME);

		R2OJoin joinVia = rm.getJoinsVia();

		R2OJoin joinVia2 = joinVia.clone();
		R2OConditionalExpression joinsViaCE1 = joinVia.getJoinConditionalExpression();
		R2OConditionalExpression joinsViaCE2 = 
			R2OConditionalExpression.addCondition(joinsViaCE1, R2OConstants.AND_TAG, condition);
		joinVia2.setJoinConditionalExpression(joinsViaCE2);

		R2ORelationMapping rm2 = rm.clone();
		rm2.setJoinsVia(joinVia2);

		return rm2;
	}
}
