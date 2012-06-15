package es.upm.fi.dia.oeg.obdi.core.model;





public abstract class AbstractPropertyMapping extends AbstractRDB2RDFMapping implements IPropertyMapping{

	protected AbstractConceptMapping parent;

//	(32) propertymap-def::= attributemap-def | relfromatt-def |
//    relationmap-def


	public abstract String getMappedPredicateName();

	public AbstractConceptMapping getParent() {
		return parent;
	}
	

	
	public abstract MappingType getPropertyMappingType();

	public boolean isObjectPropertyMapping() {
		return MappingType.RELATION == this.getPropertyMappingType();
	}

	public boolean isDataPropertyMapping() {
		return MappingType.ATTRIBUTE == this.getPropertyMappingType();
	}

	
}
