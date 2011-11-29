package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.Constants;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoinQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;

/*
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.R2OQueryTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.R2OTranslationException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.TranslatorUtility;
 */

public abstract class AbstractQueryTranslator {
	public enum POS {sub, pre, obj}

	private static Logger logger = Logger.getLogger(AbstractQueryTranslator.class);
	
	protected Map<Op, Collection<Node>> mapTermsC = new HashMap<Op, Collection<Node>>();
	protected Map<Op, String> mapTransGP1Alias = new HashMap<Op, String>();
	protected Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes;
	protected AbstractMappingDocument mappingDocument;
	protected AbstractAlphaGenerator alphaGenerator;
	protected AbstractBetaGenerator betaGenerator;
	protected NameGenerator nameGenerator;
	protected boolean optimizeTripleBlock = false;
	protected boolean subQueryElimination = false;
	protected AbstractPRSQLGenerator prSQLGenerator;
	protected AbstractCondSQLGenerator condSQLGenerator;
	protected boolean ignoreRDFTypeStatement = false;
	
	public AbstractQueryTranslator(AbstractMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
		this.nameGenerator = new NameGenerator();
	}


	protected Collection<ZSelectItem> generateSelectItems(Collection<Node> nodes, String prefix) {
		Collection<ZSelectItem> result = new LinkedHashSet<ZSelectItem>();

		for(Node node : nodes) {
			ZSelectItem selectItem = this.generateSelectItem(node, prefix);
			result.add(selectItem);
		}

		return result;
	}
	
