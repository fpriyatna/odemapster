package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZGroupBy;
import Zql.ZOrderBy;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZUtils;
import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.QueryTranslatorException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;

public class SQLQuery extends ZQuery implements IQuery {
	private static Logger logger = Logger.getLogger(SQLQuery.class);
	private String joinType;
	private ZExp onExp;

	private Collection<SQLLogicalTable> logicalTables;
	private Collection<SQLJoinQuery> joinQueries;
	private Collection<SQLQuery> joinQueries2;
	//private Collection<SQLQuery> unionQueries;
	private String alias;
	private long slice = -1;
	private long offset = -1;
			
	public void setOffset(long offset) {
		this.offset = offset;
	}

	private boolean distinct = false;

	private String comments;
	
	public SQLQuery(ZQuery zQuery) {
		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("substring", 3);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		ZUtils.addCustomFunction("lower", 1);
		
		if(zQuery.getSelect() != null) { this.addSelect(zQuery.getSelect());}
		if(zQuery.getFrom() != null) { this.addFrom(zQuery.getFrom());}
		if(zQuery.getWhere() != null) { this.addWhere(zQuery.getWhere());}
		if(zQuery.getGroupBy() != null) {this.addGroupBy(zQuery.getGroupBy());}
		
		String tableAlias = "";
		if(this.getFrom().size() == 1) {


		}


	}

	public SQLQuery() {
		super();
		this.addSelect(new Vector<ZSelectItem>());
		this.addFrom(new Vector<ZFromItem>()); 
	}

	public void addFrom(ZFromItem fromItem) {
		if(this.getFrom() == null) {
			this.addFrom(new Vector<ZFromItem>());
		}
		this.getFrom().add(fromItem);
	}

	
	public void addLogicalTable(SQLLogicalTable logicalTable) {
		if(this.logicalTables == null) {
			this.logicalTables = new HashSet<SQLLogicalTable>();
		}

		this.logicalTables.add(logicalTable);
	}


	public void addJoinQuery(SQLJoinQuery joinQuery) {
		if(this.joinQueries == null) {
			this.joinQueries = new Vector<SQLJoinQuery>();
		}

		this.joinQueries.add(joinQuery);
	}

	public void addJoinQuery(SQLQuery joinQuery) {
		if(this.joinQueries2 == null) {
			this.joinQueries2 = new Vector<SQLQuery>();
		}

		this.joinQueries2.add(joinQuery);
	}

	public void addSelect(ZSelectItem newSelectItem) {
		Collection<ZSelectItem> selectItems = this.getSelect();
		if(selectItems == null) {
			this.addSelect(new Vector<ZSelectItem>());
		}

		String newSelectItemAlias = newSelectItem.getAlias();
		newSelectItem.setAlias("");

		boolean alreadySelected = false;
		for(ZSelectItem selectItem : selectItems) {
			String selectItemAlias = selectItem.getAlias();
			selectItem.setAlias("");
			if(selectItemAlias != null && !selectItemAlias.equals("")) {
				if(selectItemAlias.equalsIgnoreCase(newSelectItemAlias)) {
					alreadySelected = true;
					logger.debug(selectItemAlias + " already selected");
				}
				selectItem.setAlias(selectItemAlias);
			}			
		}

		if(newSelectItemAlias != null && !newSelectItemAlias.equals("")) {
			newSelectItem.setAlias(newSelectItemAlias);
		}

		if(!alreadySelected) {
			selectItems.add(newSelectItem);
		}

	}

//	public void addUnionQuery(SQLQuery unionQuery) {
//		if(this.unionQueries == null) {
//			this.unionQueries = new Vector<SQLQuery>();
//		}
//
//		this.unionQueries.add(unionQuery);
//	}

	public void addWhere(Collection<ZExp> newWheres) {
		for(ZExp newWhere : newWheres) {
			this.addWhere(newWhere);
		}

	}

