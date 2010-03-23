package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class Condition extends R2OElement {
	//(24) condition::= primitive-condition (arg-restriction arg-restrict)*
	private String primitiveCondition;
	private List<ArgumentRestriction> argRestricts;
	private String operId;
	
	@Override
	R2OElement parse(Element element) throws R2OParserException {
		Condition result = new Condition();
		
		result.primitiveCondition = element.getNodeName();
		if(Constants.CONDITION_TAG.equalsIgnoreCase(result.primitiveCondition)) {
			result.operId = element.getAttribute(Constants.OPER_ID_ATTRIBUTE);
		}
		NodeList argRestrictionElements = element.getElementsByTagName(Constants.ARG_RESTRICTION_TAG);
		result.argRestricts = new ArrayList<ArgumentRestriction>();
		for(int i=0; i<argRestrictionElements.getLength();i++) {
			Element argRestrictionElement = (Element) argRestrictionElements.item(i);
			ArgumentRestriction argRestrictionObject = (ArgumentRestriction) new ArgumentRestriction().parse(argRestrictionElement);
			result.argRestricts.add(argRestrictionObject);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if(Constants.CONDITION_TAG.equalsIgnoreCase(this.primitiveCondition)) {
			result.append("<" + this.primitiveCondition+ " ");
			result.append(Constants.OPER_ID_ATTRIBUTE + "=\"" + this.operId + "\">\n");
		} else {
			result.append("<" + this.primitiveCondition + ">\n");
		}
		
		for(ArgumentRestriction argRestrict : this.argRestricts) {
			result.append(argRestrict.toString() + "\n");
		}
		result.append("</" + this.primitiveCondition + ">");
		return result.toString();
	}
	
	
}
