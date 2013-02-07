package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.exception.InsatisfiableSQLExpression;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResultUnion;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.BetaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.BetaResultSet;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.CondSQLResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslatorUtility;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap.TermMapType;

public class R2RMLQueryTranslator extends AbstractQueryTranslator {
	private static Logger logger = Logger.getLogger(R2RMLQueryTranslator.class);
	
	static Map<Triple, String> mapTripleAlias= new HashMap<Triple, String>();
	private Map<String, Matcher> mapTemplateMatcher = new HashMap<String, Matcher>();
	private Map<String, Collection<String>> mapTemplateAttributes = new HashMap<String, Collection<String>>();

	public R2RMLQueryTranslator() {
		super();
		AbstractUnfolder unfolder = new R2RMLElementUnfoldVisitor();
		this.unfolder = unfolder;
	}

	public static AbstractQueryTranslator createQueryTranslator(
			String mappingDocumentPath) throws Exception {
		AbstractMappingDocument mappingDocument = 
				new R2RMLMappingDocument(mappingDocumentPath);
		AbstractQueryTranslator queryTranslator = 
				new R2RMLQueryTranslator();
		queryTranslator.setMappingDocument(mappingDocument);
		
		return queryTranslator;
	}



	@Override
	protected String generateTermCName(Node termC) {
		String termCName = super.getNameGenerator().generateName(termC);
		return termCName;
	}

	@Override
	protected ZExp transIRI(Node node) {
		ZExp result = null;

		Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(node);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();

		boolean hasWellDefinedURI = cm.hasWellDefinedURIExpression();
		logger.debug("hasWellDefinedURI = " + hasWellDefinedURI);
		if(hasWellDefinedURI) {
			String pkValue = cm.getSubjectMap().getTemplateValue(node.getURI());
			if(pkValue == null) {
				ZConstant constant0 = new ZConstant("0", ZConstant.NUMBER);
				ZConstant constant1 = new ZConstant("1", ZConstant.NUMBER);
				result = new ZExpression("=", constant0, constant1);
				
			} else {
				result = new ZConstant(pkValue, ZConstant.UNKNOWN);	
			}
			
		}

		return result;
	}


	private SQLQuery trans(Triple tp, AlphaResult alphaResult, BetaResult betaResult
			, AbstractConceptMapping cm) throws Exception {
		SQLQuery result = new SQLQuery();
		//result.setComments("Query from TriplesMap : " + cm.toString() + " with predicate " + betaResult.getPredicateURI());
		
		SQLLogicalTable alphaSubject = alphaResult.getAlphaSubject();
		Collection<SQLQuery> alphaPredicateObjects = alphaResult.getAlphaPredicateObjects();
		logger.debug("alpha logicalTable = " + alphaSubject);
		result.addLogicalTable(alphaSubject);//alpha from subject
		if(alphaPredicateObjects != null && !alphaPredicateObjects.isEmpty()) {
			for(SQLQuery alphaPredicateObject : alphaPredicateObjects) {
				result.addJoinQuery(alphaPredicateObject);//alpha predicate object
				logger.debug("alphaPredicateObject = " + alphaPredicateObject);
			}
		}


		//PRSQL
		Collection<ZSelectItem> selectItems = super.getPrSQLGenerator().genPRSQL(
				tp, betaResult, super.getNameGenerator(), cm);
		result.setSelectItems(selectItems);

		//CondSQL
		CondSQLResult condSQL = super.getCondSQLGenerator().genCondSQL(tp, alphaResult, betaResult, cm);
		logger.debug("condSQL = " + condSQL);
		if(condSQL != null) {
			result.addWhere(condSQL.getExpression());
		}

		//subquery elimination
		IQueryTranslationOptimizer optimizer = super.getOptimizer();
		if(optimizer != null && optimizer.isSubQueryElimination()) {
			result = QueryTranslatorUtility.eliminateSubQuery(result);
		}

		logger.debug("transTP = " + result);
		return result;
	}

