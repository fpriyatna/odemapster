package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidConditionOperationException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidRelationMappingException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidTransfomationExperessionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OJoinQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseView;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OJoin;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConceptRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTableRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.RestrictionValue;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ORelationMapping;

public class R2ORelationMappingUnfolder {
	private static Logger logger = Logger.getLogger(R2ORelationMappingUnfolder.class);

	private R2OConceptMapping parentMapping;
	//private R2OPrimitiveOperationsProperties primitiveOperationsProperties;
	private R2ORelationMapping relationMapping;
	//private R2OConfigurationProperties configurationProperties;
	private R2OConceptMapping rangeConceptMapping;

	public R2ORelationMappingUnfolder(R2OConceptMapping parentMapping,R2ORelationMapping relationMapping) {
		super();
		this.parentMapping = parentMapping;
		this.relationMapping = relationMapping;
	}

	private R2OQuery processHasView(R2ODatabaseView rmHasView) 
	throws RelationMappingUnfolderException, InvalidConditionOperationException, InvalidTransfomationExperessionException {
		Vector<R2ODatabaseTable> rangeTables = 
			this.rangeConceptMapping.getHasTables();
		R2OJoin joinsVia = this.relationMapping.getJoinsVia();

		R2OQuery viewQuery = new R2OQuery();

		Vector<ZSelectItem> viewQuerySelectItems = new Vector<ZSelectItem>();
		viewQuery.addSelect(viewQuerySelectItems);
		Vector<ZFromItem> viewQueryFromItems = new Vector<ZFromItem>();
		viewQuery.addFrom(viewQueryFromItems);


		ZFromItem viewQueryFromItem = new ZFromItem();
		String viewAlias = rmHasView.generateViewAlias();
		viewQueryFromItem.setAlias(viewAlias);


		//add uri-as columns to the view select items
		Collection<ZSelectItem> rangeURIAsSelectItems =
			this.rangeConceptMapping.getURIAs().getInvolvedExpression();
		viewQuerySelectItems.addAll(rangeURIAsSelectItems);

		//add joins-via columns to the view select items
		//the ones not corresponding to domain concept mapping has-table
		R2OConditionalExpression joinsViaCondition = 
			joinsVia.getJoinConditionalExpression();
		Collection<String> rmJoinsViaSelectItems =
			joinsViaCondition.getInvolvedColumns();
		String parentMappingHasTable = this.parentMapping.getHasTable().getName();
		parentMappingHasTable += ".";
		for(String rmJoinsViaSelectItem : rmJoinsViaSelectItems) {
			if(!rmJoinsViaSelectItem.startsWith(parentMappingHasTable)) {
				ZConstant zColumn = Utility.constructDatabaseColumn(rmJoinsViaSelectItem);
				ZSelectItem selectItem = new ZSelectItem();
				selectItem.setExpression(zColumn);
				viewQuerySelectItems.add(selectItem);
			}
		}

		R2ODatabaseView r2oView= relationMapping.getHasView();
		R2OJoin r2oViewJoin = r2oView.getJoinsVia();

		//add join query to the view
		R2OJoinQuery viewQueryJoinQuery = new R2OJoinQuery();
		viewQueryJoinQuery.setJoinType(r2oViewJoin.getJoinType());
		viewQuery.addSubQuery(viewQueryJoinQuery);
		
		
		R2ORestriction firstArgumentRestrictionValue = 
			r2oView.getArgRestricts().get(0)
			.getRestriction();
		if(firstArgumentRestrictionValue instanceof R2OTableRestriction) {
			R2OTableRestriction restrictionTable = (R2OTableRestriction) firstArgumentRestrictionValue;
			R2ODatabaseTable r2oDatabaseTable = restrictionTable.getDatabaseTable();
			ZFromItem zFromItem = new ZFromItem(r2oDatabaseTable.getName());
			if(r2oDatabaseTable.getAlias() != null) {
				zFromItem.setAlias(r2oDatabaseTable.getAlias());
			}
			viewQueryFromItems.add(zFromItem);
		} else if(firstArgumentRestrictionValue instanceof R2OConceptRestriction) {
			R2OConceptRestriction restrictionConcept = 
				(R2OConceptRestriction) firstArgumentRestrictionValue;
			String toConcept = this.relationMapping.getToConcept();
			if(!toConcept.equalsIgnoreCase(restrictionConcept.getConceptName())) {
				String errorMessage = "Invalid to-concept value!";
				logger.error(errorMessage);
				throw new RelationMappingUnfolderException(errorMessage);								
			}

			//add  range table to the join query
			if(rangeTables.size() > 1) {
				String errorMessage = "Multiple range tables is not supported!";
				logger.error(errorMessage);
				throw new RelationMappingUnfolderException(errorMessage);								
			}

			R2ODatabaseTable r2oDatabaseTable = rangeTables.get(0);
			ZFromItem zFromItem = new ZFromItem(r2oDatabaseTable.getName());
			if(r2oDatabaseTable.getAlias() != null) {
				zFromItem.setAlias(r2oDatabaseTable.getAlias());
			}
			viewQueryFromItems.add(zFromItem);
		} else {
			String errorMessage = "Invalid has-view elements!";
			logger.error(errorMessage);
			throw new RelationMappingUnfolderException(errorMessage);
		}



		R2ORestriction secondArgumentRestrictionValue = 
			relationMapping.getHasView().getArgRestricts().get(1)
			.getRestriction();
		if(secondArgumentRestrictionValue instanceof R2OTableRestriction) {
			R2OTableRestriction restrictionTable = (R2OTableRestriction) secondArgumentRestrictionValue;
			R2ODatabaseTable r2oDatabaseTable = restrictionTable.getDatabaseTable();
			String tableName = r2oDatabaseTable.getName();
			ZConstant zTableName = new ZConstant(tableName, ZConstant.UNKNOWN); 
			viewQueryJoinQuery.setJoinSource(zTableName);
		} else if(secondArgumentRestrictionValue instanceof R2OConceptRestriction) {
			R2OConceptRestriction restrictionConcept = 
				(R2OConceptRestriction) secondArgumentRestrictionValue;
			String toConcept = this.relationMapping.getToConcept();
			if(!toConcept.equalsIgnoreCase(restrictionConcept.getConceptName())) {
				String errorMessage = "Invalid to-concept value!";
				logger.error(errorMessage);
				throw new RelationMappingUnfolderException(errorMessage);								
			}

			//add  range table to the join query
			if(rangeTables.size() > 1) {
				String errorMessage = "Multiple range tables is not supported!";
				logger.error(errorMessage);
				throw new RelationMappingUnfolderException(errorMessage);								
			}
			ZConstant subQueryJoinQueryFromItem = 
				new ZConstant(rangeTables.get(0).getName(), ZConstant.UNKNOWN);
			viewQueryJoinQuery.setJoinSource(subQueryJoinQueryFromItem);
		} else {
			String errorMessage = "Invalid has-view elements!";
			logger.error(errorMessage);
			throw new RelationMappingUnfolderException(errorMessage);
		}


		R2OConditionalExpressionUnfolder condExprUnfolder = 
			new R2OConditionalExpressionUnfolder(r2oViewJoin.getJoinConditionalExpression());
		ZExpression joinsViaExpression = condExprUnfolder.unfoldDelegableConditionalExpression();
		viewQueryJoinQuery.setOnExpression(joinsViaExpression);

		return viewQuery;

	}

