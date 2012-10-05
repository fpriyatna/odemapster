package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLJoinCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLAlphaGenerator extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(R2RMLAlphaGenerator.class);
	private AbstractUnfolder unfolder;
	
	public R2RMLAlphaGenerator(
			Map<Node, Set<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument
			, AbstractUnfolder unfolder) {
		super(mapNodeConceptMapping, mappingDocument);
		this.unfolder = unfolder;
	}

	@Override
	public Collection calculateAlpha(Triple tp) throws Exception {
		Collection result = new Vector();
		
		//alpha subject
		Node tpSubject = tp.getSubject();
		Collection<AbstractConceptMapping> cms = super.mapNodeConceptMapping.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
		SQLLogicalTable alphaSubject = this.calculateAlphaSubject(tpSubject, cm);
		
		result.add(alphaSubject);
		

		//alpha predicate object
		Node tpPredicate = tp.getPredicate();
		Node tpObject = tp.getObject();
		Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(tpPredicate.getURI());
		if(pms != null && pms.size() > 0) {
			R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
			
			R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
			if(refObjectMap != null) { 
				SQLQuery alphaPredicateObject = 
						this.calculateAlphaPredicateObject(pm, tp, cm);
				result.add(alphaPredicateObject);
			}
		}

		logger.debug("calculateAlpha = " + result);
		return result;
		

	}

	@Override
	protected SQLQuery calculateAlphaPredicateObject  (
			AbstractPropertyMapping abstractPropertyMapping,
			Triple triple, AbstractConceptMapping abstractConceptMapping) throws Exception {
		Node tpObject = triple.getObject();
		R2RMLTriplesMap cm = (R2RMLTriplesMap) abstractConceptMapping;
		//String tripleMapAlias = cm.getAlias();
		String tripleMapAlias = cm.getLogicalTable().getAlias();
		
		SQLQuery joinQuery = null;
		R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) abstractPropertyMapping;  
		R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
		if(refObjectMap != null) { 
			joinQuery = new SQLQuery();
			joinQuery.setJoinType("INNER");
			String joinQueryAlias = joinQuery.generateAlias();
			joinQuery.setAlias(joinQueryAlias);
			
			//refObjectMap.setAlias(joinQueryAlias);
			R2RMLQueryTranslator.mapTripleAlias.put(triple, joinQueryAlias);
			
			R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
			//SQLLogicalTable sqlParentLogicalTable = new R2RMLElementUnfoldVisitor().visit(parentLogicalTable);
			SQLLogicalTable sqlParentLogicalTable = ((R2RMLElementUnfoldVisitor)this.unfolder).visit(parentLogicalTable);
			if(this.subqueryAsView && sqlParentLogicalTable instanceof SQLQuery) {
				Connection conn = AbstractRunner.getConfigurationProperties().getConn();
				
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
			ZExp onExpression = R2RMLUtility.generateJoinCondition(joinConditions, tripleMapAlias, joinQueryAlias);
			if(onExpression != null) {
				joinQuery.setOnExp(onExpression);
			}
		}

		return joinQuery;
	}

//	@Override
//	public AbstractConceptMapping calculateAlphaCM(Triple tp) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public AbstractConceptMapping calculateAlphaCMTB(Collection<Triple> triples)
//			throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	protected SQLLogicalTable calculateAlphaSubject(Node subject,
			AbstractConceptMapping abstractConceptMapping) throws Exception {
		R2RMLTriplesMap cm = (R2RMLTriplesMap) abstractConceptMapping;
		R2RMLLogicalTable r2rmlLogicalTable = cm.getLogicalTable();
		//SQLLogicalTable sqlLogicalTable = new R2RMLElementUnfoldVisitor().visit(logicalTable);;
		SQLLogicalTable sqlLogicalTable = ((R2RMLElementUnfoldVisitor)this.unfolder).visit(r2rmlLogicalTable);
		if(this.subqueryAsView && sqlLogicalTable instanceof SQLQuery) {
			Connection conn = AbstractRunner.getConfigurationProperties().getConn();
			
			String subQueryViewName = "sa" + Math.abs(subject.hashCode());
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
			logger.info(dropViewSQL + ";\n");
			boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);
			
			String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + sqlLogicalTable;
			logger.info(createViewSQL + ";\n");
			boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);
			
			sqlLogicalTable = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
		}
		
		String logicalTableAlias = sqlLogicalTable.generateAlias();
		r2rmlLogicalTable.setAlias(logicalTableAlias);
		sqlLogicalTable.setAlias(logicalTableAlias);
//		cm.setAlias(logicalTableAlias);
		return sqlLogicalTable;
	}

	public  Object calculateAlphaTSS(Collection<Triple> triples)
			throws Exception {
		Collection result = new Vector();
		
		Triple firstTriple = triples.iterator().next();
		Node tpSubject = firstTriple.getSubject();
		Collection<AbstractConceptMapping> cms = super.mapNodeConceptMapping.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
		SQLLogicalTable alphaSubject = this.calculateAlphaSubject(tpSubject, cm);
		result.add(alphaSubject);
		
		
		//mapping projection of corresponding predicates
		for(Triple tp : triples) {
			Node tpPredicate = tp.getPredicate();
			String tpPredicateURI = tpPredicate.getURI();
			boolean isRDFTypeStatement = RDF.type.getURI().equals(tpPredicateURI);
			if(this.ignoreRDFTypeStatement && isRDFTypeStatement) {
				//do nothing
			} else {
				Node tpObject = tp.getObject();
				Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(tpPredicateURI);
				if(pms != null) {
					R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
					logger.debug("pm = " + pm);
					R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
					if(refObjectMap != null) { 
						SQLQuery alphaPredicateObject = 
								this.calculateAlphaPredicateObject(pm, tp, cm);

//						if(this.subqueryAsView) {
//							String alphaPredicateObjectAlias = alphaPredicateObject.getAlias();
//							alphaPredicateObject.setAlias("");
//							String alphaPredicateObjectJoinType = alphaPredicateObject.getJoinType();
//							alphaPredicateObject.setJoinType(null);
//							
//							Connection conn = AbstractRunner.getConfigurationProperties().getConn();
//							
//							String subQueryViewName = "sa" + Math.abs(tp.hashCode());
//							String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
//							logger.info(dropViewSQL + ";\n");
//							boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);
//							
//							String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + alphaPredicateObject;
//							logger.info(createViewSQL + ";\n");
//							boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);
//							
//							SQLFromItem alphaPredicateObject2 = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE);
//							result.add(alphaPredicateObject2);
//						} else {
//							result.add(alphaPredicateObject);
//						}

						result.add(alphaPredicateObject);
						
					}
				}				
			}
		}

		logger.debug("alphaTSS = " + result);
		return result;
	}

}
 