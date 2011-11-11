package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2OTransformationRestriction extends R2ORestriction {
	private R2OTransformationExpression transformationExpression;
	
	public R2OTransformationRestriction(R2OTransformationExpression transformationExpression) {
		this.transformationExpression = transformationExpression;
	}
	
	public R2OTransformationRestriction(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}

	@Override
	public void parse(Element xmlElement) throws ParseException {
		//R2OTransformationRestriction result = new R2OTransformationRestriction();
		this.transformationExpression = new R2OTransformationExpression(xmlElement);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(XMLUtility.toOpenTag(R2OConstants.HAS_TRANSFORMATION_TAG) + "\n");
		result.append(this.transformationExpression.toString() + "\n");
		result.append(XMLUtility.toCloseTag(R2OConstants.HAS_TRANSFORMATION_TAG));
		
		return result.toString();
	}

	public R2OTransformationExpression getTransformationExpression() {
		return transformationExpression;
	}

	public void setTransformationExpression(
			R2OTransformationExpression transformationExpression) {
		this.transformationExpression = transformationExpression;
	}

	
}
