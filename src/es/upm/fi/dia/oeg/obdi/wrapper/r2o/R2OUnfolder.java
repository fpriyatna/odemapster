package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.sparql.engine.main.LeftJoinClassifier;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZUtils;
import es.upm.fi.dia.oeg.obdi.ILogicalQuery;
import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction.RestrictionType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ORelationMapping;


public class R2OUnfolder extends AbstractUnfolder {
	private R2OMappingDocument r2oMappingDocument;
	
	private static Logger logger = Logger.getLogger(R2OUnfolder.class);
	private ZQuery zQuery;
	private Collection<String> leftJoinTables;
	private String leftJoinCondition;
	private Properties primitiveOperations;
	private String[] delegableConditionalOperations;
	
	public R2OUnfolder(R2OMappingDocument r2oMappingDocument) {
		this.r2oMappingDocument = r2oMappingDocument;
	}

	public R2OUnfolder(R2OMappingDocument r2oMappingDocument, String primitiveOperationsFile) throws Exception {
		this.r2oMappingDocument = r2oMappingDocument;
		this.primitiveOperations = new Properties();
		try {
			this.primitiveOperations.load(new FileInputStream(primitiveOperationsFile));
		} catch(Exception e) {
			logger.error("Error loading primitive operations file : " + primitiveOperationsFile);
			throw e;
		}
		
		String delegableConditionaloperations = this.primitiveOperations.getProperty("delegable.conditionaloperations");
		logger.debug("delegableConditionaloperations = " + delegableConditionaloperations);
		this.delegableConditionalOperations = delegableConditionaloperations.split(",");

	}

	private void unfoldPropertyMapping(R2OPropertyMapping r2oPropertyMapping) throws Exception {
		if(r2oPropertyMapping instanceof R2OAttributeMapping) {
			this.unfoldAttributeMapping((R2OAttributeMapping) r2oPropertyMapping);
		} else if(r2oPropertyMapping instanceof R2ORelationMapping) {
			this.unfoldRelationMapping((R2ORelationMapping) r2oPropertyMapping);
		}
	}
	
	private void unfoldRelationMapping(R2ORelationMapping r2oRelationMapping) throws Exception {
		String toConcept = r2oRelationMapping.getToConcept();
		Collection<AbstractConceptMapping> conceptMappings = this.r2oMappingDocument.getConceptMappings(toConcept);
		R2OConceptMapping rangeConceptMapping = null;
		if(conceptMappings.size() == 1) {
			rangeConceptMapping = (R2OConceptMapping) conceptMappings.iterator().next();
		} else if(conceptMappings.size() > 1) {
			String errorMessage = "Multiple concept mapping with the same name defined! The first mapping will be used as range.";
			logger.warn(errorMessage);
			rangeConceptMapping = (R2OConceptMapping) conceptMappings.iterator().next();
		} else {
			String errorMessage = "Mapping for the range concept " + toConcept + " is not defined!";
			throw new Exception(errorMessage);
		}
		
		//this.processURIAs(rangeConceptMapping, r2oRelationMapping.getId());
		R2OTransformationExpression transformationExpressionURIAs = rangeConceptMapping.getTransformationExpressionURIAs();
		ZExp selectExpression = this.unfoldTransformationExpression(transformationExpressionURIAs);
		ZSelectItem zSelectItem = new ZSelectItem();
		zSelectItem.setExpression(selectExpression);
		zSelectItem.setAlias(r2oRelationMapping.getId());
		zQuery.getSelect().add(zSelectItem);		
		
		
		//process left join tables
		this.leftJoinTables = new ArrayList<String>();
		Vector<R2ODatabaseTable> rangeTables = rangeConceptMapping.getHasTables();
		for(R2ODatabaseTable rangeTable : rangeTables) {
			this.leftJoinTables.add(rangeTable.getName());
		}
		 
		//process left join conditions
		R2OConditionalExpression joinsVia = r2oRelationMapping.getJoinsVia();
		ZExp joinsViaExp1 = this.unfoldConditionalExpression(joinsVia);
		
		R2OConditionalExpression rangeConditionalExpression = null;
		if(rangeConceptMapping.getAppliesIf() != null) {
			rangeConditionalExpression = rangeConceptMapping.getAppliesIf();
		} else {
			if(rangeConceptMapping.getAppliesIfTop() != null) {
				rangeConditionalExpression = rangeConceptMapping.getAppliesIfTop();
			}
		}
		if(rangeConditionalExpression != null) {
			ZExp joinsViaExp2 = this.unfoldConditionalExpression(rangeConditionalExpression);
			if(joinsViaExp2 != null) {
				joinsViaExp1 = new ZExpression("AND", joinsViaExp1, joinsViaExp2);
			}
		}

		if(joinsViaExp1 != null) {
			this.leftJoinCondition = joinsViaExp1.toString();
		}

	}

	/*
	private void unfoldSelector(Selector selector) throws Exception {
		ConditionalExpression selectorAppliesIf = selector.getAppliesIf();
		if(selectorAppliesIf != null) {
			this.unfoldConditionalExpression(selectorAppliesIf);
		}
		
		TransformationExpression selectorAfterTransform = selector.getAfterTransform();
		ZExp selectExpression = this.unfoldTransformationExpression(selectorAfterTransform);
		ZSelectItem zSelectItem = new ZSelectItem();
		zSelectItem.setExpression(selectExpression);
		String alias = selector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
		zSelectItem.setAlias(alias);
		zQuery.getSelect().add(zSelectItem);		
	}
	

	private void unfoldSelector2(Selector selector) throws Exception {
		ConditionalExpression selectorAppliesIf = selector.getAppliesIf();
		if(selectorAppliesIf != null) {
			Collection<String> involvedColumns = selectorAppliesIf.getInvolvedColumns();
			for(String columnName : involvedColumns) {
				ZSelectItem zSelectItemAppliesIf = new ZSelectItem();
				String columnNameAlias = columnName.replaceAll("\\.", "_");
				zSelectItemAppliesIf.setExpression(new ZConstant(columnName, ZConstant.COLUMNNAME));
				zSelectItemAppliesIf.setAlias(columnNameAlias);
				zQuery.getSelect().add(zSelectItemAppliesIf);
			}				
		}


		TransformationExpression selectorAfterTransform = selector.getAfterTransform();
		ZExp selectExpression = this.unfoldTransformationExpression(selectorAfterTransform);
		ZSelectItem zSelectItem = new ZSelectItem();
		zSelectItem.setExpression(selectExpression);
		String alias = selector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
		zSelectItem.setAlias(alias);
		zQuery.getSelect().add(zSelectItem);		
	}
	*/
	
	private void unfoldAttributeMapping(R2OAttributeMapping r2oAttributeMapping) throws Exception {

		if(r2oAttributeMapping.getUseDBCol() != null) {
			ZSelectItem zSelectItemAppliesIf = new ZSelectItem();
			String columnNameAlias = r2oAttributeMapping.getUseDBCol().replaceAll("\\.", "_");
			zSelectItemAppliesIf.setExpression(new ZConstant(r2oAttributeMapping.getUseDBCol(), ZConstant.COLUMNNAME));
			zSelectItemAppliesIf.setAlias(columnNameAlias);
			zQuery.getSelect().add(zSelectItemAppliesIf);
		} else {
			Collection<R2OSelector> attributeSelectors = r2oAttributeMapping.getSelectors();
			if(attributeSelectors != null) {
				for(R2OSelector attributeSelector : attributeSelectors) {
					R2OConditionalExpression selectorAppliesIf = attributeSelector.getAppliesIf();
					if(selectorAppliesIf != null) {
						Collection<String> involvedColumns = selectorAppliesIf.getInvolvedColumns();
						for(String columnName : involvedColumns) {
							ZSelectItem zSelectItemAppliesIf = new ZSelectItem();
							String columnNameAlias = columnName.replaceAll("\\.", "_");
							zSelectItemAppliesIf.setExpression(new ZConstant(columnName, ZConstant.COLUMNNAME));
							zSelectItemAppliesIf.setAlias(columnNameAlias);
							zQuery.getSelect().add(zSelectItemAppliesIf);
						}	
					}
					
					R2OTransformationExpression selectorAfterTransform = attributeSelector.getAfterTransform();
					ZExp selectExpression = this.unfoldTransformationExpression(selectorAfterTransform);
					ZSelectItem zSelectItem = new ZSelectItem();
					zSelectItem.setExpression(selectExpression);
					String alias = attributeSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
					zSelectItem.setAlias(alias);
					zQuery.getSelect().add(zSelectItem);
				}			
			}			
		}


	}

	private void unfoldConceptMappingAppliesIf(R2OConceptMapping r2oConceptMapping) throws Exception {


		//sql select generation for applies if
		Collection<String> conceptMappingAppliesIfSelect = null; 
		R2OConditionalExpression appliesIf = null;

		if(r2oConceptMapping.getAppliesIf() != null) {
			appliesIf = r2oConceptMapping.getAppliesIf();
		} else {
			if(r2oConceptMapping.getAppliesIfTop() != null) {
				appliesIf = r2oConceptMapping.getAppliesIfTop();
			}
		}

		if(appliesIf != null) {
			//WHERE part
			ZExp conceptMappingAppliesIfWhere = this.unfoldConditionalExpression(appliesIf);
			if(conceptMappingAppliesIfWhere != null) {
				ZExp whereExpression = null;
				if(zQuery.getWhere() == null) {
					whereExpression = conceptMappingAppliesIfWhere;
				} else {
					whereExpression = new ZExpression("AND", zQuery.getWhere(), conceptMappingAppliesIfWhere);
				}
				zQuery.addWhere(whereExpression);
				
			}

			//FROM part
			conceptMappingAppliesIfSelect = appliesIf.getInvolvedColumns();
			logger.debug("involvedColumns = " + conceptMappingAppliesIfSelect);
			for(String involvedColumn : conceptMappingAppliesIfSelect) {
				ZSelectItem zSelectItem = new ZSelectItem(involvedColumn);
				zSelectItem.setAlias(involvedColumn.replaceAll("\\.", "_"));
				zQuery.getSelect().add(zSelectItem);
			}

		}
	}

	private ZExp unfoldCondition(R2OCondition condition) throws Exception {
		ZExp result = null;

		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}

		//delegable operation that needs to be done on the database
		if(Utility.inArray(this.delegableConditionalOperations, operationId)) {
			if(operationId.equalsIgnoreCase(R2OConstants.CONDITIONAL_OPERATOR_NOT_EQUALS_NAME)) {
				result = this.unfoldNotEqualsConditional(condition);
			} else if(operationId.equalsIgnoreCase(R2OConstants.CONDITIONAL_OPERATOR_IN_KEYWORD_NAME)) {
				result = this.unfoldInKeywordConditional(condition);
			} else {
				result = unfoldDelegableConditional(operationId, condition);
			}
		} else {
			logger.info("Delegating conditional operation " + operationId);
		}

