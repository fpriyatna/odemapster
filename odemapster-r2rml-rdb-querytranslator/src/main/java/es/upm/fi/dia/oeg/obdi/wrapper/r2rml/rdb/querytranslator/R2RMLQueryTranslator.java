package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractResultSet;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.exception.InsatisfiableSQLExpression;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractCondSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractPRSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.CondSQLResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.sql.IQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoinQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap.TermMapType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;
import es.upm.fi.oeg.obdi.core.utility.RegexUtility;

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
			AbstractMappingDocument mappingDocument, Connection conn) throws Exception {
		AbstractQueryTranslator queryTranslator = 
				new R2RMLQueryTranslator();
		queryTranslator.setMappingDocument(mappingDocument);

		return queryTranslator;
	}

	public static AbstractQueryTranslator createQueryTranslator(
			String mappingDocumentPath) throws Exception {
		return R2RMLQueryTranslator.createQueryTranslator(
				mappingDocumentPath, null);
	}

	public static AbstractQueryTranslator createQueryTranslator(
			String mappingDocumentPath, Connection conn) throws Exception {
		ConfigurationProperties properties = new ConfigurationProperties();
		properties.setConn(conn);
		AbstractMappingDocument mappingDocument = 
				new R2RMLMappingDocument(mappingDocumentPath, properties);
		return R2RMLQueryTranslator.createQueryTranslator(mappingDocument, conn);
	}

	@Override
	protected String generateTermCName(Node termC) {
		String termCName = super.getNameGenerator().generateName(termC);
		return termCName;
	}

	@Override
	protected List<ZExp> transIRI(Node node) {
		List<ZExp> result = new LinkedList<ZExp>();
		ZExp resultAux = null;

		Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(node);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();

		Map<String, String> mapColumnsValues = cm.getSubjectMap().getTemplateValues(node.getURI());
		if(mapColumnsValues == null || mapColumnsValues.size() == 0) {
			//do nothing
		} else {
			for(String column : mapColumnsValues.keySet()) {
				String value = mapColumnsValues.get(column);
				ZConstant constant = new ZConstant(value, ZConstant.UNKNOWN);
				result.add(constant);
			}
		}

		return result;
	}

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
	public String translateResultSet(String varName, AbstractResultSet rs) {
		String result = null;

		try {
			if(rs != null) {
				List<String> rsColumnNames = rs.getColumnNames();
				Map<String, Object> mapNodeMapping = this.getMapVarMapping2();
				Object mapValue = mapNodeMapping.get(varName);
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
					TermMapType termMapType = termMap.getTermMapType();
					
					if(termMapType == TermMapType.TEMPLATE) {
						String templateString = termMap.getTemplateString();
						List<String> templateColumns = termMap.getTemplateColumns();
						Matcher matcher = this.mapTemplateMatcher.get(templateString);
						if(matcher == null) {
							Pattern pattern = Pattern.compile(R2RMLConstants.R2RML_TEMPLATE_PATTERN);
							matcher = pattern.matcher(templateString);
							this.mapTemplateMatcher.put(templateString, matcher);
						}
						Collection<String> templateAttributes = this.mapTemplateAttributes.get(templateString);
						if(templateAttributes == null) {
							//attributes = R2RMLUtility.getAttributesFromStringTemplate(template);
							RegexUtility regexUtility = new RegexUtility();
							templateAttributes = regexUtility.getTemplateColumns(templateString, true);
							this.mapTemplateAttributes.put(templateString, templateAttributes);
						}

						Map<String, String> replacements = new HashMap<String, String>();
						
						int i=0;
						for(String templateAttribute : templateAttributes) {
							String columnName = rsColumnNames.get(i);
							String dbValue = rs.getString(columnName);
							replacements.put(templateAttribute, dbValue);
							i++;
						}
						
						result = R2RMLUtility.replaceTokens(templateString, replacements);
					} else if(termMapType == TermMapType.COLUMN) {
						//String columnName = termMap.getColumnName();
						result = rs.getString(varName);
					} else if (termMapType == TermMapType.CONSTANT) {
						result = termMap.getConstantValue();
					} else {
						logger.warn("Unsupported term map type!");
					}
				}

				String termMapType = termMap.getTermType();
				if(termMapType != null) {
					if(termMapType.equals(R2RMLConstants.R2RML_IRI_URI)) {
						result = ODEMapsterUtility.encodeURI(result);
					} else if(termMapType.equals(R2RMLConstants.R2RML_LITERAL_URI)) {
						result = ODEMapsterUtility.encodeLiteral(result);
					}
				}


			}			
		} catch(Exception e) {
			logger.error("Error occured while translating result set : " + e.getMessage());
		}

		return result;
	}

	@Override
	protected IQuery trans(Triple tp, AbstractConceptMapping cm,
			String predicateURI) throws QueryTranslationException, InsatisfiableSQLExpression {

		//alpha
		AlphaResult alphaResult = super.getAlphaGenerator().calculateAlpha(tp, cm, predicateURI);
		SQLLogicalTable alphaSubject = alphaResult.getAlphaSubject();
		logger.debug("alpha logicalTable = " + alphaSubject);
		SQLQuery resultAux = new SQLQuery(alphaSubject);
		
		Collection<SQLJoinQuery> alphaPredicateObjects = alphaResult.getAlphaPredicateObjects();
		
		if(alphaPredicateObjects != null && !alphaPredicateObjects.isEmpty()) {
			for(SQLJoinQuery alphaPredicateObject : alphaPredicateObjects) {
				//result.addJoinQuery(alphaPredicateObject);//alpha predicate object
				resultAux.addLogicalTable(alphaPredicateObject);
				logger.debug("alphaPredicateObject = " + alphaPredicateObject);
			}
		}

		//beta
		//BetaResult betaResult = super.getBetaGenerator().calculateBeta(tp, cm, predicateURI, alphaResult);

		AbstractBetaGenerator betaGenerator = super.getBetaGenerator();

		//PRSQL
		AbstractPRSQLGenerator prSQLGenerator = super.getPrSQLGenerator();
		NameGenerator nameGenerator = super.getNameGenerator(); 
		Collection<ZSelectItem> selectItems = prSQLGenerator.genPRSQL(
				tp, alphaResult, betaGenerator, nameGenerator
				, cm, predicateURI);
		logger.debug("prsql = " + selectItems);
		resultAux.setSelectItems(selectItems);

		//CondSQL
		AbstractCondSQLGenerator condSQLGenerator = 
				super.getCondSQLGenerator();
		CondSQLResult condSQL = condSQLGenerator.genCondSQL(
				tp, alphaResult, betaGenerator, cm, predicateURI);
		logger.debug("condSQL = " + condSQL);
		if(condSQL != null) {
			resultAux.addWhere(condSQL.getExpression());
		}

		IQuery transTP;
		//subquery elimination
		IQueryTranslationOptimizer optimizer = super.getOptimizer();
		if(optimizer != null && optimizer.isSubQueryElimination()) {
			try {
				transTP = resultAux.eliminateSubQuery();	
			} catch(Exception e) {
				throw new QueryTranslationException("error in eliminating subquery!", e);
			}
		} else {
			transTP = resultAux;
		}

		logger.debug("transTP(tp, cm) = " + transTP);
		return transTP;
	}

	public static AbstractQueryTranslator getQueryTranslatorFreddy(
			String url, String username, String password
			, String databaseType, String databaseName
			, String mappingDocumentFile ) throws Exception {

		Properties props = new Properties();
		props.setProperty("user",username);
		props.setProperty("password",password);

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, props);	
		} catch(Exception e) {
			String errorMessage = "Error while trying to retrieve a connection: " + e.getMessage();
			logger.warn(errorMessage);
		}

		ConfigurationProperties properties = new ConfigurationProperties();
		properties.setConn(conn);
		properties.setDatabaseName(databaseName);

		AbstractMappingDocument mappingDocument = 
				new R2RMLMappingDocument(mappingDocumentFile, properties);

		IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
		queryTranslationOptimizer.setSelfJoinElimination(true);
		queryTranslationOptimizer.setUnionQueryReduction(true);
		queryTranslationOptimizer.setSubQueryElimination(true);
		queryTranslationOptimizer.setSubQueryAsView(false);

		AbstractQueryTranslator queryTranslatorFreddy = R2RMLQueryTranslator.createQueryTranslator(mappingDocument, conn); 
		queryTranslatorFreddy.setOptimizer(queryTranslationOptimizer);
		queryTranslatorFreddy.setDatabaseType(databaseType);
		queryTranslatorFreddy.setConnection(conn);

		return queryTranslatorFreddy;

	}


}
