package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslatorUtility;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.TypeInferrer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLJoinCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLQueryTranslator extends AbstractQueryTranslator {
	private static Logger logger = Logger.getLogger(R2RMLQueryTranslator.class);
	
	public R2RMLQueryTranslator(AbstractMappingDocument mappingDocument) {
		super(mappingDocument);
	}

	@Override
	protected ZExpression genCondSQLPredicateObject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		ZExpression result;
		
		Node tpSubject = tp.getSubject();
		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(tpSubject).iterator().next();
		ZExpression result1 = this.genCondSQLPredicateObject(tp, betaGenerator, cmSubject);
		
		Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
		String logicalTableAlias = cm.getAlias();
		Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(tp.getPredicate().getURI());
		Node tpObject = tp.getObject();
		
		ZExpression result2 = null;
		R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
		if(tpObject.isLiteral()) {
			Object objectLiteralValue = tpObject.getLiteral().getValue();
			R2RMLObjectMap objectMap = pm.getObjectMap();
			String columnName = objectMap.getColumnName();
			if(columnName != null) {
				ZConstant columnConstant = new ZConstant(logicalTableAlias + "." + columnName,  ZConstant.COLUMNNAME);
				ZConstant objectLiteral = new ZConstant(objectLiteralValue.toString(), ZConstant.STRING);
				result2 = new ZExpression("=", columnConstant, objectLiteral);
			}
		}
		
		result = (ZExpression) QueryTranslatorUtility.combineExpressions(result1, result2);
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
			String logicalTableAlias = cm.getAlias();
			
			boolean hasWellDefinedURI = cm.hasWellDefinedURIExpression();
			logger.info("hasWellDefinedURI = " + hasWellDefinedURI);
			if(hasWellDefinedURI) {
				result2 = this.generateCondForWellDefinedURI(cm, tpSubject.getURI(), logicalTableAlias);
			}
		}

		
		ZExpression result = (ZExpression) QueryTranslatorUtility.combineExpressions(result1, result2);
		return result;
		
	}

	@Override
	protected Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Node tpSubject = tp.getSubject();
		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(tpSubject).iterator().next();
		return this.genPRSQL(tp, betaGenerator, nameGenerator, cmSubject);
	}


	@Override
	protected Collection<ZSelectItem> genPRSQLSubject(Node subject, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject) throws Exception {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		ZSelectItem selectItemSubject = this.betaGenerator.calculateBetaCMSubject(cmSubject);
		result.add(selectItemSubject);
		String selectItemSubjectAlias = this.nameGenerator.generateName(tp, subject); 
		selectItemSubject.setAlias(selectItemSubjectAlias);
		logger.info("genPRSQLSubject = " + result);
		return result;
	}





	@Override
	protected ZExp transConstant(NodeValue nodeValue) {
		// TODO Auto-generated method stub
		logger.warn("Implement this!");
		return null;
	}


	@Override
	public SQLQuery translate(Query sparqlQuery) throws Exception {
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);
		
		TypeInferrer typeInferrer = new TypeInferrer(super.mappingDocument);
		super.mapInferredTypes = typeInferrer.infer(opQueryPattern);

		this.alphaGenerator = 
				new R2RMLAlphaGenerator(mapInferredTypes, this.mappingDocument);
		
		this.betaGenerator = new R2RMLBetaGenerator(mapInferredTypes, mappingDocument);
		
		this.trans(opQueryPattern);
		return null;
	}

	@Override
	public SQLQuery translate(String queryFilePath) throws Exception {
		//process SPARQL file
		logger.info("Parsing query file : " + queryFilePath);
		Query sparqlQuery = QueryFactory.read(queryFilePath);
		logger.debug("sparqlQuery = " + sparqlQuery);
		
		return this.translate(sparqlQuery);
	}

	@Override
	protected SQLQuery transProject(Op opQuery) throws Exception {
		// TODO Auto-generated method stub
		logger.warn("Implement this!");
		return null;
	}

	@Override
	protected SQLQuery transTB(List<Triple> triples) throws Exception {
		// TODO Auto-generated method stub
		logger.warn("Implement this!");
		return null;
	}

	private R2RMLPredicateObjectMap getPredicateObjectMap(Node tpSubject, String propertyURI) {
		Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
		Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(propertyURI);
		return (R2RMLPredicateObjectMap) pms.iterator().next();
	}
	
	@Override
	protected SQLQuery transTP(Triple tp) throws QueryTranslationException {
		SQLQuery result = new SQLQuery();
		try {
			Node tpSubject = tp.getSubject();
			Node tpPredicate = tp.getPredicate();
			String propertyURI = tpPredicate.getURI();
			Node tpObject = tp.getObject();
			
			if(RDF.type.getURI().equals(tpPredicate.getURI())) {
				return null;
			} else {
				Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(tpSubject);
				if(cms != null) {
					//alpha
					R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
					SQLLogicalTable logicalTable = (SQLLogicalTable) this.alphaGenerator.calculateAlpha(tp);
					result.addLogicalTable(logicalTable);
					String logicalTableAlias = logicalTable.getAlias();
					cm.setAlias(logicalTableAlias);
					logger.info("logicalTable = " + logicalTable);

					Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(propertyURI);
					if(pms != null) {
						R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
						logger.info("pm = " + pm);
						R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
						if(refObjectMap != null) { 
							SQLQuery joinQuery = new SQLQuery();
							joinQuery.setJoinType("INNER");
							String joinQueryAlias = joinQuery.generateAlias();
							joinQuery.setAlias(joinQueryAlias);
							refObjectMap.setAlias(joinQueryAlias);
							R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
							SQLLogicalTable sqlParentLogicalTable = new R2RMLElementUnfoldVisitor().visit(parentLogicalTable);
							joinQuery.addLogicalTable(sqlParentLogicalTable);
							
							Collection<R2RMLJoinCondition> joinConditions = refObjectMap.getJoinConditions();
							ZExp onExpression = R2RMLUtility.generateJoinCondition(joinConditions, logicalTableAlias, joinQueryAlias);
							if(onExpression != null) {
								joinQuery.setOnExp(onExpression);
							}
							result.addJoinQuery(joinQuery);
							
							if(tpObject.isURI()) {
								R2RMLTriplesMap parentTriplesMap = refObjectMap.getParentTriplesMap();
								boolean hasWellDefinedURI = parentTriplesMap.hasWellDefinedURIExpression();
								logger.info("hasWellDefinedURI = " + hasWellDefinedURI);
								if(hasWellDefinedURI) {
									ZExp objectURICondition = this.generateCondForWellDefinedURI(parentTriplesMap, tpObject.getURI(), joinQueryAlias);
									if(objectURICondition != null) {
										joinQuery.addOn(objectURICondition);
									}
								}

								

							}
						} else { //refObjectMap == null

						}
						
					}

					//PRSQL
					Collection<ZSelectItem> selectItems = this.genPRSQL(tp, betaGenerator, nameGenerator);
					result.setSelectItems(selectItems);

					//CondSQL
					ZExpression condSQL = this.genCondSQL(tp);
					if(condSQL != null) {
						result.addWhere(condSQL);
					}

					

				} else {
					String errorMessage = "Undefined triplesMap for class : " + tpSubject;
					throw new QueryTranslationException(errorMessage);
				}
				
				
				
			}
			
		} catch (Exception e) {
			throw new QueryTranslationException(e.getMessage(), e);
		}
		
		logger.info("transTP = " + result);
		logger.warn("Implement this!");
		return result;
	}

	@Override
	protected ZExp transVar(Op op, Var var) {
		// TODO Auto-generated method stub
		logger.warn("Implement this!");
		return null;
	}
	
	protected ZExp generateCondForWellDefinedURI(R2RMLTriplesMap triplesMap, String uri, String alias) {
		ZExp result = null;
		
		boolean hasWellDefinedURI = triplesMap.hasWellDefinedURIExpression();
		logger.info("hasWellDefinedURI = " + hasWellDefinedURI);
		if(hasWellDefinedURI) {
			String pkColumnString = triplesMap.getPKColumn();
			String pkValue = triplesMap.getPKValue(uri);
			ZConstant pkColumnConstant = new ZConstant(alias + "." + pkColumnString, ZConstant.COLUMNNAME);
			ZConstant pkValueConstant = new ZConstant(pkValue, ZConstant.UNKNOWN);
			result = new ZExpression("=", pkColumnConstant, pkValueConstant);
			logger.info("result = " + result);
		}
		
		return result;
		
	}

}