	private ZExpression unfoldRangeAppliesIf(R2OConditionalExpression rangeAppliesIf) 
	throws InvalidConditionOperationException, InvalidTransfomationExperessionException, InvalidRelationMappingException {
		ZExpression rangeAppliesIfExpression = null;

		if(rangeAppliesIf.isDelegableConditionalExpression()) {
			R2OConditionalExpressionUnfolder r2oConditionalExpressionUnfolder = 
				new R2OConditionalExpressionUnfolder(rangeAppliesIf);

			ZExpression rangeConceptConditionalExpression = 
				r2oConditionalExpressionUnfolder.unfoldDelegableConditionalExpression(); 
			if(rangeConceptConditionalExpression != null) {
				rangeAppliesIfExpression = rangeConceptConditionalExpression;
			}				
		} else {
			String errorMessage = 
				"Unsupported relation mapping, range concept mapping applies if is not a delegable conditional expression.";
			logger.error(errorMessage);
			throw new InvalidRelationMappingException(errorMessage);
		}

		return rangeAppliesIfExpression;
	}

	private ZExpression unfoldJoinsVia(R2OJoin joinsVia) 
	throws InvalidConditionOperationException, InvalidTransfomationExperessionException, InvalidRelationMappingException {
		R2OConditionalExpression joinCondition = joinsVia.getJoinConditionalExpression();
		ZExpression joinsViaExp = null;
		if(joinCondition.isDelegableConditionalExpression()) {
			R2OConditionalExpressionUnfolder r2oConditionalExpressionUnfolder = 
				new R2OConditionalExpressionUnfolder(joinCondition);
			joinsViaExp = r2oConditionalExpressionUnfolder.unfoldDelegableConditionalExpression();
		} else {
			throw new InvalidRelationMappingException("Invalid joins-via expression");
		}


		return joinsViaExp;
	}

