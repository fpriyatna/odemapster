package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;

public class R2ODatabaseMapping implements R2OElement {
	private String name;
	private List<R2ODatabaseTable> hasTables;
	
	public void R2ODatabaseMapping(Element element) throws ParseException {
		this.parse(element);
	}
	
	public void parse(Element element) throws ParseException {
		//R2ODatabaseMapping result = new R2ODatabaseMapping();
		this.name = element.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		this.hasTables = new ArrayList<R2ODatabaseTable>();
		NodeList nlHasTable = element.getElementsByTagName(R2OConstants.HAS_TABLE_TAG);
		for(int i=0; i<nlHasTable.getLength(); i++) {
			Element hasTableElement = (Element) nlHasTable.item(i);
			R2ODatabaseTable databaseTable = new R2ODatabaseTable(hasTableElement);
			this.hasTables.add(databaseTable);
		}
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.DBSCHEMA_DESC_TAG+ " ");
		result.append(R2OConstants.NAME_ATTRIBUTE + "=\"" + this.name + "\">\n");
		
		for(R2ODatabaseTable hasTable : hasTables) {
			result.append(hasTable.toString() + "\n");
		}
		
		result.append("</" + R2OConstants.DBSCHEMA_DESC_TAG + ">");
		return result.toString();
	}

	
}
