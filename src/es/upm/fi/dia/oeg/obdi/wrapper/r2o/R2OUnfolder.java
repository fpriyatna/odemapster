package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZUtils;
import es.upm.fi.dia.oeg.obdi.ILogicalQuery;
import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.ArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Condition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.ConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Selector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.TransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction.RestrictionType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;


public class R2OUnfolder extends AbstractUnfolder {
	private static Logger logger = Logger.getLogger(R2OUnfolder.class);

	@Override
	public Set<String> toSQL(Set<ILogicalQuery> logicalQueries,
			IMappingDocument mapping) throws Exception {

		throw new Exception("Not implemented yet!");
	}

	private ZExp processTransformationExpression(TransformationExpression transformationExpressionURIAs) throws Exception {

		String operator = transformationExpressionURIAs.getOperId();
		if(operator == null) {
			return null;
		}

		if(operator.equals(R2OConstants.TRANSFORMATION_OPERATOR_CONSTANT_NAME)) {
			Restriction restriction = transformationExpressionURIAs.getArgRestrictions().get(0).getRestriction();
			return this.processRestriction(restriction);
		} else {
			ZExpression selectExpression = new ZExpression(operator);
			List<ArgumentRestriction> argumentRestrictions = transformationExpressionURIAs.getArgRestrictions();
			for(ArgumentRestriction argumentRestriction : argumentRestrictions) {
				Restriction restriction = argumentRestriction.getRestriction();
				ZExp restrictionExpression = this.processRestriction(restriction);
				selectExpression.addOperand(restrictionExpression);
			}

			return selectExpression;
		}
	}

	private ZExp processRestriction(Restriction restriction) throws Exception {
		ZExp result = null;
		if(restriction.getRestrictionType() == RestrictionType.HAS_COLUMN) {
			result = new ZConstant(restriction.getHasColumn(), ZConstant.COLUMNNAME);
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_VALUE) {
			result = new ZConstant(restriction.getHasValue(), ZConstant.STRING);
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_TRANSFORMATION) {
			throw new Exception("Not implemented yet!");
		}


		return result;
	}

	private void processURIAs(R2OConceptMapping r2oConceptMapping, ZQuery zQuery) throws Exception {

		Selector selectorURIAs = r2oConceptMapping.getSelectorURIAs();
		if(selectorURIAs != null) { //original r2o
			//todo implement this
		} else {
			TransformationExpression transformationExpressionURIAs = r2oConceptMapping.getTransformationExpressionURIAs();
			if(transformationExpressionURIAs != null) { //modified r2o grammar
				//logger.debug("transformationExpressionURIAs = " + transformationExpressionURIAs);

				Collection<String> involvedTables = transformationExpressionURIAs.getInvolvedTables();
				//logger.debug("involvedTables = " + involvedTables);

				//sql from generation
				zQuery.addFrom((Vector<String>) involvedTables);

				//sql select generation for uri
				ZExp selectExpression = this.processTransformationExpression(transformationExpressionURIAs);
				ZSelectItem zSelectItem = new ZSelectItem();
				zSelectItem.setExpression(selectExpression);
				zSelectItem.setAlias("uri");
				zQuery.getSelect().add(zSelectItem);
			}

		}

	}

