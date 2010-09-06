package es.upm.fi.dia.oeg.obdi.wrapper;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;


public interface IMappingDocument extends IParseable {
	public IMappingDocument parse(Element xmlElement) throws ParseException;

	public List<String> getMappedConcepts();
	public Collection<AbstractConceptMapping> getConceptMappings();
	public Collection<AbstractConceptMapping> getConceptMappingsByConceptURI(String conceptURI);
	public AbstractConceptMapping getConceptMappingsByMappingId(String mappingId);
	
	public List<String> getMappedProperties();
	public Collection<IPropertyMapping> getPropertyMappings();
	public Collection<IPropertyMapping> getPropertyMappings(String domain, String range);
	
	public List<String> getMappedAttributes();
	public Collection<IAttributeMapping> getAttributeMappings();
	public Collection<IAttributeMapping> getAttributeMappings(String domain, String range);
	
	public List<String> getMappedRelations();
	public Collection<IRelationMapping> getRelationMappings();
	public Collection<IRelationMapping> getRelationMappings(String domain, String range);
}
