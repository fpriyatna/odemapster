package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.ArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Condition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.ConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.DatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Selector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.TransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction.RestrictionType;
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
	
	public R2OUnfolder(R2OMappingDocument r2oMappingDocument) {
		this.r2oMappingDocument = r2oMappingDocument;
	}
	
	private void processPropertyMapping(R2OPropertyMapping r2oPropertyMapping) throws Exception {
		if(r2oPropertyMapping instanceof R2OAttributeMapping) {
			this.processAttributeMapping((R2OAttributeMapping) r2oPropertyMapping);
		} else if(r2oPropertyMapping instanceof R2ORelationMapping) {
			this.processRelationMapping((R2ORelationMapping) r2oPropertyMapping);
		}
	}
	
	private void processRelationMapping(R2ORelationMapping r2oRelationMapping) throws Exception {
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
			String errorMessage = "Mapping for the range concept is not defined!";
			throw new Exception(errorMessage);
		}
		
		this.processURIAs(rangeConceptMapping, r2oRelationMapping.getId());
		
		//process left join tables
		this.leftJoinTables = new ArrayList<String>();
		Vector<DatabaseTable> rangeTables = rangeConceptMapping.getHasTables();
		for(DatabaseTable rangeTable : rangeTables) {
			this.leftJoinTables.add(rangeTable.getName());
		}
		 
		//process left join conditions
		ConditionalExpression joinsVia = r2oRelationMapping.getJoinsVia();
		ZExp joinsViaExp1 = this.processConditionalExpression(joinsVia);
		
		ConditionalExpression rangeConditionalExpression = null;
		if(rangeConceptMapping.getAppliesIf() != null) {
			rangeConditionalExpression = rangeConceptMapping.getAppliesIf();
		} else {
			if(rangeConceptMapping.getAppliesIfTop() != null) {
				rangeConditionalExpression = rangeConceptMapping.getAppliesIfTop();
			}
		}
		if(rangeConditionalExpression != null) {
			ZExp joinsViaExp2 = this.processConditionalExpression(rangeConditionalExpression);
			if(joinsViaExp2 != null) {
				joinsViaExp1 = new ZExpression("AND", joinsViaExp1, joinsViaExp2);
			}
		}

		if(joinsViaExp1 != null) {
			this.leftJoinCondition = joinsViaExp1.toString();
		}

	}
	
	private void processAttributeMapping(R2OAttributeMapping r2oAttributeMapping) throws Exception {

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

	private void processConceptMapping(R2OConceptMapping r2oConceptMapping) throws Exception {
		this.processURIAs(r2oConceptMapping, r2oConceptMapping.getId() + "_uri");
		this.processConceptMappingAppliesIf(r2oConceptMapping);

		List<R2OPropertyMapping> propertyMappings = r2oConceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMappping : propertyMappings) {
				this.processPropertyMapping(propertyMappping);
			}			
		}

	}

	private void processConceptMappingAppliesIf(R2OConceptMapping r2oConceptMapping) throws Exception {


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

	private ZExpression processNotEqualsConditionalExpression(Condition condition) throws Exception{
		return new ZExpression("NOT", this.processEqualsConditionalExpression(condition));
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

	@Override
	public String unfold(AbstractConceptMapping conceptMapping) throws Exception {
		logger.debug("Unfolding = " + conceptMapping.getConceptName());
		
		ZUtils.addCustomFunction("concat", 2);
		this.zQuery = new ZQuery();
		zQuery.addFrom(new Vector<String>());
		
		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping;
		Vector<DatabaseTable> relatedTables = r2oConceptMapping.getHasTables();
		if(relatedTables != null && relatedTables.size() > 0) {
			for(DatabaseTable hasTable : relatedTables) {
				ZFromItem fromItem = new ZFromItem(hasTable.getName());
				if(hasTable.getAlias() != null) {
					fromItem.setAlias(hasTable.getAlias());
				}
				this.zQuery.getFrom().add(fromItem);				
			}
		}
		
		zQuery.addSelect(new Vector<ZSelectItem>());

		this.processConceptMapping(r2oConceptMapping);

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
