package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class ArgumentRestriction extends R2OElement {
	// (26) arg-restrict::= parameter-selector restriction
	// (27) parameter-selector::= on-param literal
	private String onParam;
	private Restriction restriction;
	
	
	@Override
	R2OElement parse(Element element) throws R2OParserException {
		ArgumentRestriction result = new ArgumentRestriction();
		result.onParam = element.getAttribute(Constants.ON_PARAM_ATTRIBUTE);
		
		Element elementFirstChild = XMLUtility.getFirstElement(element);
		if(elementFirstChild != null) {
			result.restriction = (Restriction) new Restriction().parse((Element) elementFirstChild);
		}
		return result;
	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(Constants.ARG_RESTRICTION_TAG + " ");
		result.append(Constants.ON_PARAM_ATTRIBUTE + "=\"");
		result.append(onParam + "\">\n");
		result.append(this.restriction.toString() + "\n");
		result.append("</" + Constants.ARG_RESTRICTION_TAG + ">");
		
		return result.toString();
	}
	
	
}
