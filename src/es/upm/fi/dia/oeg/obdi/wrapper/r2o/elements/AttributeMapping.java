package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class AttributeMapping extends PropertyMapping {
//	(33) attributemap-def::= attributemap-def name
//    (selector* | use-dbcol)
//    documentation?
	private String useDBCol;
	private Selector selector;
	
	@Override
	R2OElement parse(Element attributeMappingElement) throws R2OParserException {
		AttributeMapping result = new AttributeMapping();
		result.name = attributeMappingElement.getAttribute(Constants.NAME_ATTRIBUTE);
		Element firstElement = XMLUtility.getFirstElement(attributeMappingElement);
		String firstElementName = firstElement.getNodeName();
		if(Constants.SELECTOR_TAG.equalsIgnoreCase(firstElementName)) {
			result.selector = (Selector) new Selector().parse(firstElement);
		} else if(Constants.USE_DBCOL_TAG.equalsIgnoreCase(firstElementName)) {
			result.useDBCol = firstElement.getTextContent();
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + Constants.ATTRIBUTEMAP_DEF_TAG + " " + Constants.NAME_ATTRIBUTE + "=\"" + this.name + "\">\n");
		if(this.useDBCol != null) {
			result.append("<" + Constants.USE_DBCOL_TAG + ">\n");
			result.append(this.useDBCol);
			result.append("</" + Constants.USE_DBCOL_TAG + ">\n");	
		}
		
		if(this.selector != null) {
			result.append(this.selector.toString() + "\n");
		}
		
		result.append("</" + Constants.ATTRIBUTEMAP_DEF_TAG + ">"); 
		return result.toString();
	}
	
	
}
