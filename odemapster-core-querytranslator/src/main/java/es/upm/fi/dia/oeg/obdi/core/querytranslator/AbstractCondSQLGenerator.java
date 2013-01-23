package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;

public abstract class AbstractCondSQLGenerator {
	private static Logger logger = Logger.getLogger(AbstractCondSQLGenerator.class);
	//protected AbstractBetaGenerator betaGenerator;
	protected boolean ignoreRDFTypeStatement = false;
	protected AbstractQueryTranslator owner;

	public AbstractCondSQLGenerator(AbstractQueryTranslator owner) {
		super();
		this.owner = owner;
	}

	public CondSQLResult genCondSQL(Triple tp
			, AlphaResult alphaResult, BetaResult betaResult
			, AbstractConceptMapping cm) throws Exception {
		ZExpression condSQLSubject = this.genCondSQLSubject(tp, alphaResult, betaResult, cm);
		ZExpression condSQLPredicateObject = this.genCondSQLPredicateObject(tp, alphaResult, betaResult, cm);
		ZExpression condSQL = null;
		if(condSQLSubject == null && condSQLPredicateObject==null) {
			condSQL = null;
		} else if(condSQLSubject != null && condSQLPredicateObject==null) {
			condSQL = condSQLSubject;
		} else if(condSQLSubject == null && condSQLPredicateObject!=null) {
			condSQL = condSQLPredicateObject;
		} else {
			condSQL = new ZExpression("AND", condSQLSubject, condSQLPredicateObject);
		}

		logger.debug("genCondSQL = " + condSQL);
		return new CondSQLResult(condSQL);
	}

	private ZExpression genCondSQL(Triple tp1, BetaResult betaResult1
			, Triple tp2, BetaResult betaResult2
			, AlphaResult alphaResult 
			) throws Exception {
		Collection<ZExpression> exps = new HashSet<ZExpression>();
		
		Node tp1Subject = tp1.getSubject();
		Node tp1Predicate = tp1.getPredicate();
		Node tp1Object = tp1.getObject();
		Node tp2Subject = tp2.getSubject();
		Node tp2Predicate = tp2.getPredicate();
		Node tp2Object = tp2.getObject();

		//ZExp betaSub1 = betaGenerator.calculateBeta(tp1, POS.sub, predicateURI1).getExpression();
		//ZExp betaPre1 = betaGenerator.calculateBeta(tp1, POS.pre).getExpression();
		//ZSelectItem betaObj1 = betaGenerator.calculateBeta(tp1, POS.obj);
		ZExp betaSub1Exp = betaResult1.getBetaSub().getExpression();
		ZExp betaPre1Exp = betaResult1.getBetaPre().getExpression();
		ZExp betaObj1Exp;
		ZSelectItem betaObj1 = betaResult1.getBetaObj();
		if(betaObj1.isExpression()) {
			betaObj1Exp = betaObj1.getExpression();
		} else {
			betaObj1Exp = new ZConstant(betaObj1.toString(), ZConstant.COLUMNNAME);
		}

//		ZExp betaSub2Exp = betaGenerator.calculateBeta(tp2, POS.sub).getExpression();
//		ZExp betaPre2Exp = betaGenerator.calculateBeta(tp2, POS.pre).getExpression();
//		ZSelectItem betaObj2 = betaGenerator.calculateBeta(tp2, POS.obj);
		ZExp betaSub2Exp = betaResult2.getBetaSub().getExpression();
		ZExp betaPre2Exp = betaResult2.getBetaPre().getExpression();
		ZSelectItem betaObj2 = betaResult2.getBetaObj();
		ZExp betaObj2Exp;
		if(betaObj2.isExpression()) {
			betaObj2Exp = betaObj2.getExpression();
		} else {
			betaObj2Exp = new ZConstant(betaObj2.toString(), ZConstant.COLUMNNAME);
		}
		
		if(tp1Subject.toString().equals(tp2Predicate.toString())) {
			ZExpression exp = new ZExpression("="
					, betaSub1Exp
					, betaPre2Exp);
			exps.add(exp);			
		}

		if(tp1Subject.toString().equals(tp2Object.toString())) {
			ZExpression exp = new ZExpression("="
					, betaSub1Exp
					, betaObj2Exp);
			exps.add(exp);			
		}

		if(tp1Predicate.toString().equals(tp2Object.toString())) {
			ZExpression exp = new ZExpression("="
					, betaPre1Exp
					, betaObj2Exp);
			exps.add(exp);			
		}
		
		if(tp1Object.toString().equals(tp2Predicate.toString())) {
			ZExpression exp = new ZExpression("="
					, betaObj1Exp
					, betaPre2Exp);
			exps.add(exp);			
		}

		if(tp1Object.toString().equals(tp2Object.toString())) {
			ZExpression exp = new ZExpression("="
					, betaObj1Exp
					, betaObj2Exp);
			exps.add(exp);			
		}
		

		ZExpression result = QueryTranslatorUtility.combineExpresions(exps);
		return result;
	}
	


