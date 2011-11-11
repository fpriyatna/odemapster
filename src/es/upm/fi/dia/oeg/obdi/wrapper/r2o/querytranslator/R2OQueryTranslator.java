package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.ArrayList;
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
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.tdb.store.Hash;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoinQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.TypeInferrer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.URIUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class R2OQueryTranslator extends AbstractQueryTranslator {
	

	private static Logger logger = Logger.getLogger(R2OQueryTranslator.class);
	//private R2OMappingDocument mappingDocument;
	private Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes;
	private Map<Node, String> mapNodeKey = new HashMap<Node, String>();
	private Map<Op, Collection<Node>> mapTermsC = new HashMap<Op, Collection<Node>>();
	private Map<Op, String> mapTransGP1Alias = new HashMap<Op, String>();
	
	private Map<Triple, R2OConceptMapping> mapTripleCM = new HashMap<Triple, R2OConceptMapping>();

	
	public R2OQueryTranslator(AbstractMappingDocument mappingDocument) {
		super(mappingDocument);
		this.nameGenerator = new NameGenerator();
	}


	private ZExpression genCondSQL(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Collection<ZExpression> exps = new HashSet<ZExpression>();
		
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		R2OConceptMapping cm = this.mapTripleCM.get(tp);
		
		ZExp betaSub = betaGenerator.calculateBetaCM(tp, POS.sub, cm).getExpression();
		ZExp betaPre = betaGenerator.calculateBetaCM(tp, POS.pre, cm).getExpression();
		ZExp betaObj = betaGenerator.calculateBetaCM(tp, POS.obj, cm).getExpression();

		if(!predicate.isVariable()) {
			ZExp exp = new ZExpression("="
					, betaPre
					, new ZConstant(predicate.toString(), ZConstant.STRING));
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

			if(exp != null) {
				//result = new ZExpression("AND", result, exp);
			}
			
		} else {
			ZExpression exp = new ZExpression("IS NOT NULL");
			exp.addOperand(betaObj);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);

		}

		if(subject == predicate) {
			ZExpression exp = new ZExpression("="
					, betaSub
					, betaPre);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);
		}

		if(subject == object) {
			ZExpression exp = new ZExpression("="
					, betaSub
					, betaObj);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);
		}

		if(object == predicate) {
			ZExpression exp = new ZExpression("="
					, betaObj
					, betaPre);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);
		}

		ZExpression result2;
		if(exps.size() == 0) {
			result2 = null;
		} else if(exps.size() == 1) {
			result2 = exps.iterator().next();
		} else {
			result2 = new ZExpression("AND");
			for(ZExp exp : exps) {
				((ZExpression) result2).addOperand(exp);
			}
		}
		return result2;
	}

	private ZExpression genCondSQLSubject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		ZExpression exp = null;
		Node subject = tp.getSubject();
		R2OConceptMapping cm = this.mapTripleCM.get(tp);
		ZSelectItem betaCMSelectItem = betaGenerator.calculateBetaCM(tp, POS.sub, cm);
		ZExp betaSub = betaCMSelectItem.getExpression();

		if(!subject.isVariable()) {
			
			if(subject.isURI()) {
				/*
				exp = new ZExpression("="
						, betaSub
						, new ZConstant(subject.toString(), ZConstant.STRING));
				*/
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
			
		}
		
		return exp;
		
	}
	
	private ZExpression genCondSQLTB(Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator) throws Exception {
		
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Collection<ZExpression> exps = new HashSet<ZExpression>();

		ZExpression condSubject = this.genCondSQLSubject(tripleBlock.iterator().next(), betaGenerator);
		if(condSubject != null) {
			exps.add(condSubject);
		}
		for(Triple tp : tripleBlock) {
			ZExpression cond = this.genCondSQL(tp, betaGenerator);
			if(cond != null) {
				exps.add(cond);
			}
			result = new ZExpression("AND", result, cond);
		}

		ZExpression result2;
		if(exps.size() == 0) {
			result2 = null;
		} else if(exps.size() == 1) {
			result2 = exps.iterator().next();
		} else {
			result2 = new ZExpression("AND");
			for(ZExp exp : exps) {
				((ZExpression)result2).addOperand(exp);
			}
		}
		logger.debug("genCondSQLTB = " + result2);
		return result2;

	}
	
	private Collection<ZSelectItem> generateSelectItems(Collection<Node> nodes, String prefix) {
		Collection<ZSelectItem> result = new LinkedHashSet<ZSelectItem>();

		for(Node node : nodes) {
			ZSelectItem selectItem = this.generateSelectItems(node, prefix);
			result.add(selectItem);
			
			String nodeKey = this.mapNodeKey.get(node);
			if(nodeKey != null) {
				if(prefix != null) {
					nodeKey = prefix + nodeKey;
				}
				ZSelectItem selectItem2 = new SQLSelectItem(nodeKey);
				result.add(selectItem2);
			}
		}

		return result;
	}

	private ZSelectItem generateSelectItems(Node node, String prefix) {
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
	

	
	private Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		R2OConceptMapping cmSubject = this.mapTripleCM.get(tp);
		Collection<ZSelectItem> prList = new Vector<ZSelectItem>();

		Collection<ZSelectItem> selectItemsSubjects = this.genPRSQLSubject(subject, tp, betaGenerator, cmSubject);
		prList.addAll(selectItemsSubjects);
		
		if(predicate != subject) {
			//line 22
			ZSelectItem selectItemPredicate = this.genPRSQLPredicate(predicate, tp, betaGenerator, cmSubject);
			prList.add(selectItemPredicate);
		}

		if(object != subject && object != predicate) {
			//line 23
			Collection<ZSelectItem> selectItems = this.genPRSQLObject(object, tp, betaGenerator, cmSubject);
			prList.addAll(selectItems);
		}

		return prList;

	}

	private Collection<ZSelectItem> genPRSQLObject(Node object, Triple tp, AbstractBetaGenerator betaGenerator, R2OConceptMapping cmSubject) throws Exception {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();
		
		ZSelectItem selectItem = betaGenerator.calculateBetaCM(tp, POS.obj, cmSubject);
		String selectItemAlias = nameGenerator.generateName(tp, object);
		if(selectItemAlias != null) {
			selectItem.setAlias(selectItemAlias);
		}
		selectItems.add(selectItem); //line 23
		
		Collection<AbstractConceptMapping> cmObjects = this.mapInferredTypes.get(object);
		if(cmObjects != null) {
			R2OConceptMapping cmObject = (R2OConceptMapping) cmObjects.iterator().next();
			if(cmObject != null) {
				R2OTransformationExpression cmObjectURIAs = cmObject.getURIAs();
				if(URIUtility.isWellDefinedURIExpression(cmObjectURIAs)) {
					String selectItemColumn2 = selectItem.getExpression() + R2OConstants.KEY_SUFFIX;
					String selectItemAlias2 = selectItem.getAlias() + R2OConstants.KEY_SUFFIX;
					ZSelectItem selectItem2 = new SQLSelectItem(selectItemColumn2);
					selectItem2.setAlias(selectItemAlias2);
					selectItems.add(selectItem2);
					this.mapNodeKey.put(object, selectItemAlias2);
				} 
			}			
		} 
		

		
		return selectItems;
	}

	private ZSelectItem genPRSQLPredicate(Node predicate, Triple tp, AbstractBetaGenerator betaGenerator, R2OConceptMapping cmSubject) throws Exception {
		ZSelectItem selectItem = betaGenerator.calculateBetaCM(tp, POS.pre, cmSubject);
		selectItem.setAlias(nameGenerator.generateName(tp, predicate));
		return selectItem;
	}

	
	private Collection<ZSelectItem> genPRSQLSubject(Node subject, Triple tp, AbstractBetaGenerator betaGenerator, R2OConceptMapping cmSubject) throws Exception {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();
		
		R2OTransformationExpression cmSubjectURIAs = cmSubject.getURIAs();
		ZSelectItem selectItemSubject = betaGenerator.calculateBetaCM(tp, POS.sub, cmSubject);
		String selectItemSubjectAlias = nameGenerator.generateName(tp, subject); 
		selectItemSubject.setAlias(selectItemSubjectAlias);
		selectItems.add(selectItemSubject); //line 21
		if(URIUtility.isWellDefinedURIExpression(cmSubjectURIAs)) {
			R2OColumnRestriction pkColumnRestriction = (R2OColumnRestriction) cmSubjectURIAs.getLastRestriction();
			R2ODatabaseColumn pkColumn = pkColumnRestriction.getDatabaseColumn();
			//ZSelectItem selectItemSubjectPK = new R2OSelectItem(pkColumn.getFullColumnName());
			ZSelectItem selectItemSubjectPK = new SQLSelectItem(cmSubject.generatePKColumnAlias());
			String selectItemSubjectPKAlias = selectItemSubjectAlias + R2OConstants.KEY_SUFFIX;
			selectItemSubjectPK.setAlias(selectItemSubjectPKAlias);
			selectItems.add(selectItemSubjectPK);
			this.mapNodeKey.put(subject, selectItemSubjectPKAlias);
		} 

		return selectItems;
	}

	private Collection<ZSelectItem> genPRSQLTB(
			Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Collection<ZSelectItem> prList = new HashSet<ZSelectItem>();
		Triple firstTriple = tripleBlock.iterator().next();
		Node subject = firstTriple.getSubject();
		R2OConceptMapping cmSubject = this.mapTripleCM.get(firstTriple);

		Collection<ZSelectItem> selectItemsSubjects = this.genPRSQLSubject(
				subject, firstTriple, betaGenerator, cmSubject);
		prList.addAll(selectItemsSubjects);

		
		for(Triple tp : tripleBlock) {
			Node predicate = tp.getPredicate();
			Node object = tp.getObject();

			if(predicate != subject) {
				ZSelectItem selectItemPredicate = this.genPRSQLPredicate(predicate, tp, betaGenerator, cmSubject);
				prList.add(selectItemPredicate);
				
			}

			if(object != subject && object != predicate) {
				Collection<ZSelectItem> selectItemsObject = this.genPRSQLObject(object, tp, betaGenerator, cmSubject);
				prList.addAll(selectItemsObject);
				
			}
		}

		Collection<ZSelectItem> prList2 = new Vector<ZSelectItem>(prList);

		return prList2;
	}


	private SQLQuery trans(Op op) throws Exception {


		SQLQuery result = null;
		if(op instanceof OpBGP) { //triple or bgp pattern
			OpBGP bgp = (OpBGP) op;

			if(TranslatorUtility.isTriplePattern(bgp)) { //triple pattern
				Triple tp = bgp.getPattern().getList().get(0);
				result = this.transTP(tp);
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
					result = this.transTB(triples);
				} else {
					int separationIndex = 1;
					if(this.optimizeTripleBlock) {
						separationIndex = TranslatorUtility.getFirstTBEndIndex(triples);
					}
					List<Triple> gp1TripleList = triples.subList(0, separationIndex);
					OpBGP gp1 = new OpBGP(BasicPattern.wrap(gp1TripleList));
					List<Triple> gp2TripleList = triples.subList(separationIndex, triples.size());
					OpBGP gp2 = new OpBGP(BasicPattern.wrap(gp2TripleList));

					// result = this.transJoin(gp1, gp2, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_INNER);					
					result = this.transInnerJoin(op, gp1, gp2);
				}
			}
		} else if(op instanceof OpJoin) { // AND pattern
//			logger.debug("op instanceof OpJoin");
			OpJoin opJoin = (OpJoin) op;
			Op opLeft = opJoin.getLeft();
			Op opRight = opJoin.getRight();
			//result = this.transJoin(opLeft, opRight, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_INNER);
			result = this.transInnerJoin(op, opLeft, opRight);
		} else if(op instanceof OpLeftJoin) { //OPT pattern
//			logger.debug("op instanceof OpLeftJoin");
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Op opLeft = opLeftJoin.getLeft();
			Op opRight = opLeftJoin.getRight();
			if(opLeftJoin.getExprs() == null) {
				// result = this.transJoin(opLeft, opRight, alphaGenerator, betaGenerator, R2OConstants.JOINS_TYPE_LEFT);				
				result = this.transLeftJoin(op, opLeft, opRight);
			} else {
//				logger.debug("op instanceof OpFilter");
				ExprList exprList = opLeftJoin.getExprs();
				Expr exprNull = null;
				Op opLeftJoin2 = OpLeftJoin.create(opLeft, opRight, exprNull);
				result = this.transFilter(opLeftJoin2, exprList);
			}

		} else if(op instanceof OpUnion) { //UNION pattern
//			logger.debug("op instanceof OpUnion");
			OpUnion opUnion = (OpUnion) op;
			Op opLeft = opUnion.getLeft();
			Op opRight = opUnion.getRight();
			result = this.transUNION(opLeft, opRight);
		} else if(op instanceof OpFilter) { //FILTER pattern
//			logger.debug("op instanceof OpFilter");
			OpFilter opFilter = (OpFilter) op;
			Op opFilterSubOp = opFilter.getSubOp();
			ExprList exprList = opFilter.getExprs();
			result = this.transFilter(opFilterSubOp, exprList);
		} else if(op instanceof OpProject || op instanceof OpSlice || op instanceof OpDistinct) {
//			logger.debug("op instanceof OpProject/OpSlice/OpDistinct");
			result = this.transProject(op);
		} else {
			throw new R2OTranslationException("Unsupported query!");
		}

		return result;
	}

	private ZExp transConstant(NodeValue nodeValue) {
		ZExp result = null;

		Node node = nodeValue.getNode();
		if(nodeValue.isLiteral()) {
			if(nodeValue.isNumber()) {
				result = new ZConstant(node.getLiteralValue().toString(), ZConstant.NUMBER);	
			} else {
				result = new ZConstant(node.getLiteralValue().toString(), ZConstant.STRING);
			}
		} else if(nodeValue.isIRI()) {
			Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(node);
			if(cms != null) {
				R2OConceptMapping cm = (R2OConceptMapping) cms.iterator().next();
				if(cm != null) {
					R2OTransformationExpression uriAs = cm.getURIAs();
					if(URIUtility.isWellDefinedURIExpression(uriAs)) {
						int index = URIUtility.getIRILengthWithoutPK(uriAs);
						String pk = node.getURI().substring(index);
						
						R2OColumnRestriction lastRestriction = URIUtility.getLastRestriction(uriAs);
						String dataType = lastRestriction.getDatabaseColumn().getDataType();
						if(R2OConstants.DATATYPE_DOUBLE.equals(dataType) ||
								R2OConstants.DATATYPE_INTEGER.equals(dataType) ||
								R2OConstants.DATATYPE_NUMBER.equals(dataType)) {
							result = new ZConstant(pk, ZConstant.NUMBER);
						} else {
							result = new ZConstant(pk, ZConstant.STRING);
						}
					} else {
						result = new ZConstant(node.getLocalName(), ZConstant.COLUMNNAME);
					}
				} else {
					result = new ZConstant(node.getLocalName(), ZConstant.COLUMNNAME);
				}					
			} else {
				result = new ZConstant(node.getLocalName(), ZConstant.COLUMNNAME);
			}

		}
		return result;
	}
	
	private ZExp transExpr(Op op, Expr expr) {
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


	private SQLQuery transFilter(Op gp, ExprList exprList)
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
			result = TranslatorUtility.eliminateSubQuery(newSelectItems, transGP, newWhere, null);
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
	};




	private ZExp transFunction(Op op, ExprFunction exprFunction) {
		ZExp result = null;
		
		String databaseType = R2ORunner.configurationProperties.getDatabaseType();
		if(databaseType == null) {
			databaseType = R2OConstants.DATABASE_MYSQL;
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
			if(R2OConstants.DATABASE_MONETDB.equalsIgnoreCase(databaseType)) {
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
		return this.transJoin(opParent, gp1, gp2, R2OConstants.JOINS_TYPE_INNER);
	}


	private SQLQuery transJoin(Op opParent, Op gp1, Op gp2, String joinType)
	throws Exception  {
		logger.debug("transJoin");
		logger.debug("gp1 = " + gp1);
		logger.debug("gp2 = " + gp2);

		SQLQuery transJoin = new SQLQuery();
		Collection<ZSelectItem> selectItems = new HashSet<ZSelectItem>();

		Collection<Node> termsGP1 = TranslatorUtility.terms(gp1);
		logger.debug("termsGP1 = " + termsGP1);
		Collection<Node> termsGP2 = TranslatorUtility.terms(gp2);
		logger.debug("termsGP2 = " + termsGP2);

		SQLQuery transGP1 = this.trans(gp1);
		String transGP1Alias = transGP1.generateAlias();
		this.mapTransGP1Alias.put(opParent, transGP1Alias);
		//SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), SQLFromItem.FORM_QUERY);
		SQLFromItem transGP1FromItem = new SQLFromItem(transGP1.toString(), LogicalTableType.SQLQUERY);
		transGP1FromItem.setAlias(transGP1Alias);
		transJoin.addFrom(transGP1FromItem);

		SQLQuery transGP2 = this.trans(gp2);
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
			Collection<AbstractConceptMapping> cmTermCs = this.mapInferredTypes.get(termC);
			if(cmTermCs != null) {
				R2OConceptMapping cmTermC = (R2OConceptMapping) cmTermCs.iterator().next();
				if(cmTermC != null && URIUtility.isWellDefinedURIExpression(cmTermC.getURIAs())) {
					String pkColumnAlias = cmTermC.generatePKColumnAlias();
					//termCName = pkColumnAlias;
					termCName = this.mapNodeKey.get(termC);
				} else {
					termCName = nameGenerator.generateName(null, termC);
				}
			} else {
				termCName = nameGenerator.generateName(null, termC);
			}
			
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



		logger.info("transJoin = \n" + transJoin + "\n");
		return transJoin;
	}

	@Override
	public SQLQuery translate(Query sparqlQuery) throws Exception  {
		
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);
		Op opSparqlQuery = Algebra.compile(sparqlQuery) ;

		TranslatorUtility tu = new TranslatorUtility((R2OMappingDocument) this.mappingDocument);
		TypeInferrer typeInferrer = new TypeInferrer((R2OMappingDocument) this.mappingDocument);
		this.mapInferredTypes = typeInferrer.infer(opQueryPattern);
		AbstractAlphaGenerator alphaGenerator = 
			new AlphaGenerator3(mapInferredTypes, (R2OMappingDocument) this.mappingDocument);
		this.alphaGenerator = alphaGenerator;
		AbstractBetaGenerator betaGenerator = 
			new BetaGenerator1(mapInferredTypes, (R2OMappingDocument) this.mappingDocument);
		this.betaGenerator = betaGenerator;
		
		SQLQuery sparql2SQLQuery =  this.trans(opSparqlQuery);
		logger.info("translate = \n" + sparql2SQLQuery);
		return sparql2SQLQuery;
	}
	
	private SQLQuery transLeftJoin(Op opParent, Op gp1, Op gp2) throws Exception {
		return this.transJoin(opParent, gp1, gp2, R2OConstants.JOINS_TYPE_LEFT);
	}
	
	private SQLQuery transProject(Op opQuery) throws Exception {
		

		long sliceLength = -1;
		boolean isDistinct = false;
		Vector<ZOrderBy> orderByConditions = null;

		OpSlice opSlice = null;
		OpDistinct opDistinct = null;
		OpProject opProject = null;
		OpOrder opOrder = null;
		Op graphPatternOp = null;
		
		
		//slice
		//distinct
		//project
		//order
		if(opQuery instanceof OpSlice) {
			opSlice = (OpSlice) opQuery;
			sliceLength = opSlice.getLength();
			Op opSliceSubOp = opSlice.getSubOp();
			if(opSliceSubOp instanceof OpProject) {
				opProject = (OpProject) opSliceSubOp;
			} else if(opSliceSubOp instanceof OpDistinct) {
				opDistinct = (OpDistinct) opSliceSubOp;
				Op opDistinctSubOp = opDistinct.getSubOp();
				if (opDistinctSubOp instanceof OpProject) {
					opProject = (OpProject) opDistinctSubOp;
				}
			}
		} else if(opQuery instanceof OpDistinct) {
			opDistinct = (OpDistinct) opQuery;
			Op opDistinctSubOp = opDistinct.getSubOp();
			if (opDistinctSubOp instanceof OpProject) {
				opProject = (OpProject) opDistinctSubOp;
			}				
		} else if (opQuery instanceof OpProject) {
			opProject = (OpProject) opQuery;
		} 
		

		if(opDistinct != null) {
			isDistinct = true;
		}
		
		if(opProject != null) {
			Op opProjectSubOp = opProject.getSubOp();
			if(opProjectSubOp instanceof OpOrder) {
				opOrder = (OpOrder) opProjectSubOp;
				graphPatternOp = opOrder.getSubOp();
			} else {
				graphPatternOp = opProject.getSubOp();
			}
		}
		
		SQLQuery gpSQL = this.trans(graphPatternOp);
		String gpSQLAlias = gpSQL.generateAlias();
		logger.debug("gpSQL result = " + gpSQL.toString());

		if(opOrder != null) {
			orderByConditions = new Vector<ZOrderBy>();
			for(SortCondition sortCondition : opOrder.getConditions()) {
				int sortConditionDirection = sortCondition.getDirection();
				Expr sortConditionExpr = sortCondition.getExpression();
				Var sortConditionVar = sortConditionExpr.asVar();
				//ZExp zExp = this.transExpr(graphPatternOp, sortConditionExpr);
				ZExp zExp = new ZConstant(sortConditionVar.getName(), ZConstant.COLUMNNAME);
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
		}
		
		Collection<ZSelectItem> newSelectItems = new HashSet<ZSelectItem>();
		List<Var> selectVars = opProject.getVars();
		for(Var selectVar : selectVars) {
			String nameSelectVar = nameGenerator.generateName(null, selectVar);
//			ZSelectItem selectItem = new ZSelectItem(gpSQLAlias + "." + nameSelectVar);
			ZSelectItem selectItem = new ZSelectItem(nameSelectVar);
			selectItem.setAlias(selectVar.getName());
			newSelectItems.add(selectItem);
		}


		SQLQuery sparql2sqlResult = new SQLQuery();
		if(this.subQueryElimination) {
			sparql2sqlResult = TranslatorUtility.eliminateSubQuery(newSelectItems, gpSQL, null, orderByConditions);
			
			if(orderByConditions != null) {
				sparql2sqlResult.addOrderBy(orderByConditions);
			}
			
		} else {
			//SQLFromItem fromItem = new SQLFromItem(gpSQL.toString(), SQLFromItem.FORM_QUERY);
			SQLFromItem fromItem = new SQLFromItem(gpSQL.toString(), LogicalTableType.SQLQUERY);
			fromItem.setAlias(gpSQLAlias);
			sparql2sqlResult.setSelectItems(newSelectItems);
			sparql2sqlResult.addOrderBy(orderByConditions);
			sparql2sqlResult.addFrom(fromItem);
		}
		
		sparql2sqlResult.setSlice(sliceLength);
		sparql2sqlResult.setDistinct(isDistinct);
		return sparql2sqlResult;

	}
	
	private SQLQuery transTB(List<Triple> triples) throws Exception {
		SQLQuery tbQuery = new SQLQuery();

		R2OConceptMapping cm = (R2OConceptMapping) alphaGenerator.calculateAlphaCMTB(triples);
		for(Triple tp : triples) {
			this.mapTripleCM.put(tp, cm);
		}
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, (R2OMappingDocument) this.mappingDocument);
		SQLQuery alphaTB = cmu.unfoldConceptMapping();
		logger.debug("alphaTB = \n" + alphaTB);

		Vector<ZSelectItem> prSQLList = (Vector<ZSelectItem>) this.genPRSQLTB(
				triples, betaGenerator, nameGenerator);
		logger.debug("genPRSQLTB = \n" + prSQLList);

		ZExpression condSQL2 = this.genCondSQLTB(triples, betaGenerator);
		logger.debug("genCondSQLTB = " + condSQL2);

		if(this.subQueryElimination) {
			tbQuery = TranslatorUtility.eliminateSubQuery(prSQLList, alphaTB, condSQL2, null);
		} else {
			tbQuery.addSelect(prSQLList);
			
			//SQLFromItem fromItem = new SQLFromItem(alphaTB.toString(), SQLFromItem.FORM_QUERY);
			SQLFromItem fromItem = new SQLFromItem(alphaTB.toString(), LogicalTableType.SQLQUERY);
			fromItem.setAlias(fromItem.generateAlias());
			tbQuery.addFrom(fromItem);	
			
			if(condSQL2 != null) {
				tbQuery.addWhere(condSQL2);
			}
		}
		
		logger.debug("transTB = \n" + tbQuery + "\n");
		return tbQuery;
	}

	private SQLQuery transTP(Triple tp) 
	throws R2OTranslationException  {
		logger.debug("transTP : " + tp);
		SQLQuery tpQuery = new SQLQuery();
		tpQuery.addSelect(new Vector<ZSelectItem>());
		tpQuery.addFrom(new Vector<ZFromItem>());

		try {
			R2OConceptMapping cm = (R2OConceptMapping) alphaGenerator.calculateAlphaCM(tp);
			//logger.debug("cm(tp) = " + cm);
			this.mapTripleCM.put(tp, cm);
			R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, (R2OMappingDocument) this.mappingDocument);
			SQLQuery alpha = cmu.unfoldConceptMapping();
			logger.debug("alpha(tp) = \n" + alpha);

			ZSelectItem betaSub = betaGenerator.calculateBetaCM(tp, POS.sub, cm);
//			logger.debug("beta(tp,sub) = " + betaSub); 
			ZSelectItem betaPre = betaGenerator.calculateBetaCM(tp, POS.pre, cm);
//			logger.debug("beta(tp,pre) = " + betaPre);
			ZSelectItem betaObj = betaGenerator.calculateBetaCM(tp, POS.obj, cm);
//			logger.debug("beta(tp,obj) = " + betaObj);

			ZExpression condSQLSubject = this.genCondSQLSubject(tp, betaGenerator);
			ZExpression condSQL = this.genCondSQL(tp, betaGenerator);
			ZExpression condSQL2 = null;
			if(condSQLSubject == null && condSQL==null) {
				condSQL2 = null;
			} else if(condSQLSubject != null && condSQL==null) {
				condSQL2 = condSQLSubject;
			} else if(condSQLSubject == null && condSQL!=null) {
				condSQL2 = condSQL;
			} else {
				condSQL2 = new ZExpression("AND", condSQLSubject, condSQL);
			}
//			logger.debug("genCondSQL(tp,beta) = " + condSQL2);

			Vector<ZSelectItem> prSQLList = (Vector<ZSelectItem>) genPRSQL(tp, betaGenerator, nameGenerator);
			logger.debug("genPRSQL(tp,beta,name) = \n" + prSQLList);
			
			if(this.subQueryElimination) {
				tpQuery = TranslatorUtility.eliminateSubQuery(prSQLList, alpha, condSQL2, null);
			} else {
				tpQuery.addSelect(prSQLList);

				//SQLFromItem fromItem = new SQLFromItem(alpha.toString(), SQLFromItem.FORM_QUERY);
				SQLFromItem fromItem = new SQLFromItem(alpha.toString(), LogicalTableType.SQLQUERY);
				tpQuery.addFrom(fromItem);
				fromItem.setAlias(fromItem.generateAlias());

				if(condSQL2 != null) {
					tpQuery.addWhere(condSQL2);
				}
			}
			
			logger.debug("transTP = \n" + tpQuery + "\n");
			return tpQuery;
		} catch(Exception e) {
			//e.printStackTrace();
			logger.error("Error processing tp : " + tp);
			throw new R2OTranslationException(e.getMessage(), e);
		}
	}

	private SQLQuery transUNION(Op gp1, Op gp2)
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
		joinQuery1.setJoinType(R2OConstants.JOINS_TYPE_LEFT);
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
		joinQuery2.setJoinType(R2OConstants.JOINS_TYPE_LEFT);
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
		logger.info("query1 UNION query2 = \n" + result + "\n");
		return result;

	}



	private ZExp transVar(Op op, Var var) {
		String colName = nameGenerator.generateName(null, var);
		Node node = var.asNode();
		String nodePKColumn = this.mapNodeKey.get(node); 
		
		if(op == null) {
			if(nodePKColumn != null) {
				colName = nodePKColumn;
			}
		} else {
			Collection<Node> termsC = this.mapTermsC.get(op);
			if(nodePKColumn != null && termsC != null && termsC.contains(node)) {
				//colName = this.transGP1Aliases.iterator().next() + "." + nodePKColumn;
				colName = this.mapTransGP1Alias.get(op) + "." + nodePKColumn;
			} else {
				Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(node);
				if(cms != null) {
					R2OConceptMapping cm = (R2OConceptMapping) cms.iterator().next();
					boolean isWellDefinedURI = URIUtility.isWellDefinedURIExpression(cm.getURIAs());
					if(isWellDefinedURI) {
						colName += R2OConstants.KEY_SUFFIX;
					}
				}
			}			
		}


		ZExp result = new ZConstant(colName, ZConstant.COLUMNNAME); 
		return result;
	}


	@Override
	public SQLQuery translate(String queryFilePath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}



}
