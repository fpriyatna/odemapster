package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class Restriction extends R2OElement {
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
	R2OElement parse(Element element) throws R2OParserException {
		Restriction result = new Restriction();
		
		String nodeName = element.getNodeName();
		if(nodeName.equalsIgnoreCase(Constants.HAS_VALUE_TAG)) {
			result.restrictionType = RestrictionType.HAS_VALUE;
			result.hasValue = element.getTextContent();
		} else if(nodeName.equalsIgnoreCase(Constants.HAS_COLUMN_TAG)) {
			result.restrictionType = RestrictionType.HAS_COLUMN;
			result.hasColumn = element.getTextContent();
		} else if(nodeName.equalsIgnoreCase(Constants.HAS_TRANSFORMATION_TAG)) {
			result.restrictionType = RestrictionType.HAS_TRANSFORMATION;
			result.hasTransformation = (TransformationExpression) new TransformationExpression().parse(element);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(this.restrictionType == RestrictionType.HAS_VALUE) {
			result.append("<" + Constants.HAS_VALUE_TAG + ">");
			result.append(this.hasValue);
			result.append("</" + Constants.HAS_VALUE_TAG + ">");
		} else if(this.restrictionType == RestrictionType.HAS_COLUMN) {
			result.append("<" + Constants.HAS_COLUMN_TAG + ">");
			result.append(this.hasColumn);
			result.append("</" + Constants.HAS_COLUMN_TAG + ">");
		} else if(this.restrictionType == RestrictionType.HAS_TRANSFORMATION) {
			// TODO Implement this!
			result.append(this.hasTransformation.toString() + "\n");
		}
		
		return result.toString();
	};
	
	
}
