package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	private Collection<R2OSelector> rmSelectors;
	private String joinType;

	public static final String JOINS_TYPE_INNER = "inner";
	public static final String JOINS_TYPE_LEFT = "left";
	
	@Override
	public R2ORelationMapping parse(Element rmElement) throws ParseException {
		R2ORelationMapping result = new R2ORelationMapping();
		result.name = rmElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		//parse identifiedBy attribute
		result.id = rmElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);
		
		int noOfBases = 0;
		result.toConcept = rmElement.getAttribute(R2OConstants.TO_CONCEPT_ATTRIBUTE);
		if(result.toConcept == "") { result.toConcept = null; }
		if(result.toConcept != null) { noOfBases++;}
		List<Element> rmSelectorsElements = XMLUtility.getChildElementsByTagName(rmElement, R2OConstants.SELECTOR_TAG);
		if(rmSelectorsElements != null) { noOfBases++;}		
		if(noOfBases == 0) {
			String errorMessage = "Specify either toConcept+joinsVia or selector!";
			throw new ParseException(errorMessage);			
		} else if (noOfBases > 1) {
			String errorMessage = "Specify only one of toConcept+joinsVia or selector!";
			throw new ParseException(errorMessage);			
		}
		
		if(result.toConcept != null) {
			Element joinsViaElement = XMLUtility.getFirstChildElementByTagName(rmElement, R2OConstants.JOINS_VIA_TAG);
			if(joinsViaElement != null) {
				result.joinType = joinsViaElement.getAttribute(R2OConstants.JOINS_TYPE_ATTRIBUTE);
				if(result.joinType == null || result.joinType == "") {
					result.joinType = R2ORelationMapping.JOINS_TYPE_LEFT;
					//throw new ParseException("join-type attribute needs to be defined on joins-via element");
				}
				
				Element joinsViaConditionElement = XMLUtility.getFirstChildElementByTagName(joinsViaElement, R2OConstants.CONDITION_TAG);
				result.joinsVia = new R2OConditionalExpression().parse(joinsViaConditionElement);			
			}
		}
		
		if(rmSelectorsElements != null) {
			result.rmSelectors = new ArrayList<R2OSelector>();
			for(Element childElement : rmSelectorsElements) {
				R2OSelector selector = new R2OSelector().parse(childElement);
				result.rmSelectors.add(selector);
			}						
		}
		


		
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
		if(this.toConcept != null) {
			result.append(R2OConstants.TO_CONCEPT_ATTRIBUTE +"=\"" + this.toConcept + "\" ");
		}
		
		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}
		result.append(">\n");

		
		if(this.joinsVia != null) {
			result.append("<" + R2OConstants.JOINS_VIA_TAG + " ");
			result.append(R2OConstants.JOINS_TYPE_ATTRIBUTE+"=\"" + this.joinType + "\" ");
			result.append(">\n");
			
			result.append(this.joinsVia.toString() + "\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.JOINS_VIA_TAG)+ "\n");
			
		}
		
		if(this.rmSelectors != null) {
			for(R2OSelector selector : this.rmSelectors) {
				result.append(selector.toString() + "\n");
			}
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

	public String getJoinType() {
		return joinType;
	}

	public Collection<R2OSelector> getRmSelectors() {
		return rmSelectors;
	}

}
