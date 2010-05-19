package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;


public class R2ORelationMapping extends R2OPropertyMapping implements R2OElement, IRelationMapping {
	private String toConcept;
	private R2OConditionalExpression joinsVia;
	
	@Override
	public R2ORelationMapping parse(Element xmlElement) throws ParseException {
		R2ORelationMapping result = new R2ORelationMapping();
		result.name = xmlElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		//parse identifiedBy attribute
		result.id = xmlElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);
		
		result.toConcept = xmlElement.getAttribute(R2OConstants.TO_CONCEPT_ATTRIBUTE);
		
		Element joinsViaElement = XMLUtility.getFirstChildElementByTagName(xmlElement, R2OConstants.JOINS_VIA_TAG);
		Element joinsViaConditionElement = XMLUtility.getFirstChildElementByTagName(joinsViaElement, R2OConstants.CONDITION_TAG);
		result.joinsVia = new R2OConditionalExpression().parse(joinsViaConditionElement);
		
		return result;
	}

	@Override
	public String getRelationName() {
		return this.name;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.DBRELATION_DEF_TAG + " ");
		result.append(R2OConstants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		result.append(R2OConstants.TO_CONCEPT_ATTRIBUTE +"=\"" + this.toConcept + "\" ");
		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}
		result.append(">\n");

		
		if(this.joinsVia != null) {
			result.append(XMLUtility.toOpenTag(R2OConstants.JOINS_VIA_TAG)+ "\n");
			result.append(this.joinsVia.toString() + "\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.JOINS_VIA_TAG)+ "\n");
			
		}
		
		result.append(XMLUtility.toCloseTag(R2OConstants.DBRELATION_DEF_TAG)+ "\n");
		return result.toString();
	}

	public R2OConditionalExpression getJoinsVia() {
		return joinsVia;
	}

	public String getToConcept() {
		return toConcept;
	}

}
