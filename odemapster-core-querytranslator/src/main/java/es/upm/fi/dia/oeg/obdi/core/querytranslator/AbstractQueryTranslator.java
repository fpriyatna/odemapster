package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractResultSet;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.exception.InsatisfiableSQLExpression;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.sql.IQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoin;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoinTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLUtility;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLUnion;
import es.upm.fi.oeg.obdi.core.utility.SPARQLUtility;
import es.upm.fi.oeg.obdi.core.utility.SQLConstant;

public abstract class AbstractQueryTranslator implements IQueryTranslator {
	private static Logger logger = Logger.getLogger(AbstractQueryTranslator.class);

	//protected String queryFilePath;
	protected AbstractMappingDocument mappingDocument;
	protected AbstractUnfolder unfolder;
	private Connection connection;
	protected Map<Node, Set<AbstractConceptMapping>> mapInferredTypes;
	protected IQueryTranslationOptimizer optimizer = null;
	protected boolean ignoreRDFTypeStatement = false;
	protected Map<Op, Collection<Node>> mapTermsC = new HashMap<Op, Collection<Node>>();
	protected Map<Op, String> mapTransGP1Alias = new HashMap<Op, String>();
	private Map<String, Object> mapVarMapping2 = new HashMap<String, Object>();
	public enum POS {sub, pre, obj}
	private ConfigurationProperties configurationProperties;
	protected String databaseType = Constants.DATABASE_MYSQL;
	private Map<String, String> functionsMap = new HashMap<String, String>();
	Collection<String> notNullColumns = new Vector<String>();
	
	//chebotko functions
	private AbstractAlphaGenerator alphaGenerator;
	private AbstractBetaGenerator betaGenerator;
	private NameGenerator nameGenerator;
	private AbstractPRSQLGenerator prSQLGenerator;
	private AbstractCondSQLGenerator condSQLGenerator;


	public AbstractQueryTranslator() {
		this.nameGenerator = new NameGenerator();
		Optimize.setFactory(new QueryRewritterFactory());

		functionsMap.put(E_Bound.class.toString(), "IS NOT NULL");
		functionsMap.put(E_LogicalNot.class.toString(), "NOT");
		functionsMap.put(E_LogicalOr.class.toString(), "OR");
		functionsMap.put(E_LogicalAnd.class.toString(), "AND");
		functionsMap.put(E_Regex.class.toString(), "LIKE");
		functionsMap.put(E_OneOf.class.toString(), "IN");
	}


	protected abstract void buildAlphaGenerator();

	protected abstract void buildBetaGenerator();

	protected abstract void buildCondSQLGenerator();

	protected abstract void buildPRSQLGenerator();

	private List<String> getColumnsByNode(Node node, Collection<ZSelectItem> oldSelectItems) {
		List<String> result = new LinkedList<String>();
		String nameSelectVar = nameGenerator.generateName(node);

		Iterator<ZSelectItem> oldSelectItemsIterator = oldSelectItems.iterator();
		while(oldSelectItemsIterator.hasNext()) {
			ZSelectItem oldSelectItem = oldSelectItemsIterator.next(); 
			String oldAlias = oldSelectItem.getAlias();
			String selectItemName;
			if(oldAlias == null || oldAlias.equals("")) {
				selectItemName = oldSelectItem.getColumn();
			} else {
				selectItemName = oldAlias; 
			}

			if(selectItemName.equalsIgnoreCase(nameSelectVar)) {
				result.add(selectItemName);
			} else if (selectItemName.contains(nameSelectVar + "_")) {
				result.add(selectItemName);
			}
		}
		return result;
	}

	private Collection<ZSelectItem> generateSelectItem(Node node, String prefix
			, Collection<ZSelectItem> oldSelectItems, boolean useAlias) {
		Collection<ZSelectItem> result = new LinkedList<ZSelectItem>();
		if(prefix != null && !prefix.endsWith(".")) {
			prefix += ".";
		}

		String nameSelectVar = nameGenerator.generateName(node);

		ZSelectItem newSelectItem = null;
		if(node.isVariable() || node.isURI()) {
			if(oldSelectItems == null) {
				if(prefix == null) {
					newSelectItem = new ZSelectItem(nameSelectVar);
				} else {
					newSelectItem = new ZSelectItem(prefix + nameSelectVar);	
				}

				if(useAlias) {
					newSelectItem.setAlias(nameSelectVar);	
				}
				result.add(newSelectItem);
			} else {
				Iterator<ZSelectItem> oldSelectItemsIterator = oldSelectItems.iterator();
				while(oldSelectItemsIterator.hasNext()) {
					ZSelectItem oldSelectItem = oldSelectItemsIterator.next(); 
					String selectItemName;
					String oldAlias = oldSelectItem.getAlias();
					String oldTable = null;
					if(oldAlias == null || oldAlias.equals("")) {
						selectItemName = oldSelectItem.getColumn();
						oldTable = oldSelectItem.getTable();
					} else {
						selectItemName = oldAlias; 
					}

					String newSelectItemAlias = null;
					if(selectItemName.equalsIgnoreCase(nameSelectVar)) {
						if(prefix == null || prefix.equals("")) {
							if(oldTable == null || oldTable.equals("")) {
								newSelectItem = new ZSelectItem(selectItemName);
							} 
							//							else {
							//								newSelectItem = new ZSelectItem(oldTable + "." + selectItemName);
							//							}
						} else {
							newSelectItem = new ZSelectItem(prefix + selectItemName);
						}

						if(node.isVariable()) {
							newSelectItemAlias = node.getName();	
						} else if(node.isURI()) {
							newSelectItemAlias = node.getURI();
						}
					} else if (selectItemName.contains(nameSelectVar + "_")) {
						if(prefix == null || prefix.equals("")) {
							if(oldTable == null || oldTable.equals("")) {
								newSelectItem = new ZSelectItem(selectItemName);
							} else {
								newSelectItem = new ZSelectItem(oldTable + "." + selectItemName);
							}
						} else {
							newSelectItem = new ZSelectItem(prefix + selectItemName);
						}

						if(node.isVariable()) {
							newSelectItemAlias = node.getName();	
						} else if(node.isURI()) {
							newSelectItemAlias = node.getURI();
						}						
						newSelectItemAlias += selectItemName.replaceAll(nameSelectVar, "");
					} else {
						newSelectItem = null;
					}

					if(newSelectItem != null) {
						if(newSelectItemAlias != null && useAlias) {
							newSelectItem.setAlias(newSelectItemAlias);
						}
						result.add(newSelectItem);
					}
				}	
			}
		} else if(node.isLiteral()){
			Object literalValue = node.getLiteralValue();
			ZExp exp;
			String constantValue;
			if(prefix == null) {
				constantValue = literalValue.toString();
			} else {
				constantValue = prefix + literalValue.toString();
			}

			if(literalValue instanceof String) {
				exp = new ZConstant(constantValue, ZConstant.STRING);							
			} else if (literalValue instanceof Double) {
				exp = new ZConstant(constantValue, ZConstant.NUMBER);
			} else {
				exp = new ZConstant(constantValue, ZConstant.STRING);							

			}
			newSelectItem = new SQLSelectItem();
			newSelectItem.setExpression(exp);
			if(useAlias) { newSelectItem.setAlias(nameSelectVar); }

			result.add(newSelectItem);
		} else {
			logger.warn("unsupported node " + node.toString());
		}

		return result;
	}


