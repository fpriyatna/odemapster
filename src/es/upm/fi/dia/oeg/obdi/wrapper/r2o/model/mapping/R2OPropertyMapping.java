package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;

public abstract class R2OPropertyMapping extends AbstractPropertyMapping implements R2OElement, IPropertyMapping {
	private R2OConceptMapping parent;
//	private String id;
	
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


	@Override
	public String getPropertyMappingID() {
		String result;
		if(this.id != null && !this.id.equals("")) {
			result = this.id;
		} else {
			result = "pm" + this.hashCode();
		}
		
		return result;
	}


	@Override
	public void parse(Element xmlElement) throws ParseException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getId() {
		return this.getPropertyMappingID();
	}
	
	
	
	
}
