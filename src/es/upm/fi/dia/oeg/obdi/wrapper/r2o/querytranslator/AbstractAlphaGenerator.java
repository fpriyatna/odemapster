package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;

import Zql.ZQuery;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;


public abstract class AbstractAlphaGenerator {
	Map<Node, Collection<R2OConceptMapping>> mapNodeConceptMapping;
	R2OMappingDocument mappingDocument;
	
	public AbstractAlphaGenerator(Map<Node, Collection<R2OConceptMapping>> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		this.mapNodeConceptMapping = mapNodeConceptMapping;
		this.mappingDocument = mappingDocument;
	}
	
	abstract Object calculateAlpha(Triple tp) throws Exception;
	abstract Object calculateAlphaTB(Collection<Triple> triples) throws Exception;
	abstract R2OConceptMapping calculateAlphaCM(Triple tp) throws Exception;
	abstract R2OConceptMapping calculateAlphaCMTB(Collection<Triple> triples) throws Exception;



}
