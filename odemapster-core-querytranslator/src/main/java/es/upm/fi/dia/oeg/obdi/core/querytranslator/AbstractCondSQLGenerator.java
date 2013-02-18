package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.exception.InsatisfiableSQLExpression;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
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
			, AlphaResult alphaResult, AbstractBetaGenerator betaGenerator
			, AbstractConceptMapping cm, String predicateURI) throws QueryTranslationException {

		ZExpression condSQLSubject = this.genCondSQLSubject(
				tp, alphaResult, betaGenerator, cm);

		ZExpression condSQLPredicateObject = null;
		try {
			condSQLPredicateObject = this.genCondSQLPredicateObject(
					tp, alphaResult, betaGenerator, cm, predicateURI);			
		} catch(Exception e) {
			logger.error(e.getMessage());
		}

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



	public ZExpression generateIsNotNullExpression(ZExp betaObjectExpression) {
		ZExpression exp = new ZExpression("IS NOT NULL");
		exp.addOperand(betaObjectExpression);
		return exp;
	}

	protected ZExpression genCondSQLPredicateObject(Triple tp
			,AlphaResult alphaResult, AbstractBetaGenerator betaGenerator
			, AbstractConceptMapping cm, String predicateURI) throws QueryTranslationException, InsatisfiableSQLExpression {
		Collection<ZExpression> exps = new HashSet<ZExpression>();

		Collection<AbstractPropertyMapping> pms = 
				cm.getPropertyMappings(predicateURI);
		if(pms == null || pms.size() == 0) {
			String errorMessage = "No mappings found for predicate : " + predicateURI;
			throw new QueryTranslationException(errorMessage);
		}
		if(pms.size() > 1) {
			String errorMessage = "Multiple mappings are not permitted for predicate " + predicateURI;
			throw new QueryTranslationException(errorMessage);
		}		
		AbstractPropertyMapping pm = pms.iterator().next();
		ZExpression result1 = this.genCondSQLPredicateObject(
				tp, alphaResult, betaGenerator, cm, pm);
		if(result1 != null) {
			exps.add(result1);	
		}
		
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		ZSelectItem betaSubject = betaGenerator.calculateBetaSubject(cm, alphaResult);
		ZSelectItem betaPredicate = betaGenerator.calculateBetaPredicate(predicateURI);
		ZSelectItem betaObject = betaGenerator.calculateBetaObject(
				tp, cm, predicateURI, alphaResult);
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
				ZExpression exp = this.generateIsNotNullExpression(betaObjectExpression);
				if(exp != null) {
					exps.add(exp);
				}

			}
		}

		if(subject == predicate) { //line 10
			ZExpression exp = new ZExpression("="
					, betaSubjectExpression
					, betaPredicateExpression);
			exps.add(exp);
		}

		if(subject == object) { //line 11
			ZExpression exp = new ZExpression("="
					, betaSubjectExpression
					, betaObjectExpression);
			exps.add(exp);
		}

		if(object == predicate) { //line 12
			ZExpression exp = new ZExpression("="
					, betaObjectExpression
					, betaPredicateExpression);
			exps.add(exp);
		}

		ZExpression resultFinal = QueryTranslatorUtility.combineExpresions(exps);
		return resultFinal;
	}


	protected ZExpression genCondSQLSubject(Triple tp, AlphaResult alphaResult 
			, AbstractBetaGenerator betaGenerator, AbstractConceptMapping cm) throws QueryTranslationException {
		ZExpression result1 = null;
		Node subject = tp.getSubject();
		//ZSelectItem betaCMSelectItem = betaGenerator.calculateBeta(tp, POS.sub);
		ZSelectItem betaSubject = betaGenerator.calculateBetaSubject(cm, alphaResult);
		ZExp betaSubjectExpression = betaSubject.getExpression();

		if(!subject.isVariable()) {
			if(subject.isURI()) {
				Node tpSubject = tp.getSubject();
				result1 = this.genCondSQLSubjectURI(tpSubject, alphaResult, cm);
			} else if(subject.isLiteral()) {
				logger.warn("Literal as subject is not supported!");
				Object literalValue = subject.getLiteralValue();
				if(literalValue instanceof String) {
					result1 = new ZExpression("="
							, betaSubjectExpression
							, new ZConstant(subject.toString(), ZConstant.STRING));				
				} else if (literalValue instanceof Double) {
					result1 = new ZExpression("="
							, betaSubjectExpression
							, new ZConstant(subject.toString(), ZConstant.NUMBER));
				} else {
					result1 = new ZExpression("="
							, betaSubjectExpression
							, new ZConstant(subject.toString(), ZConstant.STRING));				
				}
			}

		}

		return result1;

	}

	protected abstract ZExpression genCondSQLSubjectURI(Node tpSubject,
			AlphaResult alphaResult, AbstractConceptMapping cm) 
					throws QueryTranslationException;

	public ZExpression genCondSQLSTG(List<Triple> stg
			, AlphaResult alphaResult, AbstractBetaGenerator betaGenerator
			, AbstractConceptMapping cm) 
					throws Exception {

		Collection<ZExpression> exps = new HashSet<ZExpression>();
		Triple firstTriple = stg.get(0);

		ZExpression condSubject = this.genCondSQLSubject(firstTriple
				, alphaResult, betaGenerator, cm);
		if(condSubject != null) {
			exps.add(condSubject);
			//condSQLTB.add(condSubject);
		} 

		for(int i=0; i<stg.size(); i++) {
			Triple iTP = stg.get(i);

			Node iTPPredicate = iTP.getPredicate();
			if(!iTPPredicate.isURI()) {
				String errorMessage = "Only bounded predicate is not supported in triple : " + iTP;
				logger.warn(errorMessage);
				throw new QueryTranslationException(errorMessage);
			}
			String iTPPredicateURI = iTPPredicate.getURI();
			if(this.ignoreRDFTypeStatement && RDF.type.getURI().equals(iTPPredicateURI)) {
				//do nothing
			} else {
				ZExpression condPredicateObject = this.genCondSQLPredicateObject(
						iTP, alphaResult, betaGenerator, cm, iTPPredicateURI);
				//condSQLTB.add(condPredicateObject);
				if(condPredicateObject != null) {
					exps.add(condPredicateObject);
				}

				for(int j=i+1; j<stg.size(); j++) {
					Triple jTP = stg.get(j);

					Node jTPPredicate = jTP.getPredicate();
					if(jTPPredicate.isVariable()) {
						String errorMessage = "Unbounded predicate is not permitted in triple : " + jTP;
						logger.warn(errorMessage);
					}

					if(jTPPredicate.isURI() &&  this.ignoreRDFTypeStatement 
							&& RDF.type.getURI().equals(jTPPredicate.getURI())) {

					} else {
						ZExpression expsPredicateObject = this.genCondSQL(
								iTP, jTP, alphaResult, betaGenerator, cm);
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

	private ZExpression genCondSQL(Triple tp1, Triple tp2, 
			AlphaResult alphaResult, AbstractBetaGenerator betaGenerator, AbstractConceptMapping cm
			) throws Exception {
		Collection<ZExpression> exps = new HashSet<ZExpression>();

		Node tp1Subject = tp1.getSubject();
		Node tp1Predicate = tp1.getPredicate();
		Node tp1Object = tp1.getObject();
		ZExp betaSub1Exp = betaGenerator.calculateBetaSubject(cm, alphaResult).getExpression();
		ZExp betaPre1Exp = betaGenerator.calculateBetaPredicate(
				tp1Predicate.getURI()).getExpression();
		ZExp betaObj1Exp;
		ZSelectItem betaObj1 = betaGenerator.calculateBetaObject(
				tp1, cm, tp1Predicate.getURI(), alphaResult);
		if(betaObj1.isExpression()) {
			betaObj1Exp = betaObj1.getExpression();
		} else {
			betaObj1Exp = new ZConstant(betaObj1.toString(), ZConstant.COLUMNNAME);
		}

		Node tp2Predicate = tp2.getPredicate();
		Node tp2Object = tp2.getObject();
		ZExp betaPre2Exp = betaGenerator.calculateBetaPredicate(tp2Predicate.getURI()).getExpression();
		ZSelectItem betaObj2 = betaGenerator.calculateBetaObject(tp2, cm, tp2Predicate.getURI(), alphaResult);
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

	protected abstract ZExpression genCondSQLPredicateObject(Triple tp,
			AlphaResult alphaResult, AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cm, AbstractPropertyMapping pm)
					throws QueryTranslationException, InsatisfiableSQLExpression;

}
