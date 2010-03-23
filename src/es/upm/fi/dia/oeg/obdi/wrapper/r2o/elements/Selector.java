package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class Selector extends R2OElement {
	
	
//	(38) selector::= selector (applies-if cond-expr)?
//            (aftertransform transformation)?
	private ConditionalExpression appliesIf;
	private TransformationExpression afterTransform;
	
	@Override
	R2OElement parse(Element element) throws R2OParserException {
		Selector result = new Selector();
		NodeList afterTransformElements = element.getElementsByTagName(Constants.AFTERTRANSFORM_TAG);
		if(afterTransformElements != null && afterTransformElements.getLength() > 0) {
			Element afterTransformElement = (Element) afterTransformElements.item(0);
			result.afterTransform = (TransformationExpression) new TransformationExpression().parse(afterTransformElement);
		}
		
		
		NodeList appliesIfElements = element.getElementsByTagName(Constants.APPLIES_IF_TAG);
		if(appliesIfElements != null && appliesIfElements.getLength() > 0) {
			Element appliesIfElement = (Element) appliesIfElements.item(0);
			result.appliesIf = (ConditionalExpression) new ConditionalExpression().parse(appliesIfElement);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + Constants.SELECTOR_TAG + ">\n");
		if(appliesIf != null) {
			result.append(appliesIf.toString() + "\n");
		}
		
		if(afterTransform != null) {
			result.append("<" + Constants.AFTERTRANSFORM_TAG + ">\n");
			result.append(afterTransform.toString() + "\n");
			result.append("</" + Constants.AFTERTRANSFORM_TAG + ">\n");
		}
		
		result.append("</" + Constants.SELECTOR_TAG + ">");
		return result.toString();
	}
	
	
}
