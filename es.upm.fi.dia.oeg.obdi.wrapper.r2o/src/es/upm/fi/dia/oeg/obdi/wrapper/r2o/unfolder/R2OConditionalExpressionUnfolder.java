package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConditionOperationException;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidTransfomationExperessionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;

public class R2OConditionalExpressionUnfolder {
	private static Logger logger = Logger.getLogger(R2OConditionalExpressionUnfolder.class);

	private R2OConditionalExpression conditionalExpression;



	public R2OConditionalExpressionUnfolder(R2OConditionalExpression conditionalExpression) {
		super();
		this.conditionalExpression = conditionalExpression;
	}







	public ZExpression unfoldDelegableConditionalExpression() 
	throws InvalidConditionOperationException, InvalidTransfomationExperessionException {
		ZExpression result = null;
		String conditionalExpressionOperator = conditionalExpression.getOperator();

		if(conditionalExpressionOperator == null) {
			R2OConditionUnfolder r2oConditionUnfolder = new R2OConditionUnfolder(conditionalExpression.getCondition());
			
			result = r2oConditionUnfolder.unfold();
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {
			ZExpression zExpression = new ZExpression(R2OConstants.AND_TAG);
			Collection<R2OConditionalExpression> condExprs = conditionalExpression.getCondExprs();
			for(R2OConditionalExpression condExpr : condExprs) {
				R2OConditionalExpressionUnfolder r2oConditionalExpressionUnfolder = 
					new R2OConditionalExpressionUnfolder(condExpr);
				
				ZExp operand = r2oConditionalExpressionUnfolder.unfoldDelegableConditionalExpression();
				zExpression.addOperand(operand);
			}
			result = zExpression;
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.OR_TAG)) {
			ZExpression zExpression = new ZExpression(R2OConstants.OR_TAG);
			Collection<R2OConditionalExpression> condExprs = conditionalExpression.getCondExprs();
			for(R2OConditionalExpression condExpr : condExprs) {
				R2OConditionalExpressionUnfolder r2oConditionalExpressionUnfolder = 
					new R2OConditionalExpressionUnfolder(condExpr);
				
				ZExp operand = r2oConditionalExpressionUnfolder.unfoldDelegableConditionalExpression();
				zExpression.addOperand(operand);
			}
			result = zExpression;
		}


		return result;
	}







	private Collection<ZSelectItem> unfoldNonDelegableConditionalExpression(
			R2OConditionalExpression conditionalExpression) {
		return conditionalExpression.getSelectItems();
	}







	
	public Collection<ZSelectItem> unfold(String alias) 
	throws InvalidConditionOperationException, InvalidTransfomationExperessionException {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		if(this.conditionalExpression != null) {
			if(this.conditionalExpression.isDelegableConditionalExpression()) {
				//logger.debug("Delegable conditional expression of attribute selector.");
				
				ZExp selectorAppliesIfExpression = this.unfoldDelegableConditionalExpression();

				ZSelectItem selectorAppliesIfSQL = new ZSelectItem();
				selectorAppliesIfSQL.setExpression(selectorAppliesIfExpression);
				selectorAppliesIfSQL.setAlias(alias);
				result.add(selectorAppliesIfSQL);
				//mainQuery.getSelect().add(selectorAppliesIfSQL);							
			} else {
				//logger.debug("Non Delegable conditional expression of attribute selector.");
				Collection<ZSelectItem> involvedColumns = 
					this.unfoldNonDelegableConditionalExpression(this.conditionalExpression);
				for(ZSelectItem selectItem : involvedColumns) {
					String columnNameAlias = selectItem.toString().replaceAll("\\.", "_");
//					zSelectItem.setExpression(new ZConstant(columnName, ZConstant.COLUMNNAME));
					selectItem.setAlias(columnNameAlias);	
					result.add(selectItem);
					//mainQuery.getSelect().add(zSelectItem);
				}
			}
		}

		return result;
	}
}
