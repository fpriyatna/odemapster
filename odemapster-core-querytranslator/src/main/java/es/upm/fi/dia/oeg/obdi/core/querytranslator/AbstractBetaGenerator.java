package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;


public abstract class AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(AbstractBetaGenerator.class);
	protected AbstractQueryTranslator owner;

	public AbstractBetaGenerator(AbstractQueryTranslator owner) {
		super();
		this.owner = owner;
	}

	
//	private BetaResult calculateBeta(Triple tp, String predicateURI) throws Exception {
//		Node tpSubject = tp.getSubject();
//		Node tpObject = tp.getObject();
//		
//		Collection<AbstractConceptMapping> cms = 
//				this.mapNodeConceptMapping.get(tpSubject);
//		if(cms == null) {
//			String errorMessage = "No maps found for subject " + tpSubject;
//			throw new QueryTranslationException(errorMessage);				
//		}
//		if(cms.size() > 1) {
//			String errorMessage = "Multiple maps is not supported for for subject ";
//			throw new QueryTranslationException(errorMessage);				
//		}
//		AbstractConceptMapping cm = cms.iterator().next();
//		
//
//		
//		BetaResult betaResult = this.calculateBeta(tp, cm, predicateURI);
//		return betaResult;
//	}
	
	List<BetaResultSet> calculateBetaSTG(List<Triple> stg, AbstractConceptMapping cm, List<AlphaResultUnion> listofAlphaResultUnion) throws Exception {
		List<BetaResultSet> result = new Vector<BetaResultSet>();
		for(int i=0; i<stg.size(); i++) {
			Triple tp = stg.get(i);
			AlphaResultUnion alphaResultUnion = listofAlphaResultUnion.get(i);
			BetaResultSet betaResult = this.calculateBeta(tp, cm, alphaResultUnion);
			result.add(betaResult);
		}
		return result;
	}
	
	public BetaResultSet calculateBeta(Triple tp, AbstractConceptMapping cm, AlphaResultUnion alphaResultSet) throws Exception {
		BetaResultSet result = null;
		Node tpSubject = tp.getSubject();
		Node tpPredicate = tp.getPredicate();
		Node tpObject = tp.getObject();
		
		if(tpPredicate.isURI()) {
			String predicateURI = tp.getPredicate().getURI();
			AlphaResult alphaResult = alphaResultSet.get(0);
			BetaResult betaResult = this.calculateBeta(tp, cm, predicateURI, alphaResult);
			result = new BetaResultSet(betaResult);
		} else if(tpPredicate.isVariable()) {
			Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings();
			Iterator<AbstractPropertyMapping> pmsIterator = pms.iterator();
			List<BetaResult> betaResults = new Vector<BetaResult>();
			while(pmsIterator.hasNext()) {
				AbstractPropertyMapping pm = pmsIterator.next();
				String predicateURI = pm.getMappedPredicateName();
				AlphaResult alphaResult = alphaResultSet.get(predicateURI);
				BetaResult betaResult = this.calculateBeta(tp, cm, predicateURI, alphaResult);
				betaResults.add(betaResult);
			}
			result = new BetaResultSet(betaResults);
			
		} else {
			String errorMessage = "Predicate has to be either an URI or a variable";
			throw new QueryTranslationException(errorMessage);
		}
		
		return result;
	}
	
	private BetaResult calculateBeta(Triple tp, AbstractConceptMapping cm
			, String predicateURI, AlphaResult alphaResult) throws Exception {
		SQLSelectItem betaSubject = this.calculateBetaSubject(tp, cm, alphaResult);
		SQLSelectItem betaPredicate = this.calculateBetaPredicate(predicateURI);
		SQLSelectItem betaObject;
		boolean predicateIsRDFSType = RDF.type.getURI().equals(predicateURI);
		if(predicateIsRDFSType) {
			ZConstant className = new ZConstant(cm.getConceptName(), ZConstant.STRING);
			betaObject = new SQLSelectItem();betaObject.setExpression(className);
		} else {
			betaObject = this.calculateBetaObject(tp, cm, predicateURI, alphaResult);	
		}
		
		BetaResult betaResult = new BetaResult(betaSubject, betaPredicate, betaObject, predicateURI);
		return betaResult;
	}
	
//	public List<ZSelectItem> calculateBeta(Triple tp, POS pos) throws Exception {
//		List<ZSelectItem> result = new Vector<ZSelectItem>();
//		
//		Node tpSubject = tp.getSubject();
//		Collection<AbstractConceptMapping> cms = 
//				this.mapNodeConceptMapping.get(tpSubject);
//		AbstractConceptMapping cm = cms.iterator().next();
//		result = this.calculateBeta(tp, pos, cm);
//		
//		return result;
//	}
	
//	private List<ZSelectItem> calculateBeta(Triple tp, POS pos
//			, AbstractConceptMapping cm)
//	throws Exception {
//		List<ZSelectItem> result = new Vector<ZSelectItem>();
//		
//		Node tpPredicate = tp.getPredicate();
//		if(tpPredicate.isURI()) {
//			String predicateURI = tp.getPredicate().getURI();
//		} else if(tpPredicate.isVariable()) {
//			
//		} else {
//			String errorMessage = "Predicate has to be either an URI or a variable";
//			throw new QueryTranslationException(errorMessage);
//		}
//		
//		
//		if(pos == POS.sub) {
//			result = this.calculateBetaSubject(tp, cm);
//		} else if(pos == POS.pre) {
//			result = this.calculateBetaPredicate(tp, cm);
//		} else if(pos == POS.obj) {
//			result = this.calculateBetaObject(tp, cm);
//		}
//		
//		logger.debug("beta " + pos + " = " + result);
//		return result;
//	}

	protected abstract SQLSelectItem calculateBetaObject(Triple triple
			, AbstractConceptMapping cm, String predicateURI, AlphaResult alphaResult)
	throws QueryTranslationException;

	public SQLSelectItem calculateBetaPredicate(String predicateURI) {
		ZConstant predicateURIConstant = new ZConstant(predicateURI, ZConstant.STRING);
		SQLSelectItem selectItem = new SQLSelectItem();
		selectItem.setExpression(predicateURIConstant);
		logger.debug("calculateBetaCMPredicate = " + selectItem);
		return selectItem;
	}
	
	protected abstract SQLSelectItem calculateBetaSubject(Triple tp, AbstractConceptMapping cm, AlphaResult alphaResult);

	protected AbstractQueryTranslator getOwner() {
		return owner;
	}

	
}
