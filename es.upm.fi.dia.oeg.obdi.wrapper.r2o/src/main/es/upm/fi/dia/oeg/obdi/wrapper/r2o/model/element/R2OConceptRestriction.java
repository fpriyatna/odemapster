package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
	
public class R2OConceptRestriction extends R2ORestriction {
	private String conceptName;
	
	public R2OConceptRestriction(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}
	
	@Override
	public void parse(Element xmlElement) throws ParseException {
		this.conceptName = xmlElement.getTextContent();
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append(XMLUtility.toOpenTag(R2OConstants.HAS_CONCEPT_TAG));
		result.append(this.conceptName);
		result.append(XMLUtility.toCloseTag(R2OConstants.HAS_CONCEPT_TAG));
		return result.toString();
	}

	public String getConceptName() {
		return conceptName;
	}


	
	

}