	private R2OQuery processToConcept(R2OQuery cmQuery) throws Exception {
		String cmBaseTable = this.parentMapping.getHasTables().get(0).getName();
		
		R2OJoinQuery rmQuery = new R2OJoinQuery();
		cmQuery.addSubQuery(rmQuery);
		rmQuery.setJoinType(this.relationMapping.getJoinsVia().getJoinType());

		//unfold range URI
		Collection<ZSelectItem> rangeURISelectItems = this.unfoldRangeURI();


		//unfold range applies-if && joins-via
		R2OConditionalExpression rangeAppliesIf = rangeConceptMapping.getAppliesIf();
		ZExpression rangeAppliesIfExpression = null;
		if(rangeAppliesIf != null) {
			rangeAppliesIfExpression = this.unfoldRangeAppliesIf(rangeAppliesIf);
		}
		R2OJoin joinsVia = relationMapping.getJoinsVia();
		rmQuery.setJoinType(joinsVia.getJoinType());
		ZExpression joinsViaExpression = this.unfoldJoinsVia(joinsVia);
		ZExpression rmQueryOnExpression = null;
		if(rangeAppliesIf == null) {
			rmQueryOnExpression = joinsViaExpression;
		} else {
			rmQueryOnExpression = new ZExpression("AND", joinsViaExpression, rangeAppliesIfExpression);
		}


		R2ODatabaseView rmHasView = this.relationMapping.getHasView();
		if(rmHasView == null) {
			Vector<R2ODatabaseTable> rangeTables = this.rangeConceptMapping.getHasTables();
			if(rangeTables.size() > 1) {
				String errorMessage = "Unsupported multiple tables in the range concept mapping!";
				throw new RelationMappingUnfolderException(errorMessage);
			}
			R2ODatabaseTable rangeTable = rangeTables.get(0);
			String rtAlias = rangeTable.getAlias();
			if(rtAlias == null || rtAlias == "") {
				rtAlias = R2OConstants.RANGE_TABLE_ALIAS + this.relationMapping.hashCode();
			}
			
			ZConstant joinSource = new ZConstant(
					rangeTable.getName(), ZConstant.UNKNOWN);
			rmQuery.setJoinSource(joinSource);
			rmQuery.setJoinSourceAlias(rtAlias);

			ZExpression onExpressionRenamed = Utility.renameColumnsIfNotMatch(
					rmQueryOnExpression, cmBaseTable, rtAlias);
			rmQuery.setOnExpression(onExpressionRenamed);


			Collection<ZSelectItem> rangeURISelectItemsRenamed = 
				Utility.renameColumnsIfNotMatch(rangeURISelectItems, cmBaseTable, rtAlias);
			cmQuery.getSelect().addAll(rangeURISelectItemsRenamed);
			
			/*
			if(rangeTable.getAlias() != null && rangeTable.getAlias() != "") {
				Collection<ZSelectItem> rangeURISelectItemsRenamed = 
					Utility.renameColumnsIfNotMatch(rangeURISelectItems, cmBaseTable, rangeTable.getAlias());
				cmQuery.getSelect().addAll(rangeURISelectItemsRenamed);
			} else {
				cmQuery.getSelect().addAll(rangeURISelectItems);
			}
			*/

		} else {

			//process has-view
			R2OQuery rmViewQuery = this.processHasView(rmHasView);
			rmQuery.setJoinSource(rmViewQuery);
			rmQuery.setJoinSourceAlias(rmHasView.generateViewAlias());
			
			ZExpression onExpressionRenamed = Utility.renameColumnsIfNotMatch(
					rmQueryOnExpression, cmBaseTable, rmHasView.generateViewAlias());

			rmQuery.setOnExpression(onExpressionRenamed);

			Collection<ZSelectItem> rangeURISelectItemsRenamed = 
				Utility.renameColumnsIfNotMatch(rangeURISelectItems, cmBaseTable, rmHasView.generateViewAlias());
			
			cmQuery.getSelect().addAll(rangeURISelectItemsRenamed);
		}


		return cmQuery;
	}


