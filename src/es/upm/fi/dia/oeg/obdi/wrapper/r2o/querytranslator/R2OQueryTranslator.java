package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslatorUtility;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.TypeInferrer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.URIUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class R2OQueryTranslator extends AbstractQueryTranslator {
	

	private static Logger logger = Logger.getLogger(R2OQueryTranslator.class);
	//private R2OMappingDocument mappingDocument;
	
	
	private Map<Triple, R2OConceptMapping> mapTripleCM = new HashMap<Triple, R2OConceptMapping>();

	
	public R2OQueryTranslator(AbstractMappingDocument mappingDocument) {
		super(mappingDocument);
		this.nameGenerator = new NameGenerator();
	}

	@Override
	protected ZExpression genCondSQLPredicateObject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		R2OConceptMapping cm = this.mapTripleCM.get(tp);
		return this.genCondSQLPredicateObject(tp, betaGenerator, cm);
	}
	

	@Override
	protected ZExpression genCondSQLSubject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		AbstractConceptMapping cm = this.mapTripleCM.get(tp);
		return this.genCondSQLSubject(tp, betaGenerator, cm);
	}
	

	
	private ZExpression genCondSQLTB(Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator) throws Exception {
		
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Collection<ZExpression> exps = new HashSet<ZExpression>();

		ZExpression condSubject = this.genCondSQLSubject(tripleBlock.iterator().next(), betaGenerator);
		if(condSubject != null) {
			exps.add(condSubject);
		}
		for(Triple tp : tripleBlock) {
			ZExpression cond = this.genCondSQLPredicateObject(tp, betaGenerator);
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
				result2.addOperand(exp);
			}
		}
		logger.debug("genCondSQLTB = " + result2);
		return result2;

	}
	
	@Override
	protected Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		R2OConceptMapping cmSubject = this.mapTripleCM.get(tp);
		return this.genPRSQL(tp, betaGenerator, nameGenerator, cmSubject);
	}
	

	


	@Override
	protected Collection<ZSelectItem> genPRSQLObject(Node object, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject) throws Exception {
		
		Collection<ZSelectItem> selectItems = super.genPRSQLObject(object, tp, betaGenerator, cmSubject);
		ZSelectItem selectItem = selectItems.iterator().next();
		
		Collection<AbstractConceptMapping> cmObjects = this.mapInferredTypes.get(object);
		if(cmObjects != null) {
			AbstractConceptMapping cmObject = cmObjects.iterator().next();
			if(cmObject != null) {
				
				if(cmSubject.hasWellDefinedURIExpression()) {
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




	@Override
	protected ZSelectItem genPRSQLPredicate(Node predicate, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject) throws Exception {
		ZSelectItem selectItem = betaGenerator.calculateBetaCM(tp, POS.pre, cmSubject);
		selectItem.setAlias(nameGenerator.generateName(tp, predicate));
		return selectItem;
	}
	









	





	@Override
	protected Collection<ZSelectItem> genPRSQLSubject(Node subject, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject) throws Exception {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();
		
		R2OTransformationExpression cmSubjectURIAs = ((R2OConceptMapping)cmSubject).getURIAs();
		ZSelectItem selectItemSubject = betaGenerator.calculateBetaCM(tp, POS.sub, cmSubject);
		String selectItemSubjectAlias = nameGenerator.generateName(tp, subject); 
		selectItemSubject.setAlias(selectItemSubjectAlias);
		selectItems.add(selectItemSubject); //line 21
		if(URIUtility.isWellDefinedURIExpression(cmSubjectURIAs)) {
			R2OColumnRestriction pkColumnRestriction = (R2OColumnRestriction) cmSubjectURIAs.getLastRestriction();
			R2ODatabaseColumn pkColumn = pkColumnRestriction.getDatabaseColumn();
			//ZSelectItem selectItemSubjectPK = new R2OSelectItem(pkColumn.getFullColumnName());
			ZSelectItem selectItemSubjectPK = new SQLSelectItem(((R2OConceptMapping)cmSubject).generatePKColumnAlias());
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
	
	@Override
	protected ZExp transConstant(NodeValue nodeValue) {
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

	@Override
	public SQLQuery translate(Query sparqlQuery) throws Exception  {
		
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);
		Op opSparqlQuery = Algebra.compile(sparqlQuery) ;

		TranslatorUtility tu = new TranslatorUtility((R2OMappingDocument) this.mappingDocument);
		TypeInferrer typeInferrer = new TypeInferrer(this.mappingDocument);
		super.mapInferredTypes = typeInferrer.infer(opQueryPattern);
		AbstractAlphaGenerator alphaGenerator = 
			new R2OAlphaGenerator(mapInferredTypes, this.mappingDocument);
		this.alphaGenerator = alphaGenerator;
		AbstractBetaGenerator betaGenerator = 
			new R2OBetaGenerator(mapInferredTypes, this.mappingDocument);
		this.betaGenerator = betaGenerator;
		
		SQLQuery sparql2SQLQuery =  this.trans(opSparqlQuery);
		logger.debug("translate = \n" + sparql2SQLQuery);
		return sparql2SQLQuery;
	}








	@Override
	public SQLQuery translate(String queryFilePath) throws Exception {
		// TODO Auto-generated method stub
		logger.warn("Implement this!");
		return null;
	}


	@Override
	protected SQLQuery transProject(Op opQuery) throws Exception {
		

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
			sparql2sqlResult = QueryTranslatorUtility.eliminateSubQuery(newSelectItems, gpSQL, null, orderByConditions);
			
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


	@Override
	protected SQLQuery transTB(List<Triple> triples) throws Exception {
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
			tbQuery = QueryTranslatorUtility.eliminateSubQuery(prSQLList, alphaTB, condSQL2, null);
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


	@Override
	protected SQLQuery transTP(Triple tp) throws QueryTranslationException  {
		logger.debug("transTP : " + tp);
		SQLQuery tpQuery = new SQLQuery();
		tpQuery.addSelect(new Vector<ZSelectItem>());
		tpQuery.addFrom(new Vector<ZFromItem>());

		try {
			R2OConceptMapping cm = (R2OConceptMapping) alphaGenerator.calculateAlphaCM(tp);
			this.mapTripleCM.put(tp, cm);
			R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, (R2OMappingDocument) this.mappingDocument);
			SQLQuery alpha = cmu.unfoldConceptMapping();
			logger.debug("alpha(tp) = \n" + alpha);

//			ZExpression condSQLSubject = this.genCondSQLSubject(tp, betaGenerator);
//			ZExpression condSQL = this.genCondSQLPredicateObject(tp, betaGenerator);
//			ZExpression condSQL2 = null;
//			if(condSQLSubject == null && condSQL==null) {
//				condSQL2 = null;
//			} else if(condSQLSubject != null && condSQL==null) {
//				condSQL2 = condSQLSubject;
//			} else if(condSQLSubject == null && condSQL!=null) {
//				condSQL2 = condSQL;
//			} else {
//				condSQL2 = new ZExpression("AND", condSQLSubject, condSQL);
//			}

			ZExpression condSQL = this.genCondSQL(tp);
			
			Vector<ZSelectItem> prSQLList = (Vector<ZSelectItem>) genPRSQL(tp, betaGenerator, nameGenerator);
			logger.debug("genPRSQL(tp,beta,name) = \n" + prSQLList);
			
			if(this.subQueryElimination) {
				tpQuery = QueryTranslatorUtility.eliminateSubQuery(prSQLList, alpha, condSQL, null);
			} else {
				tpQuery.addSelect(prSQLList);

				//SQLFromItem fromItem = new SQLFromItem(alpha.toString(), SQLFromItem.FORM_QUERY);
				SQLFromItem fromItem = new SQLFromItem(alpha.toString(), LogicalTableType.SQLQUERY);
				tpQuery.addFrom(fromItem);
				fromItem.setAlias(fromItem.generateAlias());

				if(condSQL != null) {
					tpQuery.addWhere(condSQL);
				}
			}
			
			logger.debug("transTP = \n" + tpQuery + "\n");
			return tpQuery;
		} catch(Exception e) {
			//e.printStackTrace();
			logger.error("Error processing tp : " + tp);
			throw new QueryTranslationException(e.getMessage(), e);
		}
	}


	@Override
	protected ZExp transVar(Op op, Var var) {
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
					AbstractConceptMapping cm = cms.iterator().next();
					boolean isWellDefinedURI = cm.hasWellDefinedURIExpression();
					if(isWellDefinedURI) {
						colName += R2OConstants.KEY_SUFFIX;
					}
				}
			}			
		}


		ZExp result = new ZConstant(colName, ZConstant.COLUMNNAME); 
		return result;
	}
}
