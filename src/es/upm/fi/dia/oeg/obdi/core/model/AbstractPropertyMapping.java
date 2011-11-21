package es.upm.fi.dia.oeg.obdi.core.model;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;




public abstract class AbstractPropertyMapping extends AbstractRDB2RDFMapping implements IPropertyMapping{

	protected AbstractConceptMapping parent;

//	(32) propertymap-def::= attributemap-def | relfromatt-def |
//    relationmap-def


	public abstract String getMappedPredicateName();

	public AbstractConceptMapping getParent() {
		return parent;
	}
	
	public void setParent(R2OConceptMapping parent) {
		this.parent = parent;
	}
	
	public abstract MappingType getPropertyMappingType();

	public boolean isObjectPropertyMapping() {
		return MappingType.RELATION == this.getPropertyMappingType();
	}

	public boolean isDataPropertyMapping() {
		return MappingType.ATTRIBUTE == this.getPropertyMappingType();
	}

	
}
