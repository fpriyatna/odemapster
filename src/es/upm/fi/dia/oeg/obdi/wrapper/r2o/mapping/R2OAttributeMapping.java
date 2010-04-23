package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.IParseable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Selector;

public class R2OAttributeMapping extends R2OPropertyMapping implements R2OElement, IAttributeMapping {
//	(33) attributemap-def::= attributemap-def name
//    (selector* | use-dbcol)
//    documentation?
	private String useDBCol;
	private Collection<Selector> selectors;

	@Override
	public R2OAttributeMapping parse(Element attributeMappingElement) throws ParseException {
		R2OAttributeMapping result = new R2OAttributeMapping();
		result.name = attributeMappingElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		//parse identifiedBy attribute
		result.id = attributeMappingElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);
		
		Element firstElement = XMLUtility.getFirstElement(attributeMappingElement);
		String firstElementName = firstElement.getNodeName();
		if(R2OConstants.SELECTOR_TAG.equalsIgnoreCase(firstElementName)) {
			result.selectors = new ArrayList<Selector>();
			List<Element> childElements = XMLUtility.getChildElements(attributeMappingElement);
			for(Element childElement : childElements) {
				Selector selector = new Selector().parse(childElement);
				result.selectors.add(selector);
				
			}
		} else if(R2OConstants.USE_DBCOL_TAG.equalsIgnoreCase(firstElementName)) {
			result.useDBCol = firstElement.getTextContent();
		}
		return result;
	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.ATTRIBUTEMAP_DEF_TAG + " ");
		result.append(R2OConstants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}
		result.append(">\n");

		
		if(this.useDBCol != null) {
			result.append("<" + R2OConstants.USE_DBCOL_TAG + ">\n");
			result.append(this.useDBCol);
			result.append("</" + R2OConstants.USE_DBCOL_TAG + ">\n");	
		}
		
		if(this.selectors != null) {
			for(Selector selector : this.selectors) {
				result.append(selector.toString() + "\n");
			}
			
		}
		
		result.append("</" + R2OConstants.ATTRIBUTEMAP_DEF_TAG + ">"); 
		return result.toString();
	}


	public Collection<Selector> getSelectors() {
		return selectors;
	}


	public String getUseDBCol() {
		return useDBCol;
	}


	@Override
	public String getAttributeName() {
		return this.name;
	}

	
	
}
