package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZQuery;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.URIUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseView;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class AlphaGenerator3 extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(AlphaGenerator3.class);

	public AlphaGenerator3(Map<Node, Collection<R2OConceptMapping>> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}

	@Override
	ZQuery calculateAlpha(Triple tp) throws Exception {
		R2OConceptMapping cmStripped = this.calculateAlphaCM(tp);
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(
				cmStripped, this.mappingDocument);
		return cmu.unfoldConceptMapping();
	}

	R2OConceptMapping calculateAlphaCM(Triple tp) throws Exception {
		Node subject = tp.getSubject();
		
		Collection<R2OConceptMapping> cms = this.mapNodeConceptMapping.get(subject); 
		R2OConceptMapping cm = cms.iterator().next();
		R2OConceptMapping cmStripped = cm.getStripped();

		//mapping selection for corresponding subject value
		cmStripped = this.calculateAlphaSubject(subject, cmStripped);
		

		
		//mapping projection of the predicate
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();

		Collection<R2OPropertyMapping> pms = cm.getPropertyMappings(predicate.getURI());
		for(R2OPropertyMapping pm : pms) {
			cmStripped = this.calculateAlphaPredicateObject(pm, object, cmStripped);
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
		Collection<R2OConceptMapping> cms = this.mapNodeConceptMapping.get(subject);
		R2OConceptMapping cm = cms.iterator().next();
		R2OConceptMapping cmStripped = cm.getStripped();

		//mapping selection for corresponding subject value
		cmStripped = this.calculateAlphaSubject(subject, cmStripped);

		//mapping projection of corresponding predicates
		for(Triple tp : triples) {
			Node predicate = tp.getPredicate();
			Node object = tp.getObject();
			Collection<R2OPropertyMapping> pms = cm.getPropertyMappings(predicate.getURI());
			for(R2OPropertyMapping pm : pms) {
				this.calculateAlphaPredicateObject(pm, object, cmStripped);
				//cmStripped.addPropertyMapping(pm.clone());
				//				cmStripped.addPropertyMapping(pm);
			}
		}

		return cmStripped;
	}

	private R2OConceptMapping calculateAlphaSubject(Node subject, R2OConceptMapping cm) {
		R2OTransformationExpression cmURIAs = cm.getURIAs();
		
		//mapping selection for corresponding uri subject
		if(subject.isURI()) {
			String subjectURI = subject.getURI();
			R2OCondition condition = TranslatorUtility.generateEquityCondition(cmURIAs, subjectURI);
			R2OConditionalExpression appliesIf = cm.getAppliesIf();
			R2OConditionalExpression appliesIf2 = R2OConditionalExpression.addCondition(
					appliesIf, R2OConstants.AND_TAG, condition);
			cm.setAppliesIf(appliesIf2);
		}

		if(URIUtility.isWellDefinedURIExpression(cmURIAs)) {
			R2OColumnRestriction pkColumnRestriction = (R2OColumnRestriction) cmURIAs.getLastRestriction();
			R2ODatabaseColumn pkColumn = pkColumnRestriction.getDatabaseColumn();
			R2OTransformationExpression te = new R2OTransformationExpression(R2OConstants.TRANSFORMATION_OPERATOR_CONSTANT);
			R2ORestriction restriction = new R2OColumnRestriction(pkColumn);
			te.addRestriction(restriction);
			R2OSelector selector = new R2OSelector(null, te);
			String pkColumnAlias = cm.generatePKColumnAlias();
			R2OAttributeMapping pkAttributeMapping = new R2OAttributeMapping(pkColumnAlias);
			pkAttributeMapping.addSelector(selector);
			pkAttributeMapping.setMappedPKColumn(true);
			cm.addAttributeMapping(pkAttributeMapping);
		} 

		return cm;
	}

	private R2OConceptMapping calculateAlphaPredicateObject(R2OPropertyMapping pm
			, Node object, R2OConceptMapping cm) {
		if(pm instanceof R2OAttributeMapping) {
			R2OAttributeMapping am = (R2OAttributeMapping) pm;
			cm.addAttributeMapping(am.clone());
			//if attribute mapping with literal object
			// then do mapping selection

		} else if(pm instanceof R2ORelationMapping) {
			R2ORelationMapping rm = (R2ORelationMapping) pm;
			
			
			// if relation mapping with uri object
			// then append conditional expression in the relation mapping
			R2ORelationMapping rm2;
			if(object.isURI()) {
				try {
					rm2 = TranslatorUtility.processObjectPredicateObjectURI(object, rm, this.mappingDocument);
				} catch(Exception e) {
					rm2 = rm.clone();
					logger.error("error processing relation mapping with uri object");
				}
			} else if(object.isVariable()) {
				rm2 = rm.clone();
			} else if(object.isLiteral()) {
				rm2 = null;
				logger.warn("literal object is not permitted for relation mapping");
			} else {
				rm2 = null;
				logger.warn("Unknown type of object");
			}
			
			if(rm2 != null) {
				cm.addRelationMapping(rm2);
				
				String rangeConceptName = rm.getToConcept();
				R2OConceptMapping rangeConceptMapping = 
						this.mappingDocument.getConceptMappingById(rangeConceptName);
				R2OTransformationExpression rangeUriAs = rangeConceptMapping.getURIAs();
				if(URIUtility.isWellDefinedURIExpression(rangeUriAs)) {
					String pkColumnAlias = cm.generatePKColumnAlias();
//					String pkColumnAlias = rm2.generateAlias();
					Collection<R2OPropertyMapping> pms = cm.getPropertyMappings(pkColumnAlias);
					if(pms == null || pms.size() == 0) {
						R2OAttributeMapping rangePKColumnMapping = URIUtility.generatePKColumnAttributeMapping(
								rangeConceptMapping, pkColumnAlias);
						R2ODatabaseView rmHasView = rm.getHasView();
						String alias;
						if(rmHasView == null) {
							alias = null;
						} else {
							alias = rmHasView.generateAlias();
							rmHasView.setAlias(alias);
						}
						
						Collection<R2OArgumentRestriction> ars = rangePKColumnMapping.getSelectors().iterator().next().getAfterTransform().getArgRestrictions();
						String rangeTableName = rangeConceptMapping.getHasTable().getName();
						
						if(alias != null) {
							for(R2OArgumentRestriction ar : ars) {
								Utility.renameColumns(ar, rangeTableName, alias, true);
							}						
						}
						cm.addAttributeMapping(rangePKColumnMapping);					
					}
				}

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