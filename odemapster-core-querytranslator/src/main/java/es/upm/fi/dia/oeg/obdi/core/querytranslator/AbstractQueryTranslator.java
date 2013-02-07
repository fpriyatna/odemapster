package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
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
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoinQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;

public abstract class AbstractQueryTranslator implements IQueryTranslator {
	private static Logger logger = Logger.getLogger(AbstractQueryTranslator.class);
	
	protected String queryFilePath;
	protected AbstractMappingDocument mappingDocument;
	protected AbstractUnfolder unfolder;
	private Connection connection;
	protected Map<Node, Set<AbstractConceptMapping>> mapInferredTypes;
	private IQueryTranslationOptimizer optimizer = null;
	protected boolean ignoreRDFTypeStatement = true;
	protected Map<Op, Collection<Node>> mapTermsC = new HashMap<Op, Collection<Node>>();
	protected Map<Op, String> mapTransGP1Alias = new HashMap<Op, String>();
	private Map<String, Object> mapVarMapping = new HashMap<String, Object>();
	public enum POS {sub, pre, obj}
	private AbstractAlphaGenerator alphaGenerator;
	private AbstractBetaGenerator betaGenerator;
	private NameGenerator nameGenerator;
	private AbstractPRSQLGenerator prSQLGenerator;
	private AbstractCondSQLGenerator condSQLGenerator;
	
	public AbstractQueryTranslator() {
		this.nameGenerator = new NameGenerator();
		Optimize.setFactory(new QueryRewritterFactory());
	}

	protected abstract void buildAlphaGenerator();

	protected abstract void buildBetaGenerator();

	protected abstract void buildCondSQLGenerator();

	protected abstract void buildPRSQLGenerator();

	//	public AbstractQueryTranslator(AbstractMappingDocument mappingDocument) {
	//		super();
	//		this.mappingDocument = mappingDocument;
	//	}


	private ZSelectItem generateSelectItem(Node node, String prefix) {
		if(prefix == null) {
			prefix = "";
		}

		NameGenerator nameGenerator = new NameGenerator();
		String nameA = nameGenerator.generateName(node);
		ZSelectItem selectItem = null;
		if(node.isVariable()) {
			selectItem = new ZSelectItem(prefix + nameA);
		} else if(node.isURI()){
			selectItem = new ZSelectItem(prefix + nameA);
		} else if(node.isLiteral()){
			Object literalValue = node.getLiteralValue();
			ZExp exp;
			if(literalValue instanceof String) {
				exp = new ZConstant(prefix + literalValue.toString(), ZConstant.STRING);							
			} else if (literalValue instanceof Double) {
				exp = new ZConstant(prefix + literalValue.toString(), ZConstant.NUMBER);
			} else {
				exp = new ZConstant(prefix + literalValue.toString(), ZConstant.STRING);							

			}
			selectItem = new SQLSelectItem();
			selectItem.setExpression(exp);
		} else {
			logger.warn("unsupported node " + node.toString());
		}

		if(selectItem != null) {
			if(nameA != null) {
				selectItem.setAlias(nameA);
			}
		}

		return selectItem;
	}


	private Collection<ZSelectItem> generateSelectItems(Collection<Node> nodes, String prefix) {
		Collection<ZSelectItem> result = new LinkedHashSet<ZSelectItem>();

		for(Node node : nodes) {
			ZSelectItem selectItem = this.generateSelectItem(node, prefix);
			result.add(selectItem);
		}

		return result;
	}

	protected abstract String generateTermCName(Node termC);

	public AbstractMappingDocument getMappingDocument() {
		return mappingDocument;
	}

	public String getQueryFilePath() {
		return queryFilePath;
	}

	//	public abstract AbstractQueryTranslator createQueryTranslator(
	//			AbstractMappingDocument mappingDocument, AbstractUnfolder unfolder);

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

	

	public void setQueryFilePath(String queryFilePath) {
		this.queryFilePath = queryFilePath;
	}


	public void setUnfolder(AbstractUnfolder unfolder) {
		this.unfolder = unfolder;
	}

