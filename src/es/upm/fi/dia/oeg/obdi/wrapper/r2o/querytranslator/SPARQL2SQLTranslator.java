package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

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
import Zql.ZFromItem;
import Zql.ZOrderBy;
import Zql.ZQuery;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
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
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OFromItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OJoinQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class SPARQL2SQLTranslator {
	private static Logger logger = Logger.getLogger(SPARQL2SQLTranslator.class);

	private R2OMappingDocument mappingDocument;
	Map<Node, R2OConceptMapping> mapNodeConceptMapping;
	boolean optimizeTripleBlock = false;

	public void setOptimizeTripleBlock(boolean optimizeTripleBlock) {
		this.optimizeTripleBlock = optimizeTripleBlock;
	}

	//	private AbstractAlphaGenerator alphaGenerator;
	//	private AbstractBetaGenerator betaGenerator;
	private NameGenerator nameGenerator;

	public SPARQL2SQLTranslator(R2OMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
		this.nameGenerator = new NameGenerator();
	}



	private R2OQuery trans(Op op, AbstractAlphaGenerator alphaGenerator, AbstractBetaGenerator betaGenerator)
	throws Exception {


		R2OQuery result = null;
		if(op instanceof OpBGP) { //triple or bgp pattern
			OpBGP bgp = (OpBGP) op;

			if(TranslatorUtility.isTriplePattern(bgp)) { //triple pattern
				Triple tp = bgp.getPattern().getList().get(0);
				result = this.transTP(tp, alphaGenerator, betaGenerator);
			} else { //bgp pattern
				List<Triple> triples = bgp.getPattern().getList();

				boolean isTB;
				if(this.optimizeTripleBlock) {
					isTB = TranslatorUtility.isTripleBlock(triples);
					logger.debug("isTB = " + isTB);
				} else {
					isTB = false;
				}
				
				if(isTB) {
					result = this.transTB(triples, alphaGenerator, betaGenerator);
				} else {
					int separationIndex = 1;
					if(this.optimizeTripleBlock) {
						separationIndex = TranslatorUtility.getFirstTBEndIndex(triples);
					}
					List<Triple> gp1TripleList = triples.subList(0, separationIndex);
					OpBGP gp1 = new OpBGP(BasicPattern.wrap(gp1TripleList));
					List<Triple> gp2TripleList = triples.subList(separationIndex, triples.size());
					OpBGP gp2 = new OpBGP(BasicPattern.wrap(gp2TripleList));

					result = this.transANDOPT(gp1, gp2, alphaGenerator, betaGenerator
							, R2OConstants.JOINS_TYPE_INNER);					
				}
			}
		} else if(op instanceof OpJoin) { // AND pattern
			logger.debug("op instanceof OpJoin");
			OpJoin opJoin = (OpJoin) op;
			Op opLeft = opJoin.getLeft();
			Op opRight = opJoin.getRight();
			result = this.transANDOPT(opLeft, opRight, alphaGenerator, betaGenerator
					, R2OConstants.JOINS_TYPE_INNER);
		} else if(op instanceof OpLeftJoin) { //OPT pattern
			logger.debug("op instanceof OpLeftJoin");
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Op opLeft = opLeftJoin.getLeft();
			Op opRight = opLeftJoin.getRight();
			if(opLeftJoin.getExprs() == null) {
				result = this.transANDOPT(opLeft, opRight, alphaGenerator, betaGenerator
						, R2OConstants.JOINS_TYPE_LEFT);				
			} else {
				logger.debug("op instanceof OpFilter");
				ExprList exprList = opLeftJoin.getExprs();
				Expr exprNull = null;
				Op opLeftJoin2 = OpLeftJoin.create(opLeft, opRight, exprNull);
				result = this.transFilter(opLeftJoin2, exprList, alphaGenerator, betaGenerator);
			}

		} else if(op instanceof OpUnion) { //UNION pattern
			logger.debug("op instanceof OpUnion");
			OpUnion opUnion = (OpUnion) op;
			Op opLeft = opUnion.getLeft();
			Op opRight = opUnion.getRight();
			result = this.transUNION(opLeft, opRight, alphaGenerator, betaGenerator);
		} else if(op instanceof OpFilter) { //FILTER pattern
			logger.debug("op instanceof OpFilter");
			OpFilter opFilter = (OpFilter) op;
			Op OpFilterSubOp = opFilter.getSubOp();
			ExprList exprList = opFilter.getExprs();
			result = this.transFilter(OpFilterSubOp, exprList, alphaGenerator, betaGenerator);
		} else if(op instanceof OpProject) { //SELECT solution modifier

		} else {
			throw new R2OTranslationException("Unsupported query!");
		}

		return result;
	}

	public R2OQuery query2SQL(Query sparqlQuery) throws Exception  {
		R2OQuery sparql2sqlResult = null;
		Vector<ZOrderBy> orderByConditions = null;
		
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);

		Op opQuery = Algebra.compile(sparqlQuery) ;
		Op opQuery2 = Algebra.optimize(opQuery);


		TranslatorUtility tu = new TranslatorUtility(mappingDocument);
		this.mapNodeConceptMapping = tu.initializeMapConceptMapping(opQueryPattern);
		AbstractAlphaGenerator alphaGenerator;
		if(this.optimizeTripleBlock) {
			alphaGenerator = new AlphaGenerator3(mapNodeConceptMapping, mappingDocument);
		} else {
			alphaGenerator = new AlphaGenerator3(mapNodeConceptMapping, mappingDocument);
		}
		
		AbstractBetaGenerator betaGenerator = 
			new BetaGenerator1(mapNodeConceptMapping, this.mappingDocument);
		if(opQuery instanceof OpProject || opQuery instanceof OpSlice) {
			sparql2sqlResult = new R2OQuery();
			OpSlice opSlice = null;
			OpProject opProject = null;
			OpOrder opOrder = null;
			Op graphPatternOp = null;
			long sliceLength = -1;
			
			if(opQuery instanceof OpSlice) {
				opSlice = (OpSlice) opQuery;
				sliceLength = opSlice.getLength();
				sparql2sqlResult.setSlice(sliceLength);
				Op opSliceSubOp = opSlice.getSubOp();
				if(opSliceSubOp instanceof OpProject) {
					opProject = (OpProject) opSliceSubOp;
				}
			} else if (opQuery instanceof OpProject) {
				opProject = (OpProject) opQuery;
			}
			
			if(opProject != null) {
				Op opProjectSubOp = opProject.getSubOp();
				if(opProjectSubOp instanceof OpOrder) {
					opOrder = (OpOrder) opProjectSubOp;
					orderByConditions = new Vector<ZOrderBy>();
					for(SortCondition sortCondition : opOrder.getConditions()) {
						int sortConditionDirection = sortCondition.getDirection();
						Expr sortConditionExpr = sortCondition.getExpression();
						ZExp zExp = this.transExpr(sortConditionExpr);
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
					sparql2sqlResult.addOrderBy(orderByConditions);
					graphPatternOp = opOrder.getSubOp();
				} else {
					graphPatternOp = opProject.getSubOp();
				}

			}
			

			R2OQuery gpSQL = this.trans(graphPatternOp, alphaGenerator, betaGenerator);
			String gpSQLAlias = gpSQL.generateAlias();
			logger.debug("gpSQL result = " + gpSQL.toString());

			List<Var> selectVars = opProject.getVars();
			for(Var selectVar : selectVars) {
				String nameSelectVar = nameGenerator.generateName(selectVar);
				ZSelectItem selectItem = new ZSelectItem(gpSQLAlias + "." + nameSelectVar);
				selectItem.setAlias(selectVar.getName());
				sparql2sqlResult.addSelect(selectItem);
			}
			
			R2OFromItem fromItem = new R2OFromItem(gpSQL.toString(), R2OFromItem.FORM_QUERY);
			fromItem.setAlias(gpSQLAlias);
			sparql2sqlResult.addFrom(fromItem);

		} else {
			throw new R2OTranslationException("Unsupported query!");
		}	


		if(queryPattern instanceof ElementTriplesBlock) {
			logger.debug("queryPattern instanceof ElementTriplesBlock");
		} else if(queryPattern instanceof ElementOptional) {
			logger.debug("queryPattern instanceof ElementOptional");
		} else if(queryPattern instanceof ElementFilter) {
			logger.debug("queryPattern instanceof ElementFilter");
		} else if(queryPattern instanceof ElementUnion) {
			logger.debug("queryPattern instanceof ElementUnion");
		}

		logger.debug("sparql2sqlResult result = " + sparql2sqlResult.toString());

		return sparql2sqlResult;
	}


	private Map<Triple, R2OConceptMapping> mapTripleCM = new HashMap<Triple, R2OConceptMapping>();
	
	private R2OQuery transTP(Triple tp, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator) 
	throws R2OTranslationException  {
		logger.info("tp = " + tp);
		R2OQuery query = new R2OQuery();
		query.addSelect(new Vector<ZSelectItem>());
		query.addFrom(new Vector<ZFromItem>());


		try {
			R2OConceptMapping cm = alphaGenerator.calculateAlphaCM(tp);
			this.mapTripleCM.put(tp, cm);
			R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, this.mappingDocument);
			ZQuery alpha = cmu.unfoldConceptMapping();
			
			
			logger.debug("alpha(tp) = " + alpha);

			ZSelectItem betaSub = betaGenerator.calculateBetaCM(tp, POS.sub, cm);
			logger.debug("beta(tp,sub) = " + betaSub); 
			ZSelectItem betaPre = betaGenerator.calculateBetaCM(tp, POS.pre, cm);
			logger.debug("beta(tp,pre) = " + betaPre);
			ZSelectItem betaObj = betaGenerator.calculateBetaCM(tp, POS.obj, cm);
			logger.debug("beta(tp,obj) = " + betaObj);

			ZExp condSQL = this.genCondSQL(tp, betaGenerator);
			logger.debug("genCondSQL(tp,beta) = " + condSQL);


			Vector<ZSelectItem> prSQLList = (Vector<ZSelectItem>) genPRSQL(tp, betaGenerator, nameGenerator);
			logger.debug("genPRSQL(tp,beta,name) = " + prSQLList);

			query.addSelect(prSQLList);

			R2OFromItem fromItem = new R2OFromItem(alpha.toString(), R2OFromItem.FORM_QUERY);
			query.addFrom(fromItem);
			fromItem.setAlias(fromItem.generateAlias());

			query.addWhere(condSQL);



		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error processing tp : " + tp);
			throw new R2OTranslationException(e.getMessage(), e);
		}

		logger.debug("tpQuery = " + query + "\n");
		return query;
	}

	private R2OQuery transTB(List<Triple> triples, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator) throws Exception {
		R2OQuery result = new R2OQuery();

		R2OConceptMapping cm = alphaGenerator.calculateAlphaCM(triples);
		for(Triple tp : triples) {
			this.mapTripleCM.put(tp, cm);
		}
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, this.mappingDocument);
		ZQuery alphaTB = cmu.unfoldConceptMapping();
				
		logger.debug("alphaTB = " + alphaTB);
		R2OFromItem fromItem = new R2OFromItem(alphaTB.toString(), R2OFromItem.FORM_QUERY);
		fromItem.setAlias(fromItem.generateAlias());
		result.addFrom(fromItem);	

		Vector<ZSelectItem> selectItems = (Vector<ZSelectItem>) this.genPRSQLTB(
				triples, betaGenerator, nameGenerator);
		logger.debug("genPRSQLTB = " + selectItems);
		result.addSelect(selectItems);


		ZExp condSQL = this.genCondSQLTB(triples, betaGenerator);
		logger.debug("genCondSQLTB = " + condSQL);

		result.addWhere(condSQL);

		return result;
	}
	
	private R2OQuery transUNION(Op gp1, Op gp2, AbstractAlphaGenerator alphaGenerator, AbstractBetaGenerator betaGenerator)
	throws Exception  {
		Collection<Node> termsGP1 = TranslatorUtility.terms(gp1);
		Collection<Node> termsGP2 = TranslatorUtility.terms(gp2);
		Set<Node> termsA = new LinkedHashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
		Set<Node> termsB = new LinkedHashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
		Set<Node> termsC = new LinkedHashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
		Collection<ZSelectItem> selectItemsA = this.generateSelectItems(termsA, null);
		Vector<ZSelectItem> selectItemsAList = new Vector<ZSelectItem>(selectItemsA);
		Collection<ZSelectItem> selectItemsB = this.generateSelectItems(termsB, null);
		Vector<ZSelectItem> selectItemsBList = new Vector<ZSelectItem>(selectItemsB);
		Vector<Node> termsCList = new Vector<Node>(termsC);


		R2OQuery query1 = new R2OQuery();

		R2OQuery transGP1 = this.trans(gp1, alphaGenerator, betaGenerator);
		String transGP1Alias = transGP1.generateAlias() + "R1";;
		R2OFromItem transGP1FromItem = new R2OFromItem(transGP1.toString(), R2OFromItem.FORM_QUERY);
		transGP1FromItem.setAlias(transGP1Alias);
		query1.addFrom(transGP1FromItem);

		R2OQuery transGP2 = this.trans(gp2, alphaGenerator, betaGenerator);
		String transGP2Alias = transGP2.generateAlias() + "R2";
		R2OFromItem transGP2FromItem = new R2OFromItem(transGP2.toString(), R2OFromItem.FORM_QUERY);
		transGP2FromItem.setAlias(transGP2Alias);

		R2OJoinQuery joinQuery1 = new R2OJoinQuery();
		joinQuery1.setJoinType(R2OConstants.JOINS_TYPE_LEFT);
		joinQuery1.setJoinSource(transGP2FromItem);
		joinQuery1.setOnExpression(new ZConstant("FALSE", ZConstant.UNKNOWN));
		query1.addJoinQuery(joinQuery1);			


		Vector<ZSelectItem> selectItems1 = new Vector<ZSelectItem>();
		query1.addSelect(selectItems1);
		selectItems1.addAll(selectItemsAList);
		selectItems1.addAll(selectItemsBList);
		Collection<ZSelectItem> selectItemsC = this.generateSelectItems(termsCList, transGP1Alias + ".");
		selectItems1.addAll(selectItemsC);
		query1.addSelect(selectItems1);



		R2OQuery query2 = new R2OQuery();

		R2OQuery transR3 = this.trans(gp2, alphaGenerator, betaGenerator);
		String transR3Alias = transR3.generateAlias() + "R3";;
		R2OFromItem transR3FromItem = new R2OFromItem(transR3.toString(), R2OFromItem.FORM_QUERY);
		transR3FromItem.setAlias(transR3Alias);
		query2.addFrom(transR3FromItem);

		R2OQuery transR4 = this.trans(gp1, alphaGenerator, betaGenerator);
		String transR4Alias = transR4.generateAlias() + "R4";
		R2OFromItem transR4FromItem = new R2OFromItem(transR4.toString(), R2OFromItem.FORM_QUERY);
		transR4FromItem.setAlias(transR4Alias);

		R2OJoinQuery joinQuery2 = new R2OJoinQuery();
		joinQuery2.setJoinType(R2OConstants.JOINS_TYPE_LEFT);
		joinQuery2.setJoinSource(transR4FromItem);
		joinQuery2.setOnExpression(new ZConstant("FALSE", ZConstant.UNKNOWN));
		query2.addJoinQuery(joinQuery2);

		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		query2.addSelect(selectItems2);
		selectItems2.addAll(selectItemsAList);
		selectItems2.addAll(selectItemsBList);
		Collection<ZSelectItem> selectItemsC2 = this.generateSelectItems(termsCList, transR3Alias + ".");
		selectItems2.addAll(selectItemsC2);

		logger.debug("query1 = " + query1);
		logger.debug("query2 = " + query2);

		R2OQuery result = query1;
		query1.addUnionQuery(query2);
		logger.debug("query1 UNION query2 = " + result);
		return result;

	}

	private R2OQuery transFilter2(Op gp, ExprList exprList, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator)
	throws Exception  {
		R2OQuery transGP = this.trans(gp, alphaGenerator, betaGenerator);
		ZExp transGPWhere = transGP.getWhere();
		Collection<ZExp> zExps = this.transExpr(exprList);
		if(transGPWhere == null) {
			if(zExps.size() == 1) {
				transGP.addWhere(zExps.iterator().next());
			} else if(zExps.size() > 1){
				ZExpression whereExpression = new ZExpression("AND");
				whereExpression.addOperand(transGPWhere);
				for(ZExp expression : zExps) {
					whereExpression.addOperand(expression);
				}
				transGP.addWhere(whereExpression);
			}
		} else {
			if(zExps.size() > 0) {
				ZExpression whereExpression = new ZExpression("AND");
				whereExpression.addOperand(transGPWhere);
				for(ZExp expression : zExps) {
					whereExpression.addOperand(expression);
				}
				transGP.addWhere(whereExpression);
			}			
		}


		return transGP;
	}

	private R2OQuery transFilter(Op gp, ExprList exprList, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator)
	throws Exception  {
		R2OQuery result = new R2OQuery();
		result.addSelect(new ZSelectItem("*"));

		R2OQuery transGP = this.trans(gp, alphaGenerator, betaGenerator);
		String transGPAlias = transGP.generateAlias();
		R2OFromItem fromItem = new R2OFromItem(transGP.toString(), R2OFromItem.FORM_QUERY);
		fromItem.setAlias(transGPAlias);
		result.addFrom(fromItem);

		Collection<ZExp> zExps = this.transExpr(exprList);
		if(zExps.size() > 0) {
			if(zExps.size() == 1) {
				result.addWhere(zExps.iterator().next());
			} else {
				ZExpression whereExpression = new ZExpression("AND");
				for(ZExp expression : zExps) {
					whereExpression.addOperand(expression);
				}
				result.addWhere(whereExpression);
			}
		}

		return result;
	}

	
	private Collection<ZExp> transExpr(ExprList exprList) {
		Collection<ZExp> result = new HashSet<ZExp>();
		List<Expr> exprs = exprList.getList();
		for(Expr expr : exprs) {
			result.add(this.transExpr(expr));
		}
		return result;
	}

	private ZExp transExpr(Expr expr) {
		ZExp result = null;

		if(expr.isVariable()) {
			logger.debug("expr is var");
			result = new ZConstant(nameGenerator.generateName(expr.asVar()), ZConstant.COLUMNNAME);
		} else if(expr.isConstant()) {
			logger.debug("expr is constant");
			NodeValue nodeValue = expr.getConstant();
			Node node = nodeValue.getNode();
			if(nodeValue.isLiteral()) {
				if(nodeValue.isNumber()) {
					result = new ZConstant(node.getLiteralValue().toString(), ZConstant.NUMBER);	
				} else {
					result = new ZConstant(node.getLiteralValue().toString(), ZConstant.STRING);
				}
			} else if(nodeValue.isIRI()) {
				result = new ZConstant(node.getLocalName(), ZConstant.COLUMNNAME);
			}
		} else if(expr.isFunction()) {
			logger.debug("expr is function");
			String databaseType = R2ORunner.configurationProperties.getDatabaseType();
			if(databaseType == null) {
				databaseType = R2OConstants.DATABASE_MYSQL;
			}
			
			ExprFunction exprFunction = expr.getFunction();
			String functionSymbol;
			if(exprFunction instanceof E_Bound) {
				functionSymbol = "IS NOT NULL";
			} else if(exprFunction instanceof E_LogicalNot) {
				functionSymbol = "NOT";
			} else if(exprFunction instanceof E_LogicalAnd) {
				functionSymbol = "AND";
			} else if(exprFunction instanceof E_LogicalOr) {
				functionSymbol = "OR";
			} else if(exprFunction instanceof E_NotEquals && R2OConstants.DATABASE_MONETDB.equalsIgnoreCase(databaseType)){
				functionSymbol = "<>";
			} else if(exprFunction instanceof E_Regex) {
				functionSymbol = "LIKE";
			} else {
				functionSymbol = exprFunction.getOpName();
			}
			result = new ZExpression(functionSymbol);

			List<Expr> args = exprFunction.getArgs();
			for(int i=0; i<args.size(); i++) {
				Expr arg = args.get(i);
				ZExp argExp = this.transExpr(arg);
				
				if(exprFunction instanceof E_Regex && i==1) {
					argExp = new ZConstant("%" + ((ZConstant)argExp).getValue() + "%", ZConstant.STRING);
				}
				((ZExpression)result).addOperand(argExp);
			}
		}



		return result;
	}

	private R2OQuery transANDOPT(Op gp1, Op gp2, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator
			, String joinType)
	throws Exception  {
		R2OQuery result = new R2OQuery();
		Collection<ZSelectItem> selectItems = new HashSet<ZSelectItem>();

		Collection<Node> termsGP1 = TranslatorUtility.terms(gp1);
		Collection<Node> termsGP2 = TranslatorUtility.terms(gp2);

		R2OQuery transGP1 = this.trans(gp1, alphaGenerator, betaGenerator);
		String transGP1Alias = transGP1.generateAlias();
		R2OFromItem transGP1FromItem = new R2OFromItem(transGP1.toString(), R2OFromItem.FORM_QUERY);
		transGP1FromItem.setAlias(transGP1Alias);
		result.addFrom(transGP1FromItem);

		R2OQuery transGP2 = this.trans(gp2, alphaGenerator, betaGenerator);
		String transGP2Alias = transGP2.generateAlias();
		R2OFromItem transGP2FromItem = new R2OFromItem(transGP2.toString(), R2OFromItem.FORM_QUERY);
		transGP2FromItem.setAlias(transGP2Alias);

		R2OJoinQuery joinQuery = new R2OJoinQuery();
		joinQuery.setJoinType(joinType);
		joinQuery.setJoinSource(transGP2FromItem);

		logger.debug("transGP1 = " + transGP1);
		logger.debug("transGP2 = " + transGP2);

		Set<Node> termsA = new HashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
		Set<Node> termsB = new HashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
		Set<Node> termsC = new HashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
		Collection<ZSelectItem> selectItemsA = this.generateSelectItems(termsA, null);
		selectItems.addAll(selectItemsA);
		Collection<ZSelectItem> selectItemsB = this.generateSelectItems(termsB, null);
		selectItems.addAll(selectItemsB);
		logger.debug("selectItemsA = " + selectItemsA);
		logger.debug("selectItemsB = " + selectItemsB);
		logger.debug("termsGP1 = " + termsGP1);
		logger.debug("termsGP2 = " + termsGP2);
		logger.debug("termsA = " + termsA);
		logger.debug("termsB = " + termsB);
		logger.debug("termsC = " + termsC);
		Collection<ZSelectItem> selectItemsC = new HashSet<ZSelectItem>();
		for(Node c : termsC) {
			ZSelectItem selectItemC = TranslatorUtility.generateCoalesceSelectItem(
					c, transGP1Alias, transGP2Alias, nameGenerator);
			selectItemsC.add(selectItemC);
		}
		logger.debug("selectItemsC = " + selectItemsC);
		selectItems.addAll(selectItemsC);


		ZExp joinOnExpression = new ZConstant("TRUE", ZConstant.UNKNOWN);
		for(Node termC : termsC) {
			ZExp exp1 = new ZExpression("="
					, new ZConstant(transGP1Alias + "." + nameGenerator.generateName(termC)
							, ZConstant.UNKNOWN)
					, new ZConstant(transGP2Alias + "." + nameGenerator.generateName(termC)
							, ZConstant.UNKNOWN));

			ZExp exp2 = new ZExpression("IS NULL"
					, new ZConstant(transGP1Alias + "." + nameGenerator.generateName(termC)
							, ZConstant.UNKNOWN));

			ZExp exp3 = new ZExpression("IS NULL"
					, new ZConstant(transGP2Alias + "." + nameGenerator.generateName(termC)
							, ZConstant.UNKNOWN));

			ZExpression exp12 = new ZExpression("OR", exp1, exp2);
			ZExpression exp123 = new ZExpression("OR", exp12, exp3);

			joinOnExpression = new ZExpression("AND", joinOnExpression, exp123);
		}
		logger.debug("joinOnExpression = " + joinOnExpression);
		joinQuery.setOnExpression(joinOnExpression);
		
//		if(joinOnExpression instanceof ZConstant) {
//			ZExpression onExpr = new ZExpression(joinOnExpression.toString());
//		} else {
//			joinQuery.setOnExpression((ZExpression) joinOnExpression);
//		}


		result.addJoinQuery(joinQuery);			


		logger.debug("selectItems = " + selectItems);
		result.setSelectItems(selectItems);




		return result;
	}


	enum POS {sub, pre, obj};


	private ZExp genCondSQLTB(Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator) throws Exception {
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);

		for(Triple tp : tripleBlock) {
			ZExp cond = this.genCondSQL(tp, betaGenerator);
			result = new ZExpression("AND", result, cond);
		}

		return result;

	}

	private ZExp genCondSQL(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		R2OConceptMapping cm = this.mapTripleCM.get(tp);
		
		ZExp betaSub = betaGenerator.calculateBetaCM(tp, POS.sub, cm).getExpression();
		ZExp betaPre = betaGenerator.calculateBetaCM(tp, POS.pre, cm).getExpression();
		ZExp betaObj = betaGenerator.calculateBetaCM(tp, POS.obj, cm).getExpression();
		ZExpression betaObj2 = new ZExpression("IS NOT NULL");
		betaObj2.addOperand(betaObj);
		result = new ZExpression("AND", result, betaObj2);

		if(!subject.isVariable()) {
			ZExp exp = null;
			
			if(subject.isURI()) {
				exp = new ZExpression("="
						, betaSub
						, new ZConstant(subject.toString(), ZConstant.STRING));
			} else if(subject.isLiteral()) {
				logger.warn("Literal as subject is not supported!");
				Object literalValue = subject.getLiteralValue();
				if(literalValue instanceof String) {
					exp = new ZExpression("="
							, betaSub
							, new ZConstant(subject.toString(), ZConstant.STRING));				
				} else if (literalValue instanceof Double) {
					exp = new ZExpression("="
							, betaSub
							, new ZConstant(subject.toString(), ZConstant.NUMBER));
				} else {
					exp = new ZExpression("="
							, betaSub
							, new ZConstant(subject.toString(), ZConstant.STRING));				
				}
			}
			
			if(exp != null) {
				result = new ZExpression("AND", result, exp);
			}
			
		}

		if(!predicate.isVariable()) {
			ZExp exp = new ZExpression("="
					, betaPre
					, new ZConstant(predicate.toString(), ZConstant.STRING));

			result = new ZExpression("AND", result, exp);
		}

		if(!object.isVariable()) {
			ZExp exp = null;

			if(object.isURI()) {
				ZConstant objConstant = new ZConstant(object.getURI(), ZConstant.STRING);
				exp = new ZExpression("=", betaObj, objConstant);
			} else if(object.isLiteral()) {
				Object literalValue = object.getLiteralValue();
				if(literalValue instanceof String) {
					ZConstant objConstant = new ZConstant(literalValue.toString(), ZConstant.STRING);
					exp = new ZExpression("=", betaObj, objConstant);					
				} else if (literalValue instanceof Double) {
					ZConstant objConstant = new ZConstant(literalValue.toString(), ZConstant.NUMBER);
					exp = new ZExpression("=", betaObj, objConstant);
				} else {
					ZConstant objConstant = new ZConstant(literalValue.toString(), ZConstant.STRING);
					exp = new ZExpression("="
							, betaObj
							, objConstant);					
				}
			}

			result = new ZExpression("AND", result, exp);
		}

		if(subject == predicate) {
			ZExp exp = new ZExpression("="
					, betaSub
					, betaPre);

			result = new ZExpression("AND", result, exp);
		}

		if(subject == object) {
			ZExp exp = new ZExpression("="
					, betaSub
					, betaObj);

			result = new ZExpression("AND", result, exp);
		}

		if(object == predicate) {
			ZExp exp = new ZExpression("="
					, betaObj
					, betaPre);

			result = new ZExpression("AND", result, exp);
		}

		return result;
	}


	private Collection<ZSelectItem> genPRSQLTB(
			Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Collection<R2OSelectItem> prList = new HashSet<R2OSelectItem>();
		Triple firstTriple = tripleBlock.iterator().next();
		Node subject = firstTriple.getSubject();
		R2OConceptMapping cm = this.mapTripleCM.get(firstTriple);
		R2OSelectItem selectItem = (R2OSelectItem) betaGenerator.calculateBetaCM(firstTriple, POS.sub, cm);
		selectItem.setAlias(nameGenerator.generateName(subject));
		prList.add(selectItem);

		for(Triple tp : tripleBlock) {
			Node predicate = tp.getPredicate();
			Node object = tp.getObject();

			if(predicate != subject) {
				ZSelectItem betaCMPre = betaGenerator.calculateBetaCM(tp, POS.pre, cm);
				selectItem = new R2OSelectItem();
				selectItem.setExpression(betaCMPre.getExpression());
				String predicateAlias = nameGenerator.generateName(predicate); 
				selectItem.setAlias(predicateAlias);
				boolean contains = prList.contains(selectItem);
				prList.add(selectItem);
				contains = prList.contains(selectItem);
			}

			if(object != subject && object != predicate) {
				ZSelectItem betaCMObj = betaGenerator.calculateBetaCM(tp, POS.obj, cm);
				
				if(object.isVariable()) {
					selectItem = new R2OSelectItem(betaCMObj.toString());
				} else if(object.isLiteral()) {
					selectItem = new R2OSelectItem();
					ZExp betaCMObjExp = betaCMObj.getExpression();
					selectItem.setExpression(betaCMObjExp);
				} else if(object.isURI()) {
					selectItem = new R2OSelectItem();
					selectItem.setExpression(betaCMObj.getExpression());
				}
				selectItem.setAlias(nameGenerator.generateName(object));
				prList.add(selectItem);
			}
		}

		Collection<ZSelectItem> prList2 = new Vector<ZSelectItem>(prList);

		return prList2;
	}

	private Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		R2OConceptMapping cm = this.mapTripleCM.get(tp);
		
		Collection<ZSelectItem> prList = new Vector<ZSelectItem>();
		ZSelectItem selectItem = 
			new ZSelectItem(betaGenerator.calculateBetaCM(tp, POS.sub, cm).toString());
		selectItem.setAlias(nameGenerator.generateName(subject));
		prList.add(selectItem);

		if(predicate != subject) {
			selectItem = new ZSelectItem(betaGenerator.calculateBetaCM(tp, POS.pre, cm).toString());
			selectItem.setAlias(nameGenerator.generateName(predicate));
			prList.add(selectItem);
		}

		if(object != subject && object != predicate) {
			selectItem = new ZSelectItem(betaGenerator.calculateBetaCM(tp, POS.obj, cm).toString());
			//logger.debug("selectItem = " + selectItem);
			String alias = nameGenerator.generateName(object);
			if(alias != null) {
				selectItem.setAlias(nameGenerator.generateName(object));
			}
			
			prList.add(selectItem);
		}

		return prList;

	}

	private ZSelectItem generateSelectItems(Node node, String prefix) {
		if(prefix == null) {
			prefix = "";
		}

		NameGenerator nameGenerator = new NameGenerator();
		String nameA = nameGenerator.generateName(node);
		ZSelectItem selectItem = null;
		if(node.isVariable()) {
			//selectItem = new ZSelectItem(prefix + node.getName());
			selectItem = new ZSelectItem(prefix + nameA);
		} else if(node.isURI()){
			//selectItem = new ZSelectItem(prefix + node.getLocalName());
			selectItem = new ZSelectItem(prefix + nameA);
		} else if(node.isLiteral()){
			//selectItem = new ZSelectItem(prefix + node.toString());
			Object literalValue = node.getLiteralValue();
			ZExp exp;
			if(literalValue instanceof String) {
				exp = new ZConstant(prefix + literalValue.toString(), ZConstant.STRING);							
			} else if (literalValue instanceof Double) {
				exp = new ZConstant(prefix + literalValue.toString(), ZConstant.NUMBER);
			} else {
				exp = new ZConstant(prefix + literalValue.toString(), ZConstant.STRING);							
			
			}
			selectItem = new R2OSelectItem();
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
			ZSelectItem selectItem = this.generateSelectItems(node, prefix);
			result.add(selectItem);
		}

		return result;
	}


}
