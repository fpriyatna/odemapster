package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;

public abstract class R2OPropertyMapping extends AbstractPropertyMapping implements R2OElement, IPropertyMapping {
	private R2OConceptMapping parent;

	
	public R2OPropertyMapping() {
		super();
	}

	
	public R2OPropertyMapping(R2OConceptMapping parent) {
		super();
		this.parent = parent;
	}


	public R2OConceptMapping getParent() {
		return parent;
	}

	public void setParent(R2OConceptMapping parent) {
		this.parent = parent;
	}
}
