package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class ConditionalExpression extends Expression {
	// (22) cond-expr ::= orcond-expr | AND orcond-expr
	private boolean usingAnd;
	private OrConditionalExpression orCondExpr;

	@Override
	R2OElement parse(Element element) throws R2OParserException {
		ConditionalExpression result = new ConditionalExpression();
		
		if(element.getNodeName().equalsIgnoreCase(Constants.AND_TAG)) {
			result.usingAnd = true;
			// TODO Auto-generated method stub
		} else {
			result.usingAnd = false;
			result.orCondExpr = (OrConditionalExpression) new OrConditionalExpression().parse(element);
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(this.usingAnd) {
			// TODO Auto-generated method stub
		} else {
			result.append(this.orCondExpr.toString());
		}
		
		return result.toString();
	} 
	
	
}
