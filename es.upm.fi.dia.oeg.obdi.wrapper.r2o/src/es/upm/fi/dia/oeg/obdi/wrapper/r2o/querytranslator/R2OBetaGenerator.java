package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2ORelationMappingUnfolder;

public class R2OBetaGenerator extends AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(R2OBetaGenerator.class);

	public R2OBetaGenerator(Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}




	@Override
	public SQLSelectItem calculateBetaSubject(AbstractConceptMapping cm) {
		String cmURIAlias = ((R2OConceptMapping) cm).generateURIAlias();
		ZConstant cmURIAliasConstant = new ZConstant(cmURIAlias, ZConstant.COLUMNNAME);
		SQLSelectItem selectItem = new SQLSelectItem();
		selectItem.setExpression(cmURIAliasConstant);
		return selectItem;
	}
	

	
	@Override
	public SQLSelectItem calculateBetaObject(
			AbstractConceptMapping cm, Triple tp) 
			throws QueryTranslationException {
		SQLSelectItem selectItem = null;
		Node object = tp.getObject();
		String predicateURI = tp.getPredicate().getURI();

		if(RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
			ZConstant conceptNameConstant = new ZConstant(cm.getConceptName()
					, ZConstant.STRING);
			selectItem = new SQLSelectItem();
			selectItem.setExpression(conceptNameConstant);
		} else {
			Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(predicateURI);
			if(pms != null) {
				for(AbstractPropertyMapping pm : pms) {
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
								R2OConditionalExpression attributeMappingSelectorAppliesIf = selector.getAppliesIf();
								if(attributeMappingSelectorAppliesIf != null) {
									String newErrorMessage = "conditional expression in query translator is not supported yet.";
									logger.warn(newErrorMessage);
									throw new Exception(newErrorMessage);
								}

								R2OTransformationExpression attMapSelAT = selector.getAfterTransform();
								if(!attMapSelAT.isDelegableTransformationExpression()) {
									String newErrorMessage = "non delegable conditional expression in query translator is not supported yet.";
									logger.warn(newErrorMessage);
									throw new Exception(newErrorMessage);
								} 

								String selectorAfterTranformAlias = selector.generateAfterTransformAlias();
								//selectItem = new R2OSelectItem(selectorAfterTranformAlias);
								if(object.isVariable()) {
									selectItem = new SQLSelectItem(selectorAfterTranformAlias);
								} else if(object.isLiteral()) {
									selectItem = new SQLSelectItem();
									ZExp exp = new ZConstant(selectorAfterTranformAlias, ZConstant.STRING);
									selectItem.setExpression(exp);
								} else if(object.isURI()) {
									selectItem = new SQLSelectItem();
									ZExp exp = new ZConstant(selectorAfterTranformAlias, ZConstant.STRING);
									selectItem.setExpression(exp);
								} else {
									selectItem =  new SQLSelectItem(selectorAfterTranformAlias);
								}
								
							}
						}
						catch(Exception e) {
							e.printStackTrace();
							String newErrorMessage = e.getMessage() + " while processing attribute mapping " + pm.getName();
							logger.error(newErrorMessage);
							throw new QueryTranslationException(newErrorMessage, e);
						}

					} else if(pm instanceof R2ORelationMapping) {
						try {
							R2ORelationMapping rm = (R2ORelationMapping) pm;
							R2OConceptMapping parentCM = (R2OConceptMapping) rm.getParent();
							R2ODatabaseTable r2oBaseTable = parentCM.getHasTables().get(0);
							String cmBaseTable = r2oBaseTable.getName();
							if(r2oBaseTable.getAlias() != null && r2oBaseTable.getAlias() != "") {
								cmBaseTable = r2oBaseTable.getAlias();
							}
							String rangeTableAlias = rm.getRangeTableAlias();
							
							R2OConceptMapping rangeCM = 
								(R2OConceptMapping) this.mappingDocument.getConceptMappingByMappingId(rm.getToConcept());
							R2ORelationMappingUnfolder rmUnfolder = new R2ORelationMappingUnfolder(parentCM, rm, rangeCM);
							Collection<ZSelectItem> selectItems = rmUnfolder.unfoldRangeURI(cmBaseTable,rangeTableAlias);
							if(selectItems.size() > 1) {
								String newErrorMessage = "multiple columns in range uri-as in query translator is not supported yet!";
								logger.error(newErrorMessage);
								throw new Exception(newErrorMessage);									
							}

							//String rangeURIAlias = selectItems.iterator().next().getAlias();
							String rangeURIAlias = rm.generateRangeURIAlias();
							//selectItem = new R2OSelectItem(rangeURIAlias);
							if(object.isVariable()) {
								selectItem = new SQLSelectItem(rangeURIAlias);
							} else if(object.isLiteral()) {
								selectItem = new SQLSelectItem();
								ZExp exp = new ZConstant(rangeURIAlias, ZConstant.STRING);
								selectItem.setExpression(exp);
							} else if(object.isURI()) {
								selectItem = new SQLSelectItem(rangeURIAlias);
							} else {
								selectItem =  new SQLSelectItem(rangeURIAlias);
							}
						} catch(Exception e) {
							String newErrorMessage = e.getMessage() + " while processing relation mapping " + pm.getName();
							logger.error(newErrorMessage);
							throw new QueryTranslationException(newErrorMessage, e);
						}

					}
				}			
			}
		}
		
		return selectItem;
	}
	


}
