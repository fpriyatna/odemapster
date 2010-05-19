package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.reasoner.rulesys.Rule.ParserException;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class R2OSelector implements R2OElement {
	
	
//	(38) selector::= selector (applies-if cond-expr)?
//            (aftertransform transformation)?
	private R2OConditionalExpression appliesIf;
	private R2OTransformationExpression afterTransform;
	
	@Override
	public R2OSelector parse(Element element) throws ParseException {
		R2OSelector result = new R2OSelector();
		NodeList afterTransformElements = element.getElementsByTagName(R2OConstants.AFTERTRANSFORM_TAG);
		if(afterTransformElements != null && afterTransformElements.getLength() > 0) {
			Element afterTransformElement = (Element) afterTransformElements.item(0);
			result.afterTransform = new R2OTransformationExpression().parse(afterTransformElement);
		}
		
		
		NodeList appliesIfElements = element.getElementsByTagName(R2OConstants.APPLIES_IF_TAG);
		if(appliesIfElements != null && appliesIfElements.getLength() > 0) {
			Element appliesIfElement = (Element) appliesIfElements.item(0);
			Element conditionalExpressionElement = XMLUtility.getChildElements(appliesIfElement).get(0);
			result.appliesIf = new R2OConditionalExpression().parse(conditionalExpressionElement);
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

	public R2OConditionalExpression getAppliesIf() {
		return appliesIf;
	}

	public R2OTransformationExpression getAfterTransform() {
		return afterTransform;
	}
	
	
}
