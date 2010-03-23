package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;


public class DatabaseColumn extends R2OElement {
	private String name;
	private ColumnType columnType;
	
	public enum ColumnType {
		PRIMARY_KEY_COLUMN, FOREIGN_KEY_COLUMN, NORMAL_COLUMN
	}
	
	@Override
	R2OElement parse(Element element) throws R2OParserException {
		DatabaseColumn result = new DatabaseColumn();
		
		result.name = element.getAttribute(Constants.NAME_ATTRIBUTE);

		String nodeName = element.getNodeName();
		if(nodeName.equalsIgnoreCase(Constants.KEYCOL_DESC_TAG)) {
			result.columnType = ColumnType.PRIMARY_KEY_COLUMN;
		} else if(nodeName.equalsIgnoreCase(Constants.FORKEYCOL_DESC_TAG)) {
			result.columnType = ColumnType.FOREIGN_KEY_COLUMN;
		} else if(nodeName.equalsIgnoreCase(Constants.NONKEYCOL_DESC_TAG)) {
			result.columnType = ColumnType.NORMAL_COLUMN;
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if(this.columnType == ColumnType.PRIMARY_KEY_COLUMN) {
			result.append("<" + Constants.KEYCOL_DESC_TAG + " ");
		} else if(this.columnType == ColumnType.FOREIGN_KEY_COLUMN) {
			result.append("<" + Constants.FORKEYCOL_DESC_TAG + " ");
		} 		if(this.columnType == ColumnType.NORMAL_COLUMN) {
			result.append("<" + Constants.NONKEYCOL_DESC_TAG + " ");
		}
 
		result.append(Constants.NAME_ATTRIBUTE + "=\"" + this.name + "\"/>");
		return result.toString();
	}

	
}