	private void processConceptMappingAppliesIf(R2OConceptMapping r2oConceptMapping, ZQuery zQuery) throws Exception {


		//sql select generation for applies if
		Collection<String> conceptMappingAppliesIfSelect = null; 
		ConditionalExpression appliesIf = null;

		if(r2oConceptMapping.getAppliesIf() != null) {
			appliesIf = r2oConceptMapping.getAppliesIf();
		} else {
			if(r2oConceptMapping.getAppliesIfTop() != null) {
				appliesIf = r2oConceptMapping.getAppliesIfTop();
			}
		}

		if(appliesIf != null) {
			//WHERE part
			ZExp conceptMappingAppliesIfWhere = this.processConditionalExpression(appliesIf);
			if(conceptMappingAppliesIfWhere != null) {
				zQuery.addWhere(conceptMappingAppliesIfWhere);
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

	private ZExp processConditionalExpression(ConditionalExpression conditionalExpression) throws Exception {
		ZExp result = null;
		String conditionalExpressionOperator = conditionalExpression.getOperator();

		if(conditionalExpressionOperator == null) {
			result = this.processCondition(conditionalExpression.getCondition());
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {
			Collection<ConditionalExpression> condExprs = conditionalExpression.getCondExprs();
			List<ZExp> operands = new ArrayList<ZExp>();
			for(ConditionalExpression condExpr : condExprs) {
				ZExp operand = this.processConditionalExpression(condExpr);
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
			Collection<ConditionalExpression> condExprs = conditionalExpression.getCondExprs();
			List<ZExp> operands = new ArrayList<ZExp>();
			for(ConditionalExpression condExpr : condExprs) {
				ZExp operand = this.processConditionalExpression(condExpr);
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

	private ZExp processCondition(Condition condition) throws Exception {
		ZExp result = null;

		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}

		//delegeable operation that needs to be done on the database
		if(Utility.inArray(R2OConstants.DELEGABLE_OPERATIONS, operationId)) {
			if(operationId.equals(R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME)) {
				result = this.processEqualsConditionalExpression(condition);
			} else if(operationId.equals(R2OConstants.CONDITIONAL_OPERATOR_NOT_EQUALS_NAME)) {
				result = this.processNotEqualsConditionalExpression(condition);
			}
				
		}

		return result;
	}

	private ZExpression processEqualsConditionalExpression(Condition condition) throws Exception{
		ZExpression result = null;

		List<ArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		Restriction restriction0 = argumentRestrictions.get(0).getRestriction();
		ZExp operand0 = this.processRestriction(restriction0);
		Restriction restriction1 = argumentRestrictions.get(1).getRestriction();
		ZExp operand1 = this.processRestriction(restriction1);

		if(operand0 != null && operand1 != null) {
			ZExpression zExpression = new ZExpression("=");
			zExpression.addOperand(operand0);
			zExpression.addOperand(operand1);
			result = zExpression;
		} 

		return result;
	}


	private ZExpression processNotEqualsConditionalExpression(Condition condition) throws Exception{
		return new ZExpression("NOT", this.processEqualsConditionalExpression(condition));
	}

	@Override
	public String toSQL(AbstractConceptMapping conceptMapping) throws Exception {
		ZUtils.addCustomFunction("concat", 2);
		String result = "";


		ZQuery zQuery = new ZQuery();
		Vector<ZSelectItem> selectPart = new Vector<ZSelectItem>();
		zQuery.addSelect(selectPart);
		Vector<String> fromPart = new Vector<String>();
		zQuery.addFrom(fromPart);


		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping;
		this.processConceptMapping(r2oConceptMapping, zQuery);

		result = zQuery.toString();
		return result;
	}

	private void processConceptMapping(R2OConceptMapping r2oConceptMapping, ZQuery zQuery) throws Exception {
		this.processURIAs(r2oConceptMapping, zQuery);
		this.processConceptMappingAppliesIf(r2oConceptMapping, zQuery);

		List<R2OAttributeMapping> attributeMappings = r2oConceptMapping.getAttributeMappings();
		for(R2OAttributeMapping attributeMappping : attributeMappings) {
			this.processAttributeMapping(attributeMappping, zQuery);
		}
	}

	private void processAttributeMapping(R2OAttributeMapping r2oAttributeMapping, ZQuery zQuery) throws Exception {
		
		if(r2oAttributeMapping.getUseDBCol() != null) {
			ZSelectItem zSelectItemAppliesIf = new ZSelectItem();
			String columnNameAlias = r2oAttributeMapping.getUseDBCol().replaceAll("\\.", "_");
			zSelectItemAppliesIf.setExpression(new ZConstant(r2oAttributeMapping.getUseDBCol(), ZConstant.COLUMNNAME));
			zSelectItemAppliesIf.setAlias(columnNameAlias);
			zQuery.getSelect().add(zSelectItemAppliesIf);
		} else {
			Collection<Selector> attributeSelectors = r2oAttributeMapping.getSelectors();
			if(attributeSelectors != null) {
				for(Selector attributeSelector : attributeSelectors) {
					ConditionalExpression attributeSelectorAppliesIf = attributeSelector.getAppliesIf();
					if(attributeSelectorAppliesIf != null) {
						Collection<String> involvedColumns = attributeSelectorAppliesIf.getInvolvedColumns();
						for(String columnName : involvedColumns) {
							ZSelectItem zSelectItemAppliesIf = new ZSelectItem();
							String columnNameAlias = columnName.replaceAll("\\.", "_");
							zSelectItemAppliesIf.setExpression(new ZConstant(columnName, ZConstant.COLUMNNAME));
							zSelectItemAppliesIf.setAlias(columnNameAlias);
							zQuery.getSelect().add(zSelectItemAppliesIf);
						}				
					}

					
					TransformationExpression attributeSelectorAfterTransform = attributeSelector.getAfterTransform();
					ZExp selectExpression = this.processTransformationExpression(attributeSelectorAfterTransform);
					ZSelectItem zSelectItem = new ZSelectItem();
					zSelectItem.setExpression(selectExpression);
					String alias = r2oAttributeMapping.getId() + attributeSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
					zSelectItem.setAlias(alias);
					zQuery.getSelect().add(zSelectItem);
				}			
			}			
		}
		

	}
}
