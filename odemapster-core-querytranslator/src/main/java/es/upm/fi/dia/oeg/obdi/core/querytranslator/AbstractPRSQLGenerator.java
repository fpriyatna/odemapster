package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

	public Collection<ZSelectItem> genPRSQL(Triple tp
			, AlphaResult alphaResult
			, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator
			, AbstractConceptMapping cmSubject, String predicateURI)
					throws QueryTranslationException {
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();

		Collection<ZSelectItem> prList = new Vector<ZSelectItem>();

		Collection<ZSelectItem> selectItemsSubjects = 
				this.genPRSQLSubject(tp, alphaResult, betaGenerator
						,nameGenerator, cmSubject);
		prList.addAll(selectItemsSubjects);

		if(predicate != subject) {
			//line 22
			ZSelectItem selectItemPredicate = this.genPRSQLPredicate(
					tp, alphaResult, betaGenerator, nameGenerator
					, predicateURI);
			if(selectItemPredicate != null) {
				prList.add(selectItemPredicate);
			}
			
		}

		if(object != subject && object != predicate) {
			//line 23
			Collection<ZSelectItem> selectItems = this.genPRSQLObject(
					tp, alphaResult, betaGenerator, nameGenerator,
					cmSubject, predicateURI);
			prList.addAll(selectItems);
		}

		logger.debug("genPRSQL = " + prList);
		return prList;

	}
	
	protected Collection<ZSelectItem> genPRSQLObject(Triple tp
			, AlphaResult alphaResult, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator, AbstractConceptMapping cmSubject
			, String predicateURI
			) throws QueryTranslationException {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();

		try {
			SQLSelectItem betaObj = betaGenerator.calculateBetaObject(
					tp, cmSubject, predicateURI, alphaResult);
			ZSelectItem selectItem = betaObj.clone();
			String selectItemAlias = nameGenerator.generateName(
					tp.getObject());
			if(selectItemAlias != null) {
				selectItem.setAlias(selectItemAlias);
			}
			selectItems.add(selectItem); //line 23

			logger.debug("genPRSQLObject = " + selectItem);
			return selectItems;			
		} catch (Exception e) {
			throw new QueryTranslationException(e);
		}

	}

	protected ZSelectItem genPRSQLPredicate(Triple tp
			, AlphaResult alphaResult
			, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator
			, String predicateURI) throws QueryTranslationException {
		try {
			SQLSelectItem betaPre = 
					betaGenerator.calculateBetaPredicate(predicateURI);
			ZSelectItem selectItem = betaPre.clone();
			String alias = nameGenerator.generateName(tp.getPredicate());
			selectItem.setAlias(alias);
			logger.debug("genPRSQLPredicate = " + selectItem);
			return selectItem;			
		} catch (Exception e) {
			throw new QueryTranslationException(e);
		}

	}
	
	protected abstract Collection<ZSelectItem> genPRSQLSubject(
			Triple tp, AlphaResult alphaResult
			, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator
			, AbstractConceptMapping cmSubject) 
					throws QueryTranslationException;

//	public abstract Collection<ZSelectItem> genPRSQLSTG(List<Triple> stg
//			, List<BetaResultSet> betaResultSetList, NameGenerator nameGenerator) 
//					throws Exception;;

	protected Collection<ZSelectItem> genPRSQLSTG(List<Triple> stg,
			AlphaResult alphaResult, AbstractBetaGenerator betaGenerator,
			NameGenerator nameGenerator, AbstractConceptMapping cmSubject) throws Exception {
		Collection<ZSelectItem> prList = new HashSet<ZSelectItem>();
		
		Triple firstTriple = stg.get(0);
		Collection<ZSelectItem> selectItemsSubjects = 
				this.genPRSQLSubject(firstTriple, 
						alphaResult, betaGenerator, 
						nameGenerator, cmSubject);
		prList.addAll(selectItemsSubjects);

		Node subject = firstTriple.getSubject();
		for(int i=0; i<stg.size(); i++) {
			Triple tp = stg.get(i);
			
			Node predicate = tp.getPredicate();
			if(!predicate.isURI()) {
				String errorMessage = "Only bounded predicate is supported in STG.";
				logger.warn(errorMessage);
			}
			String predicateURI = predicate.getURI();
			
			if(this.ignoreRDFTypeStatement 
					&& RDF.type.getURI().equals(predicateURI)) {
				//do nothing
			} else {
				Node object = tp.getObject();
				if(predicate != subject) {
					ZSelectItem selectItemPredicate = 
							this.genPRSQLPredicate(tp, alphaResult,
									betaGenerator, nameGenerator,
									predicateURI);
					if(selectItemPredicate != null) {
						prList.add(selectItemPredicate);	
					}
					
				}
				if(object != subject && object != predicate) {
					Collection<ZSelectItem> selectItemsObject = 
							this.genPRSQLObject(tp, alphaResult,
									betaGenerator, nameGenerator,
									cmSubject, predicateURI);
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
