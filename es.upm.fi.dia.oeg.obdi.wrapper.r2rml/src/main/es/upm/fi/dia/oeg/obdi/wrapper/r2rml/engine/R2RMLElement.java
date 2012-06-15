package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import java.sql.SQLException;

public interface R2RMLElement {
	public Object accept(R2RMLElementVisitor visitor) throws Exception;
}
