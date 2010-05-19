package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class R2ORestriction implements R2OElement {
	private RestrictionType restrictionType;
	public enum RestrictionType {
		HAS_VALUE, HAS_COLUMN, HAS_TRANSFORMATION
	}

	//	(28) restriction::= has-value constant-value |
	//    has-column literal |
	//    has-transform transformation
	private String hasValue;
	private String hasColumn;
	private String restrictionDataType;
	private R2OTransformationExpression hasTransformation;
	
	@Override
	public R2ORestriction parse(Element element) throws ParseException {
		R2ORestriction result = new R2ORestriction();
		
		String nodeName = element.getNodeName();
		if(nodeName.equalsIgnoreCase(R2OConstants.HAS_VALUE_TAG)) {
			result.restrictionType = RestrictionType.HAS_VALUE;
			String content = element.getTextContent();
			if(content != null) {
				result.hasValue = content;
			}
		} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_COLUMN_TAG)) {
			result.restrictionType = RestrictionType.HAS_COLUMN;
			String content = element.getTextContent();
			if(content != null) {
				result.hasColumn = content;
			}
		} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_TRANSFORMATION_TAG)) {
			result.restrictionType = RestrictionType.HAS_TRANSFORMATION;
			result.hasTransformation = new R2OTransformationExpression().parse(element);
		}

		String dataType = element.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		if(dataType != null && dataType != "") {
			result.restrictionDataType = dataType;
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(this.restrictionType == RestrictionType.HAS_VALUE) {
			if(this.restrictionDataType == null) {
				result.append("<" + R2OConstants.HAS_VALUE_TAG + ">");
			} else {
				result.append("<" + R2OConstants.HAS_VALUE_TAG + " " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.restrictionDataType + "\">");
			}
			result.append(this.hasValue);
			result.append("</" + R2OConstants.HAS_VALUE_TAG + ">");
		} else if(this.restrictionType == RestrictionType.HAS_COLUMN) {
			if(this.restrictionDataType == null) {
				result.append("<" + R2OConstants.HAS_COLUMN_TAG + ">");
			} else {
				result.append("<" + R2OConstants.HAS_COLUMN_TAG + " " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.restrictionDataType + "\">");
			}
			
			result.append(this.hasColumn);
			result.append("</" + R2OConstants.HAS_COLUMN_TAG + ">");
		} else if(this.restrictionType == RestrictionType.HAS_TRANSFORMATION) {
			if(this.restrictionDataType == null) {
				result.append("<" + R2OConstants.HAS_TRANSFORMATION_TAG + ">");
			} else {
				result.append("<" + R2OConstants.HAS_TRANSFORMATION_TAG + " " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.restrictionDataType + "\">");
			}
			
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
	}

	public String getRestrictionDataType() {
		return restrictionDataType;
	};
	
	
}
