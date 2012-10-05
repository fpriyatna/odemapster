package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NodeTypeInferrer;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryRewritter;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslatorUtility;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.TypeInferrer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLQueryTranslator extends AbstractQueryTranslator {
	private static Logger logger = Logger.getLogger(R2RMLQueryTranslator.class);
	protected Map<String, R2RMLTermMap> mapVarMapping = new HashMap<String, R2RMLTermMap>();
	static Map<Triple, String> mapTripleAlias= new HashMap<Triple, String>();
	
	
//	public R2RMLQueryTranslator(AbstractMappingDocument mappingDocument
//			, R2RMLElementUnfoldVisitor unfolder) {
//		super(mappingDocument);
//		this.unfolder = unfolder;
//	}


	@Override
	protected String generateTermCName(Node termC) {
		String termCName = nameGenerator.generateName(null, termC);
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
			result = new ZConstant(pkValue, ZConstant.UNKNOWN);
		}

		return result;
	}




	@Override
	public SQLQuery translate(Query sparqlQuery) throws Exception {
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);
		Op opSparqlQuery = Algebra.compile(sparqlQuery) ;
		
		NodeTypeInferrer typeInferrer = new NodeTypeInferrer(super.mappingDocument);
		super.mapInferredTypes = typeInferrer.infer(opQueryPattern);
		this.alphaGenerator = new R2RMLAlphaGenerator(
				mapInferredTypes, this.mappingDocument, this.unfolder);
		this.alphaGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		this.alphaGenerator.setSubqueryAsView(this.subqueryAsView);
		this.betaGenerator = new R2RMLBetaGenerator(this, mapInferredTypes, mappingDocument);
		this.prSQLGenerator = new R2RMLPRSQLGenerator(this, this.mapInferredTypes);
		this.prSQLGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		this.condSQLGenerator = new R2RMLCondSQLGenerator(betaGenerator, mapInferredTypes);
		this.condSQLGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		logger.debug("opSparqlQuery = " + opSparqlQuery);
		long start = System.currentTimeMillis();
		
		//logger.info("opSparqlQuery = " + opSparqlQuery);
		if(this.optimizeTripleBlock) {
			opSparqlQuery = new QueryRewritter().rewrite(opSparqlQuery);
			logger.info("opSparqlQueryRewritten = " + opSparqlQuery);
		}
		
		if(!(opSparqlQuery instanceof OpProject)) {
			Collection<Var> allVars = OpVars.allVars(opQueryPattern);
			logger.info("vars in query pattern = " + allVars);
			List<Var> vars = new Vector<Var>(allVars);
			opSparqlQuery = new OpProject(opSparqlQuery, vars); 
		}
		
		SQLQuery result = this.trans(opSparqlQuery);
		long end = System.currentTimeMillis();
		logger.info("Query translation time = "+ (end-start)+" ms.");

		//logger.info("trans query = \n" + result + "\n");
		return result;
	}

	protected SQLQuery transTSS(List<Triple> triples) throws Exception {
		SQLQuery result = new SQLQuery();
		
		//AlphaTB
		Vector alphaTables = (Vector) this.alphaGenerator.calculateAlphaTSS(triples);
		SQLLogicalTable logicalTable = (SQLLogicalTable) alphaTables.get(0);
		if(alphaTables.size() == 1) {
			if(this.subqueryAsView) {
				boolean condition1 = false;
				if(logicalTable instanceof SQLFromItem && ((SQLFromItem)logicalTable).getForm() == LogicalTableType.QUERY_STRING ) {
					condition1 = true;
				}
				boolean condition2 = false;
				if(logicalTable instanceof SQLQuery) {
					condition2 = true;
				}
				
				if(condition1 || condition2) {
					String logicalTableAlias = logicalTable.getAlias();
					logicalTable.setAlias("");

					Connection conn = AbstractRunner.getConfigurationProperties().getConn();
					
					String subQueryViewName = "sqa" + Math.abs(triples.hashCode());
					String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
					logger.info(dropViewSQL + ";\n");
					boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);
					
					String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + logicalTable;
					logger.info(createViewSQL + ";\n");
					boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);
					
					
					logicalTable = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
					logicalTable.setAlias(logicalTableAlias);					
				}
			}
		} else if(alphaTables.size() > 1) {
			for(int i=1; i<alphaTables.size(); i++) {
				SQLQuery joinQuery = (SQLQuery) alphaTables.get(i);
				result.addJoinQuery(joinQuery);//alpha predicate object
			}
		}
			

		result.addLogicalTable(logicalTable);//alpha subject

		//PRSQLTB
		Collection<ZSelectItem> selectItems = 
				this.prSQLGenerator.genPRSQLTB(triples, betaGenerator, nameGenerator);
		result.setSelectItems(selectItems);

		//CondSQLTB
		ZExpression condSQL = this.condSQLGenerator.genCondSQLTB(triples);
		if(condSQL != null) {
			result.addWhere(condSQL);
		}
		
		if(super.subQueryElimination) {
			result = QueryTranslatorUtility.eliminateSubQuery(result);
		}
		
		logger.debug("transTSS = " + result);
		return result;
	}


	@Override
	protected SQLQuery transTP(Triple tp) throws QueryTranslationException {
		SQLQuery result = new SQLQuery();
		try {
			Node tpSubject = tp.getSubject();
			Node tpPredicate = tp.getPredicate();

			if(RDF.type.getURI().equals(tpPredicate.getURI())) {
				if(this.isIgnoreRDFTypeStatement()) {
					return null;
				}
			} else {
				Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(tpSubject);
				if(cms != null) {
					//alpha
					Vector alphaTables = (Vector) this.alphaGenerator.calculateAlpha(tp);
					SQLLogicalTable logicalTable = (SQLLogicalTable) alphaTables.get(0);
					logger.debug("alpha logicalTable = " + logicalTable);
					result.addLogicalTable(logicalTable);//alpha from subject
					if(alphaTables.size() > 1) {
						for(int i=1; i<alphaTables.size(); i++) {
							SQLQuery joinQuery = (SQLQuery) alphaTables.get(i);
							logger.debug("alpha joinQuery = " + joinQuery);
							result.addJoinQuery(joinQuery);//alpha predicate object
						}
					}

					//PRSQL
					Collection<ZSelectItem> selectItems = this.prSQLGenerator.genPRSQL(
							tp, betaGenerator, nameGenerator);
					result.setSelectItems(selectItems);

					//CondSQL
					ZExpression condSQL = this.condSQLGenerator.genCondSQL(tp);
					logger.debug("condSQL = " + condSQL);
					if(condSQL != null) {
						result.addWhere(condSQL);
					}
				} else {
					String errorMessage = "Undefined triplesMap for class : " + tpSubject;
					throw new QueryTranslationException(errorMessage);
				}



			}

			if(super.subQueryElimination) {
				result = QueryTranslatorUtility.eliminateSubQuery(result);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("Error in transTP : " + tp);
			throw new QueryTranslationException(e.getMessage(), e);
		}

		

		
		logger.debug("transTP = " + result);
		return result;
	}





	@Override
	protected ZExp transVar(Op op, Var var) {
		String nameVar = nameGenerator.generateName(null, var);
		ZExp zExp = new ZConstant(nameVar, ZConstant.COLUMNNAME);
		return zExp;
	}




	@Override
	public AbstractQueryTranslator getQueryTranslator(
			AbstractMappingDocument mappingDocument, AbstractUnfolder unfolder) {
		AbstractQueryTranslator queryTranslator = new R2RMLQueryTranslator();
		queryTranslator.setMappingDocument(mappingDocument);
		queryTranslator.setUnfolder(unfolder);
		return queryTranslator;
	}




	public Map<String, R2RMLTermMap> getMapVarMapping() {
		return this.mapVarMapping;
	}

}
