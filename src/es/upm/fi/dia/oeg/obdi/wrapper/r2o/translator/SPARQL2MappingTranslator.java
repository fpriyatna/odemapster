package es.upm.fi.dia.oeg.obdi.wrapper.r2o.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.tdb.store.Hash;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OJoin;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.MergeException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ORelationMapping;

public class SPARQL2MappingTranslator {
	private static Logger logger = Logger.getLogger(SPARQL2MappingTranslator.class);

	private R2OMappingDocument mappingDocument;
	private Query query;

	private Map<Node, R2OConceptMapping> mapNodeConceptMapping;
	private Map<Node, R2OPropertyMapping> mapPredicatePropertyMapping;
	//private Map<Node, R2OConceptMapping> mapObjectMappings;


	public SPARQL2MappingTranslator(R2OMappingDocument mappingDocument,
			Query query) {
		super();
		this.mappingDocument = mappingDocument;
		this.query = query;
	}

	public R2OMappingDocument processQuery() throws R2OTranslationException, MergeException {
		R2OMappingDocument mappingDocumentResult = null;
		Op op = Algebra.compile(query) ;
		if(op instanceof OpProject) {
			OpProject opProject = (OpProject) op;
			Op opProjectSubOp = opProject.getSubOp();

			if(opProjectSubOp instanceof OpBGP) {
				OpBGP opbg = (OpBGP) opProjectSubOp;
				Collection<R2OConceptMapping> cmsTranslated = this.processBGP(opbg);

				mappingDocumentResult = new R2OMappingDocument(cmsTranslated);

				//add range of relation mappings if not defined in the translated mapping document
				Collection<R2ORelationMapping> rms = 
					mappingDocumentResult.getR2ORelationMappings();
				for(R2ORelationMapping rm : rms) {
					String toConceptID = rm.getToConcept();
					R2OConceptMapping cmRange = mappingDocumentResult.getConceptMappingsByMappingId(toConceptID);
					R2OConceptMapping cmRangeStripped;
					if(cmRange == null) {
						cmRange = this.mappingDocument.getConceptMappingsByMappingId(toConceptID);
						cmRangeStripped = cmRange.getStripped();
						cmRangeStripped.setMaterialize(R2OConstants.STRING_FALSE);
					} else {
						cmRangeStripped = cmRange.getStripped();
					}
					mappingDocumentResult.addConceptMapping(cmRangeStripped);
				}
			}
		} else {
			throw new R2OTranslationException("Unsupported query!");
		}

		//logger.debug("mappingDocumentResult = \n" + mappingDocumentResult);
		
		Collection<String> distinctCMNames = 
			mappingDocumentResult.getDistinctConceptMappingsNames();
		R2OMappingDocument mappingDocumentResultDistinct = new R2OMappingDocument();
		for(String distinctCMName : distinctCMNames) {
			Collection<R2OConceptMapping> cms =  
				mappingDocumentResult.getR2OConceptMappings(distinctCMName);
			R2OConceptMapping mergedCM = R2OConceptMapping.merge(cms);
			mappingDocumentResultDistinct.addConceptMapping(mergedCM);
		}
		
		//logger.debug("mappingDocumentResultDistinct = \n" + mappingDocumentResultDistinct);
		return mappingDocumentResultDistinct;
	}





	private void initMapNodeConceptMappingByRDFType(OpBGP bgp) throws R2OTranslationException {
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
					this.mappingDocument.getConceptMappingsByConceptURI(subjectType);
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
	}



