package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class DatabaseTable implements R2OElement {
	private String name;
	private List<DatabaseColumn> hasColumns;
	
	@Override
	public DatabaseTable parse(Element element) throws ParseException {
		DatabaseTable result = new DatabaseTable();
		
		result.name = element.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		result.hasColumns = new ArrayList<DatabaseColumn>();
		
		NodeList nlKeyColumns = element.getElementsByTagName(R2OConstants.KEYCOL_DESC_TAG);
		for(int i=0; i<nlKeyColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlKeyColumns.item(i);
			DatabaseColumn databaseColumn = new DatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		NodeList nlForeignColumns = element.getElementsByTagName(R2OConstants.FORKEYCOL_DESC_TAG);
		for(int i=0; i<nlForeignColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlForeignColumns.item(i);
			DatabaseColumn databaseColumn = new DatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		NodeList nlNormalColumns = element.getElementsByTagName(R2OConstants.NONKEYCOL_DESC_TAG);
		for(int i=0; i<nlNormalColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlNormalColumns.item(i);
			DatabaseColumn databaseColumn = new DatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.HAS_TABLE_TAG+ " ");
		result.append(R2OConstants.NAME_ATTRIBUTE + "=\"" + this.name + "\">");
		
		for(DatabaseColumn hasColumn : hasColumns) {
			result.append(hasColumn.toString() + "\n");
		}
		
		result.append("</" + R2OConstants.HAS_TABLE_TAG + ">");
		return result.toString();
	}

	
}
