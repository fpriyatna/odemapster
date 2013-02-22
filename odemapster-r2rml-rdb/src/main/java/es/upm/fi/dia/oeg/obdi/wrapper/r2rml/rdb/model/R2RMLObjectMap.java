package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.exception.R2RMLInvalidTermMapException;

public class R2RMLObjectMap extends R2RMLTermMap {
	private static Logger logger = Logger.getLogger(R2RMLObjectMap.class);
	
	public R2RMLObjectMap(Resource resource, R2RMLTriplesMap owner) throws R2RMLInvalidTermMapException {
		super(resource, TermMapPosition.OBJECT, owner);
		
	}
	
	public R2RMLObjectMap(String constantValue) {
		super(TermMapPosition.OBJECT, constantValue);		
	}
}
