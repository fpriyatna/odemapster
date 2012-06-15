package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import java.sql.SQLException;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementVisitor;

public interface R2RMLElement {
	public Object accept(R2RMLElementVisitor visitor) throws Exception;
}
