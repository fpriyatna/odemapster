package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.utility.SQLUtility2;

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
		super();
		this.addLogicalTable(logicalTable);
	}

	public SQLQuery(IQuery logicalTable) {
		super();
		SQLJoinTable joinQuery = new SQLJoinTable(logicalTable);
		this.addFromItem(joinQuery);
	}

	public SQLQuery(ZFromItem logicalTable) {
		this.addFromItem(logicalTable);
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

	public void addLogicalTable(SQLLogicalTable logicalTable) {
		if(logicalTable instanceof ZFromItem) {
			this.addFromItem((ZFromItem) logicalTable);
		} else if(logicalTable instanceof IQuery) {
			SQLJoinTable joinQuery = new SQLJoinTable(logicalTable);
			this.addFromItem(joinQuery);
		} else {
			logger.warn("unknown type of logicalTable!");
		}
	}

	public void addFromItem(ZFromItem fromItem) {
		if(this.getFrom() == null) {
			this.addFrom(new Vector<ZFromItem>());
		}
		super.getFrom().add(fromItem);
	}

	public void addFromItems(Collection<ZFromItem> fromItems) {
		if(this.getFrom() == null) {
			this.addFrom(new Vector<ZFromItem>());
		}

		for(ZFromItem fromItem : fromItems) {
			super.getFrom().add(fromItem);	
		}
	}

	//	public void addLogicalTable(ZFromItem logicalTable) {
	//		Vector<ZFromItem> fromItems = this.getFrom();
	//		
	//		if(fromItems == null) {
	//			fromItems = new Vector<ZFromItem>();
	//		}
	//
	//		fromItems.add(logicalTable);
	//	}


	public void addSelects(Collection<ZSelectItem> selectItems) {
		for(ZSelectItem selectItem : selectItems) {
			this.addSelect(selectItem);
		}
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

		if(selectItems != null) {
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
	}


	public void clearSelectItems() {
		Collection<ZSelectItem> selectItems = this.getSelect();
		if(selectItems != null) {
			selectItems.clear();
		}
		selectItems = new Vector<ZSelectItem>();
	}

	public IQuery removeSubQuery() throws Exception {
		IQuery result = this;

		try {
			List<ZFromItem> logicalTables = this.getFrom();

			if(logicalTables == null || logicalTables.size() == 0) {
				result = this;
			} else {
				if(this.hasSubquery()) {

					SQLUtility2 sqlUtility2 = new SQLUtility2();

					Map<ZSelectItem, ZSelectItem> mapSelectItems = this.buildMapSelectItemOrigin();
					Collection<ZSelectItem> newSelectItems = mapSelectItems.values();

					SQLQuery resultAux = new SQLQuery();
					//Collection<ZSelectItem> newSelectItems = resultAux.getSelectItems();
					resultAux.setSelectItems(newSelectItems);

					for(ZFromItem fromItem : logicalTables) {
						if(fromItem instanceof SQLFromItem) {//FROM T1, T2
							resultAux.addFromItem(fromItem);
						} else if(fromItem instanceof SQLJoinTable) {
							SQLJoinTable joinQuery = (SQLJoinTable) fromItem;

							SQLLogicalTable logicalTable = joinQuery.getJoinSource();

							if(logicalTable instanceof SQLFromItem) {//FROM T1 INNER JOIN T2
								resultAux.addLogicalTable(logicalTable);	
							} else if(logicalTable instanceof SQLQuery) {//FROM T1 INNER JOIN (SELECT ... FROM T2 ....) ON ...
								String fromItemAlias = fromItem.getAlias();

								SQLQuery logicalTableSQLQuery = (SQLQuery) logicalTable;
								//add from
								Collection<ZFromItem> newFromItems = logicalTableSQLQuery.getFrom();
								for(ZFromItem newFromItem : newFromItems) {
									if(newFromItem instanceof SQLFromItem) {
										SQLJoinTable newJoinTable = new SQLJoinTable((SQLFromItem)newFromItem);
										newJoinTable.setJoinType(joinQuery.getJoinType());
										resultAux.addFromItem(newJoinTable);

										ZExpression oldOnExpression = joinQuery.getOnExpression();
										if(oldOnExpression != null) {
											ZExp newOnExp = oldOnExpression;
											Map<ZConstant, ZConstant> mapOnExpression = this.buildMapOnExp();
											newOnExp = sqlUtility2.replaceExp(newOnExp, mapOnExpression);
											//											for(ZConstant oldConstant : mapOnExpression.keySet()) {
											//												ZConstant newConstant = mapOnExpression.get(oldConstant);
											//												newOnExpression = ODEMapsterUtility.renameColumns(oldOnExpression
											//														, oldConstant.toString(), newConstant.toString(), true, databaseType);
											//											}
											newJoinTable.setOnExpression((ZExpression)newOnExp);
										}

									} else {

									}

								}



								//add where
								ZExp newWhere = logicalTableSQLQuery.getWhere();
								resultAux.addWhere(newWhere);

								Collection<ZSelectItem> innerSelectItems = logicalTableSQLQuery.getSelectItems();
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
										String outerName;
										String outerPrefix = "";
										if(outerSelectItem.isExpression()) {
											outerName = outerSelectItem.toString().trim();
											outerPrefix = fromItemAlias;
										} else {
											outerName = outerSelectItem.getColumn();
											String outerTable = outerSelectItem.getTable();
											if(outerTable == null) {
												outerPrefix = fromItemAlias;
											} else {
												outerPrefix = outerSelectItem.getTable();	
											}
										}

										String innerName = mapInnerAliasName.get(outerName);
										if(innerName != null && outerPrefix.equals(fromItemAlias)) {
											ZSelectItem newSelectItem = new ZSelectItem(innerName);
											if(outerAlias == null || outerAlias.equals("")) {
												outerAlias = outerName;
											} 
											newSelectItem.setAlias(outerAlias);
											//newSelectItems.add(newSelectItem);
										} 
									}

								}
							} else {
								logger.warn("unknown type of fromItem!");
								result = this;
							}				
						} else {
							logger.warn("unknown type of fromItem!");
							result = this;
						}				
					}
					result = resultAux;					
				} else {
					result = this;
				}
			}

		} catch(Exception e) {
			logger.error("Error occured while eliminating subqueries, original query will be used!");
		}

		return result;
	}

	public IQuery removeSubQuery(Collection<ZSelectItem> newSelectItems,
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
				} else if(logicalTable instanceof SQLUnion) {
					SQLUnion unionSqlQueries = (SQLUnion) logicalTable;
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

		Collection<ZSelectItem> selectItems = this.getSelect();
		if(selectItems != null) {
			for(ZSelectItem selectItem : selectItems) {
				result.add(selectItem.getAlias());
			}			
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



	private String printFrom() {
		String fromSQL = "";
		Vector<ZFromItem> fromItems = this.getFrom();
		if(fromItems != null && fromItems.size() != 0) {
			int i = 0;
			for(ZFromItem mainQueryFromItem : fromItems) {
				if(mainQueryFromItem instanceof SQLFromItem) {
					String separator = "";
					if(i > 0) {
						separator = ", ";	
					}
					fromSQL += separator + mainQueryFromItem.toString();
				} else if(mainQueryFromItem instanceof SQLJoinTable) {
					SQLJoinTable joinQuery = (SQLJoinTable) mainQueryFromItem;
					SQLLogicalTable logicalTable = joinQuery.getJoinSource();

					String separator = "";
					String logicalTableJoinType = joinQuery.getJoinType();
					if(logicalTableJoinType != null && !logicalTableJoinType.equals("")) {
						separator = " " + logicalTableJoinType + " JOIN ";
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
					String separator = "";
					if(i > 0) {
						separator = ", ";	
					}

					String fromItemString = "";
					if(mainQueryFromItem.getSchema() != null) {
						fromItemString += mainQueryFromItem.getSchema() + ".";
					}
					if(mainQueryFromItem.getTable() != null) {
						fromItemString += mainQueryFromItem.getTable() + ".";
					}
					if(mainQueryFromItem.getColumn() != null) {
						fromItemString += mainQueryFromItem.getColumn() + ".";
					}
					fromItemString = fromItemString.substring(0, fromItemString.length()-1);

					String mainQueryFromItemAlias = mainQueryFromItem.getAlias();
					if(mainQueryFromItemAlias != null && mainQueryFromItemAlias.length() > 0) {
						fromItemString += " " + mainQueryFromItem.getAlias();
					}

					fromSQL += separator + fromItemString;
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
		String selectSQL = SQLUtility.printSelectItems(this.getSelectItems(), this.getDistinct());

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

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return this.alias;
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

	public static boolean areBaseTables(Vector<ZFromItem> fromItems) {
		boolean result = true;
		for(ZFromItem fromItem : fromItems) {
			if(!(fromItem instanceof SQLFromItem)) {
				result = false;
			}
		}
		return result;
	}

	public Map<ZConstant, ZConstant> buildMapOnExp() {
		Vector<ZFromItem> fromItems = this.getFrom();
		Map<ZConstant, ZConstant> mapOnExp = new HashMap<ZConstant, ZConstant>();
		for(ZFromItem fromItem : fromItems) {
			if(fromItem instanceof SQLFromItem) {

			} else if(fromItem instanceof SQLJoinTable) {
				SQLJoinTable joinTable = (SQLJoinTable) fromItem;
				SQLLogicalTable joinSource = joinTable.getJoinSource();
				if(joinSource instanceof SQLQuery) {
					SQLQuery joinSourceSQLQuery = (SQLQuery) joinSource;
					String joinSourceAlias = joinSourceSQLQuery.getAlias();
					Collection<ZSelectItem> innerSelectItems = joinSourceSQLQuery.getSelectItems();
					for(ZSelectItem innerSelectItem : innerSelectItems) {
						String innerItemAlias = innerSelectItem.getAlias();
						if(innerItemAlias == null || innerItemAlias.equals("")) {
							innerItemAlias= innerSelectItem.toString();
						}
						String outerName = joinSourceAlias + "." + innerItemAlias;
						ZConstant outerConstant = new ZConstant(outerName, ZConstant.COLUMNNAME);
						ZConstant innerConstant = null;
						if(innerSelectItem.isExpression()) {
							ZExp innerSelectItemExpression = innerSelectItem.getExpression();
							if(innerSelectItemExpression instanceof ZConstant) {
								innerConstant = (ZConstant) innerSelectItemExpression; 
							}
						} else {
							String innerTable = innerSelectItem.getTable();
							String innerColumn = innerSelectItem.getColumn();
							innerConstant = new ZConstant(innerTable + "." + innerColumn, ZConstant.COLUMNNAME);							
						}

						if(innerConstant != null) {
							mapOnExp.put(outerConstant, innerConstant);	
						}

					}
				} else {
					logger.debug("joinSource not SQLQuery!");
				}
			}
		}

		return mapOnExp;

	}

	public Map<ZSelectItem, ZSelectItem> buildMapSelectItemOrigin() {

		Vector<ZFromItem> fromItems = this.getFrom();
		Map<String, ZSelectItem> mapInnerAliasSelectItem = new HashMap<String, ZSelectItem>();
		for(ZFromItem fromItem : fromItems) {
			if(fromItem instanceof SQLFromItem) {

			} else if(fromItem instanceof SQLJoinTable) {
				SQLJoinTable joinTable = (SQLJoinTable) fromItem;
				SQLLogicalTable joinSource = joinTable.getJoinSource();
				if(joinSource instanceof SQLQuery) {
					SQLQuery joinSourceSQLQuery = (SQLQuery) joinSource;
					String joinSourceAlias = joinSourceSQLQuery.getAlias();

					Collection<ZSelectItem> innerSelectItems = joinSourceSQLQuery.getSelectItems();
					for(ZSelectItem innerSelectItem : innerSelectItems) {
						String innerSelectItemAlias = innerSelectItem.getAlias();
						if(innerSelectItemAlias == null || innerSelectItemAlias.equals("")) {
							innerSelectItemAlias = innerSelectItem.toString();
						}
						innerSelectItemAlias = joinSourceAlias + "." + innerSelectItemAlias; 
						mapInnerAliasSelectItem.put(alias.trim(), innerSelectItem);
					}
				} else {
					logger.debug("joinSource not SQLQuery!");
				}
			}
		}

		Map<ZSelectItem, ZSelectItem> result = new HashMap<ZSelectItem, ZSelectItem>();

		Vector<ZSelectItem> outerSelectItems = this.getSelect();
		for(ZSelectItem outerSelectItem : outerSelectItems) {
			String outerSelectItemTable = outerSelectItem.getTable();
			if(outerSelectItemTable == null) {
				result.put(outerSelectItem, outerSelectItem);
			} else {
				String outerSelectItemAlias = outerSelectItem.getAlias();
				outerSelectItem.setAlias("");
				String outerSelectItemString = outerSelectItem.toString().trim(); 
				ZSelectItem innerSelectItem = mapInnerAliasSelectItem.get(outerSelectItemString);
				if(innerSelectItem != null) {
					result.put(outerSelectItem, innerSelectItem);
				}
				if(outerSelectItemAlias != null) {
					outerSelectItem.setAlias(outerSelectItemAlias);
				}
			}
		}

		return result;
	}

	public void setFromItems(Collection<ZFromItem> fromItems) {
		this.addFrom(new Vector<ZFromItem>(fromItems));
	}

	public boolean hasSubquery() {
		boolean subqueryFound = false;

		Vector<ZFromItem> fromItems = this.getFrom();
		for(ZFromItem fromItem : fromItems) {
			if(!subqueryFound) {
				if(fromItem instanceof SQLFromItem) {
					//not a subquery
				} else if(fromItem instanceof SQLJoinTable) {
					SQLJoinTable joinTable = (SQLJoinTable) fromItem;
					SQLLogicalTable joinSource = joinTable.getJoinSource();
					if(joinSource instanceof SQLFromItem) {
						//not a subquery
					} else {
						subqueryFound = true; 
					}
				}				
			}
		}

		return subqueryFound;
	}

	protected Map<String, ZSelectItem> buildMapAliasSelectItem() {
		return this.buildMapAliasSelectItemAux(this.alias);
	}

	protected Map<String, ZSelectItem> buildMapAliasSelectItemAux(String prefix) {
		Map<String, ZSelectItem> mapInnerAliasSelectItem = new LinkedHashMap<String, ZSelectItem>();
		Collection<ZSelectItem> innerSelectItems = this.getSelectItems();
		for(ZSelectItem innerSelectItem : innerSelectItems) {
			String innerSelectItemAlias = innerSelectItem.getAlias();
			if(innerSelectItemAlias == null || innerSelectItemAlias.equals("")) {
				innerSelectItemAlias = innerSelectItem.toString();
			}
			innerSelectItemAlias = prefix + "." + innerSelectItemAlias; 
			mapInnerAliasSelectItem.put(innerSelectItemAlias.trim(), innerSelectItem);
		}
		return mapInnerAliasSelectItem;

	}

	Collection<ZSelectItem> pushProjectionsDown(Collection<ZSelectItem> pushedProjections
			, Map<String, ZSelectItem> mapInnerAliasSelectItem) {
		//Map<String, ZSelectItem> mapInnerAliasSelectItem = this.getMapAliasSelectItem();

		Map<ZSelectItem, ZSelectItem> selectItemsReplacement = new LinkedHashMap<ZSelectItem, ZSelectItem>();
		for(ZSelectItem outerSelectItem : pushedProjections) {
			String outerSelectItemTable = outerSelectItem.getTable();
			if(outerSelectItemTable == null) {
				selectItemsReplacement.put(outerSelectItem, outerSelectItem);
			} else {
				String outerSelectItemAlias = outerSelectItem.getAlias();
				outerSelectItem.setAlias("");
				String outerSelectItemString = outerSelectItem.toString().trim();
				outerSelectItemString = outerSelectItemString.replaceAll("`", "").replaceAll("\"", "");
				if(outerSelectItemAlias != null) {
					outerSelectItem.setAlias(outerSelectItemAlias);
				}

				ZSelectItem replacementSelectItem;
				ZSelectItem innerSelectItem = mapInnerAliasSelectItem.get(outerSelectItemString);
				if(innerSelectItem != null) {
					SQLSelectItem replacementSelectItemAux = SQLSelectItem.create(innerSelectItem);
					//replacementSelectItemAux.setDbType(this.databaseType);
					if(outerSelectItemAlias != null) {
						replacementSelectItemAux.setAlias(outerSelectItemAlias);
					}
					replacementSelectItem = replacementSelectItemAux;
				} else {
					replacementSelectItem = outerSelectItem;
				}
				selectItemsReplacement.put(outerSelectItem, replacementSelectItem);
			}
		}


		Collection<ZSelectItem> newSelectItems = selectItemsReplacement.values();
		return newSelectItems;
		//this.setSelectItems(newSelectItems);
	}

	ZExp pushFilterDown(ZExpression pushedFilter
			, Map<String, ZSelectItem> mapInnerAliasSelectItem) {
		//Map<String, ZSelectItem> mapInnerAliasSelectItem = this.getMapAliasSelectItem();

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
		ZExp newFilter = sqlUtility2.replaceExp(pushedFilter, whereReplacement);
		return newFilter;
		//this.addWhere(newFilter);

	}

	public boolean getDistinct() {
		return this.distinct;
	}

	public static SQLQuery create(Collection<ZSelectItem> selectItems
			, SQLLogicalTable leftTable, SQLLogicalTable rightTable 
			, String joinType, ZExpression oldJoinExpression, String databasetype) {

		SQLQuery result = null;
		boolean proceed = true;
		if(Constants.JOINS_TYPE_LEFT.equals(joinType) && rightTable instanceof SQLQuery) {
			SQLQuery rightTableSQLQuery = (SQLQuery) rightTable;
			Vector<ZFromItem> rightTableFromItems = rightTableSQLQuery.getFrom();
			if(rightTableFromItems.size() != 1) {
				String errorMessage = "Subquery elimination for left outer join can deal with 1 right table only!";
				logger.debug(errorMessage);
				proceed = false;
			}
		}

		if(proceed) {
			result = new SQLQuery();
			result.setDatabaseType(databasetype);

			Map<String, ZSelectItem> mapAliasSelectItems = 
					new LinkedHashMap<String, ZSelectItem>();

			if(leftTable instanceof SQLQuery) {
				SQLQuery leftTableSQLQuery = (SQLQuery) leftTable;
				Vector<ZFromItem> leftTableFromItems = leftTableSQLQuery.getFrom();
				for(ZFromItem leftTableFromItem : leftTableFromItems) {
					String fromItemAlias = leftTableFromItem.getAlias();
					if(fromItemAlias == null || fromItemAlias.equals("")) {
						String logicalTableAlias = leftTable.getAlias();
						if(logicalTableAlias != null && !logicalTableAlias.equals("")) {
							leftTableFromItem.setAlias(logicalTableAlias);	
						}
					}
					result.addFromItem(leftTableFromItem);	
				}

				result.addWhere(leftTableSQLQuery.getWhere());
				Collection<ZSelectItem> leftTableSelectItems = leftTableSQLQuery.getSelectItems();
				result.addSelects(leftTableSelectItems);
				Map<String, ZSelectItem> mapAliasSelectItemsLeft = leftTableSQLQuery.buildMapAliasSelectItem();
				mapAliasSelectItems.putAll(mapAliasSelectItemsLeft);
			} else if(leftTable instanceof SQLFromItem) {
				SQLFromItem leftTableFromItem = (SQLFromItem) leftTable; 
				result.addFromItem(leftTableFromItem);
			}

			if(rightTable instanceof SQLQuery) {
				SQLQuery rightTableSQLQuery = (SQLQuery) rightTable;
				Collection<ZSelectItem> rightTableSelectItems = rightTableSQLQuery.getSelectItems();
				ZExpression rightTableWhere = (ZExpression) rightTableSQLQuery.getWhere();

				Map<String, ZSelectItem> mapAliasSelectItemsRight = rightTableSQLQuery.buildMapAliasSelectItem();
				mapAliasSelectItems.putAll(mapAliasSelectItemsRight);
				ZExpression newJoinExpression = (ZExpression) result.pushFilterDown(oldJoinExpression, mapAliasSelectItems);
				ZExpression newWhereExpression = (ZExpression) result.pushFilterDown(rightTableWhere, mapAliasSelectItems);

				String logicalTableAlias = rightTable.getAlias();
				SQLJoinTable joinTable = null;
				Vector<ZFromItem> rightTableFromItems = rightTableSQLQuery.getFrom();
				Collection<String> addedTabled = new Vector<String>();
				
				for(ZFromItem rightTableFromItem : rightTableFromItems) {
					if(rightTableFromItem instanceof SQLFromItem) {
						String rightTableAlias = rightTableFromItem.getAlias();
						LogicalTableType tableType = ((SQLFromItem) rightTableFromItem).getForm();
						if(tableType == LogicalTableType.TABLE_NAME) {
							String rightTableName = rightTableFromItem.getTable();
							SQLLogicalTable rightTableLogicalTable = new SQLFromItem(rightTableName, LogicalTableType.TABLE_NAME);

							Collection<ZExpression> containedExpressions1 = SQLUtility.containsPrefix(newJoinExpression, rightTableAlias);
							Collection<ZExpression> containedExpressions2 = SQLUtility.containsPrefix(newWhereExpression, rightTableAlias);
							if(containedExpressions1.isEmpty() && containedExpressions2.isEmpty()) {
								joinTable = new SQLJoinTable(rightTableLogicalTable, joinType, Constants.SQL_EXPRESSION_TRUE);
							} else {
								Collection<ZExpression> combinedExpressionCollection = new HashSet<ZExpression>();
								combinedExpressionCollection.addAll(containedExpressions1);
								combinedExpressionCollection.addAll(containedExpressions2);

								ZExpression combinedExpressions = SQLUtility.combineExpresions(combinedExpressionCollection, Constants.SQL_LOGICAL_OPERATOR_AND);
								joinTable = new SQLJoinTable(rightTableLogicalTable, joinType, combinedExpressions);	
							}


							String fromItemAlias = rightTableFromItem.getAlias();
							if(fromItemAlias == null || fromItemAlias.equals("")) {
								if(logicalTableAlias != null && !logicalTableAlias.equals("")) {
									rightTableLogicalTable.setAlias(logicalTableAlias);	
								}
							} else {
								rightTableLogicalTable.setAlias(fromItemAlias);
							}
						}
					} else if(rightTableFromItem instanceof SQLJoinTable) {
						joinTable = (SQLJoinTable) rightTableFromItem;
						String rightTableAlias = joinTable.getJoinSource().getAlias();
						Collection<ZExpression> containedExpressions1 = SQLUtility.containsPrefix(newJoinExpression, rightTableAlias);
						Collection<ZExpression> containedExpressions2 = SQLUtility.containsPrefix(newWhereExpression, rightTableAlias);
						Collection<ZExpression> combinedExpressionCollection = new HashSet<ZExpression>();
						combinedExpressionCollection.addAll(containedExpressions1);
						combinedExpressionCollection.addAll(containedExpressions2);
						ZExpression combinedExpressions = SQLUtility.combineExpresions(combinedExpressionCollection, Constants.SQL_LOGICAL_OPERATOR_AND);
						joinTable.addOnExpression(combinedExpressions);	
					}

					result.addFromItem(joinTable);						
				}

				//result.addWhere(rightTableSQLQuery.getWhere());
				result.addSelects(rightTableSelectItems);
				Collection<ZSelectItem> newProjections = result.pushProjectionsDown(selectItems, mapAliasSelectItems);
				result.setSelectItems(newProjections);
			} else if(rightTable instanceof SQLFromItem) {
				SQLFromItem leftTableFromItem = (SQLFromItem) rightTable; 
				result.addFromItem(leftTableFromItem);
			}
		}

		return result;
	}

	public static SQLQuery create(Collection<ZSelectItem> selectItems
			, Collection<? extends SQLLogicalTable> sqlLogicalTables
			, ZExpression whereExpression, String databasetype) {
		SQLQuery result = new SQLQuery();
		result.setDatabaseType(databasetype);

		Map<String, ZSelectItem> mapAliasSelectItems = 
				new LinkedHashMap<String, ZSelectItem>();

		for(SQLLogicalTable sqlLogicalTable : sqlLogicalTables) {
			if(sqlLogicalTable instanceof SQLQuery) {
				SQLQuery logicalTableSQLQuery = (SQLQuery) sqlLogicalTable;
				Vector<ZFromItem> logicalTableFromItems = logicalTableSQLQuery.getFrom();
				for(ZFromItem fromItem : logicalTableFromItems) {
					String fromItemAlias = fromItem.getAlias();
					if(fromItemAlias == null || fromItemAlias.equals("")) {
						String logicalTableAlias = sqlLogicalTable.getAlias();
						if(logicalTableAlias != null && !logicalTableAlias.equals("")) {
							fromItem.setAlias(logicalTableAlias);	
						}
					}
					result.addFromItem(fromItem);	
				}

				result.addWhere(logicalTableSQLQuery.getWhere());
				Collection<ZSelectItem> selectItemsLeft = logicalTableSQLQuery.getSelectItems();
				result.addSelects(selectItemsLeft);
				Map<String, ZSelectItem> mapAliasSelectItemsLeft = logicalTableSQLQuery.buildMapAliasSelectItem();
				mapAliasSelectItems.putAll(mapAliasSelectItemsLeft);
			} else if(sqlLogicalTable instanceof SQLFromItem) {
				SQLFromItem leftTableFromItem = (SQLFromItem) sqlLogicalTable; 
				result.addFromItem(leftTableFromItem);
			}
		}

		Collection<ZSelectItem> newProjections = result.pushProjectionsDown(selectItems, mapAliasSelectItems);
		result.setSelectItems(newProjections);
		ZExp newExpression = result.pushFilterDown(whereExpression, mapAliasSelectItems);
		result.addWhere(newExpression);
		return result;
	}

	//	public static SQLQuery createAux(Collection<ZSelectItem> selectItems
	//			, SQLQuery leftTable, SQLQuery rightTable, ZExpression onExpression) {
	//		SQLQuery result = new SQLQuery();
	//		
	//		Vector<ZFromItem> fromItemsLeft = leftTable.getFrom();
	//		result.addFromItems(fromItemsLeft);
	//		Vector<ZFromItem> fromItemsRight = rightTable.getFrom();
	//		result.addFromItems(fromItemsRight);
	//
	//		result.addWhere(leftTable.getWhere());
	//		result.addWhere(rightTable.getWhere());
	//		
	//		Collection<ZSelectItem> selectItemsLeft = leftTable.getSelectItems();
	//		result.addSelects(selectItemsLeft);
	//		Collection<ZSelectItem> selectItemsRight = rightTable.getSelectItems();
	//		result.addSelects(selectItemsRight);
	//		
	//		Map<String, ZSelectItem> mapAliasSelectItemsLeft = leftTable.buildMapAliasSelectItem();
	//		Map<String, ZSelectItem> mapAliasSelectItemsRight = rightTable.buildMapAliasSelectItem();
	//		Map<String, ZSelectItem> mapAliasSelectItems = 
	//				new LinkedHashMap<String, ZSelectItem>(mapAliasSelectItemsLeft);
	//		mapAliasSelectItems.putAll(mapAliasSelectItemsRight);
	//		result.pushProjectionsDown(selectItems, mapAliasSelectItems);
	//		
	//		result.pushFilterDown(onExpression, mapAliasSelectItems);
	//		
	//		return result;
	//	}



	public Collection<ZSelectItem> pushProjectionsDown(Collection<ZSelectItem> pushedProjections) {
		Map<String, ZSelectItem> mapInnerAliasSelectItem = this.buildMapAliasSelectItem();
		Collection<ZSelectItem> newProjections = this.pushProjectionsDown(pushedProjections, mapInnerAliasSelectItem);
		return newProjections;
	}

	public void pushFilterDown(ZExpression pushedFilter) {
		Map<String, ZSelectItem> mapInnerAliasSelectItem = this.buildMapAliasSelectItem();
		ZExp newFilter = this.pushFilterDown(pushedFilter, mapInnerAliasSelectItem);
		this.addWhere(newFilter);

	}

	public void pushOrderByDown(Collection<ZOrderBy> pushedOrderByCollection) {
		Map<String, ZSelectItem> mapInnerAliasSelectItem = this.buildMapAliasSelectItem();
		Vector<ZOrderBy> newOrderByCollection = SQLUtility.pushOrderByDown(pushedOrderByCollection, mapInnerAliasSelectItem);
		this.setOrderBy(newOrderByCollection);
	}

	public void addSelectItems(Collection<ZSelectItem> newSelectItems) {
		this.getSelect().addAll(newSelectItems);
		
	}
}