	public void addWhere(ZExp newWhere) {
		ZExp oldWhere = this.getWhere();
		if(newWhere != null) {
			if(oldWhere == null) {
				super.addWhere(newWhere);
			} else {
				ZExp combinedWhere = new ZExpression("AND", oldWhere, newWhere);
				super.addWhere(combinedWhere);
			}
		}
	}

	public void setWhere(ZExp where) {
		super.addWhere(where);
	}
	
	public void addOn(ZExp newOn) {
		ZExp oldOn = this.onExp;
		if(newOn != null) {
			if(this.onExp == null) {
				super.addWhere(newOn);
			} else {
				ZExp combinedWhere = new ZExpression("AND", this.onExp, newOn);
				this.setOnExp(combinedWhere);
			}
		}
	}
	
	public void clearSelectItems() {
		Collection<ZSelectItem> selectItems = this.getSelect();
		if(selectItems != null) {
			selectItems.clear();
		}
		selectItems = new Vector<ZSelectItem>();
	}

	public String generateAlias() {
		//return R2OConstants.VIEW_ALIAS + this.hashCode();
		if(this.alias == null) {
			this.alias = Constants.VIEW_ALIAS + new Random().nextInt(10000);
		}
		return this.alias;
	}

	public ZSelectItem getSelectItemByAlias(String alias) {
		Iterator selectItems = this.getSelect().iterator();
		while(selectItems.hasNext()) {
			ZSelectItem selectItem = (ZSelectItem) selectItems.next();
			String selectItemAlias = selectItem.getAlias();
			if(alias.equals(selectItemAlias)) {
				return selectItem;
			}
		}
		return null;
	}



	public ZSelectItem getSelectItemByColumnValue(String value) {
		Iterator selectItems = this.getSelect().iterator();
		while(selectItems.hasNext()) {
			ZSelectItem selectItem = (ZSelectItem) selectItems.next();
			String selectItemFullValue = DBUtility.getValueWithoutAlias(selectItem);
			String splitValues[] = selectItemFullValue.split("\\.");
			String columnValue = splitValues[splitValues.length-1];
			if(value.equals(columnValue)) {
				return selectItem;
			}
		}
		return null;
	}

	public ZSelectItem getSelectItemByValue(String value) {
		Iterator selectItems = this.getSelect().iterator();
		while(selectItems.hasNext()) {
			ZSelectItem selectItem = (ZSelectItem) selectItems.next();
			if(value.equals(DBUtility.getValueWithoutAlias(selectItem))) {
				return selectItem;
			}
		}
		return null;
	}

//	public Collection<SQLQuery> getUnionQueries() {
//		return unionQueries;
//	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public void setSelectItems(Collection<ZSelectItem> newSelectItems) {
		this.clearSelectItems();

