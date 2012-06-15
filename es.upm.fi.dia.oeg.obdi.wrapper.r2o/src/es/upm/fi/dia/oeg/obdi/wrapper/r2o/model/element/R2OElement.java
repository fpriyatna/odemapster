package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.engine.IParseable;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;

public abstract interface R2OElement extends IParseable{
	public void parse(Element xmlElement) throws ParseException;
}
