package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2OConstantRestriction extends R2ORestriction {
	private Object constantValue;
	private String datatype;
	private String dateFormat;


	public R2OConstantRestriction() {
	}

	public R2OConstantRestriction(Element element) throws ParseException {
		this.parse(element);
	}
	
	@Override
	public void parse(Element element) throws ParseException {
		//R2OConstantRestriction result = new R2OConstantRestriction();
		
		String content = element.getTextContent();
		String dataType = element.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		if(dataType != null && dataType != "") {
			this.datatype = dataType;
		}
		
		if(content != null) {
			if(dataType == null || dataType == "") {
				this.constantValue = content;
			} else if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_STRING)) {
				this.constantValue = content;
			} else if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DOUBLE)) {
				this.constantValue = Double.parseDouble(content);
			} else if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_NUMBER)) {
				this.constantValue = Integer.parseInt(content);					
			} else if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DATE)) {
				this.dateFormat = element.getAttribute(R2OConstants.DATE_FORMAT_ATTRIBUTE);
				 
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				if(dateFormat != null && !dateFormat.equals("")) {
					df = new SimpleDateFormat(dateFormat);
				} else {
					df = new SimpleDateFormat("yyyy-MM-dd");
					
				}
				
				try {
					this.constantValue = df.parse(content);
					
				} catch(Exception e) {
					throw new ParseException("Error parsing " + content + " because " + e.getMessage());
				}
			} else if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_INTEGER)) {
				this.constantValue = Integer.parseInt(content);
			} else {
				
			}
			
		}
	}



	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.HAS_VALUE_TAG);
		if(this.datatype != null) {
			result.append(" " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.datatype + "\"");
		}


		if(this.dateFormat != null) {
			result.append(" " + R2OConstants.DATE_FORMAT_ATTRIBUTE + "=\"" + this.dateFormat + "\"");
		}
		
		result.append(">");
		result.append(this.getConstantValueAsString());
		
		result.append("</" + R2OConstants.HAS_VALUE_TAG + ">");
		
		return result.toString();
	}




	public String getDatatype() {
		return datatype;
	}



	
	public String getConstantValueAsString() {
		if(this.constantValue instanceof Date) {
			DateFormat df = null;
			if(this.dateFormat != null && !this.dateFormat.equals("")) {
				df = new SimpleDateFormat(dateFormat);
			} else {
				df = new SimpleDateFormat("yyyy-MM-dd");
				
			}
			
			String dateValue = df.format(this.constantValue);
			return dateValue;
		} else {
			return this.constantValue + "";
		}		
	}



	public Object getConstantValue() {
		return constantValue;
	}

	public void setConstantValue(Object constantValue) {
		this.constantValue = constantValue;
	}
	
	
}
