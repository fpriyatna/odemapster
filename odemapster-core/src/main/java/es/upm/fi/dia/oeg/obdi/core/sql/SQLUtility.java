package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.oeg.obdi.core.utility.SQLUtility2;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

public class SQLUtility {

	public static ZExpression combineExpresions(ZExp exp1
			, Collection<? extends ZExp> exps, String logicalOperator
			) {
		Collection<ZExp> expressions = new Vector<ZExp>();
		if(exp1 != null) {
			expressions.add(exp1);	
		}
		
		if(exps != null) {
			expressions.addAll(exps);	
		}
		
		ZExpression result = SQLUtility.combineExpresions(expressions, logicalOperator);
		return result;
	}

		
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

	public boolean isSubqueryEliminationPossible(SQLLogicalTable leftTable, SQLLogicalTable rightTable) {
		boolean leftTablePossible;
		if(leftTable instanceof SQLFromItem) {
			leftTablePossible = true;
		} else if(leftTable instanceof SQLQuery) {
			SQLQuery leftTableSQLQuery = (SQLQuery) leftTable;
			Vector<ZFromItem> fromItems = leftTableSQLQuery.getFrom();
			leftTablePossible = true;
			for(ZFromItem fromItem : fromItems) {
				if(!(fromItem instanceof SQLFromItem)) {
					leftTablePossible = false;
				}
			}
		} else {
			
		}
			
		return false;
	}
	
	public IQuery joinQuery(Collection<ZSelectItem> selectItems, IQuery leftTable, IQuery rightTable, String joinType, ZExpression onExpression) {
		
		return null;
	}
	
	public static String printSelectItems(Collection<ZSelectItem> selectItems, boolean distinct ) {
		String selectSQL = "SELECT ";
		
		if(distinct) {
			selectSQL += " DISTINCT ";
		}

		if(selectItems != null && selectItems.size()!=0) {
			for(ZSelectItem mainQuerySelectItem : selectItems) {
				String selectItemAlias = mainQuerySelectItem.getAlias();
				mainQuerySelectItem.setAlias("");
				String selectItemString = mainQuerySelectItem.toString();
				if(selectItemAlias != null && !selectItemAlias.equals("")) {
					selectItemString += " AS " + selectItemAlias;
					mainQuerySelectItem.setAlias(selectItemAlias);
				}
				selectSQL = selectSQL + selectItemString + ", "; 
			}
			//remove the last coma and space
			selectSQL = selectSQL.substring(0, selectSQL.length() - 2);
		} else {
			selectSQL += " * ";
		}

		return selectSQL;		
	}
	
	public static String getValueWithoutAlias(ZSelectItem selectItem) {
		String result;
		
		String selectItemAlias = selectItem.getAlias();
		if(selectItemAlias != null && !selectItemAlias.equals("")) {
			selectItem.setAlias("");
			result = selectItem.toString();
			selectItem.setAlias(selectItemAlias);
		} else {
			result = selectItem.toString();
		}
		return result;
	}
	
	static Vector<ZOrderBy> pushOrderByDown(Collection<ZOrderBy> oldOrderByCollection,
			Map<String, ZSelectItem> mapInnerAliasSelectItem) {
		Map<ZConstant, ZConstant> whereReplacement = new LinkedHashMap<ZConstant, ZConstant>();
		for(String alias : mapInnerAliasSelectItem.keySet()) {
			ZConstant aliasColumn = new ZConstant(alias, ZConstant.COLUMNNAME);
			ZSelectItem selectItem = mapInnerAliasSelectItem.get(alias);
			ZConstant zConstant;
			if(selectItem.isExpression()) {
				String selectItemValue = selectItem.getExpression().toString();
				zConstant = new ZConstant(selectItemValue, ZConstant.UNKNOWN);
			} else {
				String selectItemTable = selectItem.getTable();
				String selectItemColumn = selectItem.getColumn();
				String selectItemValue;
				if(selectItemTable != null && !selectItemTable.equals("")) {
					selectItemValue = selectItemTable + "." + selectItemColumn;  
				} else {
					selectItemValue = selectItemColumn; 
				}
				zConstant = new ZConstant(selectItemValue, ZConstant.COLUMNNAME);
			}
			whereReplacement.put(aliasColumn, zConstant);
		}

		SQLUtility2 sqlUtility2 = new SQLUtility2();
		Vector<ZOrderBy> newOrderByCollection = new Vector<ZOrderBy>();
		for(ZOrderBy oldOrderBy : oldOrderByCollection) {
			ZExp orderByExp = oldOrderBy.getExpression();
			ZExp newOrderByExp = sqlUtility2.replaceExp(orderByExp, whereReplacement);
			ZOrderBy newOrderBy = new ZOrderBy(newOrderByExp);
			newOrderBy.setAscOrder(oldOrderBy.getAscOrder());
			newOrderByCollection.add(newOrderBy);
		}
		return newOrderByCollection;
		//this.setOrderBy(newOrderByCollection);
	}
	
	public static Collection<ZExpression> containsPrefix(ZExp exp, String prefix) {
		Collection<ZExpression> result = new HashSet<ZExpression>();
		
		if(exp instanceof ZExpression) {
			ZExpression expExpression = (ZExpression) exp;
			Vector<ZExp> operands = expExpression.getOperands();
			for(ZExp operand : operands) {
				if(operand instanceof ZConstant) {
					String operandString = operand.toString();
					operandString = operandString.replaceAll("\"", "");
					operandString = operandString.replaceAll("\'", "");
					if(operandString.contains(prefix + ".")) {
						result.add(expExpression);	
					}
				} else {
					Collection<ZExpression> resultAux = SQLUtility.containsPrefix(operand, prefix);
					result.addAll(resultAux);
				}
			}
		}
		
		return result;
	}
}
