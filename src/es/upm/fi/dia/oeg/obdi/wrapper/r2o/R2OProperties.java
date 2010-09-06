package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.Properties;

import org.apache.log4j.Logger;

public class R2OProperties extends Properties {
	private Logger logger = Logger.getLogger(R2OProperties.class);
	
	private String[] delegableConditionalOperations;
	private String[] nonDelegableConditionalOperations;
	private String[] delegableTransformationOperations;
	private String[] nonDelegableTransformationOperations;

	
	public void initOperations() {
		String delegableConditionaloperations = super.getProperty(R2OConstants.R2O_PROPERTY_CONDITIONAL_OPERATIONS_DELEGABLE);
		logger.debug("delegableConditionaloperations = " + delegableConditionaloperations);
		this.delegableConditionalOperations = delegableConditionaloperations.split(",");

		String nonDelegableConditionaloperations = super.getProperty(R2OConstants.R2O_PROPERTY_CONDITIONAL_OPERATIONS_NON_DELEGABLE);
		logger.debug("nonDelegableConditionaloperations = " + nonDelegableConditionaloperations);
		this.nonDelegableConditionalOperations = nonDelegableConditionaloperations.split(",");

		String delegableTransformationoperations = super.getProperty(R2OConstants.R2O_PROPERTY_TRANSFORMATION_OPERATIONS_DELEGABLE);
		logger.debug("delegableTransformationoperations = " + delegableTransformationoperations);
		this.delegableTransformationOperations = delegableTransformationoperations.split(",");

		String nonDelegableTransformationoperations = super.getProperty(R2OConstants.R2O_PROPERTY_CONDITIONAL_OPERATIONS_NON_DELEGABLE);
		logger.debug("nonDelegableTransformationoperations = " + nonDelegableTransformationoperations);
		this.nonDelegableTransformationOperations = nonDelegableTransformationoperations.split(",");
		
	}
	
	public String[] getDelegableConditionalOperations() {
		return delegableConditionalOperations;
	}


	public String[] getNonDelegableConditionalOperations() {
		return nonDelegableConditionalOperations;
	}


	public String[] getDelegableTransformationOperations() {
		return delegableTransformationOperations;
	}


	public String[] getNonDelegableTransformationOperations() {
		return nonDelegableTransformationOperations;
	}

}
