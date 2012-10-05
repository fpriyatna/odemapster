package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractPRSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLPRSQLGenerator extends AbstractPRSQLGenerator {
	private static Logger logger = Logger.getLogger(R2RMLPRSQLGenerator.class);
	//private R2RMLQueryTranslator owner;
	
	public R2RMLPRSQLGenerator(R2RMLQueryTranslator owner,
			Map<Node, Set<AbstractConceptMapping>> mapInferredTypes) {
		super(owner, mapInferredTypes);
		this.owner = owner;
	}
	
	@Override
	public Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
					throws Exception {
		Node tpSubject = tp.getSubject();
		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(tpSubject).iterator().next();
		return this.genPRSQL(tp, betaGenerator, nameGenerator, cmSubject);
	}
	
	@Override
	protected Collection<ZSelectItem> genPRSQLSubject(Node subject, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject, NameGenerator nameGenerator) throws Exception {
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cmSubject;
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		if(subject.isVariable()) {
			this.getOwner().mapVarMapping.put(subject.getName(), subjectMap);
		}
		
		
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		ZSelectItem selectItemSubject = betaGenerator.calculateBetaSubject(cmSubject);
		result.add(selectItemSubject);
		String selectItemSubjectAlias = nameGenerator.generateName(tp, subject); 
		selectItemSubject.setAlias(selectItemSubjectAlias);
		logger.debug("genPRSQLSubject = " + result);
		return result;
	}
	
	public Collection<ZSelectItem> genPRSQLTB(
			Collection<Triple> tripleBlock,
			AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator) throws Exception {
		Triple firstTriple = tripleBlock.iterator().next();
		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(firstTriple.getSubject()).iterator().next();

		Collection<ZSelectItem> prList = this.genPRSQLTB(tripleBlock, betaGenerator, nameGenerator, cmSubject);
		return prList;
	}

	public R2RMLQueryTranslator getOwner() {
		return (R2RMLQueryTranslator) super.owner;
	}
}
