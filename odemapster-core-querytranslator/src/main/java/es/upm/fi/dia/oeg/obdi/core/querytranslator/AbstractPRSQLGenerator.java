package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;


import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;

public abstract class AbstractPRSQLGenerator {
	private static Logger logger = Logger.getLogger(AbstractPRSQLGenerator.class);
	protected boolean ignoreRDFTypeStatement = false;
	protected AbstractQueryTranslator owner;
	
	public AbstractPRSQLGenerator(AbstractQueryTranslator owner) {
		super();
		this.owner = owner;
	}

//	public abstract Collection<ZSelectItem> genPRSQL(
//			Triple tp, BetaResult betaResult
//			, NameGenerator nameGenerator)
//					throws Exception;

	public Collection<ZSelectItem> genPRSQL(Triple tp, BetaResult betaResult
			, NameGenerator nameGenerator, AbstractConceptMapping cmSubject)
					throws Exception {
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		//R2OConceptMapping cmSubject = this.mapTripleCM.get(tp);
		//R2OConceptMapping cmSubject = (R2OConceptMapping) this.mapInferredTypes.get(subject).iterator().next();

		Collection<ZSelectItem> prList = new Vector<ZSelectItem>();

		Collection<ZSelectItem> selectItemsSubjects = this.genPRSQLSubject(
				subject, tp, betaResult, cmSubject, nameGenerator);
		prList.addAll(selectItemsSubjects);

		if(predicate != subject) {
			//line 22
			ZSelectItem selectItemPredicate = this.genPRSQLPredicate(
					predicate, tp, betaResult, cmSubject, nameGenerator);
			prList.add(selectItemPredicate);
		}

		if(object != subject && object != predicate) {
			//line 23
			Collection<ZSelectItem> selectItems = this.genPRSQLObject(
					object, tp, betaResult, cmSubject, nameGenerator);
			prList.addAll(selectItems);
		}

		logger.debug("genPRSQL = " + prList);
		return prList;

	}
	
	protected Collection<ZSelectItem> genPRSQLObject(Node object, Triple tp,
			BetaResult betaResult, AbstractConceptMapping cmSubject
			, NameGenerator nameGenerator) throws Exception {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();

		ZSelectItem selectItem = betaResult.getBetaObj().clone();
		String selectItemAlias = nameGenerator.generateName(object);
		if(selectItemAlias != null) {
			selectItem.setAlias(selectItemAlias);
		}
		selectItems.add(selectItem); //line 23

		logger.debug("genPRSQLObject = " + selectItem);
		return selectItems;
	}

	protected ZSelectItem genPRSQLPredicate(Node predicate, Triple tp,
			BetaResult betaResult,
			AbstractConceptMapping cmSubject, NameGenerator nameGenerator) throws Exception {
		ZSelectItem selectItem = betaResult.getBetaPre().clone();
		selectItem.setAlias(nameGenerator.generateName(predicate));
		logger.debug("genPRSQLPredicate = " + selectItem);
		return selectItem;
	}
	
	protected abstract Collection<ZSelectItem> genPRSQLSubject(Node subject, Triple tp
			, BetaResult betaResult, AbstractConceptMapping cmSubject, NameGenerator nameGenerator) 
					throws Exception;

//	public abstract Collection<ZSelectItem> genPRSQLSTG(List<Triple> stg
//			, List<BetaResultSet> betaResultSetList, NameGenerator nameGenerator) 
//					throws Exception;;

	protected Collection<ZSelectItem> genPRSQLSTG(List<Triple> stg
			, List<BetaResultSet> betaResultSetList, NameGenerator nameGenerator
			, AbstractConceptMapping cmSubject) throws Exception {
		if(stg.size() != betaResultSetList.size()) {
			String errorMessage = "Numbers of beta is not consistent with STG size.";
			throw new QueryTranslationException(errorMessage);
		}
		Collection<ZSelectItem> prList = new HashSet<ZSelectItem>();
		
		Triple firstTriple = stg.get(0);
		BetaResultSet firstTripleBetaResultSet = betaResultSetList.get(0);
		if(firstTripleBetaResultSet.size() > 1) {
			String errorMessage = "Multiple betas are not permitted for triple " + firstTriple;
			logger.warn(errorMessage);
		}
		
		BetaResult firstTripleBetaResult = firstTripleBetaResultSet.get(0);
		Node subject = firstTriple.getSubject();
		Collection<ZSelectItem> selectItemsSubjects = this.genPRSQLSubject(
				subject, firstTriple, firstTripleBetaResult, cmSubject, nameGenerator);
		prList.addAll(selectItemsSubjects);

		for(int i=0; i<stg.size(); i++) {
			Triple tp = stg.get(i);
			BetaResultSet betaResultSet = betaResultSetList.get(i);
			if(betaResultSet.size() > 1) {
				String errorMessage = "Multiple betas are not permitted for triple " + tp;
				logger.warn(errorMessage);
			}
			BetaResult betaResult = betaResultSet.get(0);
			
			Node predicate = tp.getPredicate();
			if(predicate.isVariable()) {
				String errorMessage = "Unbounded property is not supported in STG.";
				logger.warn(errorMessage);
			}
			
			if(predicate.isURI() && this.ignoreRDFTypeStatement 
					&& RDF.type.getURI().equals(predicate.getURI())) {
				//do nothing
			} else {
				Node object = tp.getObject();
				if(predicate != subject) {
					ZSelectItem selectItemPredicate = this.genPRSQLPredicate(
							predicate, tp, betaResult, cmSubject, nameGenerator);
					prList.add(selectItemPredicate);
				}
				if(object != subject && object != predicate) {
					Collection<ZSelectItem> selectItemsObject = this.genPRSQLObject(
							object, tp, betaResult, cmSubject, nameGenerator);
					prList.addAll(selectItemsObject);
				}				
			}
		}

		Collection<ZSelectItem> prList2 = new Vector<ZSelectItem>(prList);

		logger.debug("genPRSQLTB = " + prList2);
		return prList2;
	}

	public boolean isIgnoreRDFTypeStatement() {
		return ignoreRDFTypeStatement;
	}

	public void setIgnoreRDFTypeStatement(boolean ignoreRDFTypeStatement) {
		this.ignoreRDFTypeStatement = ignoreRDFTypeStatement;
	}

	public AbstractQueryTranslator getOwner() {
		return owner;
	}
}
