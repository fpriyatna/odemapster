package es.upm.fi.dia.oeg.obdi.wrapper;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants.MappingType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;


public interface IMappingDocument extends IParseable {
	
	
	public void parse(Element xmlElement) throws ParseException;
	public String getMappingDocumentID();
	
	public List<String> getMappedConcepts();
	public Collection<AbstractConceptMapping> getConceptMappings();
	public Collection<AbstractConceptMapping> getConceptMappingsByConceptName(String conceptURI);
	public AbstractConceptMapping getConceptMappingByConceptMappingId(String conceptMappingId);
	
	public List<String> getMappedProperties();
	public Collection<IPropertyMapping> getPropertyMappings();
	public Collection<AbstractPropertyMapping> getPropertyMappingsByPropertyURI(String propertyURI);
	public Collection<IPropertyMapping> getPropertyMappings(String domain, String range);
	public AbstractPropertyMapping getPropertyMappingByPropertyMappingID(String propertyMappingID);
	
	public List<String> getMappedAttributes();
	public Collection<IAttributeMapping> getAttributeMappings();
	public Collection<IAttributeMapping> getAttributeMappings(String domain, String range);
	
	public List<String> getMappedRelations();
	public Collection<IRelationMapping> getRelationMappings();
	public Collection<IRelationMapping> getRelationMappings(String domain, String range);
	
	public String getMappedConceptURI(String conceptMappingID);
	public String getMappedPropertyURI(String propertyMappingID);
	public MappingType getMappingType(String propertyMappingID);
	 
}
