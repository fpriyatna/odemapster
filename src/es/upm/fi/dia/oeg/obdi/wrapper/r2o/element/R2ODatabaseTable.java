package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class R2ODatabaseTable implements R2OElement {
	private String name;
	private String alias;
	private List<R2ODatabaseColumn> hasColumns;
	
	@Override
	public R2ODatabaseTable parse(Element element) throws ParseException {
		R2ODatabaseTable result = new R2ODatabaseTable();
		
		result.name = element.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		result.hasColumns = new ArrayList<R2ODatabaseColumn>();
		
		NodeList nlKeyColumns = element.getElementsByTagName(R2OConstants.KEYCOL_DESC_TAG);
		for(int i=0; i<nlKeyColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlKeyColumns.item(i);
			R2ODatabaseColumn databaseColumn = new R2ODatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		NodeList nlForeignColumns = element.getElementsByTagName(R2OConstants.FORKEYCOL_DESC_TAG);
		for(int i=0; i<nlForeignColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlForeignColumns.item(i);
			R2ODatabaseColumn databaseColumn = new R2ODatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		NodeList nlNormalColumns = element.getElementsByTagName(R2OConstants.NONKEYCOL_DESC_TAG);
		for(int i=0; i<nlNormalColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlNormalColumns.item(i);
			R2ODatabaseColumn databaseColumn = new R2ODatabaseColumn().parse(hasColumnElement);
			result.hasColumns.add(databaseColumn);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.HAS_TABLE_TAG+ " ");
		result.append(R2OConstants.NAME_ATTRIBUTE + "=\"" + this.name + "\">");
		if(this.alias != null) {
			result.append(R2OConstants.ALIAS_ATTRIBUTE + "=\"" + this.alias + "\">");
		}
		
		if(this.hasColumns != null && this.hasColumns.size() > 0) {
			for(R2ODatabaseColumn hasColumn : hasColumns) {
				result.append(hasColumn.toString() + "\n");
			}
			
		}
		
		result.append("</" + R2OConstants.HAS_TABLE_TAG + ">");
		return result.toString();
	}

	public String getName() {
		return name;
	}

	public String getAlias() {
		return alias;
	}

	
}
