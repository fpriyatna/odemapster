package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.Collection;
import java.util.Iterator;

import es.upm.fi.dia.oeg.obdi.core.Constants;

import Zql.ZExp;
import Zql.ZExpression;

public class SQLUtility {

	public static ZExpression combineExpresions(
			Collection<? extends ZExp> exps, String logicalOperator
			) {
		Iterator<? extends ZExp> it = exps.iterator();
		ZExpression result = null;
		exps.size();

		if(exps.size() == 1) {
			ZExp exp = it.next();
			if(exp instanceof ZExpression) {
				result = (ZExpression) exp;
			} else {
				result = new ZExpression(it.next().toString());	
			}
		} else if(exps.size() > 1) {
			result = new ZExpression("AND");
			while(it.hasNext()) {
				result.addOperand(it.next());
			}
		}

		return result;
	}

	public static ZExpression combineExpressions(ZExp exp1, ZExp exp2,
			String logicalOperator) {
		ZExpression result;
		
		if(exp1 == null && exp2 == null) {
			result = null;
		} else if(exp1 == null) {
			if(exp2 instanceof ZExpression) {
				result = (ZExpression) exp2; 
			} else {
				result = new ZExpression(exp2.toString());	
			}
		} else if (exp2 == null) {
			if(exp1 instanceof ZExpression) {
				result = (ZExpression) exp1;
			} else {
				result = new ZExpression(exp1.toString());	
			}
		} else {
			result = new ZExpression(logicalOperator, exp1, exp2);	
		}
		
		return result;
	}

}
