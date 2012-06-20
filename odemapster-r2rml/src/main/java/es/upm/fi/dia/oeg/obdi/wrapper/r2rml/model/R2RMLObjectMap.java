package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;

public class R2RMLObjectMap extends R2RMLTermMap {
	private static Logger logger = Logger.getLogger(R2RMLObjectMap.class);
	
	
	public R2RMLObjectMap(Resource resource) throws R2RMLInvalidTermMapException {
		super(resource, TermMapPosition.OBJECT);
		
	}
	
	public R2RMLObjectMap(String constantValue) {
		super(TermMapPosition.OBJECT, constantValue);		
	}
}
