package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.reasoner.rulesys.Rule.ParserException;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class Selector implements R2OElement {
	
	
//	(38) selector::= selector (applies-if cond-expr)?
//            (aftertransform transformation)?
	private ConditionalExpression appliesIf;
	private TransformationExpression afterTransform;
	
	@Override
	public Selector parse(Element element) throws ParseException {
		Selector result = new Selector();
		NodeList afterTransformElements = element.getElementsByTagName(R2OConstants.AFTERTRANSFORM_TAG);
		if(afterTransformElements != null && afterTransformElements.getLength() > 0) {
			Element afterTransformElement = (Element) afterTransformElements.item(0);
			result.afterTransform = new TransformationExpression().parse(afterTransformElement);
		}
		
		
		NodeList appliesIfElements = element.getElementsByTagName(R2OConstants.APPLIES_IF_TAG);
		if(appliesIfElements != null && appliesIfElements.getLength() > 0) {
			Element appliesIfElement = (Element) appliesIfElements.item(0);
			Element conditionalExpressionElement = XMLUtility.getChildElements(appliesIfElement).get(0);
			result.appliesIf = new ConditionalExpression().parse(conditionalExpressionElement);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + R2OConstants.SELECTOR_TAG + ">\n");
		if(appliesIf != null) {
			result.append("<" + R2OConstants.APPLIES_IF_TAG + ">\n");
			result.append(appliesIf.toString() + "\n");
			result.append("</" + R2OConstants.APPLIES_IF_TAG + ">\n");
		}
		
		if(afterTransform != null) {
			result.append("<" + R2OConstants.AFTERTRANSFORM_TAG + ">\n");
			result.append(afterTransform.toString() + "\n");
			result.append("</" + R2OConstants.AFTERTRANSFORM_TAG + ">\n");
		}
		
		result.append("</" + R2OConstants.SELECTOR_TAG + ">");
		return result.toString();
	}

	public ConditionalExpression getAppliesIf() {
		return appliesIf;
	}

	public TransformationExpression getAfterTransform() {
		return afterTransform;
	}
	
	
}
