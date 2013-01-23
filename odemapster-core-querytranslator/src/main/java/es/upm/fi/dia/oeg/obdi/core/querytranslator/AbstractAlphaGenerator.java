package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;

public abstract class AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(AbstractAlphaGenerator.class);
	
	protected AbstractQueryTranslator owner; 
	protected boolean ignoreRDFTypeStatement = false;
	protected boolean subqueryAsView = false;
	
	public AbstractAlphaGenerator(AbstractQueryTranslator owner) {
		this.owner = owner;
	}
	

	public AlphaResultUnion calculateAlpha(Triple tp, AbstractConceptMapping cm) throws Exception {
		AlphaResultUnion result = null;
		
		Node tpPredicate = tp.getPredicate();
		if(tpPredicate.isURI()) {
			String predicateURI = tpPredicate.getURI();
			AlphaResult alphaResult = this.calculateAlpha(tp, predicateURI, cm);
//			String logicalTableAlias = cm.getLogicalTableAlias();
//			if(logicalTableAlias == null || logicalTableAlias.equals("")) {
//				cm.setLogicalTableAlias(alphaResult.getAlphaSubject().getAlias());
//			}
			result = new AlphaResultUnion(alphaResult);
		} else if(tpPredicate.isVariable()) {
			Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings();
			if(pms != null && pms.size() > 0) {
				List<AlphaResult> alphaResults = new Vector<AlphaResult>();
				for(AbstractPropertyMapping pm : pms) {
					String predicateURI = pm.getMappedPredicateName();
					AlphaResult alphaResult = this.calculateAlpha(tp, predicateURI, cm);
					alphaResults.add(alphaResult);
				}
				result = new AlphaResultUnion(alphaResults);
			} else {
				//TODO : deal when no predicateobjectmap is specified, but only subjectmap
//				SQLLogicalTable alphaSubject = (SQLLogicalTable) this.calculateAlphaSubject(tp.getSubject(), cm);
//				AlphaResult alphaResult = new AlphaResult(alphaSubject, null, null);
//				result = new AlphaResultUnion(alphaResult);	
			}
		} else {
			String errorMessage = "Predicate has to be either an URI or a variable";
			throw new QueryTranslationException(errorMessage);
		}
		
		
		logger.debug("alpha(tp) = " + result);
		return result;
	}
	
	public abstract AlphaResult calculateAlpha(Triple tp, String predicateURI, AbstractConceptMapping abstractConceptMapping) throws Exception;
	
	protected abstract Object calculateAlphaPredicateObject(
			AbstractPropertyMapping pm, Triple triple
			, AbstractConceptMapping abstractConceptMapping, String logicalTableAlias) throws Exception;
	
	protected abstract Object calculateAlphaSubject(
			Node subject, AbstractConceptMapping abstractConceptMapping) throws Exception;
	
	public abstract List<AlphaResultUnion> calculateAlphaSTG(Collection<Triple> triples
			, AbstractConceptMapping cm) throws Exception;
	
//	public abstract AbstractConceptMapping calculateAlphaCM(Triple tp) throws Exception;
//	public abstract AbstractConceptMapping calculateAlphaCMTB(Collection<Triple> triples) throws Exception;

	public boolean isIgnoreRDFTypeStatement() {
		return ignoreRDFTypeStatement;
	}

	public void setIgnoreRDFTypeStatement(boolean ignoreRDFTypeStatement) {
		this.ignoreRDFTypeStatement = ignoreRDFTypeStatement;
	}

	public boolean isSubqueryAsView() {
		return subqueryAsView;
	}

	public void setSubqueryAsView(boolean subqueryAsView) {
		this.subqueryAsView = subqueryAsView;
	}



}
