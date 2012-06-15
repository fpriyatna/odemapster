package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;

import Zql.ZExpression;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractCondSQLGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;

public class R2OCondSQLGenerator extends AbstractCondSQLGenerator {
	private Map<Triple, R2OConceptMapping> mapTripleCM;
	
	public R2OCondSQLGenerator(AbstractBetaGenerator betaGenerator,
			Map<Node, Collection<AbstractConceptMapping>> mapInferredTypes,
			Map<Triple, R2OConceptMapping> mapTripleCM) {
		super(betaGenerator, mapInferredTypes);
		this.mapTripleCM = mapTripleCM;
	}

	
	@Override
	protected ZExpression genCondSQLPredicateObject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		R2OConceptMapping cm = this.mapTripleCM.get(tp);
		return this.genCondSQLPredicateObject(tp, betaGenerator, cm);
	}
	

	@Override
	protected ZExpression genCondSQLSubject(Triple tp, AbstractBetaGenerator betaGenerator) throws Exception {
		AbstractConceptMapping cm = this.mapTripleCM.get(tp);
		return this.genCondSQLSubject(tp, betaGenerator, cm);
	}

}
