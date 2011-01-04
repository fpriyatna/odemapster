package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OJoin;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConceptRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSQLRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.RestrictionValue;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ORelationMapping;


public class R2OUnfolder extends AbstractUnfolder {
	private R2OMappingDocument r2oMappingDocument;

	private static Logger logger = Logger.getLogger(R2OUnfolder.class);

	private R2OPrimitiveOperationsProperties primitiveOperationsProperties;
	private R2OConfigurationProperties configurationProperties;

	public R2OUnfolder(R2OMappingDocument r2oMappingDocument) {
		this.r2oMappingDocument = r2oMappingDocument;
	}

	public R2OUnfolder(R2OMappingDocument r2oMappingDocument
			, R2OPrimitiveOperationsProperties primitiveOperationsProperties
			, R2OConfigurationProperties configurationProperties) {
		this(r2oMappingDocument);
		this.primitiveOperationsProperties = primitiveOperationsProperties;
		this.configurationProperties = configurationProperties;
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
	public Set<String> unfold(Set<ILogicalQuery> logicalQueries,
			IMappingDocument mapping) throws Exception {

		throw new Exception("Not implemented yet!");
	}

	@Override
	protected String unfold(IMappingDocument mapping) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String unfoldConceptMapping(AbstractConceptMapping mapping)
			throws Exception {
		R2OQuery mainQuery = new R2OQuery();
		Collection<ZQuery> leftJoinQueries = new Vector<ZQuery>();
		Collection<ZQuery> innerJoinQueries = new Vector<ZQuery>();

		
		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) mapping;
		R2OConceptMappingUnfolder r2oConceptMappingUnfolder = new R2OConceptMappingUnfolder(
				primitiveOperationsProperties, configurationProperties, r2oConceptMapping, r2oMappingDocument);
		String conceptMappingUnfoldingSQL = 
			r2oConceptMappingUnfolder.unfoldConceptMapping(mainQuery);
		
		// TODO Auto-generated method stub
		return conceptMappingUnfoldingSQL;
	}





}