	private Collection<ZSelectItem> generateSelectItems(Collection<Node> nodes
			, String prefix, Collection<ZSelectItem> oldSelectItems, boolean useAlias) {
		if(prefix != null && !prefix.endsWith(".")) {
			prefix += ".";
		}		

		Collection<ZSelectItem> result = new LinkedHashSet<ZSelectItem>();

		for(Node node : nodes) {
			Collection<ZSelectItem> selectItems = this.generateSelectItem(
					node, prefix, oldSelectItems, useAlias);
			result.addAll(selectItems);
		}

		return result;
	}

	protected abstract String generateTermCName(Node termC);

	public AbstractMappingDocument getMappingDocument() {
		return mappingDocument;
	}

	public boolean isIgnoreRDFTypeStatement() {
		return ignoreRDFTypeStatement;
	}

	public void setIgnoreRDFTypeStatement(boolean ignoreRDFTypeStatement) {
		this.ignoreRDFTypeStatement = ignoreRDFTypeStatement;
	}

	public void setMappingDocument(AbstractMappingDocument mappingDocument) {
		this.mappingDocument = mappingDocument;
	}

	public void setNameGenerator(NameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}

	public void setUnfolder(AbstractUnfolder unfolder) {
		this.unfolder = unfolder;
	}

	protected IQuery trans(Op op) throws Exception {
		IQuery result = null;
		if(op instanceof OpBGP) { //triple or bgp pattern
			OpBGP bgp = (OpBGP) op;
			if(bgp.getPattern().size() == 1) {
				Triple tp = bgp.getPattern().get(0);
				result = this.trans(tp);
			} else {
				result = this.trans(bgp);	
			}
		} else if(op instanceof OpJoin) { // AND pattern
			OpJoin opJoin = (OpJoin) op;
			result = this.trans(opJoin);
		} else if(op instanceof OpLeftJoin) { //OPT pattern
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			result = this.trans(opLeftJoin);
		} else if(op instanceof OpUnion) { //UNION pattern
			OpUnion opUnion = (OpUnion) op;
			result = this.trans(opUnion);
		} else if(op instanceof OpFilter) { //FILTER pattern
			OpFilter opFilter = (OpFilter) op;
			result = this.trans(opFilter);
		} else if(op instanceof OpProject) {
			OpProject opProject = (OpProject) op;
			result = this.trans(opProject);
		} else if(op instanceof OpSlice) {
			OpSlice opSlice = (OpSlice) op;
			result = this.trans(opSlice);
		} else if(op instanceof OpDistinct) {
			OpDistinct opDistinct = (OpDistinct) op;
			result = this.trans(opDistinct);
		} else if(op instanceof OpOrder) {
			OpOrder opOrder = (OpOrder) op;
			result = this.trans(opOrder);			
		} else {
			throw new QueryTranslationException("Unsupported query!");
		}

		if(result != null) {
			result.setDatabaseType(databaseType);	
		}
		
		return result;
	}

	protected IQuery trans(OpBGP bgp) throws Exception {
		IQuery result = null;

		if(QueryTranslatorUtility.isTriplePattern(bgp)) { //triple pattern
			Triple tp = bgp.getPattern().getList().get(0);
			result = this.trans(tp);
		} else { //bgp pattern
			List<Triple> triples = bgp.getPattern().getList();
			boolean isSTG = QueryTranslatorUtility.isSTG(triples);

			if(this.optimizer != null && this.optimizer.isSelfJoinElimination() && isSTG) {
				result = this.transSTG(triples);
			} else {
				int separationIndex = 1;
				if(this.optimizer != null && this.optimizer.isSelfJoinElimination()) {
					separationIndex = QueryTranslatorUtility.getFirstTBEndIndex(triples);
				}
				List<Triple> gp1TripleList = triples.subList(0, separationIndex);
				OpBGP gp1 = new OpBGP(BasicPattern.wrap(gp1TripleList));
				List<Triple> gp2TripleList = triples.subList(separationIndex, triples.size());
				OpBGP gp2 = new OpBGP(BasicPattern.wrap(gp2TripleList));

				result = this.transJoin(bgp, gp1, gp2, Constants.JOINS_TYPE_INNER);
			}
		}

		return result;
	}

	protected IQuery trans(OpDistinct opDistinct) throws Exception {
		Op opDistinctSubOp = opDistinct.getSubOp(); 
		IQuery opDistinctSubOpSQL = this.trans(opDistinctSubOp);
		if(opDistinctSubOpSQL instanceof SQLQuery) {
			((SQLQuery)opDistinctSubOpSQL).setDistinct(true);	
		}
		return opDistinctSubOpSQL;
	}

	protected IQuery trans(OpFilter opFilter) throws Exception {
		Op opFilterSubOp = opFilter.getSubOp();
		IQuery subOpSQL = this.trans(opFilterSubOp);
		
		Collection<ZSelectItem> subOpSelectItems = subOpSQL.getSelectItems(); 
		String transGPSQLAlias = subOpSQL.generateAlias();

		ExprList exprList = opFilter.getExprs();
		SQLFromItem resultFrom;
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();
			String subQueryViewName = "sqf" + Math.abs(opFilterSubOp.hashCode());
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
			logger.info(dropViewSQL + ";\n");
			DBUtility.execute(conn, dropViewSQL);
			String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + subOpSQL;
			logger.info(createViewSQL + ";\n");
			DBUtility.execute(conn, createViewSQL);
			resultFrom = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
		} else {
			resultFrom = new SQLFromItem(subOpSQL.toString(), LogicalTableType.QUERY_STRING);
		}
		resultFrom.setAlias(transGPSQLAlias);

