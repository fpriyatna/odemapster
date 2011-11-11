package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;

public class AlphaGenerator4 extends AbstractAlphaGenerator {

	public AlphaGenerator4(Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected R2ODatabaseTable calculateAlpha(Triple tp) throws Exception {
		Node subject = tp.getSubject();
		Collection<AbstractConceptMapping> cms = this.mapNodeConceptMapping.get(subject);
		R2OConceptMapping cm = (R2OConceptMapping) cms.iterator().next();
		
		return cm.getHasTable();
	}

	@Override
	protected Object calculateAlphaTB(Collection<Triple> triples) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R2OConceptMapping calculateAlphaCM(Triple tp) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R2OConceptMapping calculateAlphaCMTB(Collection<Triple> triples)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
