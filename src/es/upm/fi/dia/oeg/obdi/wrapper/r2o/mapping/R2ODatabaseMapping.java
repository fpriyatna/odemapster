package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.DatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;

public class R2ODatabaseMapping implements R2OElement {
	private String name;
	private List<DatabaseTable> hasTables;
	
	@Override
	public R2ODatabaseMapping parse(Element element) throws ParseException {
		R2ODatabaseMapping result = new R2ODatabaseMapping();
		result.name = element.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		result.hasTables = new ArrayList<DatabaseTable>();
		NodeList nlHasTable = element.getElementsByTagName(R2OConstants.HAS_TABLE_TAG);
		for(int i=0; i<nlHasTable.getLength(); i++) {
			Element hasTableElement = (Element) nlHasTable.item(i);
			DatabaseTable databaseTable = new DatabaseTable().parse(hasTableElement);
			result.hasTables.add(databaseTable);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.DBSCHEMA_DESC_TAG+ " ");
		result.append(R2OConstants.NAME_ATTRIBUTE + "=\"" + this.name + "\">\n");
		
		for(DatabaseTable hasTable : hasTables) {
			result.append(hasTable.toString() + "\n");
		}
		
		result.append("</" + R2OConstants.DBSCHEMA_DESC_TAG + ">");
		return result.toString();
	}

	
}
