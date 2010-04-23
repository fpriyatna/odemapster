package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction.RestrictionType;

public class Condition implements R2OElement {
	//(24) condition::= primitive-condition (arg-restriction arg-restrict)*
	private String primitiveCondition;
	private List<ArgumentRestriction> argRestricts;
	private String operId;
	
	@Override
	public Condition parse(Element element) throws ParseException {
		Condition result = new Condition();
		
		result.primitiveCondition = element.getNodeName();
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(result.primitiveCondition)) {
			result.operId = element.getAttribute(R2OConstants.OPER_ID_ATTRIBUTE);
		}
		NodeList argRestrictionElements = element.getElementsByTagName(R2OConstants.ARG_RESTRICTION_TAG);
		result.argRestricts = new ArrayList<ArgumentRestriction>();
		for(int i=0; i<argRestrictionElements.getLength();i++) {
			Element argRestrictionElement = (Element) argRestrictionElements.item(i);
			ArgumentRestriction argRestrictionObject = new ArgumentRestriction().parse(argRestrictionElement);
			result.argRestricts.add(argRestrictionObject);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(this.primitiveCondition)) {
			result.append("<" + this.primitiveCondition+ " ");
			result.append(R2OConstants.OPER_ID_ATTRIBUTE + "=\"" + this.operId + "\">\n");
		} else {
			result.append("<" + this.primitiveCondition + ">\n");
		}
		
		for(ArgumentRestriction argRestrict : this.argRestricts) {
			result.append(argRestrict.toString() + "\n");
		}
		result.append("</" + this.primitiveCondition + ">");
		return result.toString();
	}
	
	public Collection<String> getInvolvedTables() {
		
		Vector<String> result = new Vector<String>();
		
		Collection<String> involvedColumns = this.getInvolvedColumns();
		for(String columnName : involvedColumns) {
			String tableName = columnName.substring(0, columnName.lastIndexOf("."));
			result.add(tableName);
		}
		
		return result;
	}
	
	public Collection<String> getInvolvedColumns() {
		Vector<String> result = new Vector<String>();
		
		for(ArgumentRestriction argRestriction : this.argRestricts) {
			Restriction restriction = argRestriction.getRestriction();
			RestrictionType restrictionType = restriction.getRestrictionType();
			if(restrictionType == RestrictionType.HAS_COLUMN) {
				String columnName = restriction.getHasColumn(); 
				result.add(columnName);
			}
		}
		return result;
	}

	public String getPrimitiveCondition() {
		return primitiveCondition;
	}

	public String getOperId() {
		return operId;
	}

	public List<ArgumentRestriction> getArgRestricts() {
		return argRestricts;
	}	
	
	public Restriction getArgRestricts(String onParam) {
		Restriction result = null;
		
		if(onParam != null) {
			for(ArgumentRestriction argRestrict : argRestricts) {
				if(onParam.equals(argRestrict.getOnParam())) {
					result = argRestrict.getRestriction();
				}
			}			
		}

		return result;
	}
}
