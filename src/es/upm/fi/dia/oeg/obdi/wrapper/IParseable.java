package es.upm.fi.dia.oeg.obdi.wrapper;

import org.w3c.dom.Element;

public interface IParseable {
	public void parse(Element xmlElement) throws ParseException;
}
