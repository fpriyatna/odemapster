package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConditionOperationException;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidTransfomationExperessionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;

public class R2OAttributeMappingUnfolder {
	private static Logger logger = Logger.getLogger(R2OAttributeMappingUnfolder.class);

	private R2OAttributeMapping attributeMapping;


	public R2OAttributeMappingUnfolder(R2OAttributeMapping r2oAttributeMapping) {
		super();
		this.attributeMapping = r2oAttributeMapping;
	}


	public Collection<ZSelectItem> unfold() throws AttributeMappingUnfolderException {
		logger.debug("Unfolding attribute mapping : " + attributeMapping.getAttributeName());

		try {
			Collection<ZSelectItem> result = new Vector<ZSelectItem>();

			String langDBCol = attributeMapping.getLangDBCol();
			if(langDBCol != null) {
				ZSelectItem selectItem = new ZSelectItem();
				String columnNameAlias = langDBCol.replaceAll("\\.", "_");
				selectItem.setExpression(new ZConstant(langDBCol, ZConstant.COLUMNNAME));
				selectItem.setAlias(columnNameAlias);
				result.add(selectItem);
				//mainQuery.getSelect().add(selectItem);
			}

			if(attributeMapping.getUseDBCol() != null) {
				ZSelectItem selectItem = new ZSelectItem();
				String columnNameAlias = attributeMapping.getUseDBCol().replaceAll("\\.", "_");
				selectItem.setExpression(new ZConstant(attributeMapping.getUseDBCol(), ZConstant.COLUMNNAME));
				selectItem.setAlias(columnNameAlias);
				result.add(selectItem);
				//mainQuery.getSelect().add(selectItem);
			} else if(attributeMapping.getUseSQL() != null) {
				ZSelectItem selectItem = new ZSelectItem();
				String selectItemAlias = attributeMapping.getUseSQLAlias();
				String useSql = attributeMapping.getUseSQL();
				ZConstant useSqlExpression = new ZConstant(useSql, ZConstant.UNKNOWN);
				selectItem.setExpression(useSqlExpression);
				selectItem.setAlias(selectItemAlias);
				result.add(selectItem);
				//mainQuery.getSelect().add(selectItem);			
			} else if (attributeMapping.getSelectors() != null){ //use selectors
				Collection<R2OSelector> attributeSelectors = attributeMapping.getSelectors();
				for(R2OSelector attributeSelector : attributeSelectors) {

					//processing selector applies if
					R2OConditionalExpression selectorAppliesIf = attributeSelector.getAppliesIf();
					if(selectorAppliesIf != null) {
						R2OConditionalExpressionUnfolder r2oConditionalExpressionUnfolder =
							new R2OConditionalExpressionUnfolder(selectorAppliesIf);
						String appliesIfAlias = attributeSelector.generateAppliesIfAlias();
						result.addAll(r2oConditionalExpressionUnfolder.unfold(appliesIfAlias));
					}


					//processing selector after transform
					R2OTransformationExpression attSelectorAfterTransform = attributeSelector.getAfterTransform();
					R2OTransformationExpressionUnfolder r2oTransformationExpressionUnfolder =
						new R2OTransformationExpressionUnfolder(attSelectorAfterTransform);
					String afterTransformAlias;
					if(attributeMapping.isMappedPKColumn()) {
						afterTransformAlias = attributeMapping.getName();
					} else {
						afterTransformAlias = attributeSelector.generateAfterTransformAlias();
					}
					
					Collection<ZSelectItem> afterTransformSelectItems = 
						r2oTransformationExpressionUnfolder.unfold(afterTransformAlias);
					result.addAll(afterTransformSelectItems);

				}			
			} else {

			}

			return result;			
		} catch(InvalidTransfomationExperessionException e) {
			String errorMessage = "Error unfolding attribute mapping : " + this.attributeMapping.getName();
			logger.error(errorMessage);
			throw new AttributeMappingUnfolderException(errorMessage);
		} catch (InvalidConditionOperationException e) {
			String errorMessage = "Error unfolding attribute mapping : " + this.attributeMapping.getName();
			logger.error(errorMessage);
			throw new AttributeMappingUnfolderException(errorMessage);
		}


	}

}
