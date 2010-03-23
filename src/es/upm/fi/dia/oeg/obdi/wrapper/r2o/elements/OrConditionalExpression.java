package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class OrConditionalExpression extends Expression {
	//(23) orcond-expr::= condition | OR orcond-expr condition
	private Condition condition;
	private boolean usingOr;
	private OrConditionalExpression orCondExpr;
	
	@Override
	R2OElement parse(Element element) throws R2OParserException {
		OrConditionalExpression result = new OrConditionalExpression();
		
		if(element.getNodeName().equalsIgnoreCase(Constants.OR_TAG)) {
			result.usingOr = true;
			
			// TODO Implement this!
			result.orCondExpr = null;
		} else {
			result.usingOr = false;
			result.condition = (Condition) new Condition().parse(element);
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if(this.usingOr) {
			// TODO Implement this!
			result.append(this.orCondExpr.toString());
		} else {
			result.append(this.condition.toString());
		}
		
		return result.toString();
	}
	
	
}