	protected SQLQuery trans(Op op) throws Exception {
		SQLQuery result = null;
		if(op instanceof OpBGP) { //triple or bgp pattern
			OpBGP bgp = (OpBGP) op;
			if(bgp.getPattern().size() == 1) {
				Triple tp = bgp.getPattern().get(0);
				result = this.trans(tp);
			} else {
				result = this.trans(bgp);	
			}
		} else if(op instanceof OpJoin) { // AND pattern
			//			logger.debug("op instanceof OpJoin");
			OpJoin opJoin = (OpJoin) op;
			result = this.trans(opJoin);
		} else if(op instanceof OpLeftJoin) { //OPT pattern
			//			logger.debug("op instanceof OpLeftJoin");
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			result = this.trans(opLeftJoin);
		} else if(op instanceof OpUnion) { //UNION pattern
			//			logger.debug("op instanceof OpUnion");
			OpUnion opUnion = (OpUnion) op;
			result = this.trans(opUnion);
		} else if(op instanceof OpFilter) { //FILTER pattern
			//			logger.debug("op instanceof OpFilter");
			OpFilter opFilter = (OpFilter) op;
			result = this.trans(opFilter);
		} else if(op instanceof OpProject) {
			//			logger.debug("op instanceof OpProject/OpSlice/OpDistinct");
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

		return result;
	}

	protected SQLQuery trans(OpBGP bgp) throws Exception {
		SQLQuery result = null;

		if(QueryTranslatorUtility.isTriplePattern(bgp)) { //triple pattern
			Triple tp = bgp.getPattern().getList().get(0);
			result = this.trans(tp);
		} else { //bgp pattern
			BasicPattern basicPattern = bgp.getPattern();

			List<Triple> triples = basicPattern.getList();
			logger.debug("triples = " + triples);
			boolean isTSS;
			if(this.optimizer != null && this.optimizer.isSelfJoinElimination()) {
				isTSS = QueryTranslatorUtility.isTriplesSameSubject(triples);
				logger.debug("isTSS = " + isTSS);
			} else {
				isTSS = false;
			}


			if(this.optimizer != null && this.optimizer.isSelfJoinElimination() && isTSS) {
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

				// result = this.transJoin(gp1, gp2, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_INNER);					
				//result = this.transJoinInner(bgp, gp1, gp2);
				result = this.transJoin(bgp, gp1, gp2, Constants.JOINS_TYPE_INNER);
			}
		}

		return result;
	}

	protected SQLQuery trans(OpDistinct opDistinct) throws Exception {
		Op opDistinctSubOp = opDistinct.getSubOp(); 
		SQLQuery sqlQuery = this.trans(opDistinctSubOp);
		sqlQuery.setDistinct(true);
		//logger.info("transDistinct = \n" + sqlQuery + "\n");
		return sqlQuery;
	}

	protected SQLQuery trans(OpFilter opFilter) throws Exception {
		SQLQuery result = null;
		Op opFilterSubOp = opFilter.getSubOp();
		ExprList exprList = opFilter.getExprs();
		//result = this.transFilter(opFilterSubOp, exprList);
		SQLFromItem resultFrom;

		SQLQuery transGPSQL = this.trans(opFilterSubOp);
		String transGPSQLAlias = transGPSQL.generateAlias();


		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();

			String subQueryViewName = "sqf" + Math.abs(opFilterSubOp.hashCode());
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
			logger.info(dropViewSQL + ";\n");
			boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);

			String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + transGPSQL;
			logger.info(createViewSQL + ";\n");
			boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);

