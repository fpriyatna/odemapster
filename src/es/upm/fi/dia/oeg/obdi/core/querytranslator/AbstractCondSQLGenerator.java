package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
	protected AbstractBetaGenerator betaGenerator;
	protected Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes;
	protected boolean ignoreRDFTypeStatement = false;


	public AbstractCondSQLGenerator(AbstractBetaGenerator betaGenerator,
			Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes) {
		super();
		this.betaGenerator = betaGenerator;
		this.mapInferredTypes = mapInferredTypes;
	}

	public ZExpression genCondSQL(Triple tp) throws Exception {
		ZExpression condSQLSubject = this.genCondSQLSubject(tp, betaGenerator);
		ZExpression condSQL = this.genCondSQLPredicateObject(tp, betaGenerator);
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

		logger.debug("genCondSQL = " + condSQL2);
		return condSQL2;
	}

	protected abstract ZExpression genCondSQLPredicateObject(Triple tp
			, AbstractBetaGenerator betaGenerator) throws Exception;

	protected ZExpression genCondSQLPredicateObject(Triple tp
			, AbstractBetaGenerator betaGenerator, AbstractConceptMapping cm) throws Exception {
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Collection<ZExpression> exps = new HashSet<ZExpression>();

		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		//R2OConceptMapping cm = this.mapTripleCM.get(tp);

		ZSelectItem betaSubject = betaGenerator.calculateBeta(tp, POS.sub, cm);
		ZSelectItem betaPredicate = betaGenerator.calculateBeta(tp, POS.pre, cm);
		ZSelectItem betaObject = betaGenerator.calculateBeta(tp, POS.obj, cm);
		ZExp betaSub = betaGenerator.calculateBeta(tp, POS.sub, cm).getExpression();
		ZExp betaPre = betaGenerator.calculateBeta(tp, POS.pre, cm).getExpression();
		ZExp betaObj;
		if(betaObject.isExpression()) {
			betaObj = betaObject.getExpression();
		} else {
			betaObj = new ZConstant(betaObject.toString(), ZConstant.COLUMNNAME);
		}

		if(!predicate.isVariable()) { //line 08
			ZExp exp = new ZExpression("="
					, betaPre
					, new ZConstant(predicate.toString(), ZConstant.STRING));
		}

		if(!object.isVariable()) { //line 09
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

		} else { // improvement by Freddy
			ZExpression exp = new ZExpression("IS NOT NULL");
			exp.addOperand(betaObj);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);

		}

		if(subject == predicate) { //line 10
			ZExpression exp = new ZExpression("="
					, betaSub
					, betaPre);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);
		}

		if(subject == object) { //line 11
			ZExpression exp = new ZExpression("="
					, betaSub
					, betaObj);
			exps.add(exp);
			result = new ZExpression("AND", result, exp);
		}

		if(object == predicate) { //line 12
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
	protected abstract ZExpression genCondSQLSubject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception;
	protected ZExpression genCondSQLSubject(Triple tp, AbstractBetaGenerator betaGenerator, AbstractConceptMapping cm) throws Exception {
		ZExpression exp = null;
		Node subject = tp.getSubject();
		ZSelectItem betaCMSelectItem = betaGenerator.calculateBeta(tp, POS.sub, cm);
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
	
	public ZExpression genCondSQLTB(Collection<Triple> tripleBlock) throws Exception {
		//List<ZExpression> condSQLTB = new ArrayList<ZExpression>();
		
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Collection<ZExpression> exps = new HashSet<ZExpression>();

		ZExpression condSubject = this.genCondSQLSubject(tripleBlock.iterator().next(), this.betaGenerator);
		if(condSubject != null) {
			exps.add(condSubject);
			//condSQLTB.add(condSubject);
		} 
		
		for(Triple tp : tripleBlock) {
			String tpPredicateURI = tp.getPredicate().getURI();
			boolean isRDFTypeStatement = RDF.type.getURI().equals(tpPredicateURI);
			if(this.ignoreRDFTypeStatement && isRDFTypeStatement) {
				//do nothing
			} else {
				ZExpression condPredicateObject = 
						this.genCondSQLPredicateObject(tp, this.betaGenerator);
				//condSQLTB.add(condPredicateObject);
				if(condPredicateObject != null) {
					exps.add(condPredicateObject);
				}
				result = new ZExpression("AND", result, condPredicateObject);				
			}

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

	public boolean isIgnoreRDFTypeStatement() {
		return ignoreRDFTypeStatement;
	}

	public void setIgnoreRDFTypeStatement(boolean ignoreRDFTypeStatement) {
		this.ignoreRDFTypeStatement = ignoreRDFTypeStatement;
	}
	
}
