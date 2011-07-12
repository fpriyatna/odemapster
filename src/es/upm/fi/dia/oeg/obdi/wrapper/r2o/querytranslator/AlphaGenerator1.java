package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZQuery;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class AlphaGenerator1 extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(AlphaGenerator1.class);
	
	public AlphaGenerator1(Map<Node, R2OConceptMapping> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}
	
	
	@Override
	ZQuery calculateAlpha(Triple tp) throws Exception {
		Node subject = tp.getSubject();

		R2OConceptMapping cm = this.mapNodeConceptMapping.get(subject);
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, this.mappingDocument);
		R2OQuery cmQuery = cmu.unfoldConceptMapping();
		
		return cmQuery;
	}


	@Override
	ZQuery calculateAlpha(Collection<Triple> triples) throws Exception {
		return this.calculateAlpha(triples.iterator().next());
	}


	@Override
	R2OConceptMapping calculateAlphaCM(Triple tp) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	R2OConceptMapping calculateAlphaCM(Collection<Triple> triples)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}



}
