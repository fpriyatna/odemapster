package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZQuery;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class AlphaGenerator1 extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(AlphaGenerator1.class);
	
	public AlphaGenerator1(Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}
	
	
	@Override
	protected	ZQuery calculateAlpha(Triple tp) throws Exception {
		Node subject = tp.getSubject();

		Collection<AbstractConceptMapping> cms = this.mapNodeConceptMapping.get(subject);
		R2OConceptMapping cm = (R2OConceptMapping) cms.iterator().next();
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, (R2OMappingDocument) this.mappingDocument);
		SQLQuery cmQuery = cmu.unfoldConceptMapping();
		
		return cmQuery;
	}


	@Override
	protected ZQuery calculateAlphaTB(Collection<Triple> triples) throws Exception {
		return this.calculateAlpha(triples.iterator().next());
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
