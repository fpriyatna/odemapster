package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;


public abstract class AbstractAlphaGenerator {
	protected Map<Node, Set<AbstractConceptMapping>> mapNodeConceptMapping;
	protected AbstractMappingDocument mappingDocument;
	protected boolean ignoreRDFTypeStatement = false;
	protected boolean subqueryAsView = false;
	
	public AbstractAlphaGenerator(Map<Node, Set<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument) {
		this.mapNodeConceptMapping = mapNodeConceptMapping;
		this.mappingDocument = mappingDocument;
	}
	
	public abstract Object calculateAlpha(Triple tp) throws Exception;
	protected abstract Object calculateAlphaPredicateObject(
			AbstractPropertyMapping pm, Triple triple
			, AbstractConceptMapping abstractConceptMapping) throws Exception;
	protected abstract Object calculateAlphaSubject(
			Node subject, AbstractConceptMapping abstractConceptMapping) throws Exception;
	public abstract Object calculateAlphaTSS(Collection<Triple> triples) throws Exception;
//	public abstract AbstractConceptMapping calculateAlphaCM(Triple tp) throws Exception;
//	public abstract AbstractConceptMapping calculateAlphaCMTB(Collection<Triple> triples) throws Exception;

	public boolean isIgnoreRDFTypeStatement() {
		return ignoreRDFTypeStatement;
	}

	public void setIgnoreRDFTypeStatement(boolean ignoreRDFTypeStatement) {
		this.ignoreRDFTypeStatement = ignoreRDFTypeStatement;
	}

	public boolean isSubqueryAsView() {
		return subqueryAsView;
	}

	public void setSubqueryAsView(boolean subqueryAsView) {
		this.subqueryAsView = subqueryAsView;
	}



}
