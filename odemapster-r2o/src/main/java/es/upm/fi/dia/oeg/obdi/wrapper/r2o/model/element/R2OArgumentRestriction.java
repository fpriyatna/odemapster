package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2OArgumentRestriction implements R2OElement {
	private static Logger logger = Logger.getLogger(R2OArgumentRestriction.class);
	
	// (26) arg-restrict::= parameter-selector restriction
	// (27) parameter-selector::= on-param literal
	private String onParam;
	private R2ORestriction restriction;
	
	
	public R2OArgumentRestriction(Element element) throws ParseException {
		this.parse(element);
	}
	
	public R2OArgumentRestriction(R2ORestriction restriction) {
		super();
		this.restriction = restriction;
	}


	private R2OArgumentRestriction() {
	}


	public void parse(Element element) throws ParseException {
		//R2OArgumentRestriction result = new R2OArgumentRestriction();
		this.onParam = element.getAttribute(R2OConstants.ON_PARAM_ATTRIBUTE);
		
		Element elementFirstChild = XMLUtility.getFirstElement(element);
		if(elementFirstChild != null) {
			String nodeName = elementFirstChild.getNodeName();
			
			if(nodeName.equalsIgnoreCase(R2OConstants.HAS_VALUE_TAG)) {
				this.restriction = new R2OConstantRestriction(elementFirstChild);
			} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_COLUMN_TAG)) {
				this.restriction = new R2OColumnRestriction(elementFirstChild);
			} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_TRANSFORMATION_TAG)) {
				this.restriction = new R2OTransformationRestriction(elementFirstChild);
			} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_SQL_TAG)) {
				this.restriction = new R2OSQLRestriction(elementFirstChild);
			} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_CONCEPT_TAG)) {
				this.restriction = new R2OConceptRestriction(elementFirstChild);
			} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_TABLE_TAG)) {
				this.restriction = new R2OTableRestriction(elementFirstChild);
			} else {
				String errorMessage = "Unsupported element : " + nodeName;
				logger.error(errorMessage);
				throw new ParseException(errorMessage);
			}
			
		}
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
