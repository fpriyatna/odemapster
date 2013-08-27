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

import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
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
			String columnType = null;
			if(predicate.isVariable()) {
				columnType = Constants.COLUMN_TYPE_TEXT;
			}
			
			//line 23
			Collection<ZSelectItem> objectSelectItems = this.genPRSQLObject(
					tp, alphaResult, betaGenerator, nameGenerator,
					cmSubject, predicateURI, columnType);
			prList.addAll(objectSelectItems);
		}

		logger.debug("genPRSQL = " + prList);
		return prList;

	}
	
	protected Collection<ZSelectItem> genPRSQLObject(Triple tp
			, AlphaResult alphaResult, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator, AbstractConceptMapping cmSubject
			, String predicateURI
			, String columnType
			) throws QueryTranslationException {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();
		try {
			String dbType = owner.getDatabaseType();
			
			List<SQLSelectItem> betaObjSelectItems = betaGenerator.calculateBetaObject(
					tp, cmSubject, predicateURI, alphaResult);
			for(int i=0; i<betaObjSelectItems.size(); i++) {
				SQLSelectItem betaObjSelectItem = betaObjSelectItems.get(i); 
				SQLSelectItem selectItem = betaObjSelectItem.clone();
				String selectItemAlias = nameGenerator.generateName(
						tp.getObject());
				if(selectItemAlias != null) {
					if(betaObjSelectItems.size() > 1) {
						selectItemAlias += "_" + i;
					}
					selectItem.setAlias(selectItemAlias);
				}
				selectItems.add(selectItem); //line 23
				if(dbType != null && !dbType.equals("")) {
					selectItem.setDbType(dbType);
				}
				if(columnType != null) {
					selectItem.setColumnType(columnType);	
				}
			}
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
			SQLSelectItem selectItem = betaPre.clone();
			String dbType = this.owner.getDatabaseType();
			selectItem.setDbType(dbType);
			selectItem.setColumnType("text");
//			if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(databaseType)) {
//				selectItem.setDbType(Constants.DATABASE_POSTGRESQL);
//				selectItem.setColumnType("text");
//			}
			String alias = nameGenerator.generateName(tp.getPredicate());
			selectItem.setAlias(alias);
			String selectItemString = selectItem.toString(); 
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
									cmSubject, predicateURI, null);
					prList.addAll(selectItemsObject);
				}				
			}
		}


		logger.debug("genPRSQLTB = " + prList);
		return prList;
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
