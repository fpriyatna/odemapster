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
	protected AbstractBetaGenerator betaGenerator;
	protected Map<Node, Set<AbstractConceptMapping>> mapInferredTypes;
	protected boolean ignoreRDFTypeStatement = false;


	public AbstractCondSQLGenerator(AbstractBetaGenerator betaGenerator,
			Map<Node, Set<AbstractConceptMapping>> mapInferredTypes2) {
		super();
		this.betaGenerator = betaGenerator;
		this.mapInferredTypes = mapInferredTypes2;
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

	private Collection<ZExpression> genCondSQL(Triple tp1, Triple tp2) throws Exception {
		Collection<ZExpression> exps = new HashSet<ZExpression>();
		
		Node tp1Subject = tp1.getSubject();
		Node tp1Predicate = tp1.getPredicate();
		Node tp1Object = tp1.getObject();
		Node tp2Subject = tp2.getSubject();
		Node tp2Predicate = tp2.getPredicate();
		Node tp2Object = tp2.getObject();

		ZExp betaSub1 = betaGenerator.calculateBeta(tp1, POS.sub).getExpression();
		ZExp betaPre1 = betaGenerator.calculateBeta(tp1, POS.pre).getExpression();
		ZExp betaObj1;
		ZSelectItem betaObject1 = betaGenerator.calculateBeta(tp1, POS.obj);
		if(betaObject1.isExpression()) {
			betaObj1 = betaObject1.getExpression();
		} else {
			betaObj1 = new ZConstant(betaObject1.toString(), ZConstant.COLUMNNAME);
		}

		ZExp betaSub2 = betaGenerator.calculateBeta(tp2, POS.sub).getExpression();
		ZExp betaPre2 = betaGenerator.calculateBeta(tp2, POS.pre).getExpression();
		ZExp betaObj2;
		ZSelectItem betaObject2 = betaGenerator.calculateBeta(tp2, POS.obj);
		if(betaObject2.isExpression()) {
			betaObj2 = betaObject2.getExpression();
		} else {
			betaObj2 = new ZConstant(betaObject2.toString(), ZConstant.COLUMNNAME);
		}
		
		if(tp1Subject.toString().equals(tp2Predicate.toString())) {
			ZExpression exp = new ZExpression("="
					, betaSub1
					, betaPre2);
			exps.add(exp);			
		}

		if(tp1Subject.toString().equals(tp2Object.toString())) {
			ZExpression exp = new ZExpression("="
					, betaSub1
					, betaObj2);
			exps.add(exp);			
		}

		if(tp1Predicate.toString().equals(tp2Object.toString())) {
			ZExpression exp = new ZExpression("="
					, betaPre1
					, betaObj2);
			exps.add(exp);			
		}
		
		if(tp1Object.toString().equals(tp2Predicate.toString())) {
			ZExpression exp = new ZExpression("="
					, betaObj1
					, betaPre2);
			exps.add(exp);			
		}

		if(tp1Object.toString().equals(tp2Object.toString())) {
			ZExpression exp = new ZExpression("="
					, betaObj1
					, betaObj2);
			exps.add(exp);			
		}
		

		
		return exps;
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

		ZSelectItem betaSubject = betaGenerator.calculateBeta(tp, POS.sub);
		ZSelectItem betaPredicate = betaGenerator.calculateBeta(tp, POS.pre);
		ZSelectItem betaObject = betaGenerator.calculateBeta(tp, POS.obj);
		ZExp betaSub = betaGenerator.calculateBeta(tp, POS.sub).getExpression();
		ZExp betaPre = betaGenerator.calculateBeta(tp, POS.pre).getExpression();
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
			boolean isSingleTripleFromTripleBlock = false;
			
			if(tp instanceof ExtendedTriple) {
				ExtendedTriple etp = (ExtendedTriple) tp;
				if(etp.isSingleTripleFromTripleBlock()) {
					isSingleTripleFromTripleBlock = true;
				} 
			} 

			if(!isSingleTripleFromTripleBlock) {
				ZExpression exp = new ZExpression("IS NOT NULL");
				exp.addOperand(betaObj);
				exps.add(exp);
				result = new ZExpression("AND", result, exp);
			}
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
		ZSelectItem betaCMSelectItem = betaGenerator.calculateBeta(tp, POS.sub);
		ZExp betaSub = betaCMSelectItem.getExpression();

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
	
	public ZExpression genCondSQLTB(List<Triple> tripleBlock) throws Exception {
		//List<ZExpression> condSQLTB = new ArrayList<ZExpression>();
		
		ZExp result = new ZConstant("TRUE", ZConstant.UNKNOWN);
		Collection<ZExpression> exps = new HashSet<ZExpression>();
		Triple firstTriple = tripleBlock.iterator().next();
		ZExpression condSubject = this.genCondSQLSubject(
				firstTriple, this.betaGenerator);
		if(condSubject != null) {
			exps.add(condSubject);
			//condSQLTB.add(condSubject);
		} 
		
		for(int i=0; i<tripleBlock.size(); i++) {
			Triple tp = tripleBlock.get(i);
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

				for(int j=i+1; j<tripleBlock.size(); j++) {
					Triple tp2 = tripleBlock.get(j);
					String tp2PredicateURI = tp2.getPredicate().getURI();
					boolean isRDFTypeStatement2 = RDF.type.getURI().equals(tp2PredicateURI);
					if(this.ignoreRDFTypeStatement && isRDFTypeStatement2) {
						
					} else {
						Collection<ZExpression> exps2 = this.genCondSQL(tp, tp2);
						exps.addAll(exps2);
					}
				}
			}
			

		}
		
//		for(Triple tp : tripleBlock) {
//			String tpPredicateURI = tp.getPredicate().getURI();
//			boolean isRDFTypeStatement = RDF.type.getURI().equals(tpPredicateURI);
//			if(this.ignoreRDFTypeStatement && isRDFTypeStatement) {
//				//do nothing
//			} else {
//				ZExpression condPredicateObject = 
//						this.genCondSQLPredicateObject(tp, this.betaGenerator);
//				//condSQLTB.add(condPredicateObject);
//				if(condPredicateObject != null) {
//					exps.add(condPredicateObject);
//				}
//				result = new ZExpression("AND", result, condPredicateObject);				
//			}
//		}

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
