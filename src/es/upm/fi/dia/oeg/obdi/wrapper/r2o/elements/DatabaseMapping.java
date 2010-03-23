package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class DatabaseMapping extends R2OElement {
	private String name;
	private List<DatabaseTable> hasTables;
	
	@Override
	R2OElement parse(Element element) throws R2OParserException {
		DatabaseMapping result = new DatabaseMapping();
		result.name = element.getAttribute(Constants.NAME_ATTRIBUTE);
		
		result.hasTables = new ArrayList<DatabaseTable>();
		NodeList nlHasTable = element.getElementsByTagName(Constants.HAS_TABLE_TAG);
		for(int i=0; i<nlHasTable.getLength(); i++) {
			Element hasTableElement = (Element) nlHasTable.item(i);
			DatabaseTable databaseTable = (DatabaseTable) new DatabaseTable().parse(hasTableElement);
			result.hasTables.add(databaseTable);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + Constants.DBSCHEMA_DESC_TAG+ " ");
		result.append(Constants.NAME_ATTRIBUTE + "=\"" + this.name + "\">\n");
		
		for(DatabaseTable hasTable : hasTables) {
			result.append(hasTable.toString() + "\n");
		}
		
		result.append("</" + Constants.DBSCHEMA_DESC_TAG + ">");
		return result.toString();
	}

	
}