	protected ZExpression genCondSQLPredicateObject(Triple tp
			,AlphaResult alphaResult, BetaResult betaResult
			, AbstractConceptMapping cm) throws Exception {
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Collection<ZExpression> exps = new HashSet<ZExpression>();

		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		//R2OConceptMapping cm = this.mapTripleCM.get(tp);

//		ZSelectItem betaSubject = betaGenerator.calculateBeta(tp, POS.sub);
//		ZSelectItem betaPredicate = betaGenerator.calculateBeta(tp, POS.pre);
//		ZSelectItem betaObject = betaGenerator.calculateBeta(tp, POS.obj);
		ZSelectItem betaSubject = betaResult.getBetaSub();
		ZSelectItem betaPredicate = betaResult.getBetaPre();
		ZSelectItem betaObject = betaResult.getBetaObj();
		
		ZExp betaSubjectExpression = betaSubject.getExpression();
		ZExp betaPredicateExpression = betaPredicate.getExpression();
		ZExp betaObjectExpression;
		if(betaObject.isExpression()) {
			betaObjectExpression = betaObject.getExpression();
		} else {
			betaObjectExpression = new ZConstant(betaObject.toString(), ZConstant.COLUMNNAME);
		}

		if(!predicate.isVariable()) { //line 08
			ZExp exp = new ZExpression("="
					, betaPredicateExpression
					, new ZConstant(predicate.toString(), ZConstant.STRING));
		}

		if(!object.isVariable()) { //line 09
			ZExp exp = null;

			if(object.isURI()) {
				ZConstant objConstant = new ZConstant(object.getURI(), ZConstant.STRING);
				exp = new ZExpression("=", betaObjectExpression, objConstant);
			} else if(object.isLiteral()) {
				Object literalValue = object.getLiteralValue();
				if(literalValue instanceof String) {
					ZConstant objConstant = new ZConstant(literalValue.toString(), ZConstant.STRING);
					exp = new ZExpression("=", betaObjectExpression, objConstant);					
				} else if (literalValue instanceof Double) {
					ZConstant objConstant = new ZConstant(literalValue.toString(), ZConstant.NUMBER);
					exp = new ZExpression("=", betaObjectExpression, objConstant);
				} else {
					ZConstant objConstant = new ZConstant(literalValue.toString(), ZConstant.STRING);
					exp = new ZExpression("="
							, betaObjectExpression
							, objConstant);					
				}
			}

			if(exp != null) {
				//result = new ZExpression("AND", result, exp);
			}

		} else { // improvement by Freddy
			boolean isSingleTripleFromTripleBlock = false;
			
			if(tp instanceof ExtendedTriple) {
				ExtendedTriple etp = (ExtendedTriple) tp;
				if(etp.isSingleTripleFromTripleBlock()) {
					isSingleTripleFromTripleBlock = true;
				} 
			} 

			//for deadling with unbound() function, we should remove this part
			if(!isSingleTripleFromTripleBlock) {
				ZExpression exp = new ZExpression("IS NOT NULL");
				exp.addOperand(betaObjectExpression);
				exps.add(exp);
				result = new ZExpression("AND", result, exp);
			}
		}

		if(subject == predicate) { //line 10
			ZExpression exp = new ZExpression("="
					, betaSubjectExpression
					, betaPredicateExpression);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);
		}

