package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;


public abstract class AbstractAlphaGenerator {
	protected Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping;
	protected AbstractMappingDocument mappingDocument;
	
	public AbstractAlphaGenerator(Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument) {
		this.mapNodeConceptMapping = mapNodeConceptMapping;
		this.mappingDocument = mappingDocument;
	}
	
	public abstract Object calculateAlpha(Triple tp) throws Exception;
	public abstract Object calculateAlphaTB(Collection<Triple> triples) throws Exception;
	public abstract AbstractConceptMapping calculateAlphaCM(Triple tp) throws Exception;
	public abstract AbstractConceptMapping calculateAlphaCMTB(Collection<Triple> triples) throws Exception;



}
