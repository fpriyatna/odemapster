package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZQuery;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class AlphaGenerator3 extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(AlphaGenerator3.class);

	public AlphaGenerator3(Map<Node, R2OConceptMapping> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}

	@Override
	ZQuery calculateAlpha(Triple tp) throws Exception {
		R2OConceptMapping cmStripped = this.calculateAlphaCM(tp);
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cmStripped, this.mappingDocument);
		R2OQuery cmQuery = cmu.unfoldConceptMapping();
		return cmQuery;
	}

	R2OConceptMapping calculateAlphaCM(Triple tp) throws Exception {
		Node subject = tp.getSubject();
		R2OConceptMapping cm = this.mapNodeConceptMapping.get(subject);
		R2OConceptMapping cmStripped = cm.getStripped();

		//mapping selection for corresponding subject value
		cmStripped = this.calculateAlphaAux(subject, cmStripped);

		//mapping projection of the predicate
		Node predicate = tp.getPredicate();
		Collection<R2OPropertyMapping> pms = cm.getPropertyMappings(predicate.getURI());
		for(R2OPropertyMapping pm : pms) {
			R2OPropertyMapping pm2 = pm.clone();
			cmStripped.addPropertyMapping(pm2);
			//			cmStripped.addPropertyMapping(pm);
		}


		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cmStripped, this.mappingDocument);
		R2OQuery cmQuery = cmu.unfoldConceptMapping();

		return cmStripped;
	}

	@Override
	R2OConceptMapping calculateAlphaCMTB(Collection<Triple> triples)
	throws Exception {
		Triple firstTriple = triples.iterator().next();
		Node subject = firstTriple.getSubject();
		R2OConceptMapping cm = this.mapNodeConceptMapping.get(subject);
		R2OConceptMapping cmStripped = cm.getStripped();

		//mapping selection for corresponding subject value
		cmStripped = this.calculateAlphaAux(subject, cmStripped);

		//mapping projection of corresponding predicates
		for(Triple tp : triples) {
			Node predicate = tp.getPredicate();
			Node object = tp.getObject();
			Collection<R2OPropertyMapping> pms = cm.getPropertyMappings(predicate.getURI());
			for(R2OPropertyMapping pm : pms) {
				this.calculateAlphaAux2(pm, object, cmStripped);
				//cmStripped.addPropertyMapping(pm.clone());
				//				cmStripped.addPropertyMapping(pm);
			}
		}

		return cmStripped;
	}

	private R2OConceptMapping calculateAlphaAux(Node subject, R2OConceptMapping cm) {
		//mapping selection for corresponding literal subject
		if(subject.isURI()) {
			R2OArgumentRestriction subjectAR;
			R2OConstantRestriction cr;
			R2OArgumentRestriction uriAsAR;
			R2OTransformationExpression cmURIAs = cm.getURIAs();
			if(Utility.isWellDefinedURIExpression(cmURIAs)) {
				R2OColumnRestriction cr2 = (R2OColumnRestriction) cmURIAs.getLastRestriction();
				uriAsAR = new R2OArgumentRestriction(cr2);				
				String pkDataType = cr2.getDatabaseColumn().getDataType();
				
				cr = new R2OConstantRestriction();
				String subjectURI = subject.getURI();
				int subjectURILengthWithoutPK = Utility.getIRILengthWithoutPK(cmURIAs);
				String subjectURIWithoutPK = subjectURI.substring(0, subjectURILengthWithoutPK);
				String subjectPKOnly = subjectURI.substring(subjectURIWithoutPK.length(), subjectURI.length()); 
				cr.setConstantValue(subjectPKOnly);
				if(pkDataType != null && !pkDataType.equals("")) {
					cr.setDatatype(pkDataType);
				}
			} else {
				cr = new R2OConstantRestriction();
				cr.setConstantValue(subject.getURI());
				
				R2OTransformationRestriction tr = new R2OTransformationRestriction(cmURIAs);
				uriAsAR = new R2OArgumentRestriction(tr);
			}
			subjectAR = new R2OArgumentRestriction(cr);
			List<R2OArgumentRestriction> argRestrictions = new ArrayList<R2OArgumentRestriction>();
			argRestrictions.add(subjectAR);
			argRestrictions.add(uriAsAR);
			R2OCondition condition = new R2OCondition(R2OConstants.CONDITION_TAG
					, argRestrictions, R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME);
			R2OConditionalExpression appliesIf = cm.getAppliesIf();
			R2OConditionalExpression appliesIf2 = R2OConditionalExpression.addCondition(
					appliesIf, R2OConstants.AND_TAG, condition);
			cm.setAppliesIf(appliesIf2);

		}


		return cm;
	}

	private R2OConceptMapping calculateAlphaAux2(R2OPropertyMapping pm, Node object, R2OConceptMapping cm) {
		if(pm instanceof R2OAttributeMapping) {
			R2OAttributeMapping am = (R2OAttributeMapping) pm;
			cm.addAttributeMapping(am.clone());
			//if attribute mapping with literal object
			// then do mapping selection

		} else if(pm instanceof R2ORelationMapping) {
			R2ORelationMapping rm = (R2ORelationMapping) pm;
			// if relation mapping with uri object
			// then append conditional expression in the relation mapping
			if(object.isURI()) {
				try {
//					cm.addRelationMapping(rm.clone());

					R2ORelationMapping rm2 = TranslatorUtility.processObjectPredicateObjectURI(object, rm, this.mappingDocument);
					cm.addRelationMapping(rm2);
				} catch(Exception e) {
					cm.addRelationMapping(rm.clone());
					logger.error("error processing relation mapping with uri object");
				}
				
			} else if(object.isVariable()) {
				cm.addRelationMapping(rm.clone());
			} else if(object.isLiteral()) {
				logger.warn("literal object is not permitted for relation mapping");
			}
		}

		return cm;
	}


	@Override
	ZQuery calculateAlphaTB(Collection<Triple> triples) throws Exception {
		R2OConceptMapping cmStripped = this.calculateAlphaCMTB(triples);
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cmStripped, this.mappingDocument);
		R2OQuery cmQuery = cmu.unfoldConceptMapping();
		return cmQuery;
	}





}