		if(subject == object) { //line 11
			ZExpression exp = new ZExpression("="
					, betaSubjectExpression
					, betaObjectExpression);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);
		}

		if(object == predicate) { //line 12
			ZExpression exp = new ZExpression("="
					, betaObjectExpression
					, betaPredicateExpression);
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
	
	
	protected ZExpression genCondSQLSubject(Triple tp, AlphaResult alphaResult 
			, BetaResult betaResult, AbstractConceptMapping cm) throws Exception {
		ZExpression exp = null;
		Node subject = tp.getSubject();
		//ZSelectItem betaCMSelectItem = betaGenerator.calculateBeta(tp, POS.sub);
		ZSelectItem betaSubject = betaResult.getBetaSub();
		ZExp betaSubjectExpression = betaSubject.getExpression();

		if(!subject.isVariable()) {

			if(subject.isURI()) {
//				exp = new ZExpression("="
//						, betaSub
//						, new ZConstant(subject.toString(), ZConstant.STRING));
				//we handle this in R2RMLCondSQLGenerator
			} else if(subject.isLiteral()) {
				logger.warn("Literal as subject is not supported!");
				Object literalValue = subject.getLiteralValue();
				if(literalValue instanceof String) {
					exp = new ZExpression("="
							, betaSubjectExpression
							, new ZConstant(subject.toString(), ZConstant.STRING));				
				} else if (literalValue instanceof Double) {
					exp = new ZExpression("="
							, betaSubjectExpression
							, new ZConstant(subject.toString(), ZConstant.NUMBER));
				} else {
					exp = new ZExpression("="
							, betaSubjectExpression
							, new ZConstant(subject.toString(), ZConstant.STRING));				
				}
			}

		}

		return exp;

	}
	
	public ZExpression genCondSQLSTG(List<Triple> stg
			, List<AlphaResultUnion> stgAlphaResults, List<BetaResultSet> stgBetaResults
			, AbstractConceptMapping cm) 
					throws Exception {
		
		Collection<ZExpression> exps = new HashSet<ZExpression>();
		Triple firstTriple = stg.get(0);
		AlphaResultUnion firstTripleAlphaResultUnion = stgAlphaResults.get(0);
		BetaResultSet firstTripleBetaResultSet = stgBetaResults.get(0);
		if(firstTripleBetaResultSet.size() > 1) {
			String errorMessage = "Multiple beta is not permitted in triple : " + firstTriple;
			logger.warn(errorMessage);
		}
		AlphaResult firstTripleAlphaResult = firstTripleAlphaResultUnion.get(0);
		BetaResult firstTripleBetaResult = firstTripleBetaResultSet.get(0);
		ZExpression condSubject = this.genCondSQLSubject(firstTriple
				, firstTripleAlphaResult, firstTripleBetaResult, cm);
		if(condSubject != null) {
			exps.add(condSubject);
			//condSQLTB.add(condSubject);
		} 
		
		
		
		for(int i=0; i<stg.size(); i++) {
			Triple iTP = stg.get(i);
			AlphaResultUnion alphaResultUnion = stgAlphaResults.get(i);
			BetaResultSet betaResultSet = stgBetaResults.get(i);
			if(betaResultSet.size() > 1) {
				String errorMessage = "Multiple beta is not permitted in triple : " + iTP;
				logger.warn(errorMessage);
			}
			AlphaResult iAlphaResult = alphaResultUnion.get(0);
			BetaResult iBetaResult = betaResultSet.get(0);
			
			Node iTPPredicate = iTP.getPredicate();
			if(iTPPredicate.isVariable()) {
				String errorMessage = "Unbounded predicate is not permitted in triple : " + iTP;
				logger.warn(errorMessage);
			}
			
			if(iTPPredicate.isURI() && this.ignoreRDFTypeStatement 
					&& RDF.type.getURI().equals(iTPPredicate.getURI())) {
				//do nothing
			} else {
				ZExpression condPredicateObject = this.genCondSQLPredicateObject(
						iTP, iAlphaResult, iBetaResult, cm);
				//condSQLTB.add(condPredicateObject);
				if(condPredicateObject != null) {
					exps.add(condPredicateObject);
				}

				for(int j=i+1; j<stg.size(); j++) {
					Triple jTP = stg.get(j);
					BetaResultSet jBetaResultSet = stgBetaResults.get(j);
					if(jBetaResultSet.size() > 1) {
						String errorMessage = "Multiple beta is not permitted in triple : " + jTP;
						logger.warn(errorMessage);
					}
					BetaResult jBetaResult = jBetaResultSet.get(0);
					
					Node jTPPredicate = jTP.getPredicate();
					if(jTPPredicate.isVariable()) {
						String errorMessage = "Unbounded predicate is not permitted in triple : " + jTP;
						logger.warn(errorMessage);
					}
					
					if(jTPPredicate.isURI() &&  this.ignoreRDFTypeStatement 
							&& RDF.type.getURI().equals(jTPPredicate.getURI())) {
						
					} else {
						ZExpression expsPredicateObject = this.genCondSQL(
								iTP, iBetaResult, jTP, jBetaResult, iAlphaResult);
						if(expsPredicateObject != null) {
							exps.add(expsPredicateObject);	
						}
						
					}
				}
			}
			

		}
		
		ZExpression result = QueryTranslatorUtility.combineExpresions(exps);
		logger.debug("genCondSQLTB = " + result);
		return result;

	}

	public boolean isIgnoreRDFTypeStatement() {
		return ignoreRDFTypeStatement;
	}

	public void setIgnoreRDFTypeStatement(boolean ignoreRDFTypeStatement) {
		this.ignoreRDFTypeStatement = ignoreRDFTypeStatement;
	}
	
}