		return result;
	}

	private ZExp unfoldConditionalExpression(R2OConditionalExpression conditionalExpression) throws Exception {
		ZExp result = null;
		String conditionalExpressionOperator = conditionalExpression.getOperator();

		if(conditionalExpressionOperator == null) {
			result = this.unfoldCondition(conditionalExpression.getCondition());
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {
			Collection<R2OConditionalExpression> condExprs = conditionalExpression.getCondExprs();
			List<ZExp> operands = new ArrayList<ZExp>();
			for(R2OConditionalExpression condExpr : condExprs) {
				ZExp operand = this.unfoldConditionalExpression(condExpr);
				if(operand != null) {
					operands.add(operand);
				}
			}

			if(operands.size() == 1) {
				result = operands.get(0);
			} else if(operands.size() == 2) {
				ZExpression zExpression = new ZExpression(R2OConstants.AND_TAG);
				for(ZExp operand : operands) {
					zExpression.addOperand(operand);
				}
			}
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.OR_TAG)) {
			String errorMessage = "OR operator in conditional expression is not supported!";
			throw new Exception(errorMessage);
			
			/*
			Collection<ConditionalExpression> condExprs = conditionalExpression.getCondExprs();
			List<ZExp> operands = new ArrayList<ZExp>();
			for(ConditionalExpression condExpr : condExprs) {
				ZExp operand = this.unfoldConditionalExpression(condExpr);
				if(operand != null) {
					operands.add(operand);
				}
			}

			if(operands.size() == 1) {
				result = operands.get(0);
			} else if(operands.size() == 2) {
				ZExpression zExpression = new ZExpression(R2OConstants.OR_TAG);
				for(ZExp operand : operands) {
					zExpression.addOperand(operand);
				}
			}
			*/
		}


		return result;
	}

	private ZExpression unfoldDelegableConditional(String operationId, R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();
		int noOfArgs = Integer.parseInt(
				this.primitiveOperations.getProperty("operation." + operationId + ".nargs"));
		String dbOperation = this.primitiveOperations.getProperty("operation." + operationId + ".delegated.db");
		ZExpression zExpression = new ZExpression(dbOperation);
		for(int i=0; i<noOfArgs;i++) {
			R2ORestriction restriction = argumentRestrictions.get(i).getRestriction();
			ZExp operand = this.unfoldRestriction(restriction);
			zExpression.addOperand(operand);
		}

		result = zExpression;
		return result;		
	}
	
	private ZExpression unfoldEqualsConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.unfoldRestriction(restriction0);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.unfoldRestriction(restriction1);

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("=");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}

	private ZExpression unfoldBetweenConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.unfoldRestriction(restriction0);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.unfoldRestriction(restriction1);
		R2ORestriction restriction2 = argumentRestrictions.get(2).getRestriction();
		ZExp operand2 = this.unfoldRestriction(restriction2);

		if(operand0 != null && operand1 != null && operand2 != null) {
			ZExpression zExpression = new ZExpression("BETWEEN");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			zExpression.addOperand(operand2);
			result = zExpression;
		} 

		return result;
	}
	
	private ZExpression unfoldInKeywordConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.unfoldRestriction(restriction0);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.unfoldRestriction(restriction1);
		operand1 = new ZConstant("%" + operand1.toString().replaceAll("'", "") + "%", ZConstant.STRING);
		
		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("LIKE");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}
	
	private ZExpression unfoldLoThanConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.unfoldRestriction(restriction0);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.unfoldRestriction(restriction1);

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("<");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}

	private ZExpression unfoldLoEqThanConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.unfoldRestriction(restriction0);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.unfoldRestriction(restriction1);

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("<=");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}

	private ZExpression unfoldHiThanConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.unfoldRestriction(restriction0);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.unfoldRestriction(restriction1);

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression(">");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}

	private ZExpression unfoldHiEqThanConditional(R2OCondition condition) throws Exception{
		ZExpression result = null;

		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.unfoldRestriction(restriction0);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.unfoldRestriction(restriction1);

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression(">=");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}

	/*
	private ZExp processOrConditionalExpression(OrConditionalExpression orConditionalExpression) {
		ZExp result = null;

		if(orConditionalExpression.isUsingOr()) { //the case of : OR orcond-expr condition
			String orOperator = "OR";
			ZExp operand1 = this.processOrConditionalExpression(orConditionalExpression.getOrCondExpr());
			ZExp operand2 = this.processCondition(orConditionalExpression.getCondition());

			if(operand1 != null && operand2 != null) {
				result = new ZExpression(orOperator, operand1, operand2);
			} else if(operand1 != null) {
				result = operand1;
			} else if(operand2 != null) {
				result = operand2;
			}

		} else { //the case of : condition
			Condition condition = orConditionalExpression.getCondition();
			ZExp zExp = this.processCondition(condition);
			if(zExp != null) {
				result = zExp;
			}
		}

		return result;
	}
	 */

	private ZExpression unfoldNotEqualsConditional(R2OCondition condition) throws Exception{
		return new ZExpression("NOT", this.unfoldEqualsConditional(condition));
	}

	private ZExp unfoldRestriction(R2ORestriction restriction) throws Exception {
		ZExp result = null;
		if(restriction.getRestrictionType() == RestrictionType.HAS_COLUMN) {
			result = new ZConstant(restriction.getHasColumn(), ZConstant.COLUMNNAME);
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_VALUE) {
			String restrictionDataType = restriction.getRestrictionDataType();
			String restrictionValue = restriction.getHasValue().toString();
			if(restrictionDataType == null) {
				restrictionDataType = R2OConstants.DATATYPE_STRING;
			}
			
			if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_STRING)) {
				result = new ZConstant(restrictionValue, ZConstant.STRING);
			} else if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_DOUBLE)) {
				result = new ZConstant(restrictionValue, ZConstant.NUMBER);
			} else {
				result = new ZConstant(restrictionValue, ZConstant.STRING);
			}
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_TRANSFORMATION) {
			throw new Exception("Not implemented yet!");
		}


		return result;
	}


	private ZExp unfoldTransformationExpression(R2OTransformationExpression transformationExpressionURIAs) throws Exception {

		String operator = transformationExpressionURIAs.getOperId();
		if(operator == null) {
			return null;
		}

		if(Utility.inArray(R2OConstants.DELEGABLE_TRANSFORMATION_OPERATIONS, operator)) {
			List<R2OArgumentRestriction> argumentRestrictions = transformationExpressionURIAs.getArgRestrictions();
			if(operator.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONSTANT)) {
				R2ORestriction restriction = transformationExpressionURIAs.getArgRestrictions().get(0).getRestriction();
				return this.unfoldRestriction(restriction);				
			} else {
				ZExpression selectExpression = new ZExpression(operator);
				for(R2OArgumentRestriction argumentRestriction : argumentRestrictions) {
					R2ORestriction restriction = argumentRestriction.getRestriction();
					ZExp restrictionExpression = this.unfoldRestriction(restriction);
					selectExpression.addOperand(restrictionExpression);
				}

				return selectExpression;
			}
			

		} else {
			String errorMessage = "Operator " + operator + " is not supported yet!";
			throw new Exception(errorMessage);
		}
		

	}

	/*
	private void processURIAs(R2OConceptMapping r2oConceptMapping, String uriAlias) throws Exception {

		Selector selectorURIAs = r2oConceptMapping.getSelectorURIAs();
		if(selectorURIAs != null) { //original r2o
			//todo implement this
		} else {
			TransformationExpression transformationExpressionURIAs = r2oConceptMapping.getTransformationExpressionURIAs();
			if(transformationExpressionURIAs != null) { //modified r2o grammar
				//logger.debug("transformationExpressionURIAs = " + transformationExpressionURIAs);

				Vector<String> involvedTables = transformationExpressionURIAs.getInvolvedTables();
				//logger.debug("involvedTables = " + involvedTables);

				//sql from generation
				//zQuery.getFrom().addAll(involvedTables);

				//sql select generation for uri
				ZExp selectExpression = this.processTransformationExpression(transformationExpressionURIAs);
				ZSelectItem zSelectItem = new ZSelectItem();
				zSelectItem.setExpression(selectExpression);
				zSelectItem.setAlias(uriAlias);
				zQuery.getSelect().add(zSelectItem);
			}

		}

	}
	*/

	@Override
	public String unfoldConceptMapping(AbstractConceptMapping conceptMapping) throws Exception {
		logger.debug("Unfolding = " + conceptMapping.getConceptName());
		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping;
		ZUtils.addCustomFunction("concat", 2);
		this.zQuery = new ZQuery();
		zQuery.addFrom(new Vector<String>());
		zQuery.addSelect(new Vector<ZSelectItem>());

		
		R2OTransformationExpression transformationExpressionURIAs = r2oConceptMapping.getTransformationExpressionURIAs();
		ZExp selectExpression = this.unfoldTransformationExpression(transformationExpressionURIAs);
		ZSelectItem zSelectItemConceptURI = new ZSelectItem();
		zSelectItemConceptURI.setExpression(selectExpression);
		zSelectItemConceptURI.setAlias(r2oConceptMapping.getId() + "_uri");
		zQuery.getSelect().add(zSelectItemConceptURI);
		//this.processURIAs(r2oConceptMapping, r2oConceptMapping.getId() + "_uri");

		Vector<R2ODatabaseTable> relatedTables = r2oConceptMapping.getHasTables();
		if(relatedTables != null && relatedTables.size() > 0) {
			for(R2ODatabaseTable hasTable : relatedTables) {
				ZFromItem fromItem = new ZFromItem(hasTable.getName());
				if(hasTable.getAlias() != null) {
					fromItem.setAlias(hasTable.getAlias());
				}
				this.zQuery.getFrom().add(fromItem);				
			}
		}
		
		this.unfoldConceptMappingAppliesIf(r2oConceptMapping);

		List<R2OPropertyMapping> propertyMappings = r2oConceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMappping : propertyMappings) {
				this.unfoldPropertyMapping(propertyMappping);
			}			
		}

		if(this.leftJoinTables == null && this.leftJoinCondition == null) {
			return zQuery.toString();	
		} else {
			String selectSQL = zQuery.getSelect().toString();
			selectSQL = selectSQL.substring(1, selectSQL.length() - 1);
			String fromSQL = zQuery.getFrom().toString();
			fromSQL = fromSQL.substring(1, fromSQL.length() - 1);
			String leftJoinTableSQL = this.leftJoinTables.toString(); 
			leftJoinTableSQL = leftJoinTableSQL.substring(1, leftJoinTableSQL.length() - 1);
			String whereSQL = null;
			String sql = "SELECT " + selectSQL 
				+ " FROM "+ fromSQL 
				+ " LEFT JOIN " + leftJoinTableSQL 
				+ " ON "+ " " + this.leftJoinCondition; 
			if(zQuery.getWhere() != null) {
				whereSQL = zQuery.getWhere().toString();
				sql += " WHERE " + whereSQL; 
			}

			return sql;
		}
		
	}

	@Override
	public Set<String> unfold(Set<ILogicalQuery> logicalQueries,
			IMappingDocument mapping) throws Exception {

		throw new Exception("Not implemented yet!");
	}

	@Override
	protected String unfold(IMappingDocument mapping) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
