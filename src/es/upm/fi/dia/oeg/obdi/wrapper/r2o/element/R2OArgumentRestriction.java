package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class R2OArgumentRestriction implements R2OElement {
	// (26) arg-restrict::= parameter-selector restriction
	// (27) parameter-selector::= on-param literal
	private String onParam;
	private R2ORestriction restriction;
	
	
	@Override
	public R2OArgumentRestriction parse(Element element) throws ParseException {
		R2OArgumentRestriction result = new R2OArgumentRestriction();
		result.onParam = element.getAttribute(R2OConstants.ON_PARAM_ATTRIBUTE);
		
		Element elementFirstChild = XMLUtility.getFirstElement(element);
		if(elementFirstChild != null) {
			result.restriction = new R2ORestriction().parse((Element) elementFirstChild);
		}
		return result;
	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(R2OConstants.ARG_RESTRICTION_TAG + " ");
		if(this.onParam != null && this.onParam != "") {
			result.append(R2OConstants.ON_PARAM_ATTRIBUTE + "=\"" + onParam + "\"");
		}
		result.append(">\n");
		
		result.append(this.restriction.toString() + "\n");
		result.append("</" + R2OConstants.ARG_RESTRICTION_TAG + ">");
		
		return result.toString();
	}


	public R2ORestriction getRestriction() {
		return restriction;
	}


	String getOnParam() {
		return onParam;
	}
	
	
	
}
