package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class DatabaseTable extends R2OElement {
	private String name;
	private List<DatabaseColumn> hasColumns;
	
	@Override
	R2OElement parse(Element element) throws R2OParserException {
		DatabaseTable result = new DatabaseTable();
		
		result.name = element.getAttribute(Constants.NAME_ATTRIBUTE);
		
		result.hasColumns = new ArrayList<DatabaseColumn>();
		
		NodeList nlKeyColumns = element.getElementsByTagName(Constants.KEYCOL_DESC_TAG);
		for(int i=0; i<nlKeyColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlKeyColumns.item(i);
			DatabaseColumn databaseColumn = (DatabaseColumn) new DatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		NodeList nlForeignColumns = element.getElementsByTagName(Constants.FORKEYCOL_DESC_TAG);
		for(int i=0; i<nlForeignColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlForeignColumns.item(i);
			DatabaseColumn databaseColumn = (DatabaseColumn) new DatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		NodeList nlNormalColumns = element.getElementsByTagName(Constants.NONKEYCOL_DESC_TAG);
		for(int i=0; i<nlNormalColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlNormalColumns.item(i);
			DatabaseColumn databaseColumn = (DatabaseColumn) new DatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + Constants.HAS_TABLE_TAG+ " ");
		result.append(Constants.NAME_ATTRIBUTE + "=\"" + this.name + "\">");
		
		for(DatabaseColumn hasColumn : hasColumns) {
			result.append(hasColumn.toString() + "\n");
		}
		
		result.append("</" + Constants.HAS_TABLE_TAG + ">");
		return result.toString();
	}

	
}
