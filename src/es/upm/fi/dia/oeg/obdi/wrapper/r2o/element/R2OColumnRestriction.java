package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2OColumnRestriction extends R2ORestriction{
	private R2ODatabaseColumn dbColumn;
	
	public R2OColumnRestriction(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}
	
	@Override
	public void parse(Element xmlElement) throws ParseException {
		//R2OColumnRestriction result = new R2OColumnRestriction();
		this.dbColumn = new R2ODatabaseColumn(xmlElement);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + R2OConstants.HAS_COLUMN_TAG);

		if(dbColumn.getAlias() != null && dbColumn.getAlias() != "") {
			result.append(" " + R2OConstants.ALIAS_ATTRIBUTE + "=\"" + dbColumn.getAlias() + "\"");
		}		
		
		if(dbColumn.getDataType() != null && dbColumn.getDataType() != "") {
			result.append(" " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + dbColumn.getDataType() + "\"");
		}		

		result.append(" >");
		
		result.append(dbColumn.getColumnName());
		result.append(XMLUtility.toCloseTag(R2OConstants.HAS_COLUMN_TAG));
		
		return result.toString();
	}

	public R2ODatabaseColumn getDatabaseColumn() {
		return dbColumn;
	}


}
