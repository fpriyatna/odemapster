package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import Zql.ZSelectItem;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;

public class R2OConditionalExpression extends R2OExpression implements Cloneable {
	private Logger logger = Logger.getLogger(R2OConditionalExpression.class);
	
	// (22) cond-expr ::= orcond-expr | AND orcond-expr	--> original R2O
	// (22) cond-expr ::= condition | AND cond-expr cond-expr | OR cond-expr cond-expr --> modified R2O
	private String operator;
	private R2OCondition condition;
	private Collection<R2OConditionalExpression> condExprs;
	private String hasSQL;
	

	/*
	private boolean usingAnd;
	private OrConditionalExpression orCondExpr1;
	 */

	public R2OConditionalExpression(Element element) throws ParseException {
		this.parse(element);
	}
	
	public R2OConditionalExpression(R2OCondition condition) {
		super();
		this.condition = condition;
	}
	
	private R2OConditionalExpression() {
		// TODO Auto-generated constructor stub
	}

	public Collection<R2OConditionalExpression> getCondExprs() {
		return condExprs;
	}

	public R2OCondition getCondition() {
		return condition;
	} 




//	public Collection<String> getInvolvedColumns() {
//		Collection<String> result = new Vector<String>();
//
//		if(this.operator == null) {
//			result.addAll(this.condition.getInvolvedColumns());
//		} else {
//			for(R2OConditionalExpression condExpr : this.condExprs) {
//				result.addAll(condExpr.getInvolvedColumns());
//			}			
//		}
//
//		return result;
//	}

	public Collection<ZSelectItem> getSelectItems() {
	Collection<ZSelectItem> result = new HashSet<ZSelectItem>();

	if(this.operator == null) {
		Collection<ZSelectItem> conditionSelectItems = this.condition.getSelectItems(); 
		result.addAll(conditionSelectItems);
	} else {
		for(R2OConditionalExpression condExpr : this.condExprs) {
			Collection<ZSelectItem> condExprsSelectItems = condExpr.getSelectItems();
			result.addAll(condExprsSelectItems);
		}			
	}

	return result;
	}
	
//	public Collection<ZSelectItem> getInvolvedColumnsSelectItems() {
//		Collection<ZSelectItem> result2 = new Vector<ZSelectItem>();
//		Collection<String> involvedColumns = this.getInvolvedColumns();
//		
//		for(String involvedColumn : involvedColumns) {
//			ZSelectItem selectItem = new ZSelectItem(involvedColumn);
//			result2.add(selectItem);
//		}
//
//		return result2;
//	}
	
//	public Collection<String> getInvolvedTables() {
//		Collection<String> result = new Vector<String>();
//
//		if(this.operator == null) {
//			result.addAll(this.condition.getInvolvedTables());
//		} else {
//			for(R2OConditionalExpression condExpr : condExprs) {
//				result.addAll(condExpr.getInvolvedTables());
//			}
//		}
//
//		return result;
//	}

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

	public static R2OConditionalExpression addCondition(
			R2OConditionalExpression currentCE, String operator, R2OCondition newCondition) {
		
		if(currentCE == null) {
			R2OConditionalExpression result = new R2OConditionalExpression(newCondition);
			return result;
		} else {
			R2OConditionalExpression result = new R2OConditionalExpression();
			result.operator = operator;
			result.condExprs = new ArrayList<R2OConditionalExpression>();

			if(currentCE.condExprs == null || currentCE.condExprs.size() == 0) {
				if(currentCE.condition == null) {
					
				} else {
					R2OConditionalExpression ce1 = new R2OConditionalExpression(newCondition);
					result.condExprs.add(ce1);

					R2OConditionalExpression ce2 = new R2OConditionalExpression(currentCE.condition);
					result.condExprs.add(ce2);
				}
			} else {
				R2OConditionalExpression ce1 = new R2OConditionalExpression();
				ce1.condition = newCondition; 
				result.condExprs.add(ce1);
				
				result.condExprs.addAll(currentCE.condExprs);
			}
			
			return result;
		}
	}

	
	public static R2OConditionalExpression addConditionalExpression(
			R2OConditionalExpression currentCE, String operator
			, R2OConditionalExpression newConditionalExpression) {
		
		if(currentCE == newConditionalExpression) {
			return currentCE; 
		}
		
		if(currentCE == null) {
			R2OConditionalExpression result = newConditionalExpression;
			return result;
		} else {
			if(currentCE.contains(newConditionalExpression)) {
				return currentCE; 
			} else {
				R2OConditionalExpression result = new R2OConditionalExpression();
				result.operator = operator;
				result.condExprs = new ArrayList<R2OConditionalExpression>();

				if(currentCE.condExprs == null || currentCE.condExprs.size() == 0) {
					if(currentCE.condition == null) { //this will not happened
						
					} else {
						result.condExprs.add(newConditionalExpression);

						R2OConditionalExpression ce2 = new R2OConditionalExpression(currentCE.condition);
						result.condExprs.add(ce2);
					}
				} else {
					result.condExprs.add(newConditionalExpression);
					
					result.condExprs.addAll(currentCE.condExprs);
				}
				
				return result;				
			}

		}
	}
	
