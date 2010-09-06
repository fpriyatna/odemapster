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
import es.upm.fi.dia.oeg.obdi.XMLUtility;
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
	private ZQuery mainQuery;
	private Collection<ZQuery> leftJoinQueries;
	private Collection<ZQuery> innerJoinQueries;

	//private R2OProperties unfolderProperties;

	public R2OUnfolder(R2OMappingDocument r2oMappingDocument) {
		this.r2oMappingDocument = r2oMappingDocument;
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
		if(toConcept != null) {
			R2OConceptMapping rangeConceptMapping = (R2OConceptMapping) this.r2oMappingDocument.getConceptMappingsByMappingId(toConcept);
			if(rangeConceptMapping == null) {
				String errorMessage = "Concept mapping with id " + toConcept + " is not defined!";
				throw new Exception(errorMessage);
			}


			//this.processURIAs(rangeConceptMapping, r2oRelationMapping.getId());
			R2OTransformationExpression rangeURIAsTransformationExpression = rangeConceptMapping.getTransformationExpressionURIAs();
			String uriAsOperator = rangeURIAsTransformationExpression.getOperId();
			if(uriAsOperator != null) {
				if(this.isDelegableTransformationExpression(rangeURIAsTransformationExpression)) {
					ZExp selectExpression = this.unfoldDelegableTransformationExpression(rangeURIAsTransformationExpression);
					ZSelectItem zSelectItem = new ZSelectItem();
					zSelectItem.setExpression(selectExpression);
					zSelectItem.setAlias(R2OConstants.RELATIONMAPPING_ALIAS + r2oRelationMapping.hashCode());
					mainQuery.getSelect().add(zSelectItem);				
				} else {
					throw new Exception("Unsupported transformation operation : " + uriAsOperator);
				}
			}




			//process joins-via conditions
			R2OConditionalExpression joinsVia = r2oRelationMapping.getJoinsVia();
			if(joinsVia != null) {
				ZExp joinsViaExp = null;

				if(joinsVia.isDelegableConditionalExpression((R2OProperties) super.unfolderProperties)) {
					joinsViaExp = this.unfoldDelegableConditionalExpression(joinsVia);
				} else {
					throw new Exception("Invalid joins-via expression");
				}

				String joinType = r2oRelationMapping.getJoinType();
				ZQuery joinQuery = new ZQuery();
				if(joinType.equalsIgnoreCase(R2ORelationMapping.JOINS_TYPE_LEFT)) {
					//process left join tables
					if(this.leftJoinQueries == null) {
						this.leftJoinQueries = new ArrayList<ZQuery>();
					}
					this.leftJoinQueries.add(joinQuery);				
				} else if(joinType.equalsIgnoreCase(R2ORelationMapping.JOINS_TYPE_INNER)) {
					//process inner join tables
					if(this.innerJoinQueries == null) {
						this.innerJoinQueries = new ArrayList<ZQuery>();
					}
					this.innerJoinQueries.add(joinQuery);				
				} else {
					throw new Exception("Unsupported join type : " + joinType);
				}

				joinQuery.addFrom(new Vector<ZFromItem>());
				Vector<R2ODatabaseTable> rangeTables = rangeConceptMapping.getHasTables();
				for(R2ODatabaseTable rangeTable : rangeTables) {
					ZFromItem fromItem = new ZFromItem(rangeTable.getName());
					if(rangeTable.getAlias() != null) {
						fromItem.setAlias(rangeTable.getAlias());
					}
					joinQuery.getFrom().add(fromItem);
				}

				R2OConditionalExpression rangeConditionalExpression = rangeConceptMapping.getAppliesIf();

				if(rangeConditionalExpression != null) {
					if(rangeConditionalExpression.isDelegableConditionalExpression((R2OProperties) super.unfolderProperties)) {
						ZExp rangeConceptConditionalExpression = this.unfoldDelegableConditionalExpression(rangeConditionalExpression);
						if(rangeConceptConditionalExpression != null) {
							joinsViaExp = new ZExpression("AND", joinsViaExp, rangeConceptConditionalExpression);
						}				
					} else {
						throw new Exception("Unsupported relation mapping because range concept mapping applies if is not a delegable conditional expression.");
					}

				}

				if(joinsViaExp != null) {
					joinQuery.addWhere(joinsViaExp);
				}

			}
		}


		if(r2oRelationMapping.getRmSelectors() != null) {
			Collection<R2OSelector> selectors = r2oRelationMapping.getRmSelectors();
			if(selectors != null) {
				for(R2OSelector selector : selectors) {
					R2OConditionalExpression selectorAppliesIf = selector.getAppliesIf();

					//processing selector applies if
					if(selectorAppliesIf != null) {
						if(selectorAppliesIf.isDelegableConditionalExpression((R2OProperties) super.unfolderProperties)) {
							//logger.debug("Delegable conditional expression of attribute selector.");
							ZExp selectorAppliesIfExpression = this.unfoldDelegableConditionalExpression(selectorAppliesIf);
							ZSelectItem selectorAppliesIfSQL = new ZSelectItem();
							selectorAppliesIfSQL.setExpression(selectorAppliesIfExpression);
							String selectorAppliesIfSQLAlias = R2OConstants.APPLIES_IF_ALIAS + selector.hashCode();
							selectorAppliesIfSQL.setAlias(selectorAppliesIfSQLAlias);
							mainQuery.getSelect().add(selectorAppliesIfSQL);							
						} else {
							//logger.debug("Non Delegable conditional expression of attribute selector.");
							Collection<String> involvedColumns = this.unfoldNonDelegableConditionalExpression(selectorAppliesIf);
							for(String columnName : involvedColumns) {
								ZSelectItem zSelectItem = new ZSelectItem();
								String columnNameAlias = columnName.replaceAll("\\.", "_");
								zSelectItem.setExpression(new ZConstant(columnName, ZConstant.COLUMNNAME));
								zSelectItem.setAlias(columnNameAlias);								
								mainQuery.getSelect().add(zSelectItem);
							}
						}
					}


					//processing selector after transform
					R2OTransformationExpression attSelectorAfterTransform = selector.getAfterTransform();
					if(this.isDelegableTransformationExpression(attSelectorAfterTransform)) {
						ZExp selectExpression = this.unfoldDelegableTransformationExpression(attSelectorAfterTransform);
						ZSelectItem zSelectItem = new ZSelectItem();
						zSelectItem.setExpression(selectExpression);
						String alias = R2OConstants.AFTERTRANSFORM_ALIAS + selector.hashCode();
						zSelectItem.setAlias(alias);
						mainQuery.getSelect().add(zSelectItem);
					} else {
						logger.debug("Non Delegable transformation expression of attribute selector.");
						Collection<ZSelectItem> selectItems = this.unfoldNonDelegableTransformationExpression(attSelectorAfterTransform);
						for(ZSelectItem zSelectItem : selectItems) {
							mainQuery.getSelect().add(zSelectItem);
						}						
					}
				}			
			}			
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

	private Collection<String> unfoldNonDelegableConditionalExpression(R2OConditionalExpression conditionalExpression) {
		return conditionalExpression.getInvolvedColumns();
	}

	private Collection<ZSelectItem> unfoldNonDelegableTransformationExpression(R2OTransformationExpression transformationExpression) {
		return transformationExpression.getInvolvedExpression();
	}

	private void unfoldAttributeMapping(R2OAttributeMapping r2oAttributeMapping) throws Exception {
		String langDBCol = r2oAttributeMapping.getLangDBCol();
		if(langDBCol != null) {
			ZSelectItem selectItem = new ZSelectItem();
			String columnNameAlias = langDBCol.replaceAll("\\.", "_");
			selectItem.setExpression(new ZConstant(langDBCol, ZConstant.COLUMNNAME));
			selectItem.setAlias(columnNameAlias);
			mainQuery.getSelect().add(selectItem);
		}

		if(r2oAttributeMapping.getUseDBCol() != null) {
			ZSelectItem selectItem = new ZSelectItem();
			String columnNameAlias = r2oAttributeMapping.getUseDBCol().replaceAll("\\.", "_");
			selectItem.setExpression(new ZConstant(r2oAttributeMapping.getUseDBCol(), ZConstant.COLUMNNAME));
			selectItem.setAlias(columnNameAlias);
			mainQuery.getSelect().add(selectItem);
		} else if(r2oAttributeMapping.getUseSQL() != null) {
			ZSelectItem selectItem = new ZSelectItem();
			String selectItemAlias = r2oAttributeMapping.getUseSQLAlias();
			String useSql = r2oAttributeMapping.getUseSQL();
			ZConstant useSqlExpression = new ZConstant(useSql, ZConstant.UNKNOWN);
			selectItem.setExpression(useSqlExpression);
			selectItem.setAlias(selectItemAlias);
			mainQuery.getSelect().add(selectItem);			
		} else {
			Collection<R2OSelector> attributeSelectors = r2oAttributeMapping.getSelectors();
			if(attributeSelectors != null) {
				for(R2OSelector attributeSelector : attributeSelectors) {
					R2OConditionalExpression selectorAppliesIf = attributeSelector.getAppliesIf();

					//processing selector applies if
					if(selectorAppliesIf != null) {
						if(selectorAppliesIf.isDelegableConditionalExpression((R2OProperties) super.unfolderProperties)) {
							//logger.debug("Delegable conditional expression of attribute selector.");
							ZExp selectorAppliesIfExpression = this.unfoldDelegableConditionalExpression(selectorAppliesIf);
							ZSelectItem selectorAppliesIfSQL = new ZSelectItem();
							selectorAppliesIfSQL.setExpression(selectorAppliesIfExpression);
							String selectorAppliesIfSQLAlias = R2OConstants.APPLIES_IF_ALIAS + attributeSelector.hashCode();
							selectorAppliesIfSQL.setAlias(selectorAppliesIfSQLAlias);
							mainQuery.getSelect().add(selectorAppliesIfSQL);							
						} else {
							//logger.debug("Non Delegable conditional expression of attribute selector.");
							Collection<String> involvedColumns = this.unfoldNonDelegableConditionalExpression(selectorAppliesIf);
							for(String columnName : involvedColumns) {
								ZSelectItem zSelectItem = new ZSelectItem();
								String columnNameAlias = columnName.replaceAll("\\.", "_");
								zSelectItem.setExpression(new ZConstant(columnName, ZConstant.COLUMNNAME));
								zSelectItem.setAlias(columnNameAlias);								
								mainQuery.getSelect().add(zSelectItem);
							}
						}
					}


					//processing selector after transform
					R2OTransformationExpression attSelectorAfterTransform = attributeSelector.getAfterTransform();
					if(this.isDelegableTransformationExpression(attSelectorAfterTransform)) {
						ZExp selectExpression = this.unfoldDelegableTransformationExpression(attSelectorAfterTransform);
						ZSelectItem zSelectItem = new ZSelectItem();
						zSelectItem.setExpression(selectExpression);
						String alias = R2OConstants.AFTERTRANSFORM_ALIAS + attributeSelector.hashCode();
						zSelectItem.setAlias(alias);
						mainQuery.getSelect().add(zSelectItem);
					} else {
						logger.debug("Non Delegable transformation expression of attribute selector.");
						Collection<ZSelectItem> selectItems = this.unfoldNonDelegableTransformationExpression(attSelectorAfterTransform);
						for(ZSelectItem zSelectItem : selectItems) {
							mainQuery.getSelect().add(zSelectItem);
						}						
					}
				}			
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
		R2OProperties r2oProperties = (R2OProperties) super.unfolderProperties;
		if(Utility.inArray(r2oProperties.getDelegableConditionalOperations(), operationId)) {
			if(operationId.equalsIgnoreCase(R2OConstants.CONDITIONAL_OPERATOR_NOT_EQUALS_NAME)) {
				result = this.unfoldNotEqualsConditional(condition);
			} else if(operationId.equalsIgnoreCase(R2OConstants.CONDITIONAL_OPERATOR_IN_KEYWORD_NAME)) {
				result = this.unfoldInKeywordConditional(condition);
			} else {
				result = unfoldDelegableConditional(operationId, condition);
			}
		} else if(Utility.inArray(r2oProperties.getNonDelegableConditionalOperations(), operationId)) {
			logger.info("Delegating conditional operation " + operationId);
			//condition.getInvolvedColumns();
		} else {
			throw new Exception("Non supported conditional operation : " + operationId);
		}

		return result;
	}






	private boolean isDelegableTransformationExpression(R2OTransformationExpression transformationExpression) {
		String operator = transformationExpression.getOperId();

		//if the root operator is not delegable, then return false
		R2OProperties r2oProperties = (R2OProperties) super.unfolderProperties;
		if(!Utility.inArray(r2oProperties.getDelegableTransformationOperations(), operator)) {
			return false;
		}



		//if one of the arguments has non delegable transformation, then return false
		for(R2OArgumentRestriction argRestriction : transformationExpression.getArgRestrictions()) {
			R2ORestriction restriction = argRestriction.getRestriction();
			if(restriction.getRestrictionType() == RestrictionType.HAS_TRANSFORMATION 
					&& !isDelegableTransformationExpression(restriction.getHasTransformation())) {
				return false;
			}
		}

		return true;
	}

	private ZExp unfoldDelegableConditionalExpression(R2OConditionalExpression conditionalExpression) throws Exception {
		ZExp result = null;
		String conditionalExpressionOperator = conditionalExpression.getOperator();

		if(conditionalExpressionOperator == null) {
			result = this.unfoldCondition(conditionalExpression.getCondition());
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {


			Collection<R2OConditionalExpression> condExprs = conditionalExpression.getCondExprs();
			List<ZExp> operands = new ArrayList<ZExp>();
			for(R2OConditionalExpression condExpr : condExprs) {
				ZExp operand = this.unfoldDelegableConditionalExpression(condExpr);
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
				result = zExpression;
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
				this.unfolderProperties.getProperty("operation." + operationId + ".nargs"));
		String dbOperation = this.unfolderProperties.getProperty("operation." + operationId + ".delegated.db");
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
			ZSelectItem selectItem = new ZSelectItem(restriction.getHasColumn());
			String schema = selectItem.getSchema();
			String table = selectItem.getTable();
			String column = selectItem.getColumn();
			result = new ZConstant(table + "." + column, ZConstant.COLUMNNAME);
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
			} else if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_NUMBER)) {
				result = new ZConstant(restrictionValue, ZConstant.NUMBER);				
			} else if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_INTEGER)) {
				result = new ZConstant(restrictionValue, ZConstant.NUMBER);				
			} else {
				result = new ZConstant(restrictionValue, ZConstant.STRING);
			}
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_TRANSFORMATION) {
			if(isDelegableTransformationExpression(restriction.getHasTransformation())) {
				result = this.unfoldDelegableTransformationExpression(restriction.getHasTransformation());
			} else {
				throw new Exception("Unsupported transformation expression!");
			}
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_SQL) {
			String useSql = restriction.getHasSQL();
			result = new ZConstant(useSql, ZConstant.UNKNOWN);
		}

		return result;
	}


	private ZExp unfoldDelegableTransformationExpression(R2OTransformationExpression transformationExpression) throws Exception {
		String operator = transformationExpression.getOperId();
		if(operator == null) {
			return null;
		}

		List<R2OArgumentRestriction> argumentRestrictions = transformationExpression.getArgRestrictions();
		if(operator.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONSTANT)) {
			R2ORestriction restriction = transformationExpression.getArgRestrictions().get(0).getRestriction();
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
	}

	/*
	private ZExp unfoldTransformationExpression2(R2OTransformationExpression transformationExpression) throws Exception {

		String operator = transformationExpression.getOperId();
		if(operator == null) {
			return null;
		}

		if(Utility.inArray(this.delegableTransformationOperations, operator)) {
			return this.unfoldDelegableTransformationExpression(transformationExpression);
		} else if(Utility.inArray(this.nonDelegableTransformationOperations, operator)) {
			//return the columns
			String errorMessage = "Operator " + operator + " is not supported yet!";
			throw new Exception(errorMessage);			
		} else {
			String errorMessage = "Operator " + operator + " is not supported yet!";
			throw new Exception(errorMessage);
		}
	}
	 */

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
		this.mainQuery = new ZQuery();
		mainQuery.addFrom(new Vector<String>());
		mainQuery.addSelect(new Vector<ZSelectItem>());


		//unfolding uri-as transformation expression
		R2OTransformationExpression conceptMappingURIAsTransformationExpression = r2oConceptMapping.getTransformationExpressionURIAs();
		String uriAsOperator = conceptMappingURIAsTransformationExpression.getOperId();
		if(uriAsOperator != null) {
			if(this.isDelegableTransformationExpression(conceptMappingURIAsTransformationExpression)) {
				ZExp selectExpression = this.unfoldDelegableTransformationExpression(conceptMappingURIAsTransformationExpression);
				ZSelectItem zSelectItemConceptURI = new ZSelectItem();
				zSelectItemConceptURI.setExpression(selectExpression);
				zSelectItemConceptURI.setAlias(R2OConstants.URI_AS_ALIAS + r2oConceptMapping.getId());
				mainQuery.getSelect().add(zSelectItemConceptURI);				
			} else {
				logger.debug("Non delegable transformation expression of concept mapping uri-as.");
				Collection<ZSelectItem> selectItems = conceptMappingURIAsTransformationExpression.getInvolvedExpression();
				logger.debug("selectItems = " + selectItems);
				for(ZSelectItem zSelectItem : selectItems) {
					mainQuery.getSelect().add(zSelectItem);
				}
			}
		}

		//unfolding has-table elements
		Vector<R2ODatabaseTable> relatedTables = r2oConceptMapping.getHasTables();
		if(relatedTables != null && relatedTables.size() > 0) {
			for(R2ODatabaseTable hasTable : relatedTables) {
				ZFromItem fromItem = new ZFromItem(hasTable.getName());
				if(hasTable.getAlias() != null) {
					fromItem.setAlias(hasTable.getAlias());
				}
				this.mainQuery.getFrom().add(fromItem);				
			}
		}

		//unfolding applies-if element
		R2OConditionalExpression cmAppliesIf = r2oConceptMapping.getAppliesIf();
		if(cmAppliesIf != null) {
			if(cmAppliesIf.isDelegableConditionalExpression((R2OProperties) super.unfolderProperties)) {
				logger.debug("Delegable conditional expression of concept mapping.");
				ZExp cmAppliesIfExp = this.unfoldDelegableConditionalExpression(cmAppliesIf);
				mainQuery.addWhere(cmAppliesIfExp);

				/*
				if(conceptMappingAppliesIfWhere != null) {
					ZExp whereExpression = null;
					if(mainQuery.getWhere() == null) {
						whereExpression = conceptMappingAppliesIfWhere;
					} else {
						whereExpression = new ZExpression("AND", mainQuery.getWhere(), conceptMappingAppliesIfWhere);
					}
					mainQuery.addWhere(whereExpression);
				}
				 */
			} else {
				if(cmAppliesIf.isConjuctiveConditionalExpression()) {
					logger.debug("Conjuctive delegable conditional expression of concept mapping.");
					Collection<R2OCondition> flatConditionalExpression = cmAppliesIf.flatConjuctiveConditionalExpression();
					for(R2OCondition condition : flatConditionalExpression) {
						if(condition.isDelegableCondition((R2OProperties) super.unfolderProperties)) {
							ZExp conditionUnfolded = this.unfoldCondition(condition);
							mainQuery.addWhere(conditionUnfolded);							
						} else {
							Collection<String> conditionColumns = condition.getInvolvedColumns();
							logger.debug("conditionColumns = " + conditionColumns);
							for(String conditionColumn : conditionColumns) {
								ZSelectItem zSelectItem = new ZSelectItem(conditionColumn);
								zSelectItem.setAlias(conditionColumn.replaceAll("\\.", "_"));
								mainQuery.getSelect().add(zSelectItem);
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
						mainQuery.getSelect().add(zSelectItem);
					}					
				}

			}
		}


		if(r2oConceptMapping.getOrderBy() != null && r2oConceptMapping.getOrderBy().size() != 0) {
			this.mainQuery.addOrderBy(new Vector<String>());
			for(String orderByString : r2oConceptMapping.getOrderBy()) {
				this.mainQuery.getOrderBy().add(orderByString);
			}
		}

		List<R2OPropertyMapping> propertyMappings = r2oConceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMappping : propertyMappings) {
				this.unfoldPropertyMapping(propertyMappping);
			}			
		}





		String selectSQL = "";
		Vector<ZSelectItem> mainQuerySelectItems = mainQuery.getSelect();
		for(ZSelectItem mainQuerySelectItem : mainQuerySelectItems) {
			ZExp selectItemExpression = mainQuerySelectItem.getExpression();
			String selectItemAlias = mainQuerySelectItem.getAlias();
			String selectItem = selectItemExpression + " AS " + selectItemAlias + ", ";
			selectSQL += selectItem; 
		}
		//remove the last coma and space
		selectSQL = selectSQL.substring(0, selectSQL.length() - 2);

		String fromSQL = "";
		Vector<ZFromItem> mainQueryFromItems = mainQuery.getFrom();
		for(ZFromItem mainQueryFromItem : mainQueryFromItems) {
			fromSQL += mainQueryFromItem.getTable();
			String mainQueryFromItemAlias = mainQueryFromItem.getAlias(); 
			if(mainQueryFromItemAlias != null && mainQueryFromItemAlias.length() > 0) {
				//fromSQL += " AS " + mainQueryFromItem.getAlias() + ", ";
				fromSQL += " " + mainQueryFromItem.getAlias() + ", ";
			} else {
				fromSQL += ", ";
			}
		}
		//remove the last coma and space
		fromSQL = fromSQL.substring(0, fromSQL.length() - 2);

		if(r2oConceptMapping.getRelationMappings() != null 
				&& r2oConceptMapping.getRelationMappings().size() != 0) {




			String whereSQL = null;
			String sql = "SELECT " + selectSQL + " FROM "+ fromSQL;

			if(this.leftJoinQueries != null && this.leftJoinQueries.size() != 0) {
				String leftJoinSQL = "";
				for(ZQuery leftJoinQuery : this.leftJoinQueries) {
					String leftJoinTable = leftJoinQuery.getFrom().toString();
					leftJoinTable = leftJoinTable.substring(1, leftJoinTable.length() - 1);
					leftJoinSQL += " LEFT JOIN " + leftJoinTable 
					+ " ON "+ " " + leftJoinQuery.getWhere();
				}
				sql += leftJoinSQL;
			}

			if(this.innerJoinQueries != null && this.innerJoinQueries.size() != 0) {
				String innerJoinSQL = "";
				for(ZQuery innerJoinQuery : this.innerJoinQueries) {
					String innerJoinTable = innerJoinQuery.getFrom().toString();
					innerJoinTable = innerJoinTable.substring(1, innerJoinTable.length() - 1);
					innerJoinSQL += " INNER JOIN " + innerJoinTable 
					+ " ON "+ " " + innerJoinQuery.getWhere();
				}
				sql += innerJoinSQL;
			}


			if(mainQuery.getWhere() != null) {
				whereSQL = mainQuery.getWhere().toString();
				sql += " WHERE " + whereSQL; 
			}

			return sql;

		} else {
			String sql = "SELECT " + selectSQL + " FROM "+ fromSQL;
			if(mainQuery.getWhere() != null) {
				String whereSQL = mainQuery.getWhere().toString();
				sql += " WHERE " + whereSQL; 
			}

			return sql.toString();
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


	public void setUnfolderProperties(Properties properties) {
		this.unfolderProperties = (R2OProperties) properties;
	}


}
