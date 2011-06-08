package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.MergeException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;

public class SPARQL2MappingTranslator {
	private static Logger logger = Logger.getLogger(SPARQL2MappingTranslator.class);

	private R2OMappingDocument mappingDocument;

	private Map<Node, R2OConceptMapping> mapNodeConceptMapping;


	public SPARQL2MappingTranslator(R2OMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
		//this.query = query;
	}

	private Collection<R2OConceptMapping> queriesToMappings(Collection<Query> queries) throws R2OTranslationException {
		Collection<R2OConceptMapping> result = null;
		for(Query query : queries) {
			Collection<R2OConceptMapping> tempResult = this.queryToMappings(query);
			
			if(result == null) {
				result = tempResult;
			} else {
				result.addAll(tempResult);
			}
		}
		
		return result;
	}
	
	private Collection<R2OConceptMapping> queryToMappings(Query query) throws R2OTranslationException {
		Op op = Algebra.compile(query) ;
		Op opQueryPattern = Algebra.compile(query.getQueryPattern());
		//to get a type of each node,
		this.mapNodeConceptMapping = new HashMap<Node, R2OConceptMapping>();
		TranslatorUtility translatorUtility = new TranslatorUtility(mappingDocument);
		this.mapNodeConceptMapping = translatorUtility.initializeMapConceptMapping(opQueryPattern);

		
		if(op instanceof OpProject) {
			OpProject opProject = (OpProject) op;
			Op opProjectSubOp = opProject.getSubOp();

			if(opProjectSubOp instanceof OpBGP) {
				OpBGP opbg = (OpBGP) opProjectSubOp;
				Collection<R2OConceptMapping> cmsTranslated = this.processBGP(opbg);

				return cmsTranslated;
			} else {
				throw new R2OTranslationException("Unsupported query!");
			}
		} else {
			throw new R2OTranslationException("Unsupported query!");
		}


	}

	//	public R2OMappingDocument processQuery(Query query) throws R2OTranslationException, MergeException {
	//		R2OMappingDocument mappingDocumentResult = null;
	//		
	//		
	//		Op op = Algebra.compile(query) ;
	//		if(op instanceof OpProject) {
	//			OpProject opProject = (OpProject) op;
	//			Op opProjectSubOp = opProject.getSubOp();
	//
	//			if(opProjectSubOp instanceof OpBGP) {
	//				OpBGP opbg = (OpBGP) opProjectSubOp;
	//				Collection<R2OConceptMapping> cmsTranslated = this.processBGP(opbg);
	//
	//				mappingDocumentResult = new R2OMappingDocument(cmsTranslated);
	//
	//				//add range of relation mappings if not defined in the translated mapping document
	//				Collection<R2ORelationMapping> rms = 
	//					mappingDocumentResult.getR2ORelationMappings();
	//				for(R2ORelationMapping rm : rms) {
	//					String toConceptID = rm.getToConcept();
	//					R2OConceptMapping cmRange = mappingDocumentResult.getConceptMappingsByMappingId(toConceptID);
	//					R2OConceptMapping cmRangeStripped;
	//					if(cmRange == null) {
	//						cmRange = this.mappingDocument.getConceptMappingsByMappingId(toConceptID);
	//						cmRangeStripped = cmRange.getStripped();
	//						cmRangeStripped.setMaterialize(R2OConstants.STRING_FALSE);
	//					} else {
	//						cmRangeStripped = cmRange.getStripped();
	//					}
	//					mappingDocumentResult.addConceptMapping(cmRangeStripped);
	//				}
	//			}
	//		} else {
	//			throw new R2OTranslationException("Unsupported query!");
	//		}
	//
	//		//logger.debug("mappingDocumentResult = \n" + mappingDocumentResult);
	//		
	//		Collection<String> distinctCMNames = 
	//			mappingDocumentResult.getDistinctConceptMappingsNames();
	//		R2OMappingDocument mappingDocumentResultDistinct = new R2OMappingDocument();
	//		for(String distinctCMName : distinctCMNames) {
	//			Collection<R2OConceptMapping> cms =  
	//				mappingDocumentResult.getR2OConceptMappings(distinctCMName);
	//			R2OConceptMapping mergedCM = R2OConceptMapping.merge(cms);
	//			mappingDocumentResultDistinct.addConceptMapping(mergedCM);
	//		}
	//		
	//		//logger.debug("mappingDocumentResultDistinct = \n" + mappingDocumentResultDistinct);
	//		return mappingDocumentResultDistinct;
	//	}


	public R2OMappingDocument processQuery(Query query) throws R2OTranslationException, MergeException {
		R2OMappingDocument mappingDocumentResult = null;

		
		Collection<R2OConceptMapping> cmsTranslated = this.queryToMappings(query);
		
		
		mappingDocumentResult = new R2OMappingDocument(cmsTranslated);

		//add range of relation mappings if not defined in the translated mapping document
		Collection<R2ORelationMapping> rms = 
			mappingDocumentResult.getR2ORelationMappings();
		for(R2ORelationMapping rm : rms) {
			String toConceptID = rm.getToConcept();
			R2OConceptMapping cmRange = mappingDocumentResult.getConceptMappingByConceptMappingId(toConceptID);
			R2OConceptMapping cmRangeStripped;
			if(cmRange == null) {
				cmRange = this.mappingDocument.getConceptMappingByConceptMappingId(toConceptID);
				cmRangeStripped = cmRange.getStripped();
				cmRangeStripped.setMaterialize(R2OConstants.STRING_FALSE);
			} else {
				cmRangeStripped = cmRange.getStripped();
			}
			mappingDocumentResult.addConceptMapping(cmRangeStripped);
		}

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




	private Collection<R2OConceptMapping> processBGP(OpBGP bgp) throws R2OTranslationException {
		Collection<R2OConceptMapping> result = new ArrayList<R2OConceptMapping>();

		

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
			(R2OConceptMapping) this.mappingDocument.getConceptMappingByConceptMappingId(toConcept);
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




}
