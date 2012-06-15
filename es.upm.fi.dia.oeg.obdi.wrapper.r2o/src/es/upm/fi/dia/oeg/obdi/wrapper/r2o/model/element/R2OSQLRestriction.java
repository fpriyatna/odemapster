package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2OSQLRestriction extends R2ORestriction {
	private String hasSQL;
	private String alias;
	
	public R2OSQLRestriction(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}
	
	@Override
	public void parse(Element xmlElement) throws ParseException {
		this.hasSQL = xmlElement.getTextContent();
		this.alias = xmlElement.getTextContent();
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.HAS_SQL_TAG);
		if(this.alias != null && this.alias != "") {
			result.append(" " + R2OConstants.ALIAS_ATTRIBUTE + "=\"" + this.alias + "\"");
		}
		result.append(">");
		result.append(this.hasSQL + "\n");
		result.append(XMLUtility.toCloseTag(R2OConstants.HAS_SQL_TAG) + "\n");
		return result.toString();
		
	}


	public String getHasSQL() {
		return hasSQL;
	}

	public void setHasSQL(String hasSQL) {
		this.hasSQL = hasSQL;
	}

	public String getAlias() {
		return alias;
	}
	
	

}
