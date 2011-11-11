package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import org.apache.log4j.Logger;
import org.coode.owl.rdf.model.RDFNode;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLInvalidRefObjectMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLJoinConditionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap.TermMapPosition;

public class R2RMLPredicateObjectMap {
	private static Logger logger = Logger.getLogger(R2RMLPredicateObjectMap.class);
	private R2RMLMappingDocument owner;
	public enum ObjectMapType {ObjectMap, RefObjectMap};
	
	private R2RMLPredicateMap predicateMap;
	private R2RMLObjectMap objectMap;
	private R2RMLGraphMap graphMap;
	private R2RMLRefObjectMap refObjectMap;
	private ObjectMapType objectMapType;
	
	public R2RMLPredicateObjectMap(Resource resource, R2RMLMappingDocument owner) throws R2RMLInvalidRefObjectMapException, R2RMLJoinConditionException, R2RMLInvalidTermMapException {
		this.owner = owner;
		
		Statement predicateMapStatement = resource.getProperty(R2RMLConstants.R2RML_PREDICATEMAP_PROPERTY);
		if(predicateMapStatement != null) {
			Resource predicateMapResource = (Resource) predicateMapStatement.getObject();
			this.predicateMap = new R2RMLPredicateMap(predicateMapResource, TermMapPosition.PREDICATE);
		}

		Statement predicateStatement = resource.getProperty(R2RMLConstants.R2RML_PREDICATE_PROPERTY);
		if(predicateStatement != null) {
			String constantValueObject = predicateStatement.getObject().toString();
			this.predicateMap = new R2RMLPredicateMap(constantValueObject);
		}
		
		Statement objectMapStatement = resource.getProperty(R2RMLConstants.R2RML_OBJECTMAP_PROPERTY);
		if(objectMapStatement != null) {
			Resource objectMapResource = (Resource) objectMapStatement.getObject();
			this.objectMap = new R2RMLObjectMap(objectMapResource);
			
//			String objectMapResourceURI = objectMapResource.getURI();
//			if(objectMapResource.isURIResource() && objectMapResourceURI != null) {
//				this.objectMap = owner.getObjectMaps().get(objectMapResourceURI);
//			} else {
//				this.objectMap = new R2RMLObjectMap(objectMapResource);
//			}
			
		}

		Statement objectStatement = resource.getProperty(R2RMLConstants.R2RML_OBJECT_PROPERTY);
		if(objectStatement != null) {
			this.objectMapType = ObjectMapType.ObjectMap;
			String constantValueObject = objectStatement.getObject().toString();
			this.objectMap = new R2RMLObjectMap(constantValueObject);
		}

		Statement graphMapStatement = resource.getProperty(R2RMLConstants.R2RML_GRAPHMAP_PROPERTY);
		if(graphMapStatement != null) {
			this.graphMap = new R2RMLGraphMap((Resource) graphMapStatement.getObject());
		}
		
		Statement graphStatement = resource.getProperty(R2RMLConstants.R2RML_GRAPH_PROPERTY);
		if(graphStatement != null) {
			String constantValueObject = graphStatement.getObject().toString();
			this.graphMap = new R2RMLGraphMap(constantValueObject);
		}
		
		Statement refObjectMapStatement = resource.getProperty(R2RMLConstants.R2RML_REFOBJECTMAP_PROPERTY);
		if(refObjectMapStatement != null) {
			this.objectMapType = ObjectMapType.RefObjectMap;
			this.refObjectMap = new R2RMLRefObjectMap((Resource) refObjectMapStatement.getObject(), owner);
		}

	}

	public R2RMLPredicateMap getPredicateMap() {
		return predicateMap;
	}

	public R2RMLObjectMap getObjectMap() {
		return objectMap;
	}

	public R2RMLGraphMap getGraphMap() {
		return graphMap;
	}

	public R2RMLRefObjectMap getRefObjectMap() {
		return refObjectMap;
	}

	public ObjectMapType getObjectMapType() {
		return objectMapType;
	}
}
