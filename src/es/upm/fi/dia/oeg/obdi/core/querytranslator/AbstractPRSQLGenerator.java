package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;

public abstract class AbstractPRSQLGenerator {
	private static Logger logger = Logger.getLogger(AbstractPRSQLGenerator.class);
	protected Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes;
	protected boolean ignoreRDFTypeStatement = false;
	
	public AbstractPRSQLGenerator(
			Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes) {
		super();
		this.mapInferredTypes = mapInferredTypes;
	}

	public abstract Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator)
					throws Exception;

	protected Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator, AbstractConceptMapping cmSubject)
					throws Exception {
		Node subject = tp.getSubject();
		Node predicate = tp.getPredicate();
		Node object = tp.getObject();
		//R2OConceptMapping cmSubject = this.mapTripleCM.get(tp);
		//R2OConceptMapping cmSubject = (R2OConceptMapping) this.mapInferredTypes.get(subject).iterator().next();

		Collection<ZSelectItem> prList = new Vector<ZSelectItem>();

		Collection<ZSelectItem> selectItemsSubjects = this.genPRSQLSubject(subject, tp, betaGenerator, cmSubject, nameGenerator);
		prList.addAll(selectItemsSubjects);

		if(predicate != subject) {
			//line 22
			ZSelectItem selectItemPredicate = this.genPRSQLPredicate(predicate, tp, betaGenerator, cmSubject, nameGenerator);
			prList.add(selectItemPredicate);
		}

		if(object != subject && object != predicate) {
			//line 23
			Collection<ZSelectItem> selectItems = this.genPRSQLObject(object, tp, betaGenerator, cmSubject, nameGenerator);
			prList.addAll(selectItems);
		}

		logger.debug("genPRSQL = " + prList);
		return prList;

	}
	
	protected Collection<ZSelectItem> genPRSQLObject(Node object, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject, NameGenerator nameGenerator) throws Exception {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();

		ZSelectItem selectItem = betaGenerator.calculateBeta(tp, POS.obj);
		String selectItemAlias = nameGenerator.generateName(tp, object);
		if(selectItemAlias != null) {
			selectItem.setAlias(selectItemAlias);
		}
		selectItems.add(selectItem); //line 23

		logger.debug("genPRSQLObject = " + selectItem);
		return selectItems;
	}

	protected ZSelectItem genPRSQLPredicate(Node predicate, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject, NameGenerator nameGenerator) throws Exception {
		ZSelectItem selectItem = betaGenerator.calculateBeta(tp, POS.pre);
		selectItem.setAlias(nameGenerator.generateName(tp, predicate));
		logger.debug("genPRSQLPredicate = " + selectItem);
		return selectItem;
	}
	
	protected abstract Collection<ZSelectItem> genPRSQLSubject(Node subject, Triple tp
			, AbstractBetaGenerator betaGenerator, AbstractConceptMapping cmSubject, NameGenerator nameGenerator) 
					throws Exception;

	public abstract Collection<ZSelectItem> genPRSQLTB(
			Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator) throws Exception;;

	protected Collection<ZSelectItem> genPRSQLTB(
			Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator
			, AbstractConceptMapping cmSubject)
			throws Exception {
		Triple firstTriple = tripleBlock.iterator().next();
		Collection<ZSelectItem> prList = new HashSet<ZSelectItem>();
		Node subject = firstTriple.getSubject();
		Collection<ZSelectItem> selectItemsSubjects = this.genPRSQLSubject(
				subject, firstTriple, betaGenerator, cmSubject, nameGenerator);
		prList.addAll(selectItemsSubjects);

		
		for(Triple tp : tripleBlock) {
			Node predicate = tp.getPredicate();
			String tpPredicateURI = tp.getPredicate().getURI();
			boolean isRDFTypeStatement = RDF.type.getURI().equals(tpPredicateURI);
			if(this.ignoreRDFTypeStatement && isRDFTypeStatement) {
				//do nothing
			} else {
				Node object = tp.getObject();
				if(predicate != subject) {
					ZSelectItem selectItemPredicate = this.genPRSQLPredicate(predicate, tp, betaGenerator, cmSubject, nameGenerator);
					prList.add(selectItemPredicate);
				}
				if(object != subject && object != predicate) {
					Collection<ZSelectItem> selectItemsObject = this.genPRSQLObject(object, tp, betaGenerator, cmSubject, nameGenerator);
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
}
