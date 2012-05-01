package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractPRSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;

public class R2RMLPRSQLGenerator extends AbstractPRSQLGenerator {
	private static Logger logger = Logger.getLogger(R2RMLPRSQLGenerator.class);
	
	public R2RMLPRSQLGenerator(
			Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes) {
		super(mapInferredTypes);
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
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		ZSelectItem selectItemSubject = betaGenerator.calculateBetaSubject(cmSubject);
		result.add(selectItemSubject);
		String selectItemSubjectAlias = nameGenerator.generateName(tp, subject); 
		selectItemSubject.setAlias(selectItemSubjectAlias);
		logger.debug("genPRSQLSubject = " + result);
		return result;
	}
	
	@Override
	public Collection<ZSelectItem> genPRSQLTB(
			Collection<Triple> tripleBlock,
			AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator) throws Exception {
		Triple firstTriple = tripleBlock.iterator().next();
		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(firstTriple.getSubject()).iterator().next();

		Collection<ZSelectItem> prList = this.genPRSQLTB(tripleBlock, betaGenerator, nameGenerator, cmSubject);
		return prList;
	}

}