			resultFrom = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
		} else {
			//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
			resultFrom = new SQLFromItem(transGPSQL.toString(), LogicalTableType.QUERY_STRING);
		}
		resultFrom.setAlias(transGPSQLAlias);

		String transGP1Alias = this.mapTransGP1Alias.get(opFilterSubOp);
		Collection<ZExp> transExprs;
		if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
			transExprs = this.transExpr(opFilterSubOp, exprList);
		} else {
			transExprs = this.transExpr(null, exprList);
		}

		ZExpression newWhere = null;
		if(transExprs.size() == 1) {
			newWhere = (ZExpression) transExprs.iterator().next();
		} else if(transExprs.size() > 1) {
			for(ZExp transExpr : transExprs) {
				if(newWhere == null) {
					newWhere = (ZExpression) transExpr; 
				} else {
					newWhere = new ZExpression("AND", newWhere, transExpr);
				}
			}
		}

		ZSelectItem newSelectItem = new ZSelectItem("*");
		Collection<ZSelectItem> newSelectItems = new Vector<ZSelectItem>();
		newSelectItems.add(newSelectItem);

		if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
			result = QueryTranslatorUtility.eliminateSubQuery(newSelectItems, transGPSQL, newWhere, null);
		} else {
			result = new SQLQuery();
			result.setSelectItems(newSelectItems);
			result.addFrom(resultFrom);
			if(transExprs.size() > 0) {
				if(transExprs.size() == 1) {
					ZExp zExp = transExprs.iterator().next();
					result.addWhere(zExp);
				} else {
					ZExpression whereExpression = new ZExpression("AND");
					for(ZExp zExp : transExprs) {
						whereExpression.addOperand(zExp);
					}
					result.addWhere(whereExpression);
				}
			}
		}

		return result;

	}

	protected SQLQuery trans(OpJoin opJoin)  throws Exception {
		SQLQuery result = null;
		Op opLeft = opJoin.getLeft();
		Op opRight = opJoin.getRight();
		//result = this.transJoin(opLeft, opRight, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_INNER);
		//result = this.transJoinInner(opJoin, opLeft, opRight);
		result = this.transJoin(opJoin, opLeft, opRight, Constants.JOINS_TYPE_INNER);

		return result;
	}





	protected SQLQuery trans(OpLeftJoin opLeftJoin) throws Exception {
		SQLQuery result = null;
		Op opLeft = opLeftJoin.getLeft();
		Op opRight = opLeftJoin.getRight();


		//		if(opLeftJoin.getExprs() == null) {
		//			result = this.transLeftJoin(opLeftJoin, opLeft, opRight);
		//		} else {
		//			ExprList exprList = opLeftJoin.getExprs();
		//			Expr exprNull = null;
		//			Op opLeftJoin2 = OpLeftJoin.create(opLeft, opRight, exprNull);
		//			result = this.transFilter(opLeftJoin2, exprList);
		//		}
		result = this.transJoin(opLeftJoin, opLeft, opRight, Constants.JOINS_TYPE_LEFT);
		return result;
	}



	//protected abstract SQLQuery transJoin(Op opParent, Op gp1, Op gp2, String joinType) throws Exception;
	//protected abstract SQLQuery transFilter(Op gp, ExprList exprList) throws Exception;
	//protected abstract SQLQuery transUNION(Op gp1, Op gp2) throws Exception;
	//protected abstract SQLQuery transProject(Op opQuery) throws Exception;

	protected SQLQuery trans(OpOrder opOrder) throws Exception {
		Vector<ZOrderBy> orderByConditions = new Vector<ZOrderBy>();
		for(SortCondition sortCondition : opOrder.getConditions()) {
			int sortConditionDirection = sortCondition.getDirection();
			Expr sortConditionExpr = sortCondition.getExpression();
			Var sortConditionVar = sortConditionExpr.asVar();

			String nameSortConditionVar = nameGenerator.generateName(sortConditionVar);

			//ZExp zExp = this.transExpr(graphPatternOp, sortConditionExpr);
			//ZExp zExp = new ZConstant(sortConditionVar.getName(), ZConstant.COLUMNNAME);

			ZExp zExp;
			if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
				zExp = new ZConstant(sortConditionVar.getName(), ZConstant.COLUMNNAME);
			} else {
				zExp = new ZConstant(nameSortConditionVar, ZConstant.COLUMNNAME);
			}

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

		Op opOrderSubOp = opOrder.getSubOp();
		SQLQuery sqlQuery = this.trans(opOrderSubOp); 
		sqlQuery.addOrderBy(orderByConditions);

		logger.debug("trans orderBy = \n" + sqlQuery + "\n");
		return sqlQuery;
	}

	protected SQLQuery trans(OpProject opProject) throws Exception {
		Op graphPatternOp = opProject.getSubOp();
		SQLQuery gpSQL = this.trans(graphPatternOp);
		String gpSQLAlias = gpSQL.generateAlias();
		logger.debug("gpSQL result = " + gpSQL.toString());

		Collection<ZSelectItem> newSelectItems = new HashSet<ZSelectItem>();
		List<Var> selectVars = opProject.getVars();
		for(Var selectVar : selectVars) {
			String nameSelectVar = nameGenerator.generateName(selectVar);
			//			ZSelectItem selectItem = new ZSelectItem(gpSQLAlias + "." + nameSelectVar);
			ZSelectItem selectItem = new ZSelectItem(nameSelectVar);
			selectItem.setAlias(selectVar.getName());
			newSelectItems.add(selectItem);
		}

		SQLQuery result = new SQLQuery();
		SQLFromItem fromItem;
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();

			String subQueryViewName = "sqp" + Math.abs(opProject.hashCode());
			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
			logger.info(dropViewSQL + ";\n");
			boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);

			String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + gpSQL;
			logger.info(createViewSQL  + ";\n");
			boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);

			fromItem = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
		} else {
			fromItem = new SQLFromItem(gpSQL.toString(), LogicalTableType.QUERY_STRING);
		}

		Vector<ZOrderBy> orderByConditions = gpSQL.getOrderBy();
		if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
			result = QueryTranslatorUtility.eliminateSubQuery(newSelectItems, gpSQL, null, orderByConditions);
		} else {
			fromItem.setAlias(gpSQLAlias);
			result.setSelectItems(newSelectItems);
			result.addFrom(fromItem);
		}

		logger.debug("trans project = \n" + result + "\n");
		return result;

	}

	protected SQLQuery trans(OpSlice opSlice) throws Exception {
		long sliceLength = opSlice.getLength();
		Op opSliceSubOp = opSlice.getSubOp();
		SQLQuery sqlQuery = this.trans(opSliceSubOp);
		sqlQuery.setSlice(sliceLength);

		//logger.debug("trans slice = \n" + sqlQuery + "\n");
		return sqlQuery;
	}

	protected SQLQuery trans(OpUnion opUnion) throws Exception {
		SQLQuery result = null;
		Op opLeft = opUnion.getLeft();
		Op opRight = opUnion.getRight();
		result = this.transUnion(opLeft, opRight);
		return result;
	}


	protected ZExp transConstant(NodeValue nodeValue) {
		ZExp result = null;

		boolean isLiteral = nodeValue.isLiteral();
		//boolean isIRI = nodeValue.isIRI();
		boolean isIRI = nodeValue.getNode().isURI();

		if(isLiteral) {
			result = this.transLiteral(nodeValue);
		} else if(isIRI) {
			result = this.transIRI(nodeValue.getNode());
		}
		return result;
	}


	protected ZExp transExpr(Op op, Expr expr) {
		ZExp result = null;

		if(expr.isVariable()) {
			logger.debug("expr is var");
			Var var = expr.asVar();
			result = this.transVar(op, var);
		} else if(expr.isConstant()) {
			logger.debug("expr is constant");
			NodeValue nodeValue = expr.getConstant();
			result = this.transConstant(nodeValue);
		} else if(expr.isFunction()) {
			logger.debug("expr is function");
			ExprFunction exprFunction = expr.getFunction();
			result = this.transFunction(op, exprFunction);
		}

		return result;
	}

	private Collection<ZExp> transExpr(Op op, ExprList exprList) {
		Collection<ZExp> result = new HashSet<ZExp>();
		List<Expr> exprs = exprList.getList();
		for(Expr expr : exprs) {
			ZExp transExpr = this.transExpr(op, expr);
			result.add(transExpr);
		}
		return result;
	}


	private ZExp transFunction(Op op, ExprFunction exprFunction) {
		ZExp result = null;

		String databaseType = null;
		if(AbstractRunner.configurationProperties != null) {
			databaseType = AbstractRunner.configurationProperties.getDatabaseType();
		}
		if(databaseType == null) {
			databaseType = Constants.DATABASE_MYSQL;
		}


		List<Expr> args = exprFunction.getArgs();

		String functionSymbol;
		if(exprFunction instanceof E_Bound) {
			functionSymbol = "IS NOT NULL";
		} else if(exprFunction instanceof E_LogicalNot) {
			functionSymbol = "NOT";
		} else if(exprFunction instanceof E_LogicalAnd) {
			functionSymbol = "AND";
		} else if(exprFunction instanceof E_LogicalOr) {
			functionSymbol = "OR";
		} else if(exprFunction instanceof E_NotEquals){
			if(Constants.DATABASE_MONETDB.equalsIgnoreCase(databaseType)) {
				functionSymbol = "<>";
			} else {
				functionSymbol = "!=";
			}
		} else if(exprFunction instanceof E_Regex) {
			functionSymbol = "LIKE";
		} else {
			functionSymbol = exprFunction.getOpName();
		}
		result = new ZExpression(functionSymbol);


		for(int i=0; i<args.size(); i++) {
			Expr arg = args.get(i);
			ZExp argExp = this.transExpr(op, arg);

			if(exprFunction instanceof E_Regex && i==1) {
				argExp = new ZConstant("%" + ((ZConstant)argExp).getValue() + "%", ZConstant.STRING);
			}
			((ZExpression)result).addOperand(argExp);
		}

		return result;
	}



	protected abstract ZExp transIRI(Node node);

	protected SQLQuery transJoin(Op opParent, Op gp1, Op gp2, String joinType)
			throws Exception  {
		logger.debug("entering transJoin");

		if (opParent instanceof OpLeftJoin) {
			OpLeftJoin opLefJoin = (OpLeftJoin) opParent;
			ExprList opLeftJoinExpr = opLefJoin.getExprs();
			if(opLeftJoinExpr != null && opLeftJoinExpr.size() > 0) {
				gp2 = OpFilter.filterDirect(opLeftJoinExpr, gp2);
			}
		}



		SQLQuery transGP1SQL = this.trans(gp1);
		SQLQuery transGP2SQL = this.trans(gp2);
		if(transGP1SQL == null && transGP2SQL == null) {
			return null;
		} else if(transGP1SQL != null && transGP2SQL == null) {
			return transGP1SQL;
		} else if(transGP1SQL == null && transGP2SQL != null) {
			return transGP2SQL;
		} else {
			SQLQuery transJoin = new SQLQuery();



			String transGP1Alias = transGP1SQL.generateAlias();
			this.mapTransGP1Alias.put(opParent, transGP1Alias);
			//SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), SQLFromItem.FORM_QUERY);
			SQLFromItem transGP1FromItem;
			if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
				Connection conn = this.getConnection();

				String subQueryViewName = "sql" + Math.abs(gp1.hashCode());
				String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
				logger.info(dropViewSQL + ";\n");
				boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);

				String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + transGP1SQL;
				logger.info(createViewSQL + ";\n");
				boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);

				transGP1FromItem = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
			} else {
				//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
				transGP1FromItem = new SQLFromItem(transGP1SQL.toString(), LogicalTableType.QUERY_STRING);
			}
			transGP1FromItem.setAlias(transGP1Alias);
			transJoin.addFrom(transGP1FromItem);


			String transGP2Alias = transGP2SQL.generateAlias();
			//SQLFromItem transGP2FromItem = new SQLFromItem(transGP2.toString(), SQLFromItem.FORM_QUERY);
			SQLFromItem transGP2FromItem;
			if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
				Connection conn = this.getConnection();

				String subQueryViewName = "sqr" + Math.abs(gp2.hashCode());
				String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
				logger.info(dropViewSQL + ";\n");
				boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);

				String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + transGP2SQL;
				logger.info(createViewSQL + ";\n");
				boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);

				transGP2FromItem = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
			} else {
				//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
				transGP2FromItem = new SQLFromItem(transGP2SQL.toString(), LogicalTableType.QUERY_STRING);
			}
			transGP2FromItem.setAlias(transGP2Alias);

			SQLJoinQuery joinQuery = new SQLJoinQuery();
			joinQuery.setJoinType(joinType);
			joinQuery.setJoinSource(transGP2FromItem);

			logger.debug("transGP1 = \n" + transGP1SQL);
			logger.debug("transGP2 = \n" + transGP2SQL);

			Collection<Node> termsGP1 = QueryTranslatorUtility.terms(gp1, this.ignoreRDFTypeStatement);
			logger.debug("termsGP1 = " + termsGP1);
			Collection<Node> termsGP2 = QueryTranslatorUtility.terms(gp2, this.ignoreRDFTypeStatement);
			logger.debug("termsGP2 = " + termsGP2);
			Set<Node> termsA = new HashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
			logger.debug("termsA = " + termsA);
			Set<Node> termsB = new HashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
			logger.debug("termsB = " + termsB);
			Set<Node> termsC = new HashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
			this.mapTermsC.put(opParent, termsC);
			logger.debug("termsC = " + termsC);
			Collection<ZSelectItem> selectItems = new HashSet<ZSelectItem>();
			Collection<ZSelectItem> selectItemsA = this.generateSelectItems(termsA, null);
			logger.debug("selectItemsA = " + selectItemsA);
			selectItems.addAll(selectItemsA);
			Collection<ZSelectItem> selectItemsB = this.generateSelectItems(termsB, null);
			logger.debug("selectItemsB = " + selectItemsB);
			selectItems.addAll(selectItemsB);

			Collection<ZSelectItem> selectItemsC = this.generateSelectItems(termsC, transGP1Alias + ".");
			logger.debug("selectItemsC = " + selectItemsC);
			selectItems.addAll(selectItemsC);

			ZExp joinOnExpression = new ZConstant("TRUE", ZConstant.UNKNOWN);
			Collection<ZExpression> joinOnExps = new HashSet<ZExpression>();
			for(Node termC : termsC) {
				String termCName;
				termCName = this.generateTermCName(termC);

				//				Collection<AbstractConceptMapping> cmTermCs = this.mapInferredTypes.get(termC);
				//				if(cmTermCs != null) {
				//					AbstractConceptMapping cmTermC = cmTermCs.iterator().next();
				//					if(cmTermC != null && cmTermC.hasWellDefinedURIExpression()) {
				//						//termCName = pkColumnAlias;
				//						termCName = this.mapNodeKey.get(termC);
				//					} else {
				//						termCName = nameGenerator.generateName(null, termC);
				//					}
				//				} else {
				//					termCName = nameGenerator.generateName(null, termC);
				//				}

				ZConstant gp1TermC = new ZConstant(transGP1Alias + "." + termCName, ZConstant.UNKNOWN);
				ZConstant gp2TermC = new ZConstant(transGP2Alias + "." + termCName, ZConstant.UNKNOWN);

				ZExp exp1 = new ZExpression("=", gp1TermC, gp2TermC);

				ZExp exp2 = new ZExpression("IS NULL", gp1TermC);

				ZExp exp3 = new ZExpression("IS NULL", gp2TermC);

				ZExpression exp12 = new ZExpression("OR", exp1, exp2);

				ZExpression exp123 = new ZExpression("OR");
				exp123.addOperand(exp1);
				exp123.addOperand(exp2);
				exp123.addOperand(exp3);

				joinOnExps.add(exp123);
				joinOnExpression = new ZExpression("AND", joinOnExpression, exp123);
			}

			ZExp joinOnExpression2;
			if(joinOnExps.size() == 0) {
				//SQL Server doesnt have TRUE constant
				//joinOnExpression2 = new ZConstant("TRUE", ZConstant.UNKNOWN);
				joinOnExpression2 = new ZExpression("=", new ZConstant("1", ZConstant.NUMBER), new ZConstant("1", ZConstant.NUMBER));
			} else if(joinOnExps.size() == 1) {
				joinOnExpression2 = joinOnExps.iterator().next();
			} else {
				joinOnExpression2 = new ZExpression("AND");
				for(ZExpression exp : joinOnExps) {
					((ZExpression)joinOnExpression2).addOperand(exp);
				}
			}
			logger.debug("joinOnExpression = " + joinOnExpression2);
			joinQuery.setOnExpression(joinOnExpression2);


			transJoin.addJoinQuery(joinQuery);			


			logger.debug("selectItems = " + selectItems);

			transJoin.setSelectItems(selectItems);



			//logger.info("transJoin = \n" + transJoin + "\n");
			return transJoin;
		}

	}

	public SQLQuery translate(Query sparqlQuery) throws Exception {
		final Op opSparqlQuery = Algebra.compile(sparqlQuery) ;
		
		NodeTypeInferrer typeInferrer = new NodeTypeInferrer(
				this.mappingDocument);
		this.mapInferredTypes = typeInferrer.infer(sparqlQuery);
		logger.info("Inferred Types : " + this.mapInferredTypes);
		
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

		SQLQuery result = null;
		//logger.info("opSparqlQuery = " + opSparqlQuery);
		if(this.optimizer != null && this.optimizer.isSelfJoinElimination()) {
			Op opSparqlQuery2 = new QueryRewritter().rewrite(opSparqlQuery);
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




		Vector<ZSelectItem> selectItems = result.getSelect();
		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		for(ZSelectItem selectItem : selectItems) {
			String selectItemAlias = selectItem.getAlias();
			if(selectItemAlias != null) {
				if(selectItemAlias.startsWith(this.nameGenerator.PREFIX_VAR)) {
					String newSelectItemAlias = 
							selectItemAlias.substring(this.nameGenerator.PREFIX_VAR.length(), selectItemAlias.length());
					selectItem.setAlias(newSelectItemAlias);
					selectItems2.add(selectItem);					
				} else if(selectItemAlias.startsWith(this.nameGenerator.PREFIX_LIT)) {
					//do nothing
				} else if(selectItemAlias.startsWith(this.nameGenerator.PREFIX_URI)) {
					//do nothing
				} else {
					selectItems2.add(selectItem);
				}
			}
		}
		result.setSelectItems(selectItems2);
		
		long end = System.currentTimeMillis();
		logger.debug("Query translation time = "+ (end-start)+" ms.");

		//logger.info("trans query = \n" + result + "\n");
		return result;
	}


	public SQLQuery translateFromPropertyFile() throws Exception {
		//process SPARQL file
		if(this.queryFilePath == null) {
			throw new Exception("No query file defined");
		}
		
		logger.info("Parsing query file : " + this.queryFilePath);
		Query sparqlQuery = QueryFactory.read(this.queryFilePath);
		logger.debug("sparqlQuery = " + sparqlQuery);

		return this.translate(sparqlQuery);
	}

	public SQLQuery translateFromQueryFile(String queryFilePath) throws Exception {
		//process SPARQL file
		logger.info("Parsing query file : " + queryFilePath);
		Query sparqlQuery = QueryFactory.read(queryFilePath);
		logger.debug("sparqlQuery = " + sparqlQuery);

		return this.translate(sparqlQuery);
	}


	public SQLQuery translateFromString(String queryString) throws Exception {
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


	protected abstract SQLQuery trans(Triple tp, AbstractConceptMapping cm) throws QueryTranslationException;

	protected SQLQuery trans(Triple tp) throws QueryTranslationException {
		SQLQuery result = null;

		Node tpSubject = tp.getSubject();
		Node tpPredicate = tp.getPredicate();
		if(tpPredicate.isURI() && RDF.type.getURI().equals(tpPredicate.getURI()) 
				&& (this.isIgnoreRDFTypeStatement())) {
			result = null;
		} else {
			Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(tpSubject);
			if(cms == null) {
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

			List<SQLQuery> unionOfSQLQueries = new Vector<SQLQuery>();
			Iterator<AbstractConceptMapping> cmsIterator = cms.iterator();
			while(cmsIterator.hasNext()) {
				AbstractConceptMapping cm = cmsIterator.next();
				SQLQuery resultAux = this.trans(tp, cm);
				if(resultAux != null) {
					unionOfSQLQueries.add(resultAux);	
				}
				
			}
			
			if(unionOfSQLQueries.size() == 0) {
				result = null;
			} else if(unionOfSQLQueries.size() == 1) {
				result = unionOfSQLQueries.get(0);
			} else if(unionOfSQLQueries.size() > 1) {
				result = unionOfSQLQueries.get(0);
				for(int i = 1; i<unionOfSQLQueries.size(); i++) {
					result.addUnionQuery(unionOfSQLQueries.get(i));
				}
			}
		}

		return result;

	}


	private SQLQuery transSTG(List<Triple> triples) throws Exception {
		SQLQuery result = null;

		Node tpSubject = triples.get(0).getSubject();
		Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(tpSubject);
		if(cms == null) {
			String errorMessage = "Undefined triplesMap for triples : " + triples;
			logger.warn(errorMessage);
			String errorMessage2 = "All class mappings will be used.";
			logger.warn(errorMessage2);
			cms = this.mappingDocument.getClassMappings();
			if(cms == null || cms.size() == 0) {
				String errorMessage3 = "Mapping document doesn't contain any class mappins!";
				throw new QueryTranslationException(errorMessage3);
			}				
		}

		Iterator<AbstractConceptMapping> cmsIterator = cms.iterator();
		result = this.transSTG(triples, cmsIterator.next());
		while(cmsIterator.hasNext()) {
			AbstractConceptMapping cm = cmsIterator.next();
			SQLQuery resultAux = this.transSTG(triples, cm);
			result.addUnionQuery(resultAux);
		}

		return result;
	}

	private SQLQuery transSTG(List<Triple> triples, AbstractConceptMapping cm) throws Exception {
		SQLQuery result = new SQLQuery();


		//AlphaSTG
		List<AlphaResultUnion> alphaResultUnionList = this.alphaGenerator.calculateAlphaSTG(triples, cm);

		//check if no union in each of alpha tp
		boolean unionFree = true;
		for(AlphaResultUnion alphaTP : alphaResultUnionList) {
			if(alphaTP.size() > 1) {
				unionFree = false;
			}
		}


		if(!unionFree) {
			BasicPattern basicPatternHead = new BasicPattern();
			basicPatternHead.add(triples.get(0));
			OpBGP opBGPHead = new OpBGP(basicPatternHead);

			List<Triple> triplesTail = triples.subList(1, triples.size());
			BasicPattern basicPatternTail = BasicPattern.wrap(triplesTail);
			OpBGP opBGPTail = new OpBGP(basicPatternTail);

			Op opJoin = OpJoin.create(opBGPHead, opBGPTail);
			result = this.trans(opJoin);
		} else {// no union in alpha
			//alpha(stg) returns the same result for subject
			SQLLogicalTable alphaSubject = alphaResultUnionList.get(0).get(0).getAlphaSubject();

			Collection<SQLQuery> alphaPredicateObjects = new Vector<SQLQuery>();
			for(AlphaResultUnion alphaTP : alphaResultUnionList) {
				Collection<SQLQuery> tpAlphaPredicateObjects = alphaTP.get(0).getAlphaPredicateObjects();
				alphaPredicateObjects.addAll(tpAlphaPredicateObjects);
			}

			if(alphaPredicateObjects == null || alphaPredicateObjects.isEmpty()) {
				if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
					boolean condition1 = false;
					if(alphaSubject instanceof SQLFromItem 
							&& ((SQLFromItem)alphaSubject).getForm() == LogicalTableType.QUERY_STRING ) {
						condition1 = true;
					}
					boolean condition2 = false;
					if(alphaSubject instanceof SQLQuery) {
						condition2 = true;
					}

					if(condition1 || condition2) {
						String logicalTableAlias = alphaSubject.getAlias();
						alphaSubject.setAlias("");

						Connection conn = this.getConnection();

						String subQueryViewName = "sqa" + Math.abs(triples.hashCode());
						String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryViewName;
						logger.info(dropViewSQL + ";\n");
						boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);

						String createViewSQL = "CREATE VIEW " + subQueryViewName + " AS " + alphaSubject;
						logger.info(createViewSQL + ";\n");
						boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);


						alphaSubject = new SQLFromItem(subQueryViewName, LogicalTableType.TABLE_NAME);
						alphaSubject.setAlias(logicalTableAlias);					
					}
				}
			} else {
				for(SQLQuery alphaPredicateObject : alphaPredicateObjects) {
					result.addJoinQuery(alphaPredicateObject);//alpha predicate object
				}
			}

			result.addLogicalTable(alphaSubject);//alpha subject

			//BetaSTG
			List<BetaResultSet> betaResultSetList = this.betaGenerator.calculateBetaSTG(triples, cm, alphaResultUnionList);

			//PRSQLSTG
			Collection<ZSelectItem> selectItems = 
					this.prSQLGenerator.genPRSQLSTG(triples, betaResultSetList, nameGenerator, cm);
			result.setSelectItems(selectItems);

			//CondSQLSTG
			ZExpression condSQL = this.condSQLGenerator.genCondSQLSTG(triples
					, alphaResultUnionList, betaResultSetList, cm);
			if(condSQL != null) {
				result.addWhere(condSQL);
			}

			if(this.optimizer != null && this.optimizer.isSubQueryElimination()) {
				result = QueryTranslatorUtility.eliminateSubQuery(result);
			}
		}




		if(alphaResultUnionList.size() == 1) {
			AlphaResultUnion alphaResultUnion = alphaResultUnionList.get(0);
			if(alphaResultUnion.size() > 1) {

			} else {

			}
		} else if(alphaResultUnionList.size() > 1) {

		}



		logger.debug("transTSS = " + result);
		return result;
	}


	private SQLQuery transUnion(Op gp1, Op gp2)
			throws Exception  {
		Collection<Node> termsGP1 = QueryTranslatorUtility.terms(gp1, this.ignoreRDFTypeStatement);
		Collection<Node> termsGP2 = QueryTranslatorUtility.terms(gp2, this.ignoreRDFTypeStatement);
		Set<Node> termsA = new LinkedHashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
		Set<Node> termsB = new LinkedHashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
		Set<Node> termsC = new LinkedHashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
		Collection<ZSelectItem> selectItemsA = this.generateSelectItems(termsA, null);
		Vector<ZSelectItem> selectItemsAList = new Vector<ZSelectItem>(selectItemsA);
		Collection<ZSelectItem> selectItemsB = this.generateSelectItems(termsB, null);
		Vector<ZSelectItem> selectItemsBList = new Vector<ZSelectItem>(selectItemsB);
		Vector<Node> termsCList = new Vector<Node>(termsC);


		SQLQuery query1 = new SQLQuery();

		SQLQuery transGP1 = this.trans(gp1);
		String transGP1Alias = transGP1.generateAlias() + "R1";;
		//SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transGP1FromItem;
		String subQueryGP1ViewName = "sql" + Math.abs(gp1.hashCode());
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();

			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryGP1ViewName;
			logger.info(dropViewSQL);
			boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);

			String createViewSQL = "CREATE VIEW " + subQueryGP1ViewName + " AS " + transGP1;
			logger.info(createViewSQL);
			boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);

			transGP1FromItem = new SQLFromItem(subQueryGP1ViewName, LogicalTableType.TABLE_NAME);
		} else {
			//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
			transGP1FromItem = new SQLFromItem(transGP1.toString(), LogicalTableType.QUERY_STRING);
		}


		transGP1FromItem.setAlias(transGP1Alias);
		query1.addFrom(transGP1FromItem);

		SQLQuery transGP2 = this.trans(gp2);
		String transGP2Alias = transGP2.generateAlias() + "R2";
		//SQLFromItem transGP2FromItem = new SQLFromItem(transGP2.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transGP2FromItem;
		String subQueryGP2ViewName = "sqr" + Math.abs(gp2.hashCode());
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			Connection conn = this.getConnection();

			String dropViewSQL = "DROP VIEW IF EXISTS " + subQueryGP2ViewName;
			logger.info(dropViewSQL);
			boolean dropViewSQLResult = DBUtility.execute(conn, dropViewSQL);

			String createViewSQL = "CREATE VIEW " + subQueryGP2ViewName + " AS " + transGP2;
			logger.info(createViewSQL);
			boolean createViewSQLResult = DBUtility.execute(conn, createViewSQL);

			transGP2FromItem = new SQLFromItem(subQueryGP2ViewName, LogicalTableType.TABLE_NAME);
		} else {
			//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
			transGP2FromItem = new SQLFromItem(transGP2.toString(), LogicalTableType.QUERY_STRING);
		}
		transGP2FromItem.setAlias(transGP2Alias);

		SQLJoinQuery joinQuery1 = new SQLJoinQuery();
		joinQuery1.setJoinType(Constants.JOINS_TYPE_LEFT);
		joinQuery1.setJoinSource(transGP2FromItem);
		//joinQuery1.setOnExpression(new ZConstant("FALSE", ZConstant.UNKNOWN));
		joinQuery1.setOnExpression(new ZExpression("=", new ZConstant("1", ZConstant.NUMBER), new ZConstant("0", ZConstant.NUMBER)));

		query1.addJoinQuery(joinQuery1);			


		Vector<ZSelectItem> selectItems1 = new Vector<ZSelectItem>();
		query1.addSelect(selectItems1);
		selectItems1.addAll(selectItemsAList);
		selectItems1.addAll(selectItemsBList);
		Collection<ZSelectItem> selectItemsC = this.generateSelectItems(termsCList, transGP1Alias + ".");
		selectItems1.addAll(selectItemsC);
		query1.addSelect(selectItems1);



		SQLQuery query2 = new SQLQuery();

		SQLQuery transR3 = this.trans(gp2);
		String transR3Alias = transR3.generateAlias() + "R3";;
		//SQLFromItem transR3FromItem = new SQLFromItem(transR3.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transR3FromItem;
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			transR3FromItem = new SQLFromItem(subQueryGP2ViewName, LogicalTableType.TABLE_NAME);
		} else {
			transR3FromItem = new SQLFromItem(transR3.toString(), LogicalTableType.QUERY_STRING);
		}
		transR3FromItem.setAlias(transR3Alias);
		query2.addFrom(transR3FromItem);

		SQLQuery transR4 = this.trans(gp1);
		String transR4Alias = transR4.generateAlias() + "R4";
		//SQLFromItem transR4FromItem = new SQLFromItem(transR4.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transR4FromItem;
		if(this.optimizer != null && this.optimizer.isSubQueryAsView()) {
			transR4FromItem = new SQLFromItem(subQueryGP1ViewName, LogicalTableType.TABLE_NAME);
		} else {
			transR4FromItem = new SQLFromItem(transR4.toString(), LogicalTableType.QUERY_STRING);
		}
		transR4FromItem.setAlias(transR4Alias);

		SQLJoinQuery joinQuery2 = new SQLJoinQuery();
		joinQuery2.setJoinType(Constants.JOINS_TYPE_LEFT);
		joinQuery2.setJoinSource(transR4FromItem);
		//joinQuery2.setOnExpression(new ZConstant("FALSE", ZConstant.UNKNOWN));
		joinQuery2.setOnExpression(new ZExpression("=", new ZConstant("1", ZConstant.NUMBER), new ZConstant("0", ZConstant.NUMBER)));
		query2.addJoinQuery(joinQuery2);

		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		query2.addSelect(selectItems2);
		selectItems2.addAll(selectItemsAList);
		selectItems2.addAll(selectItemsBList);
		Collection<ZSelectItem> selectItemsC2 = this.generateSelectItems(termsCList, transR3Alias + ".");
		selectItems2.addAll(selectItemsC2);

		logger.debug("query1 = \n" + query1);
		logger.debug("query2 = \n" + query2);

		SQLQuery result = query1;
		query1.addUnionQuery(query2);
		logger.debug("query1 UNION query2 = \n" + result + "\n");
		return result;

	}


	private ZExp transVar(Op op, Var var) {
		String nameVar = nameGenerator.generateName(var);
		ZExp zExp = new ZConstant(nameVar, ZConstant.COLUMNNAME);
		return zExp;
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

	public Map<String, Object> getMapVarMapping() {
		return this.mapVarMapping;
	}
	
	public abstract String translateResultSet(String columnLabel, String dbValue);
}