		IQuery result = null;
		ZExpression newWhere = this.transExprList(opFilterSubOp, exprList, subOpSelectItems, subOpSQL.getAlias());
		if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
			subOpSQL.pushFilterDown(newWhere);
			result = subOpSQL;
		} else {
			Collection<ZSelectItem> oldSelectItems = subOpSQL.getSelectItems();
			Collection<ZSelectItem> newSelectItems = new Vector<ZSelectItem>();
			for(ZSelectItem oldSelectItem : oldSelectItems) {
				String oldSelectItemAlias = oldSelectItem.getAlias();
				ZSelectItem newSelectItem = new ZSelectItem(transGPSQLAlias + "." + oldSelectItemAlias);
				newSelectItem.setAlias(oldSelectItemAlias);
				newSelectItems.add(newSelectItem);
			}
//			ZSelectItem newSelectItem = new ZSelectItem("*");
//			newSelectItems.add(newSelectItem);
			SQLQuery resultAux = new SQLQuery(subOpSQL);
			resultAux.setSelectItems(newSelectItems);
			resultAux.addWhere(newWhere);
			result = resultAux;
		}

		return result;
	}

	protected IQuery trans(OpJoin opJoin)  throws Exception {
		IQuery result = null;
		Op opLeft = opJoin.getLeft();
		Op opRight = opJoin.getRight();
		result = this.transJoin(opJoin, opLeft, opRight, Constants.JOINS_TYPE_INNER);
		return result;
	}

	protected IQuery trans(OpLeftJoin opLeftJoin) throws Exception {
		IQuery result = null;
		Op opLeft = opLeftJoin.getLeft();
		Op opRight = opLeftJoin.getRight();

		result = this.transJoin(opLeftJoin, opLeft, opRight, Constants.JOINS_TYPE_LEFT);
		return result;
	}

	protected IQuery trans(OpOrder opOrder) throws Exception {
		Op opOrderSubOp = opOrder.getSubOp();
		IQuery opOrderSubOpSQL = this.trans(opOrderSubOp);

		Vector<ZOrderBy> orderByConditions = new Vector<ZOrderBy>();
		for(SortCondition sortCondition : opOrder.getConditions()) {
			int sortConditionDirection = sortCondition.getDirection();
			Expr sortConditionExpr = sortCondition.getExpression();
			Var sortConditionVar = sortConditionExpr.asVar();

			String nameSortConditionVar = nameGenerator.generateName(sortConditionVar);
			ZExp zExp = new ZConstant(nameSortConditionVar, ZConstant.COLUMNNAME);

			ZOrderBy zOrderBy = new ZOrderBy(zExp);
			if(sortConditionDirection == Query.ORDER_DEFAULT) {
				zOrderBy.setAscOrder(true);
			} else if(sortConditionDirection == Query.ORDER_ASCENDING) {
				zOrderBy.setAscOrder(true);
			} if(sortConditionDirection == Query.ORDER_DESCENDING) {
				zOrderBy.setAscOrder(false);
			} else {
				zOrderBy.setAscOrder(true);
			}
			orderByConditions.add(zOrderBy);
		}

		IQuery transOpOrder; 
		if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
			//result = transOrderBy.eliminateSubQuery(null, null, orderByConditions,this.databaseType);
			opOrderSubOpSQL.pushOrderByDown(orderByConditions);
			transOpOrder = opOrderSubOpSQL;
		} else {
			opOrderSubOpSQL.setOrderBy(orderByConditions);
			transOpOrder = opOrderSubOpSQL;
		}

		String transOpOrderString = transOpOrder.toString();
		logger.debug("transOpOrder = \n" + transOpOrderString + "\n");
		return transOpOrder;
	}

	protected IQuery trans(OpProject opProject) throws Exception {
		Op opProjectSubOp = opProject.getSubOp();
		IQuery opProjectSubOpSQL = this.trans(opProjectSubOp);

		Collection<ZSelectItem> oldSelectItems = opProjectSubOpSQL.getSelectItems(); 
		String subOpSQLAlias = opProjectSubOpSQL.generateAlias();

		Collection<ZSelectItem> newSelectItems = new LinkedList<ZSelectItem>();
		List<Var> selectVars = opProject.getVars();
		for(Var selectVar : selectVars) {
			Collection<ZSelectItem> selectItemsByVars = this.generateSelectItem(
					selectVar, subOpSQLAlias, oldSelectItems, true);
			newSelectItems.addAll(selectItemsByVars);
		}


		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();
			String subQueryViewName = "sqp" + Math.abs(opProject.hashCode());
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
			logger.info(dropViewSQL + ";\n");
			DBUtility.execute(conn, dropViewSQL);
			String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + opProjectSubOpSQL;
			logger.info(createViewSQL  + ";\n");
			DBUtility.execute(conn, createViewSQL);
			new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
		}
		
		IQuery transProjectSQL;
		if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
			opProjectSubOpSQL.pushProjectionsDown(newSelectItems);
			transProjectSQL = opProjectSubOpSQL;
		} else {
			SQLQuery resultAux = new SQLQuery(opProjectSubOpSQL);
			resultAux.setSelectItems(newSelectItems);
			transProjectSQL = resultAux;
		}

		return transProjectSQL;
	}

	protected IQuery trans(OpSlice opSlice) throws Exception {
		long sliceLength = opSlice.getLength();
		long offset = opSlice.getStart();

		Op opSliceSubOp = opSlice.getSubOp();
		IQuery sqlQuery = this.trans(opSliceSubOp);
		if(sqlQuery instanceof SQLQuery) {
			((SQLQuery) sqlQuery).setSlice(sliceLength);
			if(offset > 0) {
				((SQLQuery) sqlQuery).setOffset(offset);	
			}
		}
		return sqlQuery;
	}

	protected IQuery trans(OpUnion opUnion) throws Exception {
		Op gp1 = opUnion.getLeft();
		Op gp2 = opUnion.getRight();

		IQuery transGP1 = this.trans(gp1);
		IQuery transGP2 = this.trans(gp2);

		String transGP1Alias = transGP1.generateAlias() + "R1";
		transGP1.setAlias(transGP1Alias);

		//SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transGP1FromItem;
		String subQueryGP1ViewName = "sql" + Math.abs(gp1.hashCode());
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryGP1ViewName;
			logger.info(dropViewSQL);
			DBUtility.execute(conn, dropViewSQL);
			String createViewSQL = "CREATE VIEW " + subQueryGP1ViewName + " AS " + transGP1;
			logger.info(createViewSQL);
			DBUtility.execute(conn, createViewSQL);
			transGP1FromItem = new SQLFromItem(subQueryGP1ViewName, LogicalTableType.TABLE_NAME);
		} else {
			//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
			transGP1FromItem = new SQLFromItem(transGP1.toString(), LogicalTableType.QUERY_STRING);
		}

		SQLQuery query1 = new SQLQuery();
		query1.setDatabaseType(this.databaseType);
		
		transGP1FromItem.setAlias(transGP1Alias);
		//query1.addFrom(transGP1FromItem);
		query1.addFromItem(new SQLJoinTable(transGP1, null, null));

		String transGP2Alias = transGP2.generateAlias() + "R2";
		transGP2.setAlias(transGP2Alias);

		//SQLFromItem transGP2FromItem = new SQLFromItem(transGP2.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transGP2FromItem;
		String subQueryGP2ViewName = "sqr" + Math.abs(gp2.hashCode());
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryGP2ViewName;
			logger.info(dropViewSQL);
			DBUtility.execute(conn, dropViewSQL);
			String createViewSQL = "CREATE VIEW " + subQueryGP2ViewName + " AS " + transGP2;
			logger.info(createViewSQL);
			DBUtility.execute(conn, createViewSQL);
			transGP2FromItem = new SQLFromItem(subQueryGP2ViewName, LogicalTableType.TABLE_NAME);
		} else {
			//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
			transGP2FromItem = new SQLFromItem(transGP2.toString(), LogicalTableType.QUERY_STRING);
		}
		transGP2FromItem.setAlias(transGP2Alias);

		query1.addFromItem(new SQLJoinTable(transGP2, Constants.JOINS_TYPE_LEFT, Constants.SQL_EXPRESSION_FALSE));

		Collection<ZSelectItem> gp1SelectItems = transGP1.getSelectItems();
		Collection<ZSelectItem> gp2SelectItems = transGP2.getSelectItems();
		Collection<Node> termsGP1 = QueryTranslatorUtility.terms(gp1, this.ignoreRDFTypeStatement);
		Collection<Node> termsGP2 = QueryTranslatorUtility.terms(gp2, this.ignoreRDFTypeStatement);
		Set<Node> termsA = new LinkedHashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
		Set<Node> termsB = new LinkedHashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
		Set<Node> termsC = new LinkedHashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
		Collection<ZSelectItem> selectItemsA = this.generateSelectItems(
				termsA, transGP1Alias, gp1SelectItems, false);
		Vector<ZSelectItem> selectItemsAList = new Vector<ZSelectItem>(selectItemsA);
		Collection<ZSelectItem> selectItemsB = this.generateSelectItems(
				termsB, transGP2Alias, gp2SelectItems, false);
		Vector<ZSelectItem> selectItemsBList = new Vector<ZSelectItem>(selectItemsB);
		Vector<Node> termsCList = new Vector<Node>(termsC);

		Vector<ZSelectItem> selectItems1 = new Vector<ZSelectItem>();
		query1.addSelect(selectItems1);
		selectItems1.addAll(selectItemsA);
		selectItems1.addAll(selectItemsB);
		Collection<ZSelectItem> selectItemsC = this.generateSelectItems(
				termsCList, transGP1Alias + ".", gp1SelectItems, false);
		for(ZSelectItem selectItemC : selectItemsC) {
			String alias = selectItemC.getAlias();
			if(alias == null || alias.equals("")) {
				alias = selectItemC.getColumn();
				selectItemC.setAlias(alias);
			}
		}
		selectItems1.addAll(selectItemsC);
		query1.addSelect(selectItems1);



		SQLQuery query2 = new SQLQuery();
		query2.setDatabaseType(this.databaseType);
		
		IQuery transR3 = this.trans(gp2);
		Collection<ZSelectItem> r3SelectItems = transR3.getSelectItems();

		String transR3Alias = transR3.generateAlias() + "R3";
		transR3.setAlias(transR3Alias);
		//SQLFromItem transR3FromItem = new SQLFromItem(transR3.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transR3FromItem;
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			transR3FromItem = new SQLFromItem(subQueryGP2ViewName, LogicalTableType.TABLE_NAME);
		} else {
			transR3FromItem = new SQLFromItem(transR3.toString(), LogicalTableType.QUERY_STRING);
		}
		transR3FromItem.setAlias(transR3Alias);
		//query2.addFrom(transR3FromItem);
		query2.addFromItem(new SQLJoinTable(transR3, null, null));

		IQuery transR4 = this.trans(gp1);
		String transR4Alias = transR4.generateAlias() + "R4";
		transR4.setAlias(transR4Alias);
		//SQLFromItem transR4FromItem = new SQLFromItem(transR4.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transR4FromItem;
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			transR4FromItem = new SQLFromItem(subQueryGP1ViewName, LogicalTableType.TABLE_NAME);
		} else {
			transR4FromItem = new SQLFromItem(transR4.toString(), LogicalTableType.QUERY_STRING);
		}
		transR4FromItem.setAlias(transR4Alias);

		query2.addFromItem(new SQLJoinTable(transR4, Constants.JOINS_TYPE_LEFT, Constants.SQL_EXPRESSION_FALSE));

		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		query2.addSelect(selectItems2);
		Collection<ZSelectItem> selectItemsA2 = this.generateSelectItems(
				termsA, transR4Alias, gp1SelectItems, false);
		selectItems2.addAll(selectItemsA2);
		Collection<ZSelectItem> selectItemsB2 = this.generateSelectItems(
				termsB, transR3Alias, gp2SelectItems, false);
		selectItems2.addAll(selectItemsB2);
		Collection<ZSelectItem> selectItemsC2 = this.generateSelectItems(
				termsCList, transR3Alias + ".", r3SelectItems, false);
		for(ZSelectItem selectItemC : selectItemsC2) {
			String alias = selectItemC.getAlias();
			if(alias == null || alias.equals("")) {
				alias = selectItemC.getColumn();
				selectItemC.setAlias(alias);
			}
		}
		selectItems2.addAll(selectItemsC2);

		SQLUnion transUnionSQL = new SQLUnion();
		transUnionSQL.setDatabaseType(this.databaseType);
		transUnionSQL.add(query1);
		transUnionSQL.add(query2);
		logger.debug("transUnionSQL = \n" + transUnionSQL);

		return transUnionSQL;
	}

	protected List<ZExp> transConstant(NodeValue nodeValue) {
		List<ZExp> result = new LinkedList<ZExp>();

		boolean isLiteral = nodeValue.isLiteral();
		//boolean isIRI = nodeValue.isIRI();
		boolean isIRI = nodeValue.getNode().isURI();

		if(isLiteral) {
			ZExp resultAux = this.transLiteral(nodeValue);
			result.add(resultAux);
		} else if(isIRI) {
			result = this.transIRI(nodeValue.getNode());
		}
		return result;
	}

	protected List<ZExp> transExpr(Op op, Expr expr
			, Collection<ZSelectItem> subOpSelectItems, String prefix) {
		List<ZExp> result = new LinkedList<ZExp>();

		if(expr.isVariable()) {
			logger.debug("expr is var");
			Var var = expr.asVar();
			List<ZExp> resultAuxs = this.transVar(var, subOpSelectItems, prefix);
			result.addAll(resultAuxs);
		} else if(expr.isConstant()) {
			logger.debug("expr is constant");
			NodeValue nodeValue = expr.getConstant();
			result = this.transConstant(nodeValue);
		} else if(expr.isFunction()) {
			logger.debug("expr is function");
			ExprFunction exprFunction = expr.getFunction();
			ZExp resultsAux = this.transFunction(op, exprFunction, subOpSelectItems, prefix);
			result.add(resultsAux); 
		}

		return result;
	}

	private ZExpression transExprList(Op op, ExprList exprList
			, Collection<ZSelectItem> subOpSelectItems, String prefix) {
		Collection<ZExp> resultAux = new Vector<ZExp>();
		List<Expr> exprs = exprList.getList();
		for(Expr expr : exprs) {
			List<ZExp> exprTranslated = this.transExpr(op, expr, subOpSelectItems, prefix);
			resultAux.addAll(exprTranslated);
		}
		ZExpression result = SQLUtility.combineExpresions(resultAux, Constants.SQL_LOGICAL_OPERATOR_AND);
		return result;
	}


	private ZExp transFunction(Op op, ExprFunction exprFunction
			, Collection<ZSelectItem> subOpSelectItems, String prefix) {
		ZExpression result;
		String functionSymbol = null;
		List<Expr> args = exprFunction.getArgs();
		
		if(exprFunction instanceof ExprFunction1) {
			Expr arg = args.get(0);
			
			if(exprFunction instanceof E_Bound) {
				//functionSymbol = "IS NOT NULL";
				functionSymbol = functionsMap.get(E_Bound.class.toString());
			} else if(exprFunction instanceof E_LogicalNot) {
				//functionSymbol = "NOT";
				functionSymbol = functionsMap.get(E_LogicalNot.class.toString());
			} else {
				functionSymbol = exprFunction.getOpName();
			}
			
			List<ZExp> argTranslated = this.transExpr(op, arg, subOpSelectItems, prefix);
			Collection<ZExp> resultAuxs = new Vector<ZExp>();
			for(int i=0; i<argTranslated.size(); i++ ) {
				ZExpression resultAux = new ZExpression(functionSymbol);
				ZExp operand = argTranslated.get(i);
				resultAux.addOperand(operand);
				resultAuxs.add(resultAux);
			}
			
			result = SQLUtility.combineExpresions(resultAuxs, Constants.SQL_LOGICAL_OPERATOR_AND);
		} else if(exprFunction instanceof ExprFunction2) {
			Expr leftArg = args.get(0);
			Expr rightArg = args.get(1);
			List<ZExp> leftExprTranslated = this.transExpr(op, leftArg, subOpSelectItems, prefix);
			List<ZExp> rightExprTranslated = this.transExpr(op, rightArg, subOpSelectItems, prefix);
			
			if(exprFunction instanceof E_NotEquals){
				if(Constants.DATABASE_MONETDB.equalsIgnoreCase(databaseType)) {
					functionSymbol = "<>";
				} else {
					functionSymbol = "!=";
				}

				String concatSymbol;
				if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(databaseType)) {
					concatSymbol = "||";
				} else {
					concatSymbol = "CONCAT";
				}
				
				ZExpression resultAux = new ZExpression(functionSymbol);
				
				//concat only when it has multiple arguments
				int leftExprTranslatedSize = leftExprTranslated.size();
				if(leftExprTranslatedSize == 1) {
					resultAux.addOperand(leftExprTranslated.get(0));
				} else if (leftExprTranslatedSize > 1) {
					ZExpression leftConcatOperand = new ZExpression(concatSymbol);
					for(int i=0; i<leftExprTranslated.size(); i++ ) {
						ZExp leftOperand = leftExprTranslated.get(i);
						if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(databaseType) && leftOperand instanceof ZConstant) {
							String leftOperandValue = ((ZConstant) leftOperand).getValue();
							SQLConstant leftOperandNew = new SQLConstant(leftOperandValue, ((ZConstant) leftOperand).getType());
							leftOperandNew.setColumnType("text");
							leftConcatOperand.addOperand(leftOperandNew);
						} else {
							leftConcatOperand.addOperand(leftOperand);	
						}
					}
					resultAux.addOperand(leftConcatOperand);					
				}
				
				
				int rightExprTranslatedSize = rightExprTranslated.size();
				if(rightExprTranslatedSize == 1) {
					resultAux.addOperand(rightExprTranslated.get(0));
				} else if (rightExprTranslatedSize > 1) {
					ZExpression rightConcatOperand = new ZExpression(concatSymbol);
					for(int i=0; i<rightExprTranslated.size(); i++ ) {
						ZExp rightOperand = rightExprTranslated.get(i);
						if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(databaseType) && rightOperand instanceof ZConstant) {
							String rightOperandValue = ((ZConstant) rightOperand).getValue();
							SQLConstant rightOperandNew = new SQLConstant(rightOperandValue, ((ZConstant) rightOperand).getType());
							rightOperandNew.setColumnType("text");
							rightConcatOperand.addOperand(rightOperandNew);
						} else {
							rightConcatOperand.addOperand(rightOperand);	
						}
					}
					resultAux.addOperand(rightConcatOperand);					
				}
				
				result = resultAux;
			} else {
				if(exprFunction instanceof E_LogicalAnd) {
					//functionSymbol = "AND";
					functionSymbol = functionsMap.get(E_LogicalAnd.class.toString());
				} else if(exprFunction instanceof E_LogicalOr) {
					//functionSymbol = "OR";
					functionSymbol = functionsMap.get(E_LogicalOr.class.toString());
				} else {
					functionSymbol = exprFunction.getOpName();
				}
				
				Collection<ZExp> resultAuxs = new Vector<ZExp>();
				for(int i=0; i<leftExprTranslated.size(); i++ ) {
					ZExpression resultAux = new ZExpression(functionSymbol);
					ZExp leftOperand = leftExprTranslated.get(i);
					resultAux.addOperand(leftOperand);
					ZExp rightOperand = rightExprTranslated.get(i);
					resultAux.addOperand(rightOperand);
					resultAuxs.add(resultAux);
				}
				result = SQLUtility.combineExpresions(resultAuxs, Constants.SQL_LOGICAL_OPERATOR_AND);
			}
		} else {
			List<List<ZExp>> transArgs = new Vector<List<ZExp>>();
			for(int i=0; i<args.size(); i++) {
				Expr arg = args.get(i);
				List<ZExp> zExps = this.transExpr(op, arg, subOpSelectItems, prefix);
				List<ZExp> transArg = new Vector<ZExp>();

				for(ZExp zExp : zExps) {
					if(exprFunction instanceof E_Regex && i==1) {
						zExp = new ZConstant("%" + ((ZConstant)zExp).getValue() + "%", ZConstant.STRING);
					}
					transArg.add(zExp);
				}
				transArgs.add(transArg);
			}

			if(exprFunction instanceof E_Regex) {
				//functionSymbol = "LIKE";
				functionSymbol = functionsMap.get(E_Regex.class.toString());
			} else if(exprFunction instanceof E_OneOf) {
				//functionSymbol = "IN";
				functionSymbol = functionsMap.get(E_OneOf.class.toString());
			} else {
				functionSymbol = exprFunction.getOpName();
			}
			
			Collection<ZExp> resultAuxs = new Vector<ZExp>();
			int arg0Size = transArgs.get(0).size();
			for(int j=0; j<arg0Size; j++ ) {
				ZExpression resultAux = new ZExpression(functionSymbol);
				for(int i=0; i<args.size(); i++) {
					ZExp operand = transArgs.get(i).get(j);
					resultAux.addOperand(operand);
				}
				resultAuxs.add(resultAux);
			}
			result = SQLUtility.combineExpresions(resultAuxs, Constants.SQL_LOGICAL_OPERATOR_AND);
		}
		
		return result;
	}



	protected abstract List<ZExp> transIRI(Node node);

	protected IQuery transJoin(Op opParent, Op gp1, Op gp2
			, String joinType) throws Exception  {
		logger.debug("entering transJoin");

		if (opParent instanceof OpLeftJoin) {
			OpLeftJoin opLefJoin = (OpLeftJoin) opParent;
			ExprList opLeftJoinExpr = opLefJoin.getExprs();
			if(opLeftJoinExpr != null && opLeftJoinExpr.size() > 0) {
				gp2 = OpFilter.filterDirect(opLeftJoinExpr, gp2);
			}
		}

		IQuery transGP1SQL = this.trans(gp1);
		IQuery transGP2SQL = this.trans(gp2);
		if(transGP1SQL == null && transGP2SQL == null) {
			return null;
		} else if(transGP1SQL != null && transGP2SQL == null) {
			return transGP1SQL;
		} else if(transGP1SQL == null && transGP2SQL != null) {
			return transGP2SQL;
		} else {
			String transGP1Alias = transGP1SQL.generateAlias();
			this.mapTransGP1Alias.put(opParent, transGP1Alias);
			SQLFromItem transGP1FromItem;
			if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
				Connection conn = this.getConnection();
				String subQueryViewName = "sql" + Math.abs(gp1.hashCode());
				String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
				logger.info(dropViewSQL + ";\n");
				DBUtility.execute(conn, dropViewSQL);
				String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + transGP1SQL;
				logger.info(createViewSQL + ";\n");
				DBUtility.execute(conn, createViewSQL);

				transGP1FromItem = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
			} else {
				//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
				transGP1FromItem = new SQLFromItem(transGP1SQL.toString(), LogicalTableType.QUERY_STRING);
			}
			transGP1FromItem.setAlias(transGP1Alias);


			String transGP2Alias = transGP2SQL.generateAlias();
			SQLFromItem transGP2FromItem;
			if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
				Connection conn = this.getConnection();
				String subQueryViewName = "sqr" + Math.abs(gp2.hashCode());
				String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
				logger.info(dropViewSQL + ";\n");
				DBUtility.execute(conn, dropViewSQL);
				String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + transGP2SQL;
				logger.info(createViewSQL + ";\n");
				DBUtility.execute(conn, createViewSQL);
				transGP2FromItem = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
			} else {
				transGP2FromItem = new SQLFromItem(transGP2SQL.toString(), LogicalTableType.QUERY_STRING);
			}
			transGP2FromItem.setAlias(transGP2Alias);


			//			SQLJoinQuery joinQuery2 = new SQLJoinQuery();
			//			joinQuery2.setJoinType(joinType);
			//			joinQuery2.setJoinSource(transGP2FromItem);
			//joinQuery.addLogicalTable(transGP2FromItem);


			Collection<Node> termsGP1 = QueryTranslatorUtility.terms(gp1, this.ignoreRDFTypeStatement);
			Collection<Node> termsGP2 = QueryTranslatorUtility.terms(gp2, this.ignoreRDFTypeStatement);
			Set<Node> termsA = new HashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
			Set<Node> termsB = new HashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
			Set<Node> termsC = new HashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
			this.mapTermsC.put(opParent, termsC);
			Collection<ZSelectItem> selectItems = new HashSet<ZSelectItem>();
			Collection<ZSelectItem> gp1SelectItems = transGP1SQL.getSelectItems();
			Collection<ZSelectItem> gp2SelectItems = transGP2SQL.getSelectItems();
			Collection<ZSelectItem> selectItemsA = this.generateSelectItems(
					termsA, transGP1Alias, gp1SelectItems, false);
			for(ZSelectItem selectItemA : selectItemsA) {
				selectItemA.setAlias(selectItemA.getColumn());
			}
			selectItems.addAll(selectItemsA);
			Collection<ZSelectItem> selectItemsB = this.generateSelectItems(
					termsB, transGP2Alias, gp2SelectItems, false);
			for(ZSelectItem selectItemB : selectItemsB) {
				selectItemB.setAlias(selectItemB.getColumn());
			}			
			selectItems.addAll(selectItemsB);
			Collection<ZSelectItem> selectItemsC = this.generateSelectItems(
					termsC, transGP1Alias, gp1SelectItems, false);
			for(ZSelectItem selectItemC : selectItemsC) {
				selectItemC.setAlias(selectItemC.getColumn());
			}
			selectItems.addAll(selectItemsC);
			logger.debug("selectItems = " + selectItems);


			//.... JOIN ... ON <joinOnExpression>
			SPARQLUtility sparqlUtility = new SPARQLUtility();
			Collection<ZExpression> joinOnExps = new HashSet<ZExpression>();
			for(Node termC : termsC) {
				boolean isTermCInSubjectGP1 = sparqlUtility.isNodeInSubjectGraph(termC, gp1);
				boolean isTermCInSubjectGP2 = sparqlUtility.isNodeInSubjectGraph(termC, gp2);
				
				if(termC.isVariable()) {
					List<String> termCColumns1 = this.getColumnsByNode(termC, gp1SelectItems);
					List<String> termCColumns2 = this.getColumnsByNode(termC, gp2SelectItems);

					if(termCColumns1.size() == termCColumns2.size()) {
						Iterator<String> termCColumns1Iterator = termCColumns1.iterator();
						Iterator<String> termCColumns2Iterator = termCColumns2.iterator();

						Collection<ZExpression> exps1Aux = new Vector<ZExpression>();
						Collection<ZExpression> exps2Aux = new Vector<ZExpression>();
						Collection<ZExpression> exps3Aux = new Vector<ZExpression>();
						while(termCColumns1Iterator.hasNext()) {
							String termCColumn1 = termCColumns1Iterator.next();
							String termCColumn2 = termCColumns2Iterator.next();
							ZConstant gp1TermC = new ZConstant(transGP1Alias + "." + termCColumn1, ZConstant.UNKNOWN);
							ZConstant gp2TermC = new ZConstant(transGP2Alias + "." + termCColumn2, ZConstant.UNKNOWN);

							ZExpression exp1Aux = new ZExpression("=", gp1TermC, gp2TermC);
							exps1Aux.add(exp1Aux);

							if(!isTermCInSubjectGP1 && !(gp1 instanceof OpBGP)) {
								ZExpression exp2Aux = new ZExpression("IS NULL", gp1TermC);
								exps2Aux.add(exp2Aux);
							}
							
							if(!isTermCInSubjectGP2 && !(gp2 instanceof OpBGP)) {
								ZExpression exp3Aux = new ZExpression("IS NULL", gp2TermC);
								exps3Aux.add(exp3Aux);								
							}
						}
						ZExpression exp1 = SQLUtility.combineExpresions(exps1Aux, Constants.SQL_LOGICAL_OPERATOR_AND);
						ZExpression exp2 = SQLUtility.combineExpresions(exps2Aux, Constants.SQL_LOGICAL_OPERATOR_AND);
						ZExpression exp3 = SQLUtility.combineExpresions(exps3Aux, Constants.SQL_LOGICAL_OPERATOR_AND);

						if(exps2Aux.isEmpty() && exps2Aux.isEmpty()) {
							joinOnExps.add(exp1);
						} else {
							ZExpression exp123 = new ZExpression("OR");
							exp123.addOperand(exp1);
							
							if(!isTermCInSubjectGP1) {
								exp123.addOperand(exp2);	
							}
							
							if(!isTermCInSubjectGP2) {
								exp123.addOperand(exp3);	
							}
							
							joinOnExps.add(exp123);							
						}
					}						
				}
			}				
			if(joinOnExps == null || joinOnExps.size() == 0) {
				joinOnExps.add(Constants.SQL_EXPRESSION_TRUE);
			}
			ZExpression joinOnExpression = SQLUtility.combineExpresions(joinOnExps, Constants.SQL_LOGICAL_OPERATOR_AND);

			IQuery transJoin = null;
			if(this.optimizer != null) {
				boolean isTransJoinSubQueryElimination = this.optimizer.isTransJoinSubQueryElimination();
				if(isTransJoinSubQueryElimination) {
					try {
						if(Constants.JOINS_TYPE_INNER.equals(joinType ) &&   //INNER join
								transGP1SQL instanceof SQLQuery && transGP2SQL instanceof SQLQuery) {
//							Collection<SQLLogicalTable> logicalTables = new Vector<SQLLogicalTable>();
//							logicalTables.add(transGP1SQL);
//							logicalTables.add(transGP2SQL);
							transJoin = SQLQuery.create(selectItems, transGP1SQL, transGP2SQL, joinType, joinOnExpression, this.databaseType);
						}					
					} catch(Exception e) {
						String errorMessage = "error while eliminating subquery in transjoin.";
						logger.error(errorMessage);
						transJoin = null;
					}					
				}
			}

			if(transJoin == null) { //subquery not eliminated
				SQLJoinTable table1 = new SQLJoinTable(transGP1SQL, null, null);
				table1.setAlias(transGP1Alias);
				SQLJoinTable table2 = new SQLJoinTable(transGP2SQL, joinType, joinOnExpression);
				table2.setAlias(transGP2Alias);
				
				SQLQuery transJoinAux = new SQLQuery();
				transJoinAux.setSelectItems(selectItems);
				transJoinAux.addFromItem(table1);
				transJoinAux.addFromItem(table2);
				transJoin = transJoinAux;
			}

			return transJoin;
		}
	}

	public IQuery translate(Query sparqlQuery) throws Exception {
		final Op opSparqlQuery = Algebra.compile(sparqlQuery) ;

		NodeTypeInferrer typeInferrer = new NodeTypeInferrer(
				this.mappingDocument);
		this.mapInferredTypes = typeInferrer.infer(sparqlQuery);
		logger.info("Inferred Types : \n" + typeInferrer.printInferredTypes());

		this.buildAlphaGenerator();
		this.alphaGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		boolean subQueryAsView = this.optimizer != null && this.optimizer.isSubQueryAsView();
		this.alphaGenerator.setSubqueryAsView(subQueryAsView);
		this.buildBetaGenerator();
		this.buildPRSQLGenerator();
		this.prSQLGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		this.buildCondSQLGenerator();
		this.condSQLGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		logger.debug("opSparqlQuery = " + opSparqlQuery);
		long start = System.currentTimeMillis();

		IQuery result = null;
		//logger.info("opSparqlQuery = " + opSparqlQuery);
		if(this.optimizer != null && this.optimizer.isSelfJoinElimination()) {
			QueryRewritter queryRewritter = new QueryRewritter();
			queryRewritter.setMapInferredTypes(mapInferredTypes);
			Op opSparqlQuery2 = queryRewritter.rewrite(opSparqlQuery);
			logger.debug("opSparqlQueryRewritten = \n" + opSparqlQuery2);
			result = this.trans(opSparqlQuery2);
		} else {
			result = this.trans(opSparqlQuery);
		}

		//		if(!(opSparqlQuery instanceof OpProject)) {
		//			Collection<Var> allVars = OpVars.allVars(opQueryPattern);
		//			logger.info("vars in query pattern = " + allVars);
		//			List<Var> vars = new Vector<Var>(allVars);
		//			opSparqlQuery = new OpProject(opSparqlQuery, vars); 
		//		}

		if(result != null) {
			result.cleanupSelectItems();
			result.cleanupOrderBy();
		}

		long end = System.currentTimeMillis();
		logger.debug("Query translation time = "+ (end-start)+" ms.");

		logger.info("sql = \n" + result + "\n");
		return result;
	}



	public IQuery translateFromQueryFile(String queryFilePath) throws Exception {
		//process SPARQL file
		logger.info("Parsing query file : " + queryFilePath);
		Query sparqlQuery = QueryFactory.read(queryFilePath);
		logger.debug("sparqlQuery = " + sparqlQuery);

		return this.translate(sparqlQuery);
	}


	public IQuery translateFromString(String queryString) throws Exception {
		//process SPARQL string
		logger.debug("Parsing query string : " + queryString);
		Query sparqlQuery = QueryFactory.create(queryString);
		logger.debug("sparqlQuery = " + sparqlQuery);

		return this.translate(sparqlQuery);
	}





	private ZExp transLiteral(NodeValue nodeValue) {
		ZExp result = null;
		Node node = nodeValue.getNode();
		if(nodeValue.isNumber()) {
			result = new ZConstant(node.getLiteralValue().toString(), ZConstant.NUMBER);	
		} else {
			result = new ZConstant(node.getLiteralValue().toString(), ZConstant.STRING);
		}
		return result;
	}


	protected IQuery trans(Triple tp, AbstractConceptMapping cm) 
			throws QueryTranslationException {
		IQuery result = null;

		Node tpPredicate = tp.getPredicate();
		if(tpPredicate.isURI()) {
			String predicateURI = tpPredicate.getURI();
			try {
				result = this.trans(tp, cm, predicateURI);	
			} catch(InsatisfiableSQLExpression e) {

			}
		} else if(tpPredicate.isVariable()) {
			Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings();
			if(pms != null && pms.size() > 0) {
				List<IQuery> sqlQueries = new Vector<IQuery>();
				for(AbstractPropertyMapping pm : pms) {
					String predicateURI = pm.getMappedPredicateName();
					try {
						IQuery sqlQuery = this.trans(tp, cm, predicateURI);
						sqlQueries.add(sqlQuery);
					} catch(InsatisfiableSQLExpression e) {
						logger.warn("Insatifiable sql while translating : " + predicateURI + " in " + cm.getConceptName());
					}
				}
				if(sqlQueries.size() == 1) {
					result = sqlQueries.iterator().next();
				} else if(sqlQueries.size() > 1) {
					result = new SQLUnion(sqlQueries);
				}
			}
		} else {
			throw new QueryTranslationException("invalid tp.predicate : " + tpPredicate);
		}

		return result;
	}

	protected abstract IQuery trans(Triple tp, AbstractConceptMapping cm, String predicateURI) throws QueryTranslationException, InsatisfiableSQLExpression;

	protected IQuery trans(Triple tp) throws QueryTranslationException {
		IQuery result = null;

		Node tpSubject = tp.getSubject();
		Node tpPredicate = tp.getPredicate();
		if(tpPredicate.isURI() && RDF.type.getURI().equals(tpPredicate.getURI()) 
				&& (this.isIgnoreRDFTypeStatement())) {
			result = null;
		} else {
			Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(tpSubject);
			if(cms == null || cms.isEmpty()) {
				String errorMessage = "Undefined triplesMap for triple : " + tp;
				logger.warn(errorMessage);
				String errorMessage2 = "All class mappings will be used.";
				logger.warn(errorMessage2);
				cms = this.mappingDocument.getClassMappings();
				if(cms == null || cms.size() == 0) {
					String errorMessage3 = "Mapping document doesn't contain any class mappings!";
					throw new QueryTranslationException(errorMessage3);
				}				
			}

			List<IQuery> unionOfSQLQueries = new Vector<IQuery>();
			Iterator<AbstractConceptMapping> cmsIterator = cms.iterator();
			while(cmsIterator.hasNext()) {
				AbstractConceptMapping cm = cmsIterator.next();
				IQuery resultAux = this.trans(tp, cm);
				if(resultAux != null) {
					unionOfSQLQueries.add(resultAux);	
				}
			}

			if(unionOfSQLQueries.size() == 0) {
				result = null;
			} else if(unionOfSQLQueries.size() == 1) {
				result = unionOfSQLQueries.get(0);
			} else if(unionOfSQLQueries.size() > 1) {
				result = new SQLUnion(unionOfSQLQueries);
			}
		}

		return result;
	}


	private IQuery transSTG(List<Triple> stg) throws Exception {
		IQuery result = null;

		Node stgSubject = stg.get(0).getSubject();
		Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(stgSubject);
		if(cms == null) {
			String errorMessage = "Undefined triplesMap for stg : " + stg;
			logger.warn(errorMessage);
			String errorMessage2 = "All class mappings will be used.";
			logger.warn(errorMessage2);
			cms = this.mappingDocument.getClassMappings();
			if(cms == null || cms.size() == 0) {
				String errorMessage3 = "Mapping document doesn't contain any class mappins!";
				throw new QueryTranslationException(errorMessage3);
			}				
		}

		Collection<IQuery> resultAux = new Vector<IQuery>();
		for(AbstractConceptMapping cm : cms) {
			IQuery sqlQuery = this.transSTG(stg, cm);
			resultAux.add(sqlQuery);
		}

		if(resultAux.size() == 1) {
			result = resultAux.iterator().next();
		} else if(cms.size() > 1) {
			result = new SQLUnion(resultAux);
		}

		return result;
	}

	private IQuery transSTG(List<Triple> stg
			, AbstractConceptMapping cm) throws Exception {
		IQuery transSTG;

		//AlphaSTG
		List<AlphaResultUnion> alphaResultUnionList = 
				this.alphaGenerator.calculateAlphaSTG(stg, cm);

		//check if no union in each of alpha tp
		boolean unionFree = true;
		for(AlphaResultUnion alphaTP : alphaResultUnionList) {
			if(alphaTP.size() > 1) {
				unionFree = false;
			}
		}

		if(!unionFree) {
			BasicPattern basicPatternHead = new BasicPattern();
			basicPatternHead.add(stg.get(0));
			OpBGP opBGPHead = new OpBGP(basicPatternHead);

			List<Triple> triplesTail = stg.subList(1, stg.size());
			BasicPattern basicPatternTail = BasicPattern.wrap(triplesTail);
			OpBGP opBGPTail = new OpBGP(basicPatternTail);

			Op opJoin = OpJoin.create(opBGPHead, opBGPTail);
			transSTG = this.trans(opJoin);
		} else {// no union in alpha
			//ALPHA(stg) returns the same result for subject
			AlphaResult alphaResult = alphaResultUnionList.get(0).get(0);
			SQLLogicalTable alphaSubject = alphaResult.getAlphaSubject();
			Collection<SQLJoinTable> alphaPredicateObjects = new Vector<SQLJoinTable>();
			for(AlphaResultUnion alphaTP : alphaResultUnionList) {
				Collection<SQLJoinTable> tpAlphaPredicateObjects = alphaTP.get(0).getAlphaPredicateObjects();
				alphaPredicateObjects.addAll(tpAlphaPredicateObjects);
			}

			//PRSQLSTG
			Collection<ZSelectItem> selectItems = 
					this.prSQLGenerator.genPRSQLSTG(stg, alphaResult
							, betaGenerator, nameGenerator, cm);


			//CondSQLSTG
			ZExpression condSQL = this.condSQLGenerator.genCondSQLSTG(stg
					, alphaResult, betaGenerator, cm);

			//TRANS(STG)
			SQLQuery resultAux = null;
			if(this.optimizer != null) {
				boolean isTransSTGSubQueryElimination = this.optimizer.isTransSTGSubQueryElimination();
				if(isTransSTGSubQueryElimination) {
					try {
						Collection<SQLLogicalTable> logicalTables = new Vector<SQLLogicalTable>();
						Collection<ZExpression> joinExpressions = new Vector<ZExpression>();
						logicalTables.add(alphaSubject);
						for(SQLJoinTable alphaPredicateObject : alphaPredicateObjects) {
							SQLLogicalTable logicalTable = alphaPredicateObject.getJoinSource();
							logicalTables.add(logicalTable);
							ZExpression joinExpression = alphaPredicateObject.getOnExpression();
							joinExpressions.add(joinExpression);
						}
						ZExpression newWhere = SQLUtility.combineExpresions(condSQL, joinExpressions, Constants.SQL_LOGICAL_OPERATOR_AND);
						resultAux = SQLQuery.create(selectItems, logicalTables, newWhere, this.databaseType);					
					} catch(Exception e) {
						String errorMessage = "error in eliminating subquery!";
						logger.error(errorMessage);
						resultAux = null;
					}					
				}
			} 

			if(resultAux == null) { //without subquery elimination or error occured during the process
				resultAux = new SQLQuery(alphaSubject);
				resultAux.setDatabaseType(this.databaseType);
				for(SQLJoinTable alphaPredicateObject : alphaPredicateObjects) {
					resultAux.addFromItem(alphaPredicateObject);//alpha predicate object
				}
				resultAux.setSelectItems(selectItems);
				resultAux.setWhere(condSQL);
			}

			transSTG = resultAux;
		}

		logger.debug("transSTG = " + transSTG);
		return transSTG;
	}


	private List<ZExp> transVar(Var var, Collection<ZSelectItem> subOpSelectItems, String prefix) {
		//		String nameVar = nameGenerator.generateName(var);
		//		ZExp zExp = new ZConstant(nameVar, ZConstant.COLUMNNAME);

		Collection<String> columns = this.getColumnsByNode(var, subOpSelectItems);
		List<ZExp> result = new LinkedList<ZExp>();
		for(String column : columns) {
			ZConstant constant;
			if(prefix == null) {
				constant = new ZConstant(column, ZConstant.COLUMNNAME);	
			} else {
				if(!prefix.endsWith(".")) {
					prefix += ".";
				}
				constant = new ZConstant(prefix + column, ZConstant.COLUMNNAME);
			}

			result.add(constant);
		}
		return result;
	}


	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public IQueryTranslationOptimizer getOptimizer() {
		return optimizer;
	}

	public void setOptimizer(IQueryTranslationOptimizer optimizer) {
		this.optimizer = optimizer;

	}

	public AbstractUnfolder getUnfolder() {
		return unfolder;
	}

	public AbstractAlphaGenerator getAlphaGenerator() {
		return alphaGenerator;
	}

	public void setAlphaGenerator(AbstractAlphaGenerator alphaGenerator) {
		this.alphaGenerator = alphaGenerator;
	}

	public AbstractBetaGenerator getBetaGenerator() {
		return betaGenerator;
	}

	public void setBetaGenerator(AbstractBetaGenerator betaGenerator) {
		this.betaGenerator = betaGenerator;
	}

	public AbstractPRSQLGenerator getPrSQLGenerator() {
		return prSQLGenerator;
	}

	public void setPrSQLGenerator(AbstractPRSQLGenerator prSQLGenerator) {
		this.prSQLGenerator = prSQLGenerator;
	}

	public AbstractCondSQLGenerator getCondSQLGenerator() {
		return condSQLGenerator;
	}

	public void setCondSQLGenerator(AbstractCondSQLGenerator condSQLGenerator) {
		this.condSQLGenerator = condSQLGenerator;
	}

	public NameGenerator getNameGenerator() {
		return nameGenerator;
	}

	public Map<String, Object> getMapVarMapping2() {
		return this.mapVarMapping2;
	}

	public String getDatabaseType() {
		return databaseType;
	}


	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}


	public ConfigurationProperties getConfigurationProperties() {
		return configurationProperties;
	}


	public void setConfigurationProperties(
			ConfigurationProperties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}


	public abstract String translateResultSet(String varName, AbstractResultSet rs);


}
