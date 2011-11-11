package es.upm.fi.dia.oeg.obdi.core.model;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.engine.IParseable;
import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants.MappingType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;


public abstract class AbstractMappingDocument implements IParseable {
	
	
	public abstract void parse(Element xmlElement) throws ParseException;
	public abstract String getMappingDocumentID();
	
	public abstract List<String> getMappedConcepts();
	public abstract Collection<AbstractConceptMapping> getConceptMappings();
	public abstract Collection<AbstractConceptMapping> getConceptMappingsByName(String conceptURI);
	public abstract AbstractConceptMapping getConceptMappingById(String conceptMappingId);
	
	public abstract List<String> getMappedProperties();
	public abstract Collection<IPropertyMapping> getPropertyMappings();
	public abstract Collection<AbstractPropertyMapping> getPropertyMappingsByPropertyURI(String propertyURI);
	public abstract Collection<IPropertyMapping> getPropertyMappings(String domain, String range);
	public abstract AbstractPropertyMapping getPropertyMappingByPropertyMappingID(String propertyMappingID);
	
	public abstract List<String> getMappedAttributes();
	public abstract Collection<IAttributeMapping> getAttributeMappings();
	public abstract Collection<IAttributeMapping> getAttributeMappings(String domain, String range);
	
	public abstract List<String> getMappedRelations();
	public abstract Collection<IRelationMapping> getRelationMappings();
	public abstract Collection<IRelationMapping> getRelationMappings(String domain, String range);
	
	public abstract String getMappedConceptURI(String conceptMappingID);
	public abstract String getMappedPropertyURI(String propertyMappingID);
	public abstract MappingType getMappingType(String propertyMappingID);
	 
}
