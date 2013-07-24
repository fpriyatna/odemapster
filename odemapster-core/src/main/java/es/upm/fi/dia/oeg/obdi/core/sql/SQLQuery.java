package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
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

public class SQLQuery extends ZQuery implements IQuery {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SQLQuery.class);

	private String alias;
	private long slice = -1;
	private long offset = -1;
	private String databaseType;
	private boolean distinct = false;
	private String comments;

	public SQLQuery() {
		super();
		this.addSelect(new Vector<ZSelectItem>());
		this.addFrom(new Vector<ZFromItem>()); 
	}

	public SQLQuery(SQLLogicalTable logicalTable) {
		SQLJoinTable joinQuery = new SQLJoinTable(logicalTable);
		this.addFrom(joinQuery);
	}
	
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
	}

	private void addFrom(ZFromItem fromItem) {
		if(this.getFrom() == null) {
			this.addFrom(new Vector<ZFromItem>());
		}
		this.getFrom().add(fromItem);
	}

	public void addLogicalTable(ZFromItem logicalTable) {
		Vector<ZFromItem> fromItems = this.getFrom();
		
		if(fromItems == null) {
			fromItems = new Vector<ZFromItem>();
		}

		fromItems.add(logicalTable);
	}


	public void addSelect(ZSelectItem newSelectItem) {
		Vector<ZSelectItem> selectItems = this.getSelect();
		if(selectItems == null) {
			selectItems = new Vector<ZSelectItem>();
			this.addSelect(selectItems);
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


//	private void addJoinQuery(SQLJoinQuery joinQuery) {
//		if(this.joinQueries == null) {
//			this.joinQueries = new Vector<SQLJoinQuery>();
//		}
//
//		this.joinQueries.add(joinQuery);
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

	public void cleanupOrderBy() {
		Vector<ZOrderBy> orderByConditions = this.getOrderBy();
		Vector<ZOrderBy> orderByConditions2 = new Vector<ZOrderBy>();
		if(orderByConditions != null) {
			for(ZOrderBy orderByCondition : orderByConditions) {
				ZExp orderByExp = orderByCondition.getExpression();
				if(orderByExp instanceof ZConstant) {
					ZConstant orderByConstant = (ZConstant) orderByExp;
					String orderByValue = orderByConstant.getValue();
					if(orderByValue.startsWith(Constants.PREFIX_VAR)) {
						String orderByValue2 = orderByValue.substring(
								Constants.PREFIX_VAR.length(), orderByValue.length());
						ZConstant orderByConstant2 = new ZConstant(orderByValue2, orderByConstant.getType());
						ZOrderBy orderByCondition2 = new ZOrderBy(orderByConstant2);
						orderByCondition2.setAscOrder(orderByCondition.getAscOrder());
						orderByConditions2.add(orderByCondition2);				
					} else {
						orderByConditions2.add(orderByCondition);
					}
				} else {
					orderByConditions2.add(orderByCondition);
				}
			}
			this.setOrderBy(orderByConditions2);			
		}

	}

	public void cleanupSelectItems() {
		Vector<ZSelectItem> selectItems = this.getSelect();
		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		for(ZSelectItem selectItem : selectItems) {
			String selectItemName;
			String alias = selectItem.getAlias();
			if(alias == null || alias.equals("")) {
				selectItemName = selectItem.getColumn();
			} else {
				selectItemName = selectItem.getAlias();
			}

			if(selectItemName.startsWith(Constants.PREFIX_VAR)) {
				String newSelectItemAlias = 
						selectItemName.substring(Constants.PREFIX_VAR.length(), selectItemName.length());
				selectItem.setAlias(newSelectItemAlias);
				selectItems2.add(selectItem);					
			} else if(selectItemName.startsWith(Constants.PREFIX_LIT)) {
				//do nothing
			} else if(selectItemName.startsWith(Constants.PREFIX_URI)) {
				//do nothing
			} else {
				selectItems2.add(selectItem);
			}
		}
		this.setSelectItems(selectItems2);
	}


	public void clearSelectItems() {
		Collection<ZSelectItem> selectItems = this.getSelect();
		if(selectItems != null) {
			selectItems.clear();
		}
		selectItems = new Vector<ZSelectItem>();
	}

	public IQuery eliminateSubQuery() throws Exception {
		IQuery result;
		Vector<ZFromItem> logicalTables = this.getFrom();
		if(logicalTables == null || logicalTables.size() == 0) {
			result = this;
		} else if(logicalTables.size() > 1) {
			result = this;
		} else {
			ZFromItem fromItem = logicalTables.iterator().next();
			
			if(fromItem instanceof SQLFromItem) {
				result = this;
			} else if(fromItem instanceof SQLJoinTable) {
				SQLJoinTable joinQuery = (SQLJoinTable) fromItem;
				SQLLogicalTable logicalTable = joinQuery.getJoinSource();
				Collection<ZSelectItem> newSelectItems = new Vector<ZSelectItem>();
				if(logicalTable instanceof SQLFromItem) {
					result = this;
				} else  if(logicalTable instanceof SQLQuery) {
					SQLQuery resultAux = (SQLQuery) logicalTable;
					
					Collection<ZSelectItem> innerSelectItems = resultAux.getSelectItems();
					Collection<ZSelectItem> outerSelectItems = this.getSelectItems();
					
					Map<String, String> mapInnerAliasName = new HashMap<String, String>();
					for(ZSelectItem innerSelectItem : innerSelectItems) {
						String innerAlias = innerSelectItem.getAlias();
						innerSelectItem.setAlias("");
						String innerName = innerSelectItem.toString().trim();
						if(innerAlias != null && !innerAlias.equals("")) {
							innerAlias = innerAlias.trim();
							mapInnerAliasName.put(innerAlias, innerName);	
						} else {
							mapInnerAliasName.put(innerName, innerName);
						}
						innerSelectItem.setAlias(innerAlias);
					}

					ZExpression newOuterWhereCondition = (ZExpression) this.getWhere();
					for(String innerAlias : mapInnerAliasName.keySet()) {
						String innerValue = mapInnerAliasName.get(innerAlias);
						newOuterWhereCondition = ODEMapsterUtility.renameColumns(newOuterWhereCondition
										, innerAlias, innerValue, true, databaseType);					
					}

					
					ZExp innerWhereCondition = resultAux.getWhere();
					ZExp newWhereCondition = SQLUtility.combineExpressions(newOuterWhereCondition, innerWhereCondition, Constants.SQL_LOGICAL_OPERATOR_AND);
					resultAux.setWhere(newWhereCondition);
					
					if(outerSelectItems.size() == 1 
							&& outerSelectItems.iterator().next().toString().trim().equals(("*"))) {
						//do nothing
					} else {
						for(ZSelectItem outerSelectItem : outerSelectItems) {
							String outerAlias = outerSelectItem.getAlias();
							outerSelectItem.setAlias("");
							String outerName = outerSelectItem.toString().trim();
							String innerName = mapInnerAliasName.get(outerName);
							ZSelectItem newSelectItem = new ZSelectItem(innerName);
							newSelectItem.setAlias(outerAlias);
							newSelectItems.add(newSelectItem);
						}

						resultAux.setSelectItems(newSelectItems);
					}
					
					result = resultAux;
				} else {
					logger.warn("unknown type of fromItem!");
					result = this;
				}				
			} else {
				logger.warn("unknown type of fromItem!");
				result = this;
			}
		}
		
		return result;
	}

	public IQuery eliminateSubQuery2(Collection<ZSelectItem> newSelectItems,
			ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions,
			String databaseType) throws Exception {
		IQuery result;

		Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
		Collection<ZFromItem> logicalTables = this.getFrom();
		if(logicalTables != null && logicalTables.size() == 1) {
			ZFromItem fromItem = logicalTables.iterator().next();
			Collection<ZSelectItem> oldSelectItems;
			
			if(fromItem instanceof SQLJoinTable) {
				SQLJoinTable joinQuery = (SQLJoinTable) fromItem; 
				SQLLogicalTable logicalTable = joinQuery.getJoinSource();
				

				if(logicalTable instanceof SQLFromItem) {
					result = this;
					oldSelectItems = this.getSelect();
				} else if(logicalTable instanceof SQLQuery) {
					result = (SQLQuery) logicalTable; 
					oldSelectItems = result.getSelectItems();
				} else if(logicalTable instanceof UnionSQLQuery) {
					UnionSQLQuery unionSqlQueries = (UnionSQLQuery) logicalTable;
					oldSelectItems = unionSqlQueries.getUnionQueries().iterator().next().getSelect();
					result = unionSqlQueries;
				} else {

					//TODO implement this
					String errorMessage = "not implemented yet!";
					logger.error(errorMessage);
					throw new Exception(errorMessage);
				}

				if(newSelectItems == null) {
					ZSelectItem newSelectItem = new ZSelectItem("*");
					newSelectItems = new Vector<ZSelectItem>();
					newSelectItems.add(newSelectItem);			
				}

				Map<String, String> mapOldNewAlias = new HashMap<String, String>();

				//SELECT *
				if(newSelectItems.size() == 1 
						&& newSelectItems.iterator().next().toString().equals(("*"))) {
					selectItems2 = new Vector<ZSelectItem>(oldSelectItems);

					for(ZSelectItem oldSelectItem : oldSelectItems) {
						String selectItemWithoutAlias = 
								DBUtility.getValueWithoutAlias(oldSelectItem);
						String oldSelectItemAlias = oldSelectItem.getAlias();
						mapOldNewAlias.put(oldSelectItemAlias, selectItemWithoutAlias);
						newWhereCondition = ODEMapsterUtility.renameColumns(newWhereCondition
								, oldSelectItemAlias, selectItemWithoutAlias, true, databaseType); 
					}
				} else {
					String queryAlias = this.generateAlias();
					for(ZSelectItem newSelectItem : newSelectItems) {
						String newSelectItemAlias = newSelectItem.getAlias();
						String newSelectItemValue = DBUtility.getValueWithoutAlias(newSelectItem);
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
				}

				result.setSelectItems(selectItems2);
				result.addWhere(newWhereCondition);
				if(orderByConditions != null && orderByConditions.size() > 0) {
					Vector<ZOrderBy> newOrderByConditions = new Vector<ZOrderBy>();
					for(ZOrderBy orderByCondition : orderByConditions) {
						ZExp oldExp = orderByCondition.getExpression();
						ZExp newExp = ODEMapsterUtility.replaceColumnNames(oldExp, mapOldNewAlias);
						ZOrderBy newOrderBy = new ZOrderBy(newExp);
						newOrderBy.setAscOrder(orderByCondition.getAscOrder());
						newOrderByConditions.add(newOrderBy);
					}
					result.setOrderBy(newOrderByConditions);
				}
			} else if(fromItem instanceof SQLFromItem) {
				result = this;
				oldSelectItems = this.getSelect();				
			} else {
				result = this;
				oldSelectItems = this.getSelect();				
			}
			

		} else {
			result = this;
			result.setOrderBy(orderByConditions);
			result.addWhere(newWhereCondition);
		}

		result.setDatabaseType(this.databaseType);
		return result;
	}



	public String generateAlias() {
		//return R2OConstants.VIEW_ALIAS + this.hashCode();
		if(this.alias == null) {
			this.alias = Constants.VIEW_ALIAS + new Random().nextInt(10000);
		}
		return this.alias;
	}

	public String getAlias() {
		return this.alias;
	}

	//	public Collection<SQLQuery> getUnionQueries() {
	//		return unionQueries;
	//	}

	public String getDatabaseType() {
		return databaseType;
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

	public ArrayList<String> getSelectItemAliases() {
		ArrayList<String> result = new ArrayList<String>();
		for(ZSelectItem selectItem : this.getSelect()) {
			result.add(selectItem.getAlias());
		}
		return result;
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

	public Collection<ZSelectItem> getSelectItems() {
		return this.getSelect();
	}

	public String print(boolean withAlias) {
		String result;
		if(withAlias) {
			result = this.toString();
		} else {
			String alias = this.getAlias();
			if(alias == null || alias.equals("")) {
				result = this.toString();
			} else {
				this.setAlias("");
				result = this.toString();
				this.setAlias(alias);
			}
		}
		return result;
	}

	private String printFrom() {
		String fromSQL = "";
		Vector<ZFromItem> fromItems = this.getFrom();
		if(fromItems != null && fromItems.size() != 0) {
			int i = 0;
			for(ZFromItem mainQueryFromItem : fromItems) {
				if(mainQueryFromItem instanceof SQLFromItem) {
					fromSQL += mainQueryFromItem.toString() + ", ";
				} else if(mainQueryFromItem instanceof SQLJoinTable) {
					SQLJoinTable joinQuery = (SQLJoinTable) mainQueryFromItem;
					SQLLogicalTable logicalTable = joinQuery.getJoinSource();
					
					String separator = "";
					String logicalTableJoinType = joinQuery.getJoinType();
					if(logicalTableJoinType != null && !logicalTableJoinType.equals("")) {
						separator = logicalTableJoinType + " JOIN ";
					} else {
						if(i > 0) {
							separator = ", ";	
						}
					}
					
					if(logicalTable instanceof SQLFromItem) {
						fromSQL += separator + logicalTable.toString();
					} else if(logicalTable instanceof IQuery) {
						fromSQL +=  separator + " ( "+ logicalTable.print(false) + " ) " + logicalTable.getAlias();	
					}
				
					ZExpression joinExp = joinQuery.getOnExpression();
					if(joinExp != null) {
						fromSQL += " ON " + joinExp;
					}
					
					if(i < fromItems.size() - 1) {
						fromSQL += "\n";
					}
					
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
				i++;
			}
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

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public void setOrderBy(Vector<ZOrderBy> orderByConditions) {
		if(this.getOrderBy() != null) {
			this.getOrderBy().removeAllElements();	
		}

		this.addOrderBy(orderByConditions);
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

	public void setWhere(ZExp where) {
		super.addWhere(where);
	}

//	public ZExp getOnExp() {
//		return this.onExp;
//	}

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
		result += "FROM " + fromSQL + "\n";

		if(this.getWhere() != null) {
			String whereSQL = this.getWhere().toString();
			if(whereSQL.startsWith("(") && whereSQL.endsWith(")")) {
				whereSQL = whereSQL.substring(1, whereSQL.length() - 1);
			}

			whereSQL = whereSQL.replaceAll("\\) AND \\(", " AND ");
			result += "WHERE " + whereSQL + "\n"; 
		}

		Vector<ZOrderBy> orderByConditions = this.getOrderBy(); 
		if(orderByConditions != null && orderByConditions.size() > 0) {
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

}
