package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.TranslatorUtility;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZOrderBy;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class SQLQuery extends ZQuery implements SQLLogicalTable {
	private static Logger logger = Logger.getLogger(TranslatorUtility.class);
	private String joinType;
	private ZExp onExp;

	private Collection<SQLLogicalTable> logicalTables;
	private Collection<SQLJoinQuery> joinQueries;
	private Collection<SQLQuery> joinQueries2;
	private Collection<SQLQuery> unionQueries;
	private String alias;
	private long slice = -1;
	private boolean distinct = false;

	public SQLQuery(ZQuery zQuery) {
		if(zQuery.getSelect() != null) { this.addSelect(zQuery.getSelect());}
		if(zQuery.getFrom() != null) { this.addFrom(zQuery.getFrom());}
		if(zQuery.getWhere() != null) { this.addWhere(zQuery.getWhere());}

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
					logger.warn(selectItemAlias + " already selected");
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

	public void addUnionQuery(SQLQuery unionQuery) {
		if(this.unionQueries == null) {
			this.unionQueries = new Vector<SQLQuery>();
		}

		this.unionQueries.add(unionQuery);
	}

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
			this.alias = R2OConstants.VIEW_ALIAS + new Random().nextInt(10000);
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
			String selectItemFullValue = Utility.getValueWithoutAlias(selectItem);
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
			if(value.equals(Utility.getValueWithoutAlias(selectItem))) {
				return selectItem;
			}
		}
		return null;
	}

	public Collection<SQLQuery> getUnionQueries() {
		return unionQueries;
	}

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
		}

		return selectSQL;
	}

	private String printFrom() {
		String fromSQL = "";
		Vector<ZFromItem> mainQueryFromItems = this.getFrom();
		if(mainQueryFromItems != null && mainQueryFromItems.size()!=0) {
			for(ZFromItem mainQueryFromItem : mainQueryFromItems) {
				if(mainQueryFromItem instanceof SQLFromItem) {
					fromSQL += mainQueryFromItem.toString() + ", ";
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
					fromSQL += "(" + logicalTable.toString() + ") " + logicalTableAlias + ", ";
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


		//print select
		Vector<ZSelectItem> mainQuerySelectItems = (Vector<ZSelectItem>) this.getSelect();
		if(this.getSelect() != null && this.getSelect().size() != 0) {
			String selectSQL = this.printSelect();
			result += selectSQL + "\n";
		}

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


		if(this.joinType != null) {
			result = this.joinType + " JOIN " + result;
		}

		if(this.onExp != null) {
			result += "ON " + this.onExp + "\n";
		}

		String unionSQL = "UNION ";
		if(this.unionQueries != null) {
			for(SQLQuery unionQuery : this.unionQueries) {
				String unionQueryString = unionQuery.toString(); 
				result += unionSQL + unionQueryString; 
			}
		}

		Vector<ZOrderBy> orderByConditions = this.getOrderBy(); 
		if(orderByConditions != null) {
			result += this.printOrderBy() + "\n";
		}

		if(this.slice != -1) {
			result += "LIMIT " + this.slice;
		}
		return result.trim();

	}

	public void setUnionQueries(Collection<SQLQuery> unionQueries) {
		this.unionQueries = unionQueries;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public void setOnExp(ZExp onExp) {
		this.onExp = onExp;
	}

	@Override
	public String getAlias() {
		return this.alias;
	}

}
