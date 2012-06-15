package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractCondSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslatorUtility;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLJoinCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap.TermMapType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLCondSQLGenerator extends AbstractCondSQLGenerator {
	private static Logger logger = Logger.getLogger(R2RMLCondSQLGenerator.class);

	public R2RMLCondSQLGenerator(AbstractBetaGenerator betaGenerator,
			Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes) {
		super(betaGenerator, mapInferredTypes);
	}



	@Override
	protected ZExpression genCondSQLPredicateObject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		ZExpression result;

		Node tpSubject = tp.getSubject();
		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(tpSubject).iterator().next();
		ZExpression result1 = this.genCondSQLPredicateObject(tp, betaGenerator, cmSubject);

		Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
//		String logicalTableAlias = cm.getAlias();
		String logicalTableAlias = cm.getLogicalTable().getAlias();
		Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(tp.getPredicate().getURI());
		Node tpObject = tp.getObject();

		ZExpression result2 = null;
		R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
		R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
		R2RMLObjectMap objectMap = pm.getObjectMap();
		
		if(tpObject.isLiteral()) {
			Object objectLiteralValue = tpObject.getLiteral().getValue();
			String columnName = objectMap.getColumnName();
			if(columnName != null) {
				ZConstant columnConstant = new ZConstant(logicalTableAlias + "." + columnName,  ZConstant.COLUMNNAME);
				ZConstant objectLiteral = new ZConstant(objectLiteralValue.toString(), ZConstant.STRING);
				result2 = new ZExpression("=", columnConstant, objectLiteral);
			}
			
		} else if(tpObject.isURI()) {
			if(refObjectMap == null) {
				String uri = tpObject.getURI();
				TermMapType termMapType = objectMap.getTermMapType();
				if(termMapType == TermMapType.TEMPLATE) {
					result2 = this.generateCondForWellDefinedURI(objectMap, uri, logicalTableAlias);
				}
			} else {
				//String refObjectMapAlias = refObjectMap.getAlias();
				String refObjectMapAlias = R2RMLQueryTranslator.mapTripleAlias.get(tp);
				
				Collection<R2RMLJoinCondition> joinConditions = refObjectMap.getJoinConditions();
				//ZExp onExpression = R2RMLUtility.generateJoinCondition(joinConditions, logicalTableAlias, refObjectMapAlias);
				// onExpression done in alpha generator
				ZExp onExpression = null; 
						
				ZExp uriCondition = null;
				if(tpObject.isURI()) {
					R2RMLTriplesMap parentTriplesMap = 
							refObjectMap.getParentTriplesMap();
					boolean hasWellDefinedURI = 
							parentTriplesMap.hasWellDefinedURIExpression();
					logger.debug("hasWellDefinedURI = " + hasWellDefinedURI);
					if(hasWellDefinedURI) {
						uriCondition = this.generateCondForWellDefinedURI(
								parentTriplesMap, tpObject.getURI(),
								refObjectMapAlias);
					}
				}

				result2 = (ZExpression) QueryTranslatorUtility.combineExpressions(
						onExpression, uriCondition);
			}
			
		}
		
//		if(refObjectMap == null) {
//			
//			TermMapType termMapType = objectMap.getTermMapType();
//
//			if(tpObject.isLiteral()) {
//				Object objectLiteralValue = tpObject.getLiteral().getValue();
//				String columnName = objectMap.getColumnName();
//				if(columnName != null) {
//					ZConstant columnConstant = new ZConstant(logicalTableAlias + "." + columnName,  ZConstant.COLUMNNAME);
//					ZConstant objectLiteral = new ZConstant(objectLiteralValue.toString(), ZConstant.STRING);
//					result2 = new ZExpression("=", columnConstant, objectLiteral);
//				}
//			} else if(tpObject.isURI()) {
//				String uri = tpObject.getURI();
//				if(termMapType == TermMapType.TEMPLATE) {
//					result2 = this.generateCondForWellDefinedURI(objectMap, uri, logicalTableAlias);
//				}
//
//			}
//		} else {//refObjectMap != null
//			String refObjectMapAlias = refObjectMap.getAlias();
//			Collection<R2RMLJoinCondition> joinConditions = refObjectMap.getJoinConditions();
//			ZExp onExpression = R2RMLUtility.generateJoinCondition(joinConditions, logicalTableAlias, refObjectMapAlias);
//
//			ZExp uriCondition = null;
//			if(tpObject.isURI()) {
//				R2RMLTriplesMap parentTriplesMap = refObjectMap.getParentTriplesMap();
//				boolean hasWellDefinedURI = parentTriplesMap.hasWellDefinedURIExpression();
//				logger.debug("hasWellDefinedURI = " + hasWellDefinedURI);
//				if(hasWellDefinedURI) {
//					uriCondition = this.generateCondForWellDefinedURI(parentTriplesMap, tpObject.getURI(), refObjectMapAlias);
//				}
//			}
//
//			result2 = (ZExpression) QueryTranslatorUtility.combineExpressions(onExpression, uriCondition);
//
//		}




		result = (ZExpression) QueryTranslatorUtility.combineExpressions(
				result1, result2);
		return result;
	}

	@Override
	protected ZExpression genCondSQLSubject(Triple tp,
			AbstractBetaGenerator betaGenerator) throws Exception {
		Node tpSubject = tp.getSubject();
		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(tpSubject).iterator().next();
		ZExp result1 = this.genCondSQLSubject(tp, betaGenerator, cmSubject);

		ZExp result2 = null;
		if(tpSubject.isURI()) {
			Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(tpSubject);
			R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
//			String logicalTableAlias = cm.getAlias();
			String logicalTableAlias = cm.getLogicalTable().getAlias();

			boolean hasWellDefinedURI = cm.hasWellDefinedURIExpression();
			logger.debug("hasWellDefinedURI = " + hasWellDefinedURI);
			if(hasWellDefinedURI) {
				result2 = this.generateCondForWellDefinedURI(cm, tpSubject.getURI(), logicalTableAlias);
			}
		}


		ZExpression result = (ZExpression) QueryTranslatorUtility.combineExpressions(result1, result2);
		return result;

	}

	protected ZExpression generateCondForWellDefinedURI(R2RMLTermMap termMap, String uri, String alias) {
		ZExpression result = null;

		if(termMap.hasWellDefinedURIExpression()) {
			String pkColumnString = termMap.getTemplateColumn();
			String pkValue = termMap.getTemplateValue(uri);
			ZConstant pkColumnConstant = new ZConstant(alias + "." + pkColumnString, ZConstant.COLUMNNAME);
			ZConstant pkValueConstant;
			if(Utility.isInteger(pkValue)) {
				pkValueConstant = new ZConstant(pkValue, ZConstant.NUMBER);
			} else {
				pkValueConstant = new ZConstant(pkValue, ZConstant.STRING);
			}
			
			result = new ZExpression("=", pkColumnConstant, pkValueConstant);
		}

		logger.debug("generateCondForWellDefinedURI = " + result);
		return result;
	}

	protected ZExp generateCondForWellDefinedURI(R2RMLTriplesMap triplesMap, String uri, String alias) {
		ZExp result = null;

		boolean hasWellDefinedURI = triplesMap.hasWellDefinedURIExpression();
		logger.debug("hasWellDefinedURI = " + hasWellDefinedURI);
		if(hasWellDefinedURI) {
			String pkColumnString = triplesMap.getSubjectMap().getTemplateColumn();
			String pkValue = triplesMap.getSubjectMap().getTemplateValue(uri);
			String dbType = AbstractRunner.configurationProperties.getDatabaseType();
			SQLSelectItem pkColumnSelectItem = SQLSelectItem.createSQLItem(
					dbType, alias + "." + pkColumnString);
			ZConstant pkColumnConstant = new ZConstant(
					pkColumnSelectItem.toString(), ZConstant.COLUMNNAME);
			ZConstant pkValueConstant = new ZConstant(pkValue, ZConstant.UNKNOWN);
			result = new ZExpression("=", pkColumnConstant, pkValueConstant);
		}

		logger.debug("generateCondForWellDefinedURI = " + result);
		return result;
	}
}
