package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;
import Zql.ZFromItem;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.URIUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;

public class R2OConceptMappingUnfolder  {
	private static Logger logger = Logger.getLogger(R2OConceptMappingUnfolder.class);
	private R2OConceptMapping conceptMapping;
	private R2OMappingDocument r2oMappingDocument;
	



	public R2OConceptMappingUnfolder(
			R2OConceptMapping conceptMapping,
			R2OMappingDocument r2oMappingDocument) {
		super();
		this.conceptMapping = conceptMapping;
		this.r2oMappingDocument = r2oMappingDocument;
	}




	public SQLQuery unfoldConceptMapping() throws Exception {
		logger.debug("Unfolding concept mapping : " + conceptMapping.getConceptName());

		SQLQuery cmQuery = new SQLQuery();
		//ZUtils.addCustomFunction("concat", 2);
		cmQuery.addFrom(new Vector<String>());
		cmQuery.addSelect(new Vector<ZSelectItem>());


		//unfolding uri-as transformation expression
		R2OTransformationExpression conceptMappingURIAsTransformationExpression = 
			conceptMapping.getURIAs();
		String uriAsOperator = conceptMappingURIAsTransformationExpression.getOperId();
		if(uriAsOperator != null) {
			if(conceptMappingURIAsTransformationExpression.isDelegableTransformationExpression()) {
				boolean isWellDefinedURIExpression = URIUtility.isWellDefinedURIExpression(conceptMappingURIAsTransformationExpression);
				logger.debug("isWellDefinedURIExpression = " + isWellDefinedURIExpression);
				
				R2OTransformationExpressionUnfolder r2oTransformationExpressionUnfolder = 
					new R2OTransformationExpressionUnfolder(conceptMappingURIAsTransformationExpression);
				ZExp selectExpression = r2oTransformationExpressionUnfolder.unfoldDelegableTransformationExpression();
				ZSelectItem zSelectItemConceptURI = new ZSelectItem();
				zSelectItemConceptURI.setExpression(selectExpression);
				String alias = conceptMapping.generateURIAlias();
				zSelectItemConceptURI.setAlias(alias);
				cmQuery.getSelect().add(zSelectItemConceptURI);				
			} else {
				logger.debug("Non delegable transformation expression of concept mapping uri-as.");
				Collection<ZSelectItem> selectItems = 
					conceptMappingURIAsTransformationExpression.getSelectItems();
				logger.debug("selectItems = " + selectItems);
				for(ZSelectItem zSelectItem : selectItems) {
					cmQuery.getSelect().add(zSelectItem);
				}
			}
		}

		//unfolding has-table elements
		Vector<R2ODatabaseTable> relatedTables = conceptMapping.getHasTables();
		if(relatedTables != null && relatedTables.size() > 0) {
			for(R2ODatabaseTable hasTable : relatedTables) {
				ZFromItem fromItem = new ZFromItem(hasTable.getName());
				if(hasTable.getAlias() != null) {
					fromItem.setAlias(hasTable.getAlias());
				}
				
				cmQuery.getFrom().add(fromItem);
				
			}
		}

		//unfolding applies-if element
		R2OConditionalExpression cmAppliesIf = conceptMapping.getAppliesIf();
		if(cmAppliesIf != null) {
			if(cmAppliesIf.isDelegableConditionalExpression()) {
				logger.debug("Delegable conditional expression of concept mapping applies-if.");
				R2OConditionalExpressionUnfolder r2oConditionalExpressionUnfolder = 
					new R2OConditionalExpressionUnfolder(cmAppliesIf);
				
				ZExp cmAppliesIfExp = r2oConditionalExpressionUnfolder.unfoldDelegableConditionalExpression();
				cmQuery.addWhere(cmAppliesIfExp);
			} else {
				if(cmAppliesIf.isConjuctiveConditionalExpression()) {
					logger.debug("Conjuctive conditional expression of concept mapping.");
					Collection<R2OCondition> flatConditionalExpression = cmAppliesIf.flatConjuctiveConditionalExpression();
					for(R2OCondition condition : flatConditionalExpression) {
						if(condition.isDelegableCondition()) {
							R2OConditionUnfolder r2oConditionUnfolder = new R2OConditionUnfolder(condition);
							
							ZExp conditionUnfolded = r2oConditionUnfolder.unfold();
							cmQuery.addWhere(conditionUnfolded);							
						} else {
//							Collection<String> conditionColumns = condition.getInvolvedColumns();
							Collection<ZSelectItem> conditionColumns = condition.getSelectItems();
							
							logger.debug("conditionColumns = " + conditionColumns);
							for(ZSelectItem conditionColumn : conditionColumns) {
								String alias = conditionColumn.toString().replaceAll("\\.", "_");
								conditionColumn.setAlias(alias);
								cmQuery.getSelect().add(conditionColumn);
							}							
						}
					}
				} else {
					logger.debug("Non delegable conditional expression of concept mapping.");
//					Collection<String> cmAppliesIfColumns = cmAppliesIf.getInvolvedColumns();
					Collection<ZSelectItem> cmAppliesIfColumns = cmAppliesIf.getSelectItems();
					logger.debug("cmAppliesIfColumns = " + cmAppliesIfColumns);
					for(ZSelectItem cmAppliesIfColumn : cmAppliesIfColumns) {
						String alias = cmAppliesIfColumn.toString().replaceAll("\\.", "_");
						cmAppliesIfColumn.setAlias(alias);
						cmQuery.getSelect().add(cmAppliesIfColumn);
					}					
				}

			}
		}


		if(conceptMapping.getOrderBy() != null && conceptMapping.getOrderBy().size() != 0) {
			cmQuery.addOrderBy(new Vector<String>());
			for(String orderByString : conceptMapping.getOrderBy()) {
				cmQuery.getOrderBy().add(orderByString);
			}
		}

		Collection<AbstractPropertyMapping> propertyMappings = conceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(AbstractPropertyMapping propertyMappping : propertyMappings) {
				R2OPropertyMappingUnfolder r2oPropertyMappingUnfolder = new R2OPropertyMappingUnfolder(
						this.conceptMapping, propertyMappping, r2oMappingDocument);
				r2oPropertyMappingUnfolder.unfold(cmQuery);
			}			
		}

		logger.debug("cmQuery = " + cmQuery);
		return cmQuery;

	}
}
