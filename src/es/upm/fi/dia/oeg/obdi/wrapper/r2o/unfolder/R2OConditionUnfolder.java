package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.List;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidConditionOperationException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidTransfomationExperessionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ORestriction;

public class R2OConditionUnfolder {
	private static Logger logger = Logger.getLogger(R2OConditionUnfolder.class);
	
	private R2OCondition condition;
	
	
	public R2OConditionUnfolder(R2OCondition condition) {
		super();
		this.condition = condition;
	}

	private ZExpression unfoldEqualsConditional() throws InvalidTransfomationExperessionException {
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder0 = new R2ORestrictionUnfolder(restriction0);
		ZExp operand0 = r2oRestrictionUnfolder0.unfoldRestriction();
		
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder1 = new R2ORestrictionUnfolder(restriction1);
		ZExp operand1 = r2oRestrictionUnfolder1.unfoldRestriction();

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("=");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}
	
	private ZExpression unfoldHiEqThanConditional() throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder0 = new R2ORestrictionUnfolder(restriction0);
		ZExp operand0 = r2oRestrictionUnfolder0.unfoldRestriction();
		
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder1 = new R2ORestrictionUnfolder(restriction1);
		ZExp operand1 = r2oRestrictionUnfolder1.unfoldRestriction();


		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression(">=");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}
	
	private ZExpression unfoldNotEqualsConditional() throws InvalidTransfomationExperessionException {
		return new ZExpression("NOT", this.unfoldEqualsConditional());
	}
	
	private ZExpression unfoldLoThanConditional() throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder0 = new R2ORestrictionUnfolder(restriction0);
		ZExp operand0 = r2oRestrictionUnfolder0.unfoldRestriction();
		
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder1 = new R2ORestrictionUnfolder(restriction1);
		ZExp operand1 = r2oRestrictionUnfolder1.unfoldRestriction();


		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("<");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}
	
	private ZExpression unfoldHiThanConditional() throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder0 = new R2ORestrictionUnfolder(restriction0);
		ZExp operand0 = r2oRestrictionUnfolder0.unfoldRestriction();
		
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder1 = new R2ORestrictionUnfolder(restriction1);
		ZExp operand1 = r2oRestrictionUnfolder1.unfoldRestriction();


		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression(">");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}
	
	private ZExpression unfoldInKeywordConditional() throws InvalidTransfomationExperessionException {
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder0 = new R2ORestrictionUnfolder(restriction0);
		ZExp operand0 = r2oRestrictionUnfolder0.unfoldRestriction();
		
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		R2ORestrictionUnfolder  r2oRestrictionUnfolder1 = new R2ORestrictionUnfolder(restriction1);
		ZExp operand1 = r2oRestrictionUnfolder1.unfoldRestriction();

		operand1 = new ZConstant("%" + operand1.toString().replaceAll("'", "") + "%", ZConstant.STRING);

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("LIKE");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}
	
	private ZExpression unfoldDelegableConditional(String operationId) 
	throws InvalidTransfomationExperessionException {
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();
		int noOfArgs = Integer.parseInt(R2ORunner.primitiveOperationsProperties.getProperty("operation." + operationId + ".nargs"));
		String dbOperation = R2ORunner.primitiveOperationsProperties.getProperty("operation." + operationId + ".delegated.db");
		ZExpression zExpression = new ZExpression(dbOperation);
		for(int i=0; i<noOfArgs;i++) {
			R2ORestriction restriction = argumentRestrictions.get(i).getRestriction();
			R2ORestrictionUnfolder restrictionUnfolder = new R2ORestrictionUnfolder(restriction);
			ZExp operand = restrictionUnfolder.unfoldRestriction();
			zExpression.addOperand(operand);
		}

		result = zExpression;
		return result;		
	}
	
	private ZExpression unfoldBetweenConditional() throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		R2ORestrictionUnfolder r2oRestrictionUnfolder0 = new R2ORestrictionUnfolder(restriction0);
		ZExp operand0 = r2oRestrictionUnfolder0.unfoldRestriction();
		
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		R2ORestrictionUnfolder r2oRestrictionUnfolder1 = new R2ORestrictionUnfolder(restriction1);
		ZExp operand1 = r2oRestrictionUnfolder1.unfoldRestriction();
		
		R2ORestriction restriction2 = argumentRestrictions.get(2).getRestriction();
		R2ORestrictionUnfolder r2oRestrictionUnfolder2 = new R2ORestrictionUnfolder(restriction2);
		ZExp operand2 = r2oRestrictionUnfolder2.unfoldRestriction();

		if(operand0 != null && operand1 != null && operand2 != null) {
			ZExpression zExpression = new ZExpression("BETWEEN");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			zExpression.addOperand(operand2);
			result = zExpression;
		} 

		return result;
	}
	
	public ZExpression unfold() 
	throws InvalidConditionOperationException, InvalidTransfomationExperessionException {
		ZExpression result = null;

		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}

		//delegable operation that needs to be done on the database
		R2OPrimitiveOperationsProperties r2oProperties = R2ORunner.primitiveOperationsProperties;
		if(Utility.inArray(r2oProperties.getDelegableConditionalOperations(), operationId)) {
			if(operationId.equalsIgnoreCase(R2OConstants.CONDITIONAL_OPERATOR_NOT_EQUALS_NAME)) {
				result = this.unfoldNotEqualsConditional();
			} else if(operationId.equalsIgnoreCase(R2OConstants.CONDITIONAL_OPERATOR_IN_KEYWORD_NAME)) {
				result = this.unfoldInKeywordConditional();
			} else {
				result = unfoldDelegableConditional(operationId);
			}
		} else if(Utility.inArray(r2oProperties.getNonDelegableConditionalOperations(), operationId)) {
			logger.info("Delegating conditional operation " + operationId);
			//condition.getInvolvedColumns();
		} else {
			throw new InvalidConditionOperationException("Non supported conditional operation : " + operationId);
		}

		return result;
	}

	private ZExpression unfoldLoEqThanConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		R2ORestrictionUnfolder r2oRestrictionUnfolder0 = new R2ORestrictionUnfolder(restriction0);
		ZExp operand0 = r2oRestrictionUnfolder0.unfoldRestriction();
		
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		R2ORestrictionUnfolder r2oRestrictionUnfolder1 = new R2ORestrictionUnfolder(restriction1);
		ZExp operand1 = r2oRestrictionUnfolder1.unfoldRestriction();
		

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("<=");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}
}
