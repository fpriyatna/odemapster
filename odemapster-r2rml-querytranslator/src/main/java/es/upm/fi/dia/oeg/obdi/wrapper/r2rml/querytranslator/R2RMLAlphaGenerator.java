package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResultUnion;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLJoinCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLAlphaGenerator extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(R2RMLAlphaGenerator.class);
	

	public R2RMLAlphaGenerator(AbstractQueryTranslator owner) {
		super(owner);
	}



	@Override
	protected SQLQuery calculateAlphaPredicateObject  (Triple triple, AbstractConceptMapping abstractConceptMapping
			, AbstractPropertyMapping abstractPropertyMapping, String logicalTableAlias) throws Exception {
		R2RMLTriplesMap cm = (R2RMLTriplesMap) abstractConceptMapping;
		//String tripleMapAlias = cm.getAlias();
		//String tripleMapAlias = cm.getLogicalTable().getAlias();
		
		SQLQuery joinQuery = null;
		R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) abstractPropertyMapping;  
		R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
		if(refObjectMap != null) { 
			joinQuery = new SQLQuery();
			joinQuery.setJoinType("INNER");
			String joinQueryAlias = R2RMLQueryTranslator.mapTripleAlias.get(triple);
			if(joinQueryAlias == null) {
				joinQueryAlias = joinQuery.generateAlias();
				R2RMLQueryTranslator.mapTripleAlias.put(triple, joinQueryAlias);
			}
			joinQuery.setAlias(joinQueryAlias);
			
			
			R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
			//SQLLogicalTable sqlParentLogicalTable = new R2RMLElementUnfoldVisitor().visit(parentLogicalTable);
			R2RMLElementUnfoldVisitor unfolder = (R2RMLElementUnfoldVisitor) this.owner.getUnfolder();
			SQLLogicalTable sqlParentLogicalTable = unfolder.visit(parentLogicalTable);
			if(this.subqueryAsView && sqlParentLogicalTable instanceof SQLQuery) {
				Connection conn = this.owner.getConnection();
				
				String subQueryViewName = "sa" + Math.abs(triple.hashCode());
				String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
				logger.info(dropViewSQL + ";\n");
				boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);
				
				String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + sqlParentLogicalTable;
				logger.info(createViewSQL + ";\n");
				boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);

				SQLFromItem alphaPredicateObject2 = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
				joinQuery.addLogicalTable(alphaPredicateObject2);
			} else {
				joinQuery.addLogicalTable(sqlParentLogicalTable);
			}
			
			
			

			Collection<R2RMLJoinCondition> joinConditions = refObjectMap.getJoinConditions();
			ZExp onExpression = R2RMLUtility.generateJoinCondition(joinConditions, logicalTableAlias, joinQueryAlias);
			if(onExpression != null) {
				joinQuery.setOnExp(onExpression);
			}
		}

		return joinQuery;
	}



	@Override
	protected SQLLogicalTable calculateAlphaSubject(Node subject,
			AbstractConceptMapping abstractConceptMapping) throws Exception {
		R2RMLTriplesMap cm = (R2RMLTriplesMap) abstractConceptMapping;
		R2RMLLogicalTable r2rmlLogicalTable = cm.getLogicalTable();
		//SQLLogicalTable sqlLogicalTable = new R2RMLElementUnfoldVisitor().visit(logicalTable);
		R2RMLElementUnfoldVisitor unfolder = (R2RMLElementUnfoldVisitor) this.owner.getUnfolder();
		SQLLogicalTable sqlLogicalTable = unfolder.visit(r2rmlLogicalTable);
		if(this.subqueryAsView && sqlLogicalTable instanceof SQLQuery) {
			Connection conn = this.owner.getConnection();
			
			String subQueryViewName = "sa" + Math.abs(subject.hashCode());
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
			logger.info(dropViewSQL + ";\n");
			boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);
			
			String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + sqlLogicalTable;
			logger.info(createViewSQL + ";\n");
			boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);
			
			sqlLogicalTable = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
		}
		
		//String logicalTableAlias = sqlLogicalTable.generateAlias();
		String logicalTableAlias = cm.getLogicalTable().getAlias();
		if(logicalTableAlias == null || logicalTableAlias.equals("")) {
			logicalTableAlias = sqlLogicalTable.generateAlias();
		}
		//r2rmlLogicalTable.setAlias(logicalTableAlias);
		sqlLogicalTable.setAlias(logicalTableAlias);
//		cm.setAlias(logicalTableAlias);
		return sqlLogicalTable;
	}



	public List<SQLQuery> calculateAlphaPredicateObjectSTG(Triple tp
			, AbstractConceptMapping cm, String tpPredicateURI, String logicalTableAlias) throws Exception {
		List<SQLQuery> alphaPredicateObjects = new Vector<SQLQuery>();
		
		boolean isRDFTypeStatement = RDF.type.getURI().equals(tpPredicateURI);
		if(this.ignoreRDFTypeStatement && isRDFTypeStatement) {
			//do nothing
		} else {
			Node tpObject = tp.getObject();
			Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(tpPredicateURI);
			if(pms != null && !pms.isEmpty()) {
				R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
				logger.debug("pm = " + pm);
				R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
				if(refObjectMap != null) { 
					SQLQuery alphaPredicateObject = 
							this.calculateAlphaPredicateObject(tp, cm, pm, logicalTableAlias);
					
					alphaPredicateObjects.add(alphaPredicateObject);
				}
			} else {
				String errorMessage = "Undefined mapping for : " + tpPredicateURI + " in : " + cm.toString();
				throw new QueryTranslationException(errorMessage);				
				
			}
		}
		return alphaPredicateObjects;
	}
	
	@Override
	public AlphaResult calculateAlpha(Triple tp, AbstractConceptMapping abstractConceptMapping
			, String predicateURI)
			throws Exception {
		
		//alpha subject
		Node tpSubject = tp.getSubject();
		SQLLogicalTable alphaSubject = this.calculateAlphaSubject(tpSubject, abstractConceptMapping);
		String logicalTableAlias = alphaSubject.getAlias();
		
		//alpha predicate object
		Collection<AbstractPropertyMapping> pms = abstractConceptMapping.getPropertyMappings(predicateURI);
		List<SQLQuery> alphaPredicateObjects = new Vector<SQLQuery>();
		if(pms != null) {
			if(pms.size() > 1) {
				String errorMessage = "Multiple mappings of a predicate is not supported.";
				throw new QueryTranslationException(errorMessage);				
			}
			
			R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
			R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
			if(refObjectMap != null) { 
				SQLQuery alphaPredicateObject = this.calculateAlphaPredicateObject(
						tp, abstractConceptMapping, pm, logicalTableAlias);
				alphaPredicateObjects.add(alphaPredicateObject);
			}
		}
		AlphaResult alphaResult = new AlphaResult(alphaSubject, alphaPredicateObjects, predicateURI);
		
		logger.debug("calculateAlpha = " + alphaResult);
		return alphaResult;

	}

}
 