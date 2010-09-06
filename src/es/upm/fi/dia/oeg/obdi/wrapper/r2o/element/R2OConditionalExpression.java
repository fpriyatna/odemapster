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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction.RestrictionType;

public class R2OConditionalExpression extends R2OExpression {
	// (22) cond-expr ::= orcond-expr | AND orcond-expr	--> original R2O
	// (22) cond-expr ::= condition | AND cond-expr cond-expr | OR cond-expr cond-expr --> modified R2O
	private String operator;
	private R2OCondition condition;
	private Collection<R2OConditionalExpression> condExprs;


	/*
	private boolean usingAnd;
	private OrConditionalExpression orCondExpr1;
	 */

	public Collection<R2OConditionalExpression> getCondExprs() {
		return condExprs;
	}

	public R2OCondition getCondition() {
		return condition;
	} 




	public Collection<String> getInvolvedColumns() {
		Collection<String> result = new Vector<String>();

		if(this.operator == null) {
			result.addAll(this.condition.getInvolvedColumns());
		} else {
			for(R2OConditionalExpression condExpr : condExprs) {
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
			for(R2OConditionalExpression condExpr : condExprs) {
				result.addAll(condExpr.getInvolvedTables());
			}
		}

		return result;
	}

	public String getOperator() {
		return operator;
	}

	public boolean isConjuctiveConditionalExpression() {
		if(this.condition != null) {
			return true;
		} else {
			if(this.operator == R2OConstants.AND_TAG) {
				for(R2OConditionalExpression child : this.condExprs) {
					if(!child.isConjuctiveConditionalExpression()) {
						return false;
					}
				}				
			} else {
				return false;
			}
		}

		return true;

	}

	public Collection<R2OCondition> flatConjuctiveConditionalExpression() {
		Collection<R2OCondition> result = new ArrayList<R2OCondition>();

		if(this.isConjuctiveConditionalExpression()) {
			if(this.condition != null) {
				result.add(this.condition);
			} else {
				for(R2OConditionalExpression child : this.condExprs) {
					Collection<R2OCondition> tempresult = child.flatConjuctiveConditionalExpression();
					result.addAll(tempresult);
				}
			}			
		} 


		return result;
	}

	@Override
	public R2OConditionalExpression parse(Element element) throws ParseException {
		R2OConditionalExpression result = new R2OConditionalExpression();
		result.condExprs = new ArrayList<R2OConditionalExpression>();

		if(element.getNodeName().equalsIgnoreCase(R2OConstants.AND_TAG)) {
			result.operator = R2OConstants.AND_TAG;
			List<Element> condExprsElements = XMLUtility.getChildElements(element);
			R2OConditionalExpression condExpr1 = new R2OConditionalExpression().parse(condExprsElements.get(0));
			R2OConditionalExpression condExpr2 = new R2OConditionalExpression().parse(condExprsElements.get(1));
			result.condExprs.add(condExpr1);
			result.condExprs.add(condExpr2);
		} else if(element.getNodeName().equalsIgnoreCase(R2OConstants.OR_TAG)) {
			result.operator = R2OConstants.OR_TAG;
			List<Element> condExprsElements = XMLUtility.getChildElements(element);
			R2OConditionalExpression condExpr1 = new R2OConditionalExpression().parse(condExprsElements.get(0));
			R2OConditionalExpression condExpr2 = new R2OConditionalExpression().parse(condExprsElements.get(1));
			result.condExprs.add(condExpr1);
			result.condExprs.add(condExpr2);

			//result.orCondExpr1 = (OrConditionalExpression) new OrConditionalExpression().parse(element);
		} else {
			result.condition = new R2OCondition().parse(element);

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
			for(R2OConditionalExpression condExpr : condExprs) {
				result.append(condExpr.toString() + "\n");
			}
			result.append(XMLUtility.toCloseTag(this.operator) + "\n");
		}

		return result.toString();
	}

	public boolean isDelegableConditionalExpression(R2OProperties r2oProperties) {
		if(this.getOperator() == null) {
			return this.condition.isDelegableCondition(r2oProperties);
		} else {
			for(R2OConditionalExpression condExpr : this.getCondExprs()) {
				if(condExpr.isDelegableConditionalExpression(r2oProperties) == false) {
					return false;
				}
			}
			return true;
		}
	}
}