	@Override
	protected SQLQuery trans(Triple tp, AbstractConceptMapping cm) throws QueryTranslationException {
		SQLQuery result = null;
		try {
			Node tpPredicate = tp.getPredicate();

			if(tpPredicate.isURI() && RDF.type.getURI().equals(tpPredicate.getURI()) 
					&& (this.isIgnoreRDFTypeStatement())) {
				result = null;
			} else {
				AlphaResultUnion alphaResultSet = super.getAlphaGenerator().calculateAlpha(tp, cm);
				BetaResultSet betaResultSet = super.getBetaGenerator().calculateBeta(tp, cm, alphaResultSet);
				if(alphaResultSet != null && alphaResultSet != null) {
					if(alphaResultSet.size() != betaResultSet.size()) {
						String errorMessage = "Number of alpha is not consistent with number of beta.";
						throw new QueryTranslationException(errorMessage);
					}

					Collection<SQLQuery> unionSQLQueries = new Vector<SQLQuery>();
					for(int i=0; i<betaResultSet.size(); i++) {
						AlphaResult alphaResult = alphaResultSet.get(i);
						BetaResult betaResult = betaResultSet.get(i);
						try {
							SQLQuery sqlQuery = this.trans(tp, alphaResult, betaResult, cm);
							logger.debug("sqlQuery("+ i +") = " + sqlQuery);
							unionSQLQueries.add(sqlQuery);							
						} catch(InsatisfiableSQLExpression e) {
							logger.warn("Insatisfiable expression : " + e.getMessage());
						}
					}

					if(!unionSQLQueries.isEmpty()) {
						Iterator<SQLQuery> it = unionSQLQueries.iterator(); 
						result = it.next();
						while(it.hasNext()) {
							result.addUnionQuery(it.next());
						}						
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in transTP : " + tp);
			throw new QueryTranslationException(e.getMessage(), e);
		}

		return result;
	}


	//	@Override
	//	public AbstractQueryTranslator createQueryTranslator(
	//			AbstractMappingDocument mappingDocument
	//			, AbstractUnfolder unfolder) {
	//		AbstractQueryTranslator queryTranslator = 
	//				new R2RMLQueryTranslator(mappingDocument, unfolder);
	//		return queryTranslator;
	//	}









	@Override
	protected void buildAlphaGenerator() {
		super.setAlphaGenerator(new R2RMLAlphaGenerator(this));
	}




	@Override
	protected void buildBetaGenerator() {
		super.setBetaGenerator(new R2RMLBetaGenerator(this));
	}




	@Override
	protected void buildCondSQLGenerator() {
		super.setCondSQLGenerator(new R2RMLCondSQLGenerator(this));
	}




	@Override
	protected void buildPRSQLGenerator() {
		super.setPrSQLGenerator(new R2RMLPRSQLGenerator(this));
	}

	@Override
	public String translateResultSet(String columnLabel, String dbValue) {
		String result = dbValue;
		
		try {
			if(dbValue != null) {
				Map<String, Object> mapNodeMapping = this.getMapVarMapping();
				Object mapValue = mapNodeMapping.get(columnLabel);;
				R2RMLTermMap termMap = null;
				
				if(mapValue instanceof R2RMLTermMap) {
					termMap = (R2RMLTermMap) mapValue;
				} else if(mapValue instanceof R2RMLRefObjectMap) {
					R2RMLRefObjectMap refObjectMap = (R2RMLRefObjectMap) mapValue;
					termMap = refObjectMap.getParentTriplesMap().getSubjectMap();
				} else {
					logger.warn("undefined type of mapping!");
				}
				
				if(termMap != null) {
					if(termMap.getTermMapType() == TermMapType.TEMPLATE) {
						String template = termMap.getTemplate();
						Matcher matcher = this.mapTemplateMatcher.get(template);
						if(matcher == null) {
							Pattern pattern = Pattern.compile(R2RMLConstants.R2RML_TEMPLATE_PATTERN);
							matcher = pattern.matcher(template);
							this.mapTemplateMatcher.put(template, matcher);
						}
						Collection<String> attributes = this.mapTemplateAttributes.get(template);
						if(attributes == null) {
							attributes = R2RMLUtility.getAttributesFromStringTemplate(template);
							this.mapTemplateAttributes.put(template, attributes);
						}
						
						Map<String, String> replacements = new HashMap<String, String>();
						replacements.put(attributes.iterator().next(), dbValue);
						result = R2RMLUtility.replaceTokens(template, replacements);
					}					
				}

			}			
		} catch(Exception e) {
			logger.error("Error occured while translating result set!");
		}
		
		return result;
	}
}
