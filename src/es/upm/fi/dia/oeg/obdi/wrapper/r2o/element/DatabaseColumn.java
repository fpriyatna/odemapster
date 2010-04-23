package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;


public class DatabaseColumn implements R2OElement {
	private String name;
	private ColumnType columnType;
	
	public enum ColumnType {
		PRIMARY_KEY_COLUMN, FOREIGN_KEY_COLUMN, NORMAL_COLUMN
	}
	
	@Override
	public DatabaseColumn parse(Element element) throws ParseException {
		DatabaseColumn result = new DatabaseColumn();
		
		result.name = element.getAttribute(R2OConstants.NAME_ATTRIBUTE);

		String nodeName = element.getNodeName();
		if(nodeName.equalsIgnoreCase(R2OConstants.KEYCOL_DESC_TAG)) {
			result.columnType = ColumnType.PRIMARY_KEY_COLUMN;
		} else if(nodeName.equalsIgnoreCase(R2OConstants.FORKEYCOL_DESC_TAG)) {
			result.columnType = ColumnType.FOREIGN_KEY_COLUMN;
		} else if(nodeName.equalsIgnoreCase(R2OConstants.NONKEYCOL_DESC_TAG)) {
			result.columnType = ColumnType.NORMAL_COLUMN;
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if(this.columnType == ColumnType.PRIMARY_KEY_COLUMN) {
			result.append("<" + R2OConstants.KEYCOL_DESC_TAG + " ");
		} else if(this.columnType == ColumnType.FOREIGN_KEY_COLUMN) {
			result.append("<" + R2OConstants.FORKEYCOL_DESC_TAG + " ");
		} 		if(this.columnType == ColumnType.NORMAL_COLUMN) {
			result.append("<" + R2OConstants.NONKEYCOL_DESC_TAG + " ");
		}
 
		result.append(R2OConstants.NAME_ATTRIBUTE + "=\"" + this.name + "\"/>");
		return result.toString();
	}

	
}
