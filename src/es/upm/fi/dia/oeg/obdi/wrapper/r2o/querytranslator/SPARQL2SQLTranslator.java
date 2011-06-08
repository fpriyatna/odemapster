package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;

public class SPARQL2SQLTranslator {
	private static Logger logger = Logger.getLogger(SPARQL2SQLTranslator.class);

	private R2OMappingDocument mappingDocument;
	Map<Node, R2OConceptMapping> mapNodeConceptMapping;

	private AbstractAlphaGenerator alphaGenerator;
	private AbstractBetaGenerator betaGenerator;
	private NameGenerator nameGenerator;

	public SPARQL2SQLTranslator(R2OMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
		this.nameGenerator = new NameGenerator();
	}

	private R2OQuery transTB(OpBGP tb, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator) throws Exception {
		R2OQuery result = new R2OQuery();
		
		List<Triple> triples = tb.getPattern().getList();
		Vector<ZSelectItem> selectItems = (Vector<ZSelectItem>) this.genPRSQLTripleBlock(
				triples, betaGenerator, nameGenerator);
		logger.debug("genPRSQL = " + selectItems);
		result.addSelect(selectItems);

		R2OQuery alphaTB = (R2OQuery) alphaGenerator.calculateAlpha(triples);
		logger.debug("alphaTB = " + alphaTB);
		R2OFromItem fromItem = new R2OFromItem(alphaTB.toString(), R2OFromItem.FORM_QUERY);
		fromItem.setAlias(fromItem.generateAlias());
		result.addFrom(fromItem);	

		return result;
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
				boolean isTB = TranslatorUtility.isTripleBlock(bgp);
				isTB = false;
				logger.debug("isTB = " + isTB);
				if(isTB) {
					result = this.transTB(bgp, alphaGenerator, betaGenerator);
				} else {
					List<Triple> triples = bgp.getPattern().getList();
					List<Triple> gp1TripleList = triples.subList(0, 1);
					OpBGP gp1 = new OpBGP(BasicPattern.wrap(gp1TripleList));
					List<Triple> gp2TripleList = triples.subList(1, triples.size());
					OpBGP gp2 = new OpBGP(BasicPattern.wrap(gp2TripleList));

					result = this.transANDOPT(gp1, gp2, this.alphaGenerator, this.betaGenerator
							, R2OConstants.JOINS_TYPE_INNER);					
				}
			}
		} else if(op instanceof OpJoin) { // AND pattern
			logger.debug("op instanceof OpJoin");
			OpJoin opJoin = (OpJoin) op;
			Op opLeft = opJoin.getLeft();
			Op opRight = opJoin.getRight();
			result = this.transANDOPT(opLeft, opRight, this.alphaGenerator, this.betaGenerator
					, R2OConstants.JOINS_TYPE_INNER);
		} else if(op instanceof OpLeftJoin) { //OPT pattern
			logger.debug("op instanceof OpLeftJoin");
			OpLeftJoin opLeftJoin = (OpLeftJoin) op;
			Op opLeft = opLeftJoin.getLeft();
			Op opRight = opLeftJoin.getRight();
			result = this.transANDOPT(opLeft, opRight, this.alphaGenerator, this.betaGenerator
					, R2OConstants.JOINS_TYPE_LEFT);
		} else if(op instanceof OpUnion) { //UNION pattern
			logger.debug("op instanceof OpUnion");
			OpUnion opUnion = (OpUnion) op;
			Op opLeft = opUnion.getLeft();
			Op opRight = opUnion.getRight();
			result = this.transUNION(opLeft, opRight, alphaGenerator, betaGenerator);			
		} else {
			throw new R2OTranslationException("Unsupported query!");
		}

		return result;
	}

	public R2OQuery query2SQL(Query sparqlQuery) throws Exception  {
		R2OQuery sparql2sqlResult = null;

		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);

		Op opQuery = Algebra.compile(sparqlQuery) ;
		Op opQuery2 = Algebra.optimize(opQuery);


		TranslatorUtility tu = new TranslatorUtility(mappingDocument);
		this.mapNodeConceptMapping = tu.initializeMapConceptMapping(opQueryPattern);
		this.alphaGenerator = 
			new AlphaGenerator1(mapNodeConceptMapping, mappingDocument);
		this.betaGenerator = 
			new BetaGenerator1(mapNodeConceptMapping, this.mappingDocument);
		if(opQuery instanceof OpProject) {
			OpProject opProject = (OpProject) opQuery;
			Op opProjectSubOp = opProject.getSubOp();
			sparql2sqlResult = this.trans(opProjectSubOp, alphaGenerator, betaGenerator);
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

		logger.info("sparql2sql result = " + sparql2sqlResult.toString());
		return sparql2sqlResult;
	}



	private R2OQuery transTP(Triple tp, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator) 
	throws R2OTranslationException  {
		logger.info("tp = " + tp);
		R2OQuery query = new R2OQuery();
		query.addSelect(new Vector<ZSelectItem>());
		query.addFrom(new Vector<ZFromItem>());


		try {
			ZQuery alpha = alphaGenerator.calculateAlpha(tp);
			logger.debug("alpha(tp) = " + alpha);

			ZSelectItem betaSub = betaGenerator.calculateBeta(tp, POS.sub);
			logger.debug("beta(tp,sub) = " + betaSub); 
			ZSelectItem betaPre = betaGenerator.calculateBeta(tp, POS.pre);
			logger.debug("beta(tp,pre) = " + betaPre);
			ZSelectItem betaObj = betaGenerator.calculateBeta(tp, POS.obj);
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

	private R2OQuery transUNION(Op gp1, Op gp2, AbstractAlphaGenerator alphaGenerator, AbstractBetaGenerator betaGenerator)
	throws Exception  {
		Collection<Node> termsGP1 = TranslatorUtility.terms(gp1);
		Collection<Node> termsGP2 = TranslatorUtility.terms(gp2);
		Set<Node> termsA = new HashSet<Node>(termsGP1);termsA.removeAll(termsGP2);
		Set<Node> termsB = new HashSet<Node>(termsGP2);termsB.removeAll(termsGP1);
		Set<Node> termsC = new HashSet<Node>(termsGP1);termsC.retainAll(termsGP2);
		Collection<ZSelectItem> selectItemsA = this.generateSelectItems(termsA, null);
		Collection<ZSelectItem> selectItemsB = this.generateSelectItems(termsB, null);


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
		query1.addJoinQuery(joinQuery1);			

		
		Vector<ZSelectItem> selectItems1 = new Vector<ZSelectItem>();
		query1.addSelect(selectItems1);
		selectItems1.addAll(selectItemsA);
		selectItems1.addAll(selectItemsB);
		Collection<ZSelectItem> selectItemsC = this.generateSelectItems(termsC, transGP1Alias + ".");
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
		query2.addJoinQuery(joinQuery2);
		
		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		query2.addSelect(selectItems2);
		selectItems2.addAll(selectItemsA);
		selectItems2.addAll(selectItemsB);
		Collection<ZSelectItem> selectItemsC2 = this.generateSelectItems(termsC, transR3Alias + ".");
		selectItems2.addAll(selectItemsC2);
		
		logger.debug("query1 = " + query1);
		logger.debug("query2 = " + query2);

		R2OQuery result = query1;
		query1.addUnionQuery(query2);
		logger.debug("query1 UNION query2 = " + result);
		return result;
		
	}
	
	
	private R2OQuery transANDOPT(Op gp1, Op gp2, AbstractAlphaGenerator alphaGenerator
			, AbstractBetaGenerator betaGenerator
			, String joinType)
	throws Exception  {
		R2OQuery result = new R2OQuery();
		Vector<ZSelectItem> selectItems = new Vector<ZSelectItem>();
		
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
		if(joinOnExpression instanceof ZConstant) {
			joinQuery.setOnExpression(new ZExpression(joinOnExpression.toString()));
		} else {
			joinQuery.setOnExpression((ZExpression) joinOnExpression);
		}


		result.addJoinQuery(joinQuery);			


		logger.debug("selectItems = " + selectItems);
		result.addSelect(selectItems);




		return result;
	}


	enum POS {sub, pre, obj};



	private ZExp genCondSQL(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();

		ZExp betaSub = betaGenerator.calculateBeta(tp, POS.sub).getExpression();
		ZExp betaPre = betaGenerator.calculateBeta(tp, POS.pre).getExpression();
		ZExp betaObj = betaGenerator.calculateBeta(tp, POS.obj).getExpression();
		ZExpression betaObj2 = new ZExpression("IS NOT NULL");
		betaObj2.addOperand(betaObj);
		result = new ZExpression("AND", result, betaObj2);

		if(!subject.isVariable()) {
			ZExp exp = new ZExpression("="
					, betaSub
					, new ZConstant(subject.toString(), ZConstant.UNKNOWN));

			result = new ZExpression("AND", result, exp);
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
				exp = new ZExpression("="
						, betaObj
						, new ZConstant(object.getURI(), ZConstant.STRING));
			} else if(object.isLiteral()) {
				Object literalValue = object.getLiteralValue();
				if(literalValue instanceof String) {
					exp = new ZExpression("="
							, betaObj
							, new ZConstant(literalValue.toString(), ZConstant.STRING));					
				} else if (literalValue instanceof Double) {
					exp = new ZExpression("="
							, betaObj
							, new ZConstant(literalValue.toString(), ZConstant.NUMBER));
				} else {
					exp = new ZExpression("="
							, betaObj
							, new ZConstant(literalValue.toString(), ZConstant.STRING));					
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


	private Collection<ZSelectItem> genPRSQLTripleBlock(
			Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Collection<ZSelectItem> prList = new Vector<ZSelectItem>();
		Triple firstTriple = tripleBlock.iterator().next();
		Node subject = firstTriple.getSubject();
		ZSelectItem selectItem = 
			new ZSelectItem(betaGenerator.calculateBeta(firstTriple, POS.sub).toString());
		selectItem.setAlias(nameGenerator.generateName(subject));
		prList.add(selectItem);
		
		for(Triple tp : tripleBlock) {
			Node predicate = tp.getPredicate();
			Node object = tp.getObject();
			
			if(predicate != subject) {
				selectItem = new ZSelectItem(betaGenerator.calculateBeta(tp, POS.pre).toString());
				selectItem.setAlias(nameGenerator.generateName(predicate));
				prList.add(selectItem);
			}

			if(object != subject && object != predicate) {
				selectItem = new ZSelectItem(betaGenerator.calculateBeta(tp, POS.obj).toString());
				selectItem.setAlias(nameGenerator.generateName(object));
				prList.add(selectItem);
			}
		}
		

		return prList;
	}

	private Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();

		Collection<ZSelectItem> prList = new Vector<ZSelectItem>();
		ZSelectItem selectItem = 
			new ZSelectItem(betaGenerator.calculateBeta(tp, POS.sub).toString());
		selectItem.setAlias(nameGenerator.generateName(subject));
		prList.add(selectItem);

		if(predicate != subject) {
			selectItem = new ZSelectItem(betaGenerator.calculateBeta(tp, POS.pre).toString());
			selectItem.setAlias(nameGenerator.generateName(predicate));
			prList.add(selectItem);
		}

		if(object != subject && object != predicate) {
			selectItem = new ZSelectItem(betaGenerator.calculateBeta(tp, POS.obj).toString());
			selectItem.setAlias(nameGenerator.generateName(object));
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
			selectItem = new ZSelectItem(prefix + node.getName());
		} else if(node.isURI()){
			selectItem = new ZSelectItem(prefix + node.getLocalName());
		} else if(node.isLiteral()){
			selectItem = new ZSelectItem(prefix + node.toString());
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
		Collection<ZSelectItem> result = new HashSet<ZSelectItem>();

		for(Node node : nodes) {
			ZSelectItem selectItem = this.generateSelectItems(node, prefix);
			result.add(selectItem);
		}

		return result;
	}
	

}
