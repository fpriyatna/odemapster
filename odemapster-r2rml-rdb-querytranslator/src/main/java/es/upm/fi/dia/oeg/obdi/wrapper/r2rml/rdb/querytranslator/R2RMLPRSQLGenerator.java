package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractPRSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.BetaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;

public class R2RMLPRSQLGenerator extends AbstractPRSQLGenerator {
	private static Logger logger = Logger.getLogger(R2RMLPRSQLGenerator.class);
	//private R2RMLQueryTranslator owner;
	
	
	public R2RMLPRSQLGenerator(AbstractQueryTranslator owner) {
		super(owner);
	}
	
//	@Override
//	public Collection<ZSelectItem> genPRSQL(Triple tp, BetaResult betaResult
//			, NameGenerator nameGenerator) throws Exception {
//		Node tpSubject = tp.getSubject();
//		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(tpSubject).iterator().next();
//		return this.genPRSQL(tp, betaResult, nameGenerator, cmSubject);
//	}
	
	@Override
	protected Collection<ZSelectItem> genPRSQLSubject(Triple tp
			, AlphaResult alphaResult
			, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator
			, AbstractConceptMapping cmSubject)
					throws QueryTranslationException {
		Node subject = tp.getSubject();
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cmSubject;
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		if(subject.isVariable()) {
			this.getOwner().getMapVarMapping().put(subject.getName(), subjectMap);
		}
		
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		//ZSelectItem selectItemSubject = betaGenerator.calculateBetaSubject(cmSubject);
		SQLSelectItem betaSub = betaGenerator.calculateBetaSubject(cmSubject, alphaResult);
		try {
			ZSelectItem selectItemSubject = betaSub.clone();
			result.add(selectItemSubject);
			String selectItemSubjectAlias = nameGenerator.generateName(subject); 
			selectItemSubject.setAlias(selectItemSubjectAlias);
			logger.debug("genPRSQLSubject = " + result);
			return result;			
		} catch(Exception e) {
			throw new QueryTranslationException(e);
		}

	}
	
//	@Override
//	public Collection<ZSelectItem> genPRSQLSTG(List<Triple> tripleBlock,
//			List<BetaResultSet> betaResultSet, NameGenerator nameGenerator, AbstractConceptMapping cm) throws Exception {
//
//		Collection<ZSelectItem> prList = this.genPRSQLSTG(tripleBlock, betaResultSet, nameGenerator, cm);
//		return prList;
//	}

	@Override
	public AbstractQueryTranslator getOwner() {
		return super.owner;
	}
}