		for(ZSelectItem newSelectItem : newSelectItems) {
			this.addSelect(newSelectItem);
		}
	}


	public void setSlice(long slice) {
		this.slice = slice;
	}

	private String printSelect() {
		Vector<ZSelectItem> mainQuerySelectItems = (Vector<ZSelectItem>) this.getSelect();

		String selectSQL = "SELECT ";
		if(this.distinct) {
			selectSQL += " DISTINCT ";
		}

		if(mainQuerySelectItems != null && mainQuerySelectItems.size()!=0) {
			for(ZSelectItem mainQuerySelectItem : mainQuerySelectItems) {
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

	private String printFrom() {
		String fromSQL = "";
		Vector<ZFromItem> mainQueryFromItems = this.getFrom();
		if(mainQueryFromItems != null && mainQueryFromItems.size()!=0) {
			for(ZFromItem mainQueryFromItem : mainQueryFromItems) {
				if(mainQueryFromItem instanceof SQLFromItem) {
					SQLFromItem sqlFromItem = (SQLFromItem) mainQueryFromItem;
					fromSQL += mainQueryFromItem.toString() + ", ";
					
//					if(sqlFromItem.getForm() == LogicalTableType.TABLE) {
//						fromSQL += mainQueryFromItem.toString() + ", ";
//					} else {
//						String subQueryFromItem = "(" + mainQueryFromItem.toString() + ") " + mainQueryFromItem.getAlias();
//						fromSQL += subQueryFromItem + ", ";
//					}
					
				} else {
					if(mainQueryFromItem.getSchema() != null) {
						fromSQL += mainQueryFromItem.getSchema() + ".";
					}
					if(mainQueryFromItem.getTable() != null) {
						fromSQL += mainQueryFromItem.getTable() + ".";
					}
					if(mainQueryFromItem.getColumn() != null) {
						fromSQL += mainQueryFromItem.getColumn() + ".";
					}
					fromSQL = fromSQL.substring(0, fromSQL.length()-1);


					String mainQueryFromItemAlias = mainQueryFromItem.getAlias();
					if(mainQueryFromItemAlias != null && mainQueryFromItemAlias.length() > 0) {
						fromSQL += " " + mainQueryFromItem.getAlias() + ", ";
					} else {
						fromSQL += ", ";
					}
				}

			}
			//remove the last coma and space
			fromSQL = fromSQL.substring(0, fromSQL.length() - 2);
		}

		if(this.logicalTables != null) {
			for(SQLLogicalTable logicalTable : this.logicalTables) {
				String logicalTableAlias = logicalTable.getAlias();
				if(logicalTableAlias == null) {
					fromSQL += logicalTable.toString() + ", ";
				} else {
					if (logicalTable instanceof SQLFromItem) {
						fromSQL += logicalTable.toString() + ", ";
					} else {
						fromSQL += " (" + logicalTable.toString() + ") " + logicalTableAlias + ", ";
					}
				}
			}
			//remove the last coma and space
			fromSQL = fromSQL.substring(0, fromSQL.length() - 2);
		}

		return fromSQL;
	}

	private String printOrderBy() {
		String orderBySQL = "";
		for(Object orderByObject : this.getOrderBy()) {
			ZOrderBy orderBy = (ZOrderBy) orderByObject;
			orderBy.getAscOrder();
			ZExp orderByExpression = orderBy.getExpression();
			orderBySQL += orderByExpression;
			if(! orderBy.getAscOrder()) {
				orderBySQL += " DESC";
			}
			orderBySQL += ", ";
		}
		orderBySQL = orderBySQL.substring(0, orderBySQL.length() - 2);
		orderBySQL = "ORDER BY " + orderBySQL ;
		return orderBySQL; 
	}

	@Override
	public String toString() {
		String result = "";

		if(comments != null) {
			result += "--" + this.comments + "\n";
		}

		//print select
		String selectSQL = this.printSelect();
		result += selectSQL + "\n";

		//print from
		String fromSQL = this.printFrom();
		if(this.joinType != null && (this.getSelect() == null || this.getSelect().size() == 0)) {
			result += fromSQL + "\n";
		} else {
			result += "FROM " + fromSQL + "\n";
		}



		//print join queries
		if(this.joinQueries != null) {
			for(SQLJoinQuery joinQuery : this.joinQueries) {
				result += joinQuery.toString() + "\n";
			}				
		}

		//print join queries
		if(this.joinQueries2 != null) {
			for(SQLQuery joinQuery : this.joinQueries2) {
				result += joinQuery.toString() + "\n";
			}				
		}


		//		if(this.joinQueries2 != null) {
		//			for(SQLQuery joinQuery : this.joinQueries2) {
		//				String joinQueryString;
		//				if(joinQuery.alias == null) {
		//					joinQueryString = joinQuery.joinType + " JOIN " + joinQuery.toString() + "\n";
		//				} else {
		//					joinQueryString = joinQuery.joinType + " JOIN " + "(" + joinQuery.toString() 
		//							+ ") AS " + joinQuery.alias + "\n";
		//				}
		//				joinQueryString += "ON " + joinQuery.onExp;
		//						
		//				logger.info("joinQueryString = " + joinQueryString);
		//				result += joinQueryString;
		//			}				
		//		}



		String whereSQL = null;
		if(this.getWhere() != null) {
			whereSQL = this.getWhere().toString();
			if(whereSQL.startsWith("(") && whereSQL.endsWith(")")) {
				whereSQL = whereSQL.substring(1, whereSQL.length() - 1);
			}
			
			if(whereSQL.startsWith("(") && whereSQL.endsWith(")")) {
				whereSQL = whereSQL.substring(1, whereSQL.length() - 1);
			}
			
			whereSQL = whereSQL.replaceAll("\\) AND \\(", " AND ");
			result += "WHERE " + whereSQL + "\n"; 
		}

		if(this.alias != null && !this.alias.equals("") && this.joinType != null) {
			//join query has only one logical table
			SQLLogicalTable logicalTable = this.logicalTables.iterator().next();
			if(logicalTable instanceof SQLQuery) {
				result = "(" + logicalTable + ") AS " + this.alias + "\n";
			} else {
				result = logicalTable + " " + this.alias + "\n";
			}

		}


		if(this.joinType != null && !this.joinType.equals("")) {
			result = this.joinType + " JOIN " + result;
		}

		if(this.onExp != null) {
			result += "ON " + this.onExp + "\n";
		}

//		String unionSQL = "\nUNION\n";
//		if(this.unionQueries != null) {
//			for(SQLQuery unionQuery : this.unionQueries) {
//				String unionQueryString = unionQuery.toString(); 
//				result += unionSQL + unionQueryString; 
//			}
//		}

		Vector<ZOrderBy> orderByConditions = this.getOrderBy(); 
		if(orderByConditions != null) {
			result += this.printOrderBy() + "\n";
		}

		ZGroupBy groupBy = this.getGroupBy();
		if(groupBy != null) {
			result += groupBy + "\n";
		}

		if(this.offset != -1) {
			result += "OFFSET " + this.offset + " ";
		}
		if(this.slice != -1) {
			result += "LIMIT " + this.slice + " ";
		}
		
		return result.trim();

	}

//	public void setUnionQueries(Collection<SQLQuery> unionQueries) {
//		this.unionQueries = unionQueries;
//	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public void setOnExp(ZExp onExp) {
		this.onExp = onExp;
	}

	public String getAlias() {
		return this.alias;
	}

	public String getJoinType() {
		return joinType;
	}

	public Collection<SQLLogicalTable> getLogicalTables() {
		return logicalTables;
	}

	public void setLogicalTables(Collection<SQLLogicalTable> logicalTables) {
		this.logicalTables = logicalTables;
	}

	public void setLogicalTable(SQLLogicalTable logicalTable) {
		this.logicalTables = new Vector<SQLLogicalTable>();
		this.logicalTables.add(logicalTable);
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	 
	public ArrayList<String> getSelectItemAliases() {
		ArrayList<String> result = new ArrayList<String>();
		for(ZSelectItem selectItem : this.getSelect()) {
			result.add(selectItem.getAlias());
		}
		return result;
	}

	@Override
	public Vector<ZSelectItem> getSelect() {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		Vector selectItems = super.getSelect();
		if(selectItems != null) {
			for(Object selectItem : selectItems) {
				result.add((ZSelectItem) selectItem);
			}
		}
		return super.getSelect();
	}

	public void cleanupPrefixes() {
		Vector<ZSelectItem> selectItems = this.getSelect();
		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		for(ZSelectItem selectItem : selectItems) {
			String selectItemAlias = selectItem.getAlias();
			if(selectItemAlias != null) {
				if(selectItemAlias.startsWith(Constants.PREFIX_VAR)) {
					String newSelectItemAlias = 
							selectItemAlias.substring(Constants.PREFIX_VAR.length(), selectItemAlias.length());
					selectItem.setAlias(newSelectItemAlias);
					selectItems2.add(selectItem);					
				} else if(selectItemAlias.startsWith(Constants.PREFIX_LIT)) {
					//do nothing
				} else if(selectItemAlias.startsWith(Constants.PREFIX_URI)) {
					//do nothing
				} else {
					selectItems2.add(selectItem);
				}
			}
		}
		this.setSelectItems(selectItems2);
	}

	public SQLQuery setOrderBy(Vector<ZOrderBy> orderByConditions) {
		if(this.getOrderBy() != null) {
			this.getOrderBy().removeAllElements();	
		}
		
		this.addOrderBy(orderByConditions);
		return this;
	}

	public SQLQuery eliminateSubQuery(Collection<ZSelectItem> newSelectItems,
			ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions,
			String databaseType) throws Exception {
		SQLQuery result = this;
		
		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		Collection<SQLLogicalTable> logicalTables = this.getLogicalTables();
		if(logicalTables != null && logicalTables.size() == 1) {
			SQLLogicalTable logicalTable = logicalTables.iterator().next();
			if(logicalTable instanceof SQLQuery) {
				SQLQuery logicalTableSQLQuery = (SQLQuery) logicalTable; 
				Vector<ZSelectItem> oldSelectItems = logicalTableSQLQuery.getSelect();	
				//SELECT *
				if(newSelectItems.size() == 1 
						&& newSelectItems.iterator().next().toString().equals(("*"))) {
					selectItems2 = new Vector<ZSelectItem>(oldSelectItems);

					for(ZSelectItem selectItem : oldSelectItems) {
						String selectItemWithoutAlias = 
								DBUtility.getValueWithoutAlias(selectItem);
						String selectItemAlias = selectItem.getAlias();
						newWhereCondition = ODEMapsterUtility.renameColumns(newWhereCondition
								, selectItemAlias, selectItemWithoutAlias, true, databaseType); 
					}
				} else {
					String queryAlias = this.generateAlias();
					Map<String, String> mapOldNewAlias = new HashMap<String, String>();
					
					for(ZSelectItem newSelectItem : newSelectItems) {
						String newSelectItemAlias = newSelectItem.getAlias();

						String newSelectItemValue = DBUtility.getValueWithoutAlias(newSelectItem);

						//					String newSelectItemValue2 = queryAlias + "." + newSelectItemValue;
						//					newSelectItem = new ZSelectItem(newSelectItemValue2);
						//					newSelectItem.setAlias(newSelectItemAlias);
						//							
						ZSelectItem oldSelectItem = ODEMapsterUtility.getSelectItemByAlias(newSelectItemValue, oldSelectItems, queryAlias);

						if(oldSelectItem == null) {
							selectItems2.add(newSelectItem);
						} else {
							String oldSelectItemAlias = oldSelectItem.getAlias();

							mapOldNewAlias.put(oldSelectItemAlias, newSelectItemAlias);

							String oldSelectItemValue = DBUtility.getValueWithoutAlias(oldSelectItem);
							oldSelectItem.setAlias(newSelectItemAlias);
							selectItems2.add(oldSelectItem);
							if(newWhereCondition != null) {
								newWhereCondition = ODEMapsterUtility.renameColumns(newWhereCondition
										, newSelectItemValue, oldSelectItemValue, true, databaseType);
							}
						}
					}
					logicalTableSQLQuery.setSelectItems(selectItems2);
					
					if(orderByConditions != null) {
						Vector<ZOrderBy> newOrderByConditions = new Vector<ZOrderBy>();
						for(ZOrderBy orderByCondition : orderByConditions) {
							ZExp oldExp = orderByCondition.getExpression();
							ZExp newExp = ODEMapsterUtility.replaceColumnNames(oldExp, mapOldNewAlias);
							ZOrderBy newOrderBy = new ZOrderBy(newExp);
							newOrderBy.setAscOrder(orderByCondition.getAscOrder());
							newOrderByConditions.add(newOrderBy);
						}
						logicalTableSQLQuery.setOrderBy(newOrderByConditions);
					}
				}

				logicalTableSQLQuery.setLogicalTables(logicalTableSQLQuery.getLogicalTables());
				
				logicalTableSQLQuery.addWhere(newWhereCondition);
				result = logicalTableSQLQuery; 
			}
		}
		return result;
	}
}
