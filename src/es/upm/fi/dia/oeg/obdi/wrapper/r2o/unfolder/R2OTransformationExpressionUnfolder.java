package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidTransfomationExperessionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;

public class R2OTransformationExpressionUnfolder {
	private R2OTransformationExpression transformationExpression;
	
	private static Logger logger = Logger.getLogger(R2OTransformationExpressionUnfolder.class);


	


	public R2OTransformationExpressionUnfolder(R2OTransformationExpression transformationExpression) {
		super();
		this.transformationExpression = transformationExpression;
	}

	public Collection<ZSelectItem> unfold(String alias) throws InvalidTransfomationExperessionException {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		
		if(this.transformationExpression.isDelegableTransformationExpression()) {
			ZExp selectExpression = this.unfoldDelegableTransformationExpression();
			ZSelectItem zSelectItem = new ZSelectItem();
			zSelectItem.setExpression(selectExpression);
			if(alias != null) {
				zSelectItem.setAlias(alias); //we can only set alias in case delegable ones
			}
			result.add(zSelectItem);
			//mainQuery.getSelect().add(zSelectItem);
		} else {
			//we can't set alias here
			logger.debug("Non Delegable transformation expression of attribute selector.");
			Collection<ZSelectItem> selectItems = this.unfoldNonDelegableTransformationExpression(transformationExpression);
			for(ZSelectItem zSelectItem : selectItems) {
				result.add(zSelectItem);
				//mainQuery.getSelect().add(zSelectItem);
			}						
		}		
		
		return result;
	}
	
	public ZExp unfoldDelegableTransformationExpression() throws InvalidTransfomationExperessionException 
	{
		String databaseType = R2ORunner.configurationProperties.getDatabaseType();
		
		String operator = transformationExpression.getOperId();
		if(operator == null) {
			return null;
		}

		List<R2OArgumentRestriction> argumentRestrictions = transformationExpression.getArgRestrictions();
		if(operator.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONSTANT)) {
			R2ORestriction restriction = transformationExpression.getArgRestrictions().get(0).getRestriction();
			R2ORestrictionUnfolder r2oRestrictionUnfolder = 
				new R2ORestrictionUnfolder(restriction); 
			return r2oRestrictionUnfolder.unfoldRestriction();				
		} if(operator.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONCAT)) {
			if(databaseType.equalsIgnoreCase(R2OConstants.DATABASE_SQLSERVER)) {
				ZExpression selectExpression = new ZExpression("+");
				for(R2OArgumentRestriction argumentRestriction : argumentRestrictions) {
					R2ORestriction restriction = argumentRestriction.getRestriction();
					R2ORestrictionUnfolder r2oRestrictionUnfolder = 
						new R2ORestrictionUnfolder(restriction);				
					ZExp restrictionExpression = r2oRestrictionUnfolder.unfoldRestriction();
					if(restrictionExpression instanceof ZConstant) {
						ZConstant restrictionExpressionConstant = (ZConstant) restrictionExpression;
						if(restrictionExpressionConstant.getType() == ZConstant.COLUMNNAME) {
							restrictionExpression = new ZExpression("CONVERT", new ZConstant("VARCHAR", ZConstant.UNKNOWN), restrictionExpressionConstant);
							logger.debug("new expression for sqlserver = " + restrictionExpression);
						}
					}
					 
					selectExpression.addOperand(restrictionExpression);
				}
				return selectExpression;
			} else {
				ZExpression selectExpression = new ZExpression(operator);
				for(R2OArgumentRestriction argumentRestriction : argumentRestrictions) {
					R2ORestriction restriction = argumentRestriction.getRestriction();
					R2ORestrictionUnfolder r2oRestrictionUnfolder = 
						new R2ORestrictionUnfolder(restriction);				
					ZExp restrictionExpression = r2oRestrictionUnfolder.unfoldRestriction();
					selectExpression.addOperand(restrictionExpression);
				}
				return selectExpression;	
			}

		} else {
			ZExpression selectExpression = new ZExpression(operator);
			for(R2OArgumentRestriction argumentRestriction : argumentRestrictions) {
				R2ORestriction restriction = argumentRestriction.getRestriction();
				R2ORestrictionUnfolder r2oRestrictionUnfolder = 
					new R2ORestrictionUnfolder(restriction);				
				ZExp restrictionExpression = r2oRestrictionUnfolder.unfoldRestriction();
				selectExpression.addOperand(restrictionExpression);
			}
			return selectExpression;
		}		
	}
	
	private Collection<ZSelectItem> unfoldNonDelegableTransformationExpression(
			R2OTransformationExpression transformationExpression) {
		return transformationExpression.getSelectItems();
	}

}