	private void initMapNodeConceptMappingByPredicateURI(OpBGP bgp) throws R2OTranslationException {
		BasicPattern bp = bgp.getPattern();
		List<Triple> bpTriples = bp.getList();
		for(Triple tp : bpTriples) {
			//System.out.println("tp = " + tp);
			Node predicate = tp.getPredicate();
			String predicateURI = predicate.getURI();
			if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
				Node subject = tp.getSubject();
				R2OConceptMapping cmSubject = this.mapNodeConceptMapping.get(subject);

				Node object = tp.getObject();
				R2OConceptMapping cmObject = this.mapNodeConceptMapping.get(object);

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
							this.mappingDocument.getConceptMappingsByMappingId(rangeConceptMappingID);

						logger.info("Type of : " + object + " = " + rangeConceptMapping.getConceptName());
						this.mapNodeConceptMapping.put(object, rangeConceptMapping);
					}
				}
			}
		}
		//logger.info("translator.mapPropertyMappings = " + this.getMapPropertyMappings());
	}



	private Collection<R2OConceptMapping> processBGP(OpBGP bgp) throws R2OTranslationException {
		Collection<R2OConceptMapping> result = new ArrayList<R2OConceptMapping>();

		this.mapNodeConceptMapping = new HashMap<Node, R2OConceptMapping>();
		this.mapPredicatePropertyMapping = new HashMap<Node, R2OPropertyMapping>();

		//to get a type of a node, 

		//1. by explicit rdf type
		this.initMapNodeConceptMappingByRDFType(bgp);

		int mapNodeCMSizeBeforeIteration = this.mapNodeConceptMapping.size();
		int mapNodeCMSizeAfterIteration = this.mapNodeConceptMapping.size();
		do {
			mapNodeCMSizeBeforeIteration = this.mapNodeConceptMapping.size();
			//2. object type detection with defined subject
			this.initMapNodeConceptMappingByDefinedSubject(bgp);

			//3. subject type detection with defined object
			this.initMapNodeConceptMappingByDefinedObject(bgp);
			mapNodeCMSizeAfterIteration = this.mapNodeConceptMapping.size();			
		} while(mapNodeCMSizeBeforeIteration != mapNodeCMSizeAfterIteration);

		//4. subject and object type detection of predicate
		this.initMapNodeConceptMappingByPredicateURI(bgp);




		BasicPattern bp = bgp.getPattern();
		List<Triple> bpTriples = bp.getList();
		for(Triple tp : bpTriples) {
			logger.debug("tp = " + tp);
			try {
				//if(!RDF.type.getURI().equalsIgnoreCase(tp.getPredicate().getURI())) {
				R2OConceptMapping cmTranslated = this.processTriplePattern(tp);
//				logger.debug("cmTranslated = \n" + cmTranslated);
				result.add(cmTranslated);
				//}	
			} catch(R2OTranslationException e) {
				logger.error("Error processing tp : " + tp);
				throw e;
			} catch(Exception e) {
				logger.error("Error processing tp : " + tp);
				throw new R2OTranslationException(e.getMessage(), e);
			}
		}


		return result;

	}



	private void initMapNodeConceptMappingByDefinedSubject(OpBGP bgp) throws R2OTranslationException {
		BasicPattern bp = bgp.getPattern();
		List<Triple> bpTriples = bp.getList();
		for(Triple tp : bpTriples) {
			//System.out.println("tp = " + tp);
			Node predicate = tp.getPredicate();
			String predicateURI = predicate.getURI();
			if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
				Node subject = tp.getSubject();
				R2OConceptMapping cmSubject = this.mapNodeConceptMapping.get(subject);

				Node object = tp.getObject();
				R2OConceptMapping cmObject = this.mapNodeConceptMapping.get(object);

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
						R2OConceptMapping rangeConceptMapping = this.mappingDocument.getConceptMappingsByMappingId(rangeConceptMappingID);

						logger.info("Type of : " + object + " = " + rangeConceptMapping.getConceptName());
						this.mapNodeConceptMapping.put(object, rangeConceptMapping);
					}
				}
			}
		}
	}

	private void initMapNodeConceptMappingByDefinedObject(OpBGP bgp) throws R2OTranslationException {
		BasicPattern bp = bgp.getPattern();
		List<Triple> bpTriples = bp.getList();
		for(Triple tp : bpTriples) {
			//System.out.println("tp = " + tp);
			Node predicate = tp.getPredicate();
			String predicateURI = predicate.getURI();
			if(!RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
				Node subject = tp.getSubject();
				R2OConceptMapping cmSubject = this.mapNodeConceptMapping.get(subject);

				Node object = tp.getObject();
				R2OConceptMapping cmObject = this.mapNodeConceptMapping.get(object);

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
		//logger.info("translator.mapPropertyMappings = " + this.getMapPropertyMappings());	
	}

	private R2OConceptMapping processTriplePattern(Triple triple) throws R2OTranslationException {

		Node subject = triple.getSubject();
		Node predicate = triple.getPredicate();
		String predicateURI = predicate.getURI();


		R2OConceptMapping cmFull = this.processSubject(subject);
		if(RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
			return cmFull.getStripped();
		} else {
			R2OConceptMapping cmStripped = cmFull.getStripped();

			cmStripped = this.processPredicateObject(
					triple.getPredicate(), triple.getObject(), cmFull, cmStripped);
			return cmStripped;
		}
	}

	private R2OConceptMapping processSubject(Node subject) throws R2OTranslationException {
		R2OConceptMapping result = null;

		if(subject.isVariable()) {
			if(this.mapNodeConceptMapping.get(subject) != null) {
				result = this.mapNodeConceptMapping.get(subject);
			}			
		} else if(subject.isURI()) {
			R2OConceptMapping cm = this.mapNodeConceptMapping.get(subject);
			result = cm.clone();
			R2OTransformationExpression cm2URIAs = result.getURIAs();
			R2OConditionalExpression cm2AppliesIf = result.getAppliesIf();
			//			logger.debug("cm2URIAs = " + cm2URIAs);
			//			logger.debug("cm2AppliesIf = " + cm2AppliesIf);

			R2OConstantRestriction cr = new R2OConstantRestriction();
			cr.setConstantValue(subject.getURI());
			R2OArgumentRestriction subjectAR = new R2OArgumentRestriction(cr);

			R2OTransformationRestriction tr = new R2OTransformationRestriction(cm2URIAs);
			R2OArgumentRestriction uriAsAR = new R2OArgumentRestriction(tr);


			List<R2OArgumentRestriction> argRestrictions = new ArrayList<R2OArgumentRestriction>();
			argRestrictions.add(subjectAR);
			argRestrictions.add(uriAsAR);
			R2OCondition condition = new R2OCondition(R2OConstants.CONDITION_TAG
					, argRestrictions, R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME);
			//logger.debug("condition = " + condition);

			R2OConditionalExpression cm2AppliesIf2 = R2OConditionalExpression.addCondition(
					cm2AppliesIf, R2OConstants.AND_TAG, condition);
			result.setAppliesIf(cm2AppliesIf2);
			//			logger.debug("cm.appliesif = \n" + cm.getAppliesIf());
			//			logger.debug("result.appliesif = \n" + result.getAppliesIf());

		} else if(subject.isLiteral()) {
			throw new R2OTranslationException("Unsupported literal as subject.");
		}

		return result;
	}


	private R2OConceptMapping processPredicateObject(Node predicate, Node object
			, R2OConceptMapping cmFull, R2OConceptMapping cmStripped) throws R2OTranslationException {

		//		logger.debug("cmFull = \n" + cmFull);
		String predicateURI = predicate.getURI();
		Collection<R2OPropertyMapping> propertyMappings = 
			cmFull.getPropertyMappings(predicateURI);


		if(propertyMappings == null || propertyMappings.size() == 0) {
			String errorMessage = "Undefined property mapping : " + predicateURI + " in " + cmFull.getConceptName();
			logger.error(errorMessage);
			throw new R2OTranslationException(errorMessage);
		} else if(propertyMappings.size() > 1) {
			logger.warn("Multiple defined property mappings : " + predicateURI);
			logger.warn("Only the first one obtained is being used.");
		}


		R2OPropertyMapping pm = propertyMappings.iterator().next();
		if(pm instanceof R2OAttributeMapping) {
			R2OAttributeMapping am = (R2OAttributeMapping) pm;
			cmStripped = this.processDataPropertyObject(object, am, cmStripped);
			return cmStripped;
		} else if(pm instanceof R2ORelationMapping) {
			R2ORelationMapping rm = (R2ORelationMapping) pm;
			R2ORelationMapping rm2 = this.processObjectPropertyObject(
					predicate, object, cmFull, rm);
			cmStripped.addRelationMapping(rm2);
			return cmStripped;
		} else {
			throw new R2OTranslationException("Invalid property mapping type!");
		}

	}

	private R2OConceptMapping processDataPropertyObject(
			Node object, R2OAttributeMapping am, R2OConceptMapping cmStripped)
	throws R2OTranslationException {

		if(object.isLiteral()) {
			cmStripped = this.processDataPropertyObjectLiteral(object, am, cmStripped);
			return cmStripped;
		} else if(object.isVariable()) {
			cmStripped.addAttributeMapping(am);
			return cmStripped;
		} else {
			String errorMessage = "Invalid object of data property, object = " + object;
			throw new R2OTranslationException(errorMessage);
		}
	}

	/*
	private R2OAttributeMapping processDataPropertyObjectVar(Node predicate, Node object
			, R2OConceptMapping cmFull, R2OConceptMapping cmStripped) throws R2OTranslationException {

		String predicateURI = predicate.getURI();
		Collection<R2OAttributeMapping> attributeMappings = 
			cmFull.getAttributeMappings(predicateURI);

		if(attributeMappings == null || attributeMappings.size() == 0) {
			throw new R2OTranslationException("Undefined property mapping : " + predicateURI);
		} else if(attributeMappings.size() > 1) {
			logger.warn("Multiple defined property mappings : " + predicateURI);
			logger.warn("Only the first one obtained is being used.");
		}

		R2OAttributeMapping am = attributeMappings.iterator().next();
		return am;
	}
	 */

	private R2OConceptMapping processDataPropertyObjectLiteral(
			Node object, R2OAttributeMapping am, R2OConceptMapping cmStripped) throws R2OTranslationException {

		String attributeName = am.getAttributeName();
		//R2OAttributeMapping am2 = am.clone();

		Collection<R2OSelector> selectors = am.getSelectors();
		if(selectors == null || selectors.size() == 0) {
			throw new R2OTranslationException("Undefined selectors in the attribute mapping : " + attributeName);
		} else if(selectors.size() > 1) {
			logger.warn("Multiple defined selectors in the attribute mapping : " + attributeName);
			logger.warn("Only the first one obtained is being used.");
		}

		R2OSelector selector = selectors.iterator().next();
		//R2OSelector selector2 = selector.clone();

		R2OTransformationExpression selectorAfterTransform = selector.getAfterTransform();
		R2OTransformationRestriction tr = new R2OTransformationRestriction(selectorAfterTransform);
		R2OArgumentRestriction selectorAfterTransformRestriction = new R2OArgumentRestriction(tr);

		R2OConstantRestriction cr = new R2OConstantRestriction();
		cr.setConstantValue(object.getLiteralValue());
		R2OArgumentRestriction objectAsArgumentRestriction = new R2OArgumentRestriction(cr);



		List<R2OArgumentRestriction> argRestrictions = new ArrayList<R2OArgumentRestriction>();
		argRestrictions.add(selectorAfterTransformRestriction);
		argRestrictions.add(objectAsArgumentRestriction);
		R2OCondition condition = new R2OCondition(R2OConstants.CONDITION_TAG
				, argRestrictions, R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME);

		/*
		R2OConditionalExpression selectorAppliesIf = selector.getAppliesIf();
		R2OConditionalExpression newSelectorAppliesIf = R2OConditionalExpression.addCondition(
				selectorAppliesIf, R2OConstants.AND_TAG, condition);
		am2.getSelectors().iterator().next().setAppliesIf(newSelectorAppliesIf);
		 */

		R2OConditionalExpression cmStrippedAI = cmStripped.getAppliesIf();
		R2OConditionalExpression cmStrippedAI2 = R2OConditionalExpression.addCondition(cmStrippedAI, R2OConstants.AND_TAG, condition);
		cmStripped.setAppliesIf(cmStrippedAI2);



		return cmStripped;
	}


	private R2ORelationMapping processObjectPropertyObject(Node predicate, Node object
			, R2OConceptMapping cmFull, R2ORelationMapping rm) 
	throws R2OTranslationException {

		if(object.isURI()) {
			R2ORelationMapping rm2 = this.processObjectPredicateObjectURI(object, rm);
			return rm2;
			//logger.debug("rm2 = \n" + rm2); 
		} else if(object.isVariable()) {
			String predicateURI = predicate.getURI();

			Collection<R2ORelationMapping> relationMappings = 
				cmFull.getRelationMappings(predicateURI);

			if(relationMappings == null || relationMappings.size() == 0) {
				throw new R2OTranslationException("Undefined property mapping : " + predicateURI);
			} else if(relationMappings.size() > 1) {
				logger.warn("Multiple defined property mappings : " + predicateURI);
				logger.warn("Only the first one obtained is being used.");
			}
			R2ORelationMapping rm2 = relationMappings.iterator().next();
			return rm2;
		} else {
			throw new R2OTranslationException("Unsupported object type.");
		}
	}

	private R2ORelationMapping processObjectPredicateObjectURI(
			Node object , R2ORelationMapping rm) 
	throws R2OTranslationException
	{
		String toConcept = rm.getToConcept();
		R2OConceptMapping rangeConceptMapping = 
			(R2OConceptMapping) this.mappingDocument.getConceptMappingsByMappingId(toConcept);
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

	public Map<Node, R2OConceptMapping> getMapSubjectMapping() {
		return mapNodeConceptMapping;
	}

	public Map<Node, R2OPropertyMapping> getMapPropertyMappings() {
		return mapPredicatePropertyMapping;
	}



}
