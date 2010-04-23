package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class Restriction implements R2OElement {
	private RestrictionType restrictionType;
	public enum RestrictionType {
		HAS_VALUE, HAS_COLUMN, HAS_TRANSFORMATION
	}

	//	(28) restriction::= has-value constant-value |
	//    has-column literal |
	//    has-transform transformation
	private String hasValue;
	private String hasColumn;
	private TransformationExpression hasTransformation;
	
	@Override
	public Restriction parse(Element element) throws ParseException {
		Restriction result = new Restriction();
		
		String nodeName = element.getNodeName();
		if(nodeName.equalsIgnoreCase(R2OConstants.HAS_VALUE_TAG)) {
			result.restrictionType = RestrictionType.HAS_VALUE;
			String content = element.getTextContent();
			if(content != null) {
				result.hasValue = content.trim();
			}
		} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_COLUMN_TAG)) {
			result.restrictionType = RestrictionType.HAS_COLUMN;
			String content = element.getTextContent();
			if(content != null) {
				result.hasColumn = content.trim();
			}
		} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_TRANSFORMATION_TAG)) {
			result.restrictionType = RestrictionType.HAS_TRANSFORMATION;
			result.hasTransformation = new TransformationExpression().parse(element);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(this.restrictionType == RestrictionType.HAS_VALUE) {
			result.append("<" + R2OConstants.HAS_VALUE_TAG + ">");
			result.append(this.hasValue);
			result.append("</" + R2OConstants.HAS_VALUE_TAG + ">");
		} else if(this.restrictionType == RestrictionType.HAS_COLUMN) {
			result.append("<" + R2OConstants.HAS_COLUMN_TAG + ">");
			result.append(this.hasColumn);
			result.append("</" + R2OConstants.HAS_COLUMN_TAG + ">");
		} else if(this.restrictionType == RestrictionType.HAS_TRANSFORMATION) {
			result.append(XMLUtility.toOpenTag(R2OConstants.HAS_TRANSFORMATION_TAG) + "\n");
			result.append(this.hasTransformation.toString() + "\n");
			result.append(XMLUtility.toOpenTag(R2OConstants.HAS_TRANSFORMATION_TAG) + "\n");
		}
		
		return result.toString();
	}

	public RestrictionType getRestrictionType() {
		return restrictionType;
	}

	public String getHasColumn() {
		return hasColumn;
	}

	public String getHasValue() {
		return hasValue;
	};
	
	
}
