package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import org.apache.log4j.Logger;
import org.coode.owl.rdf.model.RDFNode;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLInvalidRefObjectMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLJoinConditionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap.TermMapPosition;

public class R2RMLPredicateObjectMap extends AbstractPropertyMapping implements IRelationMapping, IAttributeMapping{
	private static Logger logger = Logger.getLogger(R2RMLPredicateObjectMap.class);
	private R2RMLMappingDocument mappingDocument;
	public enum ObjectMapType {ObjectMap, RefObjectMap};
	private R2RMLPredicateMap predicateMap;
	private R2RMLObjectMap objectMap;
	private R2RMLGraphMap graphMap;
	private R2RMLRefObjectMap refObjectMap;
	private ObjectMapType objectMapType;
	
	public R2RMLPredicateObjectMap(Resource resource, R2RMLMappingDocument mappingDocument, R2RMLTriplesMap parent) 
			throws R2RMLInvalidRefObjectMapException, R2RMLJoinConditionException, R2RMLInvalidTermMapException {
		this.mappingDocument = mappingDocument;
		this.parent = parent;
		
		
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
			this.refObjectMap = new R2RMLRefObjectMap((Resource) refObjectMapStatement.getObject(), mappingDocument);
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

	@Override
	public String getPropertyMappingID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMappedPredicateName() {
		return this.predicateMap.getOriginalValue();
	}

	@Override
	public MappingType getPropertyMappingType() {
		MappingType result;
		if(this.objectMap != null) {
			String objectMapTermType = this.objectMap.getTermType();
			if(objectMapTermType.equals(R2RMLConstants.R2RML_LITERAL_URI)) {
				result = MappingType.ATTRIBUTE;
			} else {
				result = MappingType.RELATION;
			}
		} else if(this.refObjectMap != null) {
			result = MappingType.RELATION;
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public String getAttributeName() {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getAttributeName");
		return null;
	}

	@Override
	public String getRelationName() {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getRelationName");
		return null;
	}

	@Override
	public String getRangeClassMapping() {
		// TODO Auto-generated method stub
		if(this.refObjectMap != null) {
			return this.refObjectMap.getParentTripleMapName();
		} else {
			return null;
		}
		
	}

	@Override
	public String toString() {
		return "R2RMLPredicateObjectMap [predicateMap=" + predicateMap
				+ ", objectMap=" + objectMap + ", refObjectMap=" + refObjectMap
				+ ", objectMapType=" + objectMapType + "]";
	}

	
	
}