	public R2OQuery unfold(R2OQuery cmQuery) 
	throws RelationMappingUnfolderException {
		String toConcept = this.relationMapping.getToConcept();

		try {
			//process to-concept
			if(toConcept != null) {
				cmQuery = this.processToConcept(cmQuery);
			}

			//process selectors
			if(relationMapping.getRmSelectors() != null) {
				Collection<ZSelectItem> selectorSelectItems = this.unfoldRelationMappingSelectors(); 
				cmQuery.getSelect().addAll(selectorSelectItems);
			}

		} catch(InvalidRelationMappingException e) {
			String errorMessage = "Error unfolding relation mapping : " + this.relationMapping.getName();
			logger.error(errorMessage);
			throw new RelationMappingUnfolderException(errorMessage);
		} catch (InvalidTransfomationExperessionException e) {
			String errorMessage = "Error unfolding relation mapping : " + this.relationMapping.getName();
			logger.error(errorMessage);
			throw new RelationMappingUnfolderException(errorMessage);
		} catch (InvalidConditionOperationException e) {
			String errorMessage = "Error unfolding relation mapping : " + this.relationMapping.getName();
			logger.error(errorMessage);
			throw new RelationMappingUnfolderException(errorMessage);
		} catch(Exception e) {
			e.printStackTrace();
			String errorMessage = "Error unfolding relation mapping : " + this.relationMapping.getName();
			logger.error(errorMessage);
			throw new RelationMappingUnfolderException(errorMessage);			
		}

		return cmQuery;


	}

	public void setRangeConceptMapping(R2OConceptMapping rangeConceptMapping) {
		this.rangeConceptMapping = rangeConceptMapping;
	}

	private Collection<ZSelectItem> unfoldRelationMappingSelectors() 
	throws InvalidConditionOperationException, InvalidTransfomationExperessionException {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();

		Collection<R2OSelector> selectors = relationMapping.getRmSelectors();
		if(selectors != null) {
			for(R2OSelector selector : selectors) {
				//processing applies-if element of the selector
				R2OConditionalExpression selectorAppliesIf = selector.getAppliesIf();
				R2OConditionalExpressionUnfolder r2oConditionalExpressionUnfolder =
					new R2OConditionalExpressionUnfolder(selectorAppliesIf);
				String appliesIfAlias = selector.generateAppliesIfAlias();
				Collection<ZSelectItem> appliesIfSelectItems = 
					r2oConditionalExpressionUnfolder.unfold(appliesIfAlias);

				result.addAll(appliesIfSelectItems);

				//processing after-transform element of the selector
				R2OTransformationExpression attSelectorAfterTransform = selector.getAfterTransform();
				R2OTransformationExpressionUnfolder r2oTransformationExpressionUnfolder =
					new R2OTransformationExpressionUnfolder(attSelectorAfterTransform);
				String afterTransformAlias = R2OConstants.AFTERTRANSFORM_ALIAS + selector.hashCode();
				Collection<ZSelectItem> afterTransformSelectItems = 
					r2oTransformationExpressionUnfolder.unfold(afterTransformAlias);
				result.addAll(afterTransformSelectItems);
			}			
		}

		return result;
	}

	private Collection<ZSelectItem> unfoldRangeURI() 
	throws InvalidRelationMappingException, InvalidTransfomationExperessionException, InvalidConditionOperationException {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();

		if(this.rangeConceptMapping == null) {
			String toConcept = relationMapping.getToConcept();
			String errorMessage = "Concept mapping with id " + toConcept + " is not defined!";
			throw new InvalidRelationMappingException(errorMessage);
		}


		//this.processURIAs(rangeConceptMapping, r2oRelationMapping.getId());
		R2OTransformationExpression rangeURIAsTransformationExpression = 
			rangeConceptMapping.getURIAs();

		String uriAsOperator = rangeURIAsTransformationExpression.getOperId();
		if(uriAsOperator != null) {
			if(rangeURIAsTransformationExpression.isDelegableTransformationExpression()) {
				R2OTransformationExpressionUnfolder r2oTransformationExpressionUnfolder = 
					new R2OTransformationExpressionUnfolder(rangeURIAsTransformationExpression);
				String alias = R2OConstants.RELATIONMAPPING_ALIAS + relationMapping.hashCode();
				result.addAll(r2oTransformationExpressionUnfolder.unfold(alias));
			} else {
				String errorMessage = "Unsupported transformation operation : " + uriAsOperator;
				throw new InvalidRelationMappingException("Unsupported transformation operation : " + uriAsOperator);
			}
		}
		return result;
	}


}
