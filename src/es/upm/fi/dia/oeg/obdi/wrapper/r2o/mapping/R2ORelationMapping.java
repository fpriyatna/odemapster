package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;


public class R2ORelationMapping extends R2OPropertyMapping implements R2OElement, IRelationMapping {

	@Override
	public R2ORelationMapping parse(Element xmlElement) throws ParseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRelationName() {
		return this.name;
	}

}