	private ZSelectItem generateSelectItem(Node node, String prefix) {
		if(prefix == null) {
			prefix = "";
		}

		NameGenerator nameGenerator = new NameGenerator();
		String nameA = nameGenerator.generateName(null, node);
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
	
	








	public void setOptimizeTripleBlock(boolean optimizeTripleBlock) {
		this.optimizeTripleBlock = optimizeTripleBlock;
	}



	public void setSubQueryElimination(boolean subQueryElimination) {
		this.subQueryElimination = subQueryElimination;
	}

	protected SQLQuery trans(OpSlice opSlice) throws Exception {
		long sliceLength = opSlice.getLength();
		Op opSliceSubOp = opSlice.getSubOp();
		SQLQuery sqlQuery = this.trans(opSliceSubOp);
		sqlQuery.setSlice(sliceLength);
		
		//logger.debug("trans slice = \n" + sqlQuery + "\n");
		return sqlQuery;
	}
	
	protected SQLQuery trans(OpDistinct opDistinct) throws Exception {
		Op opDistinctSubOp = opDistinct.getSubOp(); 
		SQLQuery sqlQuery = this.trans(opDistinctSubOp);
		sqlQuery.setDistinct(true);
		//logger.info("transDistinct = \n" + sqlQuery + "\n");
		return sqlQuery;
	}
	
	protected SQLQuery trans(OpBGP bgp) throws Exception {
		SQLQuery result = null;

		if(QueryTranslatorUtility.isTriplePattern(bgp)) { //triple pattern
			Triple tp = bgp.getPattern().getList().get(0);
			result = this.transTP(tp);
		} else { //bgp pattern
			List<Triple> triples = bgp.getPattern().getList();

			boolean isTB;
			if(this.optimizeTripleBlock) {
				isTB = QueryTranslatorUtility.isTripleBlock(triples);
				logger.debug("isTB = " + isTB);
			} else {
				isTB = false;
			}

			if(isTB) {
				result = this.transTB(triples);
			} else {
				int separationIndex = 1;
				if(this.optimizeTripleBlock) {
					separationIndex = QueryTranslatorUtility.getFirstTBEndIndex(triples);
				}
				List<Triple> gp1TripleList = triples.subList(0, separationIndex);
				OpBGP gp1 = new OpBGP(BasicPattern.wrap(gp1TripleList));
				List<Triple> gp2TripleList = triples.subList(separationIndex, triples.size());
				OpBGP gp2 = new OpBGP(BasicPattern.wrap(gp2TripleList));

				// result = this.transJoin(gp1, gp2, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_INNER);					
				result = this.transInnerJoin(bgp, gp1, gp2);
			}
		}
		
		return result;
	}
	
	protected SQLQuery trans(OpJoin opJoin)  throws Exception {
		SQLQuery result = null;
		Op opLeft = opJoin.getLeft();
		Op opRight = opJoin.getRight();
		//result = this.transJoin(opLeft, opRight, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_INNER);
		result = this.transInnerJoin(opJoin, opLeft, opRight);
		
		return result;
	}
	
	protected SQLQuery trans(OpLeftJoin opLeftJoin) throws Exception {
		SQLQuery result = null;
		Op opLeft = opLeftJoin.getLeft();
		Op opRight = opLeftJoin.getRight();
		if(opLeftJoin.getExprs() == null) {
			// result = this.transJoin(opLeft, opRight, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_LEFT);				
			result = this.transLeftJoin(opLeftJoin, opLeft, opRight);
		} else {
			//				logger.debug("op instanceof OpFilter");
			ExprList exprList = opLeftJoin.getExprs();
			Expr exprNull = null;
			Op opLeftJoin2 = OpLeftJoin.create(opLeft, opRight, exprNull);
			result = this.transFilter(opLeftJoin2, exprList);
		}
		return result;
	}
	
	protected SQLQuery trans(OpUnion opUnion) throws Exception {
		SQLQuery result = null;
		Op opLeft = opUnion.getLeft();
		Op opRight = opUnion.getRight();
		result = this.transUNION(opLeft, opRight);
		return result;
	}
	
	protected SQLQuery trans(OpFilter opFilter) throws Exception {
		SQLQuery result = null;
		Op opFilterSubOp = opFilter.getSubOp();
		ExprList exprList = opFilter.getExprs();
		result = this.transFilter(opFilterSubOp, exprList);
		return result;
	}
	
	protected SQLQuery trans(Op op) throws Exception {
		SQLQuery result = null;
		if(op instanceof OpBGP) { //triple or bgp pattern
			OpBGP bgp = (OpBGP) op;
			result = this.trans(bgp);
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



	protected SQLQuery transFilter(Op gp, ExprList exprList)
			throws Exception  {
		SQLQuery result;

		SQLQuery transGP = this.trans(gp);
		String transGPAlias = transGP.generateAlias();
		//SQLFromItem fromItem = new SQLFromItem(transGP.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem fromItem = new SQLFromItem(transGP.toString(), LogicalTableType.SQLQUERY);
		fromItem.setAlias(transGPAlias);

		String transGP1Alias = this.mapTransGP1Alias.get(gp);
		Collection<ZExp> transExprs;
		if(this.subQueryElimination) {
			transExprs = this.transExpr(gp, exprList);
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

		if(this.subQueryElimination) {
			result = QueryTranslatorUtility.eliminateSubQuery(newSelectItems, transGP, newWhere, null);
		} else {
			result = new SQLQuery();
			result.setSelectItems(newSelectItems);
			result.addFrom(fromItem);
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

	private SQLQuery transInnerJoin(Op opParent, Op gp1, Op gp2) throws Exception {
		return this.transJoin(opParent, gp1, gp2, Constants.JOINS_TYPE_INNER);
	}
	
	protected abstract String generateTermCName(Node termC);
	
	protected SQLQuery transJoin(Op opParent, Op gp1, Op gp2, String joinType)
			throws Exception  {
		logger.debug("entering transJoin");
		logger.debug("gp1 = " + gp1);
		logger.debug("gp2 = " + gp2);

		SQLQuery transGP1 = this.trans(gp1);
		SQLQuery transGP2 = this.trans(gp2);
		if(transGP1 == null && transGP2 == null) {
			return null;
		} else if(transGP1 != null && transGP2 == null) {
			return transGP1;
		} else if(transGP1 == null && transGP2 != null) {
			return transGP2;
		} else {
			SQLQuery transJoin = new SQLQuery();
			Collection<ZSelectItem> selectItems = new HashSet<ZSelectItem>();

			Collection<Node> termsGP1 = QueryTranslatorUtility.terms(gp1, this.ignoreRDFTypeStatement);
			logger.debug("termsGP1 = " + termsGP1);
			Collection<Node> termsGP2 = QueryTranslatorUtility.terms(gp2, this.ignoreRDFTypeStatement);
			logger.debug("termsGP2 = " + termsGP2);

			String transGP1Alias = transGP1.generateAlias();
			this.mapTransGP1Alias.put(opParent, transGP1Alias);
			//SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), SQLFromItem.FORM_QUERY);
			SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), LogicalTableType.SQLQUERY);
			transGP1FromItem.setAlias(transGP1Alias);
			transJoin.addFrom(transGP1FromItem);

			
			String transGP2Alias = transGP2.generateAlias();
			//SQLFromItem transGP2FromItem = new SQLFromItem(transGP2.toString(), SQLFromItem.FORM_QUERY);
			SQLFromItem transGP2FromItem = new SQLFromItem(transGP2.toString(), LogicalTableType.SQLQUERY);
			transGP2FromItem.setAlias(transGP2Alias);

			SQLJoinQuery joinQuery = new SQLJoinQuery();
			joinQuery.setJoinType(joinType);
			joinQuery.setJoinSource(transGP2FromItem);

			logger.debug("transGP1 = \n" + transGP1);
			logger.debug("transGP2 = \n" + transGP2);

			Set<Node> termsA = new HashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
			logger.debug("termsA = " + termsA);
			Set<Node> termsB = new HashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
			logger.debug("termsB = " + termsB);
			Set<Node> termsC = new HashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
			this.mapTermsC.put(opParent, termsC);
			logger.debug("termsC = " + termsC);
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
	
	public abstract SQLQuery translate(Query sparqlQuery) throws Exception;
	




	protected SQLQuery transLeftJoin(Op opParent, Op gp1, Op gp2) throws Exception {
		return this.transJoin(opParent, gp1, gp2, Constants.JOINS_TYPE_LEFT);
	}



	//protected abstract SQLQuery transJoin(Op opParent, Op gp1, Op gp2, String joinType) throws Exception;
	//protected abstract SQLQuery transFilter(Op gp, ExprList exprList) throws Exception;
	//protected abstract SQLQuery transUNION(Op gp1, Op gp2) throws Exception;
	//protected abstract SQLQuery transProject(Op opQuery) throws Exception;

	protected abstract SQLQuery transTB(Collection<Triple> triples) throws Exception;

	protected abstract SQLQuery transTP(Triple tp) throws QueryTranslationException;
	
	protected SQLQuery transUNION(Op gp1, Op gp2)
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
		SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), LogicalTableType.SQLQUERY);
		transGP1FromItem.setAlias(transGP1Alias);
		query1.addFrom(transGP1FromItem);

		SQLQuery transGP2 = this.trans(gp2);
		String transGP2Alias = transGP2.generateAlias() + "R2";
		//SQLFromItem transGP2FromItem = new SQLFromItem(transGP2.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transGP2FromItem = new SQLFromItem(transGP2.toString(), LogicalTableType.SQLQUERY);
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
		SQLFromItem transR3FromItem = new SQLFromItem(transR3.toString(), LogicalTableType.SQLQUERY);
		transR3FromItem.setAlias(transR3Alias);
		query2.addFrom(transR3FromItem);

		SQLQuery transR4 = this.trans(gp1);
		String transR4Alias = transR4.generateAlias() + "R4";
		//SQLFromItem transR4FromItem = new SQLFromItem(transR4.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transR4FromItem = new SQLFromItem(transR4.toString(), LogicalTableType.SQLQUERY);
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
	
	protected abstract ZExp transVar(Op op, Var var);


	public boolean isIgnoreRDFTypeStatement() {
		return ignoreRDFTypeStatement;
	}


	public void setIgnoreRDFTypeStatement(boolean ignoreRDFTypeStatement) {
		this.ignoreRDFTypeStatement = ignoreRDFTypeStatement;
	}

	protected SQLQuery trans(OpOrder opOrder) throws Exception {
		Vector<ZOrderBy> orderByConditions = new Vector<ZOrderBy>();
		for(SortCondition sortCondition : opOrder.getConditions()) {
			int sortConditionDirection = sortCondition.getDirection();
			Expr sortConditionExpr = sortCondition.getExpression();
			Var sortConditionVar = sortConditionExpr.asVar();
			
			String nameSortConditionVar = nameGenerator.generateName(null, sortConditionVar);

			//ZExp zExp = this.transExpr(graphPatternOp, sortConditionExpr);
			//ZExp zExp = new ZConstant(sortConditionVar.getName(), ZConstant.COLUMNNAME);
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
		
		Op opOrderSubOp = opOrder.getSubOp();
		SQLQuery sqlQuery = this.trans(opOrderSubOp); 
		sqlQuery.addOrderBy(orderByConditions);
		
		//logger.info("trans orderBy = \n" + sqlQuery + "\n");
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
			String nameSelectVar = nameGenerator.generateName(null, selectVar);
//			ZSelectItem selectItem = new ZSelectItem(gpSQLAlias + "." + nameSelectVar);
			ZSelectItem selectItem = new ZSelectItem(nameSelectVar);
			selectItem.setAlias(selectVar.getName());
			newSelectItems.add(selectItem);
		}


		SQLQuery result = new SQLQuery();
		Vector<ZOrderBy> orderByConditions = gpSQL.getOrderBy();
		if(this.subQueryElimination) {
			result = QueryTranslatorUtility.eliminateSubQuery(newSelectItems, gpSQL, null, orderByConditions);
		} else {
			SQLFromItem fromItem = new SQLFromItem(gpSQL.toString(), LogicalTableType.SQLQUERY);
			fromItem.setAlias(gpSQLAlias);
			result.setSelectItems(newSelectItems);
			result.addFrom(fromItem);
		}
		
		//logger.info("trans project = \n" + result + "\n");
		return result;

	}
	
	protected ZExp transLiteral(NodeValue nodeValue) {
		ZExp result = null;
		Node node = nodeValue.getNode();
		if(nodeValue.isNumber()) {
			result = new ZConstant(node.getLiteralValue().toString(), ZConstant.NUMBER);	
		} else {
			result = new ZConstant(node.getLiteralValue().toString(), ZConstant.STRING);
		}
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


	protected abstract ZExp transIRI(Node node);


	public SQLQuery translateFromFile(String queryFilePath) throws Exception {
		//process SPARQL file
		logger.info("Parsing query file : " + queryFilePath);
		Query sparqlQuery = QueryFactory.read(queryFilePath);
		logger.debug("sparqlQuery = " + sparqlQuery);
		
		return this.translate(sparqlQuery);
	}


	public SQLQuery translateFromString(String queryString) throws Exception {
		//process SPARQL string
		logger.info("Parsing query string : " + queryString);
		Query sparqlQuery = QueryFactory.create(queryString);
		logger.debug("sparqlQuery = " + sparqlQuery);
		
		return this.translate(sparqlQuery);
	}

}
