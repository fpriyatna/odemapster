package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class R2ORestriction implements R2OElement {
	private RestrictionType restrictionType;
	public enum RestrictionType {
		HAS_VALUE, HAS_COLUMN, HAS_TRANSFORMATION, HAS_SQL
	}

	//	(28) restriction::= has-value constant-value |
	//    has-column literal |
	//    has-transform transformation
	private Object hasValue;
	private String hasColumn;
	private String hasSQL;
	private String restrictionDataType;
	private R2OTransformationExpression hasTransformation;
	private String dateFormat;
	private String alias;
	
	@Override
	public R2ORestriction parse(Element element) throws ParseException  {
		R2ORestriction result = new R2ORestriction();

		String dataType = element.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		if(dataType != null && dataType != "") {
			result.restrictionDataType = dataType;
		}

		String alias = element.getAttribute(R2OConstants.ALIAS_ATTRIBUTE);
		if(alias != null && alias != "") {
			result.alias = alias;
		}

		String nodeName = element.getNodeName();
		if(nodeName.equalsIgnoreCase(R2OConstants.HAS_VALUE_TAG)) {
			result.restrictionType = RestrictionType.HAS_VALUE;
			String content = element.getTextContent();
			if(content != null) {
				if(result.restrictionDataType == null) {
					result.hasValue = content;
				} else if(result.restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_STRING)) {
					result.hasValue = content;
				} else if(result.restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_DOUBLE)) {
					result.hasValue = Double.parseDouble(content);
				} else if(result.restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_DATE)) {
					String dateFormat = element.getAttribute(R2OConstants.DATE_FORMAT_ATTRIBUTE);
					DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
					if(dateFormat != null && !dateFormat.equals("")) {
						df = new SimpleDateFormat(dateFormat);
					} else {
						df = new SimpleDateFormat("yyyy/MM/dd");
					}
					
					try {
						result.hasValue = df.parse(content);
					} catch(Exception e) {
						throw new ParseException("Error parsing " + content + " because " + e.getMessage());
					}
				} else if(result.restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_INTEGER)) {
					result.hasValue = Integer.parseInt(content);
				} else {
					
				}
				
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
		} else if(nodeName.equalsIgnoreCase(R2OConstants.HAS_SQL_TAG)) {
			result.restrictionType = RestrictionType.HAS_SQL;
			result.hasSQL = element.getTextContent();
			
		} else {
			throw new ParseException("Unsupported element : " + nodeName);
		}


		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(this.restrictionType == RestrictionType.HAS_VALUE) {
			result.append("<" + R2OConstants.HAS_VALUE_TAG);
			if(this.restrictionDataType != null) {
				result.append(" " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.restrictionDataType + "\"");
			}

			if(this.alias != null && this.alias != "") {
				result.append(" " + R2OConstants.ALIAS_ATTRIBUTE + "=\"" + this.alias + "\"");
			}
			
			if(this.dateFormat != null) {
				result.append(" " + R2OConstants.DATE_FORMAT_ATTRIBUTE + "=\"" + this.dateFormat + "\"");
			}
			
			result.append(">");
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
			result.append(XMLUtility.toCloseTag(R2OConstants.HAS_TRANSFORMATION_TAG) + "\n");
		} else if(this.restrictionType == RestrictionType.HAS_SQL) {
			result.append("<" + R2OConstants.HAS_SQL_TAG);

			if(this.alias != null && this.alias != "") {
				result.append(" " + R2OConstants.ALIAS_ATTRIBUTE + "=\"" + this.alias + "\"");
			}
			if(this.restrictionDataType != null && this.restrictionDataType != "") {
				result.append(" " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.restrictionDataType + "\"");
			}

			result.append(">");
			
			result.append(this.hasSQL + "\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.HAS_SQL_TAG) + "\n");
		}
		
		return result.toString();
	}

	public RestrictionType getRestrictionType() {
		return restrictionType;
	}

	public String getHasColumn() {
		return hasColumn;
	}

	public Object getHasValue() {
		return hasValue;
	}

	public String getRestrictionDataType() {
		return restrictionDataType;
	}

	public R2OTransformationExpression getHasTransformation() {
		return hasTransformation;
	}

	public String getHasSQL() {
		return hasSQL;
	}

	public String getAlias() {
		return alias;
	};
	
	
}
