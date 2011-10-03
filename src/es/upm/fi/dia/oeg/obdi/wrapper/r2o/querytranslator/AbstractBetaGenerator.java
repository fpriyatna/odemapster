package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;

import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2SQLTranslator.POS;


public abstract class AbstractBetaGenerator {
	Map<Node, Collection<R2OConceptMapping>> mapNodeConceptMapping;
	R2OMappingDocument mappingDocument;

	public AbstractBetaGenerator(
			Map<Node, Collection<R2OConceptMapping>> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super();
		this.mapNodeConceptMapping = mapNodeConceptMapping;
		this.mappingDocument = mappingDocument;
	}



	abstract ZSelectItem calculateBeta(Triple tp, POS pos) throws Exception;
	abstract ZSelectItem calculateBetaCM(Triple tp, POS pos, R2OConceptMapping cm) throws Exception;
}
