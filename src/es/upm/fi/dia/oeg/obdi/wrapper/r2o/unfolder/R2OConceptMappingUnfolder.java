package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZUtils;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OPropertyMapping;

public class R2OConceptMappingUnfolder {
	private static Logger logger = Logger.getLogger(R2OConceptMappingUnfolder.class);
	private R2OPrimitiveOperationsProperties primitiveOperationsProperties;
	private R2OConfigurationProperties configurationProperties;
	private R2OConceptMapping conceptMapping;
	private R2OMappingDocument r2oMappingDocument;
	



	public R2OConceptMappingUnfolder(
			R2OPrimitiveOperationsProperties primitiveOperationsProperties,
			R2OConfigurationProperties configurationProperties,
			R2OConceptMapping conceptMapping,
			R2OMappingDocument r2oMappingDocument) {
		super();
		this.primitiveOperationsProperties = primitiveOperationsProperties;
		this.configurationProperties = configurationProperties;
		this.conceptMapping = conceptMapping;
		this.r2oMappingDocument = r2oMappingDocument;
	}




	public String unfoldConceptMapping(R2OQuery cmQuery) throws Exception {
		logger.debug("Unfolding = " + conceptMapping.getConceptName());

		ZUtils.addCustomFunction("concat", 2);
		cmQuery.addFrom(new Vector<String>());
		cmQuery.addSelect(new Vector<ZSelectItem>());


		//unfolding uri-as transformation expression
		R2OTransformationExpression conceptMappingURIAsTransformationExpression = 
			conceptMapping.getURIAs();
		String uriAsOperator = conceptMappingURIAsTransformationExpression.getOperId();
		if(uriAsOperator != null) {
			if(conceptMappingURIAsTransformationExpression.isDelegableTransformationExpression()) {
				R2OTransformationExpressionUnfolder r2oTransformationExpressionUnfolder = 
					new R2OTransformationExpressionUnfolder(conceptMappingURIAsTransformationExpression);
				ZExp selectExpression = r2oTransformationExpressionUnfolder.unfoldDelegableTransformationExpression();
				ZSelectItem zSelectItemConceptURI = new ZSelectItem();
				zSelectItemConceptURI.setExpression(selectExpression);
				zSelectItemConceptURI.setAlias(R2OConstants.URI_AS_ALIAS + conceptMapping.getId());
				cmQuery.getSelect().add(zSelectItemConceptURI);				
			} else {
				logger.debug("Non delegable transformation expression of concept mapping uri-as.");
				Collection<ZSelectItem> selectItems = conceptMappingURIAsTransformationExpression.getInvolvedExpression();
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
					logger.debug("Conjuctive delegable conditional expression of concept mapping.");
					Collection<R2OCondition> flatConditionalExpression = cmAppliesIf.flatConjuctiveConditionalExpression();
					for(R2OCondition condition : flatConditionalExpression) {
						if(condition.isDelegableCondition()) {
							R2OConditionUnfolder r2oConditionUnfolder = new R2OConditionUnfolder(condition);
							
							ZExp conditionUnfolded = r2oConditionUnfolder.unfold();
							cmQuery.addWhere(conditionUnfolded);							
						} else {
							Collection<String> conditionColumns = condition.getInvolvedColumns();
							logger.debug("conditionColumns = " + conditionColumns);
							for(String conditionColumn : conditionColumns) {
								ZSelectItem zSelectItem = new ZSelectItem(conditionColumn);
								zSelectItem.setAlias(conditionColumn.replaceAll("\\.", "_"));
								cmQuery.getSelect().add(zSelectItem);
							}							
						}
					}
				} else {
					logger.debug("Non delegable conditional expression of concept mapping.");
					Collection<String> cmAppliesIfColumns = cmAppliesIf.getInvolvedColumns();
					logger.debug("cmAppliesIfColumns = " + cmAppliesIfColumns);
					for(String cmAppliesIfColumn : cmAppliesIfColumns) {
						ZSelectItem zSelectItem = new ZSelectItem(cmAppliesIfColumn);
						zSelectItem.setAlias(cmAppliesIfColumn.replaceAll("\\.", "_"));
						cmQuery.getSelect().add(zSelectItem);
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

		List<R2OPropertyMapping> propertyMappings = conceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMappping : propertyMappings) {
				R2OPropertyMappingUnfolder r2oPropertyMappingUnfolder = new R2OPropertyMappingUnfolder(
						this.conceptMapping, propertyMappping
						, primitiveOperationsProperties, configurationProperties, r2oMappingDocument);
				r2oPropertyMappingUnfolder.unfold(cmQuery);
			}			
		}


		return cmQuery.toString();

	}
}
