package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import com.hp.hpl.jena.rdf.model.Resource;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.exception.R2RMLInvalidTermMapException;

public class R2RMLGraphMap extends R2RMLTermMap {

	public R2RMLGraphMap(String constantValue) {
		super(TermMapPosition.GRAPH, constantValue);
		super.setTermType(R2RMLConstants.R2RML_LITERAL_URI);
	}
	
	public R2RMLGraphMap(Resource resource, R2RMLTriplesMap owner) throws R2RMLInvalidTermMapException {
		super(resource, TermMapPosition.GRAPH, owner);
	}


}
