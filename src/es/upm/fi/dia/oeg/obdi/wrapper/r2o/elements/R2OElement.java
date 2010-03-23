package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public abstract class R2OElement {
	abstract R2OElement parse(Element element) throws R2OParserException;
}
