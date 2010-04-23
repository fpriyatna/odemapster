package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction.RestrictionType;

public class ConditionalExpression extends Expression {
	// (22) cond-expr ::= orcond-expr | AND orcond-expr	--> original R2O
	// (22) cond-expr ::= condition | AND cond-expr cond-expr | OR cond-expr cond-expr --> modified R2O
	private String operator;
	private Condition condition;
	private Collection<ConditionalExpression> condExprs;


	/*
	private boolean usingAnd;
	private OrConditionalExpression orCondExpr1;
	 */

	public Collection<ConditionalExpression> getCondExprs() {
		return condExprs;
	}

	public Condition getCondition() {
		return condition;
	} 




	public Collection<String> getInvolvedColumns() {
		Collection<String> result = new Vector<String>();

		if(this.operator == null) {
			result.addAll(this.condition.getInvolvedColumns());
		} else {
			for(ConditionalExpression condExpr : condExprs) {
				result.addAll(condExpr.getInvolvedColumns());
			}			
		}

		return result;
	}

	public Collection<String> getInvolvedTables() {
		Collection<String> result = new Vector<String>();

		if(this.operator == null) {
			result.addAll(this.condition.getInvolvedTables());
		} else {
			for(ConditionalExpression condExpr : condExprs) {
				result.addAll(condExpr.getInvolvedTables());
			}
		}

		return result;
	}

	public String getOperator() {
		return operator;
	}

	@Override
	public ConditionalExpression parse(Element element) throws ParseException {
		ConditionalExpression result = new ConditionalExpression();
		result.condExprs = new ArrayList<ConditionalExpression>();

		if(element.getNodeName().equalsIgnoreCase(R2OConstants.AND_TAG)) {
			result.operator = R2OConstants.AND_TAG;
			List<Element> condExprsElements = XMLUtility.getChildElements(element);
			ConditionalExpression condExpr1 = new ConditionalExpression().parse(condExprsElements.get(0));
			ConditionalExpression condExpr2 = new ConditionalExpression().parse(condExprsElements.get(1));
			result.condExprs.add(condExpr1);
			result.condExprs.add(condExpr2);
		} if(element.getNodeName().equalsIgnoreCase(R2OConstants.OR_TAG)) {
			result.operator = R2OConstants.OR_TAG;
			List<Element> condExprsElements = XMLUtility.getChildElements(element);
			ConditionalExpression condExpr1 = new ConditionalExpression().parse(condExprsElements.get(0));
			ConditionalExpression condExpr2 = new ConditionalExpression().parse(condExprsElements.get(1));
			result.condExprs.add(condExpr1);
			result.condExprs.add(condExpr2);

			//result.orCondExpr1 = (OrConditionalExpression) new OrConditionalExpression().parse(element);
		} else {
			result.condition = new Condition().parse(element);

			/*
			result.usingAnd = false;
			result.orCondExpr1 = (OrConditionalExpression) new OrConditionalExpression().parse(element);
			 */
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(this.operator == null) {
			result.append(this.condition.toString());
		} else {
			result.append(XMLUtility.toOpenTag(this.operator) + "\n");
			for(ConditionalExpression condExpr : condExprs) {
				result.append(condExpr.toString() + "\n");
			}
			result.append(XMLUtility.toCloseTag(this.operator) + "\n");
		}

		return result.toString();
	}

}
