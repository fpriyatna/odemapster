package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2OTableRestriction extends R2ORestriction {
	private R2ODatabaseTable databaseTable;
	
	public R2OTableRestriction(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}
	
	@Override
	public void parse(Element xmlElement) throws ParseException {
		//R2OTableRestriction result = new R2OTableRestriction();
		this.databaseTable = new R2ODatabaseTable(xmlElement);
	}



	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
//		result.append(XMLUtility.toOpenTag(R2OConstants.HAS_TABLE_TAG));
		result.append(databaseTable.toString());
//		result.append(XMLUtility.toCloseTag(R2OConstants.HAS_TABLE_TAG));
		return result.toString();
	}



	public R2ODatabaseTable getDatabaseTable() {
		return databaseTable;
	}

	
}