	@Override
	public void parse(Element element) throws ParseException {
		//R2OConditionalExpression result = new R2OConditionalExpression();
		this.condExprs = new ArrayList<R2OConditionalExpression>();
//		logger.debug("element.getNodeName() = " + element.getNodeName());
		if(element.getNodeName().equalsIgnoreCase(R2OConstants.AND_TAG)) {
			this.operator = R2OConstants.AND_TAG;
			
			List<Element> condExprsElements = XMLUtility.getChildElements(element);
			for(Element condExprsElement : condExprsElements) {
				
				R2OConditionalExpression condExpr = new R2OConditionalExpression();
				condExpr.parse(condExprsElement);
				this.condExprs.add(condExpr);
			}
		} else if(element.getNodeName().equalsIgnoreCase(R2OConstants.OR_TAG)) {
			this.operator = R2OConstants.OR_TAG;
			
			List<Element> condExprsElements = XMLUtility.getChildElements(element);
			for(Element condExprsElement : condExprsElements) {
				R2OConditionalExpression condExpr = new R2OConditionalExpression();
				condExpr.parse(condExprsElement);
				this.condExprs.add(condExpr);
			}
		} else if(element.getNodeName().equalsIgnoreCase(R2OConstants.HAS_SQL_TAG)) {
			this.hasSQL = element.getTextContent();
		} else if(element.getNodeName().equalsIgnoreCase(R2OConstants.CONDITION_TAG)) {
			this.condition = new R2OCondition(element);

			/*
			result.usingAnd = false;
			result.orCondExpr1 = (OrConditionalExpression) new OrConditionalExpression().parse(element);
			 */
		}

	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if(this.condition != null) {
			result.append(this.condition.toString());
		} else if (this.hasSQL != null) {
			result.append(XMLUtility.toOpenTag(R2OConstants.HAS_SQL_TAG) + "\n");
			result.append(this.hasSQL);
			result.append(XMLUtility.toCloseTag(R2OConstants.HAS_SQL_TAG) + "\n");
		} else if(this.operator != null) {
			result.append(XMLUtility.toOpenTag(this.operator) + "\n");
			for(R2OConditionalExpression condExpr : this.condExprs) {
				result.append(condExpr.toString() + "\n");
			}
			result.append(XMLUtility.toCloseTag(this.operator));
		}

		return result.toString();
	}

	public boolean isDelegableConditionalExpression() {
		
		if(this.getOperator() == null) {
			return this.condition.isDelegableCondition();
		} else {
			for(R2OConditionalExpression condExpr : this.getCondExprs()) {
				if(condExpr.isDelegableConditionalExpression() == false) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	protected R2OConditionalExpression clone(){
		try {
			return (R2OConditionalExpression) super.clone();
		} catch(Exception e) {
			logger.error("Error occured while cloning R2OConditionalExpression object.");
			logger.error("Error message = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public boolean contains(R2OCondition c) {
		if(this.condition != null) {
			if(this.condition.equals(c)) {
				return true;
			} else {
				return false;
			}
		} else if(this.condExprs != null && this.condExprs.size() > 0){
			for(R2OConditionalExpression ce : this.condExprs) {
				if(ce.contains(c)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
	
	public boolean contains(R2OConditionalExpression ce) {
		if(ce.condition != null) {
			if(this.condition != null && this.condition.equals(ce.condition)) {
				return true;
			}
		} else if(ce.hasSQL != null) {
			if(this.hasSQL != null && this.hasSQL.equals(ce.hasSQL)) {
				return true;
			}
		} else if (ce.operator != null) {
			if(this.operator != null && ce.operator.equals(this.operator)) {
				for(R2OConditionalExpression thisCE : this.condExprs) {
					for(R2OConditionalExpression ce2 : ce.condExprs) {
						if(!thisCE.contains(ce2)) {
							return false;
						}
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	/*
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		
		R2OConditionalExpression ce2 = (R2OConditionalExpression) obj;
		if(this.condition != null && ce2.condition != null && this.condition.equals(ce2.condition)) {
			return true;
		}
		if(this.hasSQL != null && ce2.hasSQL != null && this.hasSQL.equals(ce2.hasSQL)) {
			return true;
		}
		if(this.operator != null && ce2.operator != null && this.operator.equals(ce2.operator) {
			return false;
		}
		
		return false;
			

	}
	*/
	
	
}
