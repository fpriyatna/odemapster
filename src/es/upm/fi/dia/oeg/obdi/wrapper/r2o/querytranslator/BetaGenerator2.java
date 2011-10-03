package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2SQLTranslator.POS;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2ORelationMappingUnfolder;

public class BetaGenerator2 extends AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(BetaGenerator2.class);

	public BetaGenerator2(Map<Node, Collection<R2OConceptMapping>> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
		// TODO Auto-generated constructor stub
	}	

	@Override
	ZSelectItem calculateBeta(Triple tp, POS pos) throws Exception {
		Node subject = tp.getSubject();
		String predicateURI = tp.getPredicate().getURI();
		ZSelectItem selectItem = null;

		Collection<R2OConceptMapping> cms = this.mapNodeConceptMapping.get(subject);
		R2OConceptMapping cm = cms.iterator().next();

		if(pos == POS.sub) {
			Collection<ZSelectItem> selectItems = cm.getURIAs().getSelectItems();
			if(selectItems.size() > 1) {
				String newErrorMessage = "multiple columns in uri-as element in query translator is not supported yet.";
				logger.warn(newErrorMessage);
				throw new Exception(newErrorMessage);				
			}

			selectItem = selectItems.iterator().next();
		} else if(pos == POS.pre) {
			ZConstant predicateURIConstant = new ZConstant(predicateURI, ZConstant.STRING);
			selectItem = new ZSelectItem();
			selectItem.setExpression(predicateURIConstant);
		} else if(pos == POS.obj) {
			if(RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
				ZConstant conceptNameConstant = new ZConstant(cm.getConceptName(), ZConstant.STRING);
				selectItem = new ZSelectItem();
				selectItem.setExpression(conceptNameConstant);
			} else {
				List<R2OPropertyMapping> pms = cm.getPropertyMappings(predicateURI);
				if(pms != null) {
					for(R2OPropertyMapping pm : pms) {
						if(pm instanceof R2OAttributeMapping) {
							R2OAttributeMapping am = (R2OAttributeMapping) pm;
							Collection<R2OSelector> selectors = am.getSelectors();
							try {
								if(selectors != null){
									if(selectors.size() != 1) {
										String newErrorMessage = "multiple selectors in query translator is not supported yet.";
										logger.warn(newErrorMessage);
										throw new Exception(newErrorMessage);
									}

									R2OSelector selector = selectors.iterator().next();
									R2OConditionalExpression selectorAppliesIf = selector.getAppliesIf();
									if(selectorAppliesIf != null) {
										String newErrorMessage = "conditional expression in query translator is not supported yet.";
										logger.warn(newErrorMessage);
										throw new Exception(newErrorMessage);
									}

									R2OTransformationExpression selectorAfterTransform = selector.getAfterTransform();
									if(!selectorAfterTransform.isDelegableTransformationExpression()) {
										String newErrorMessage = "non delegable conditional expression in query translator is not supported yet.";
										logger.warn(newErrorMessage);
										throw new Exception(newErrorMessage);
									} 

									Collection<ZSelectItem> selectItems = selectorAfterTransform.getSelectItems();
									if(selectItems.size() > 1) {
										String newErrorMessage = "multiple columns in after transform element in query translator is not supported yet.";
										logger.warn(newErrorMessage);
										throw new Exception(newErrorMessage);
									}

									selectItem = selectItems.iterator().next();
								}
							}
							catch(Exception e) {
								e.printStackTrace();
								String newErrorMessage = e.getMessage() + " while processing attribute mapping " + pm.getName();
								logger.error(newErrorMessage);
								throw new Exception(newErrorMessage);
							}

						} else if(pm instanceof R2ORelationMapping) {
							try {
								R2ORelationMapping rm = (R2ORelationMapping) pm;
								R2OConceptMapping rangeCM = 
										(R2OConceptMapping) this.mappingDocument.getConceptMappingById(rm.getToConcept());
								Collection<ZSelectItem> selectItems = rangeCM.getURIAs().getSelectItems();
								if(selectItems.size() > 1) {
									String newErrorMessage = "multiple columns in range uri-as in query translator is not supported yet!";
									logger.error(newErrorMessage);
									throw new Exception(newErrorMessage);									
								}

								selectItem = selectItems.iterator().next();
							} catch(Exception e) {
								String newErrorMessage = e.getMessage() + " while processing relation mapping " + pm.getName();
								logger.error(newErrorMessage);
								throw e;
							}

						}
					}			
				}
			}

		}

		return selectItem;
	}

	@Override
	ZSelectItem calculateBetaCM(Triple tp, POS pos, R2OConceptMapping cm)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


}
