package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLConstants;

public class R2RMLPredicateMap extends R2RMLTermMap {

	public R2RMLPredicateMap(String constantValue) {
		super(TermMapPosition.PREDICATE, constantValue);
		super.setTermType(R2RMLConstants.R2RML_LITERAL_URI);
	}
	
	public R2RMLPredicateMap(Resource resource, TermMapPosition termMapPosition) throws R2RMLInvalidTermMapException {
		super(resource, termMapPosition);
	}

}
