package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZOrderBy;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.oeg.obdi.core.utility.CollectionUtility;

public class SQLUnion implements IQuery {
	String alias;
	private String databaseType;
	private Collection<SQLQuery> unionQueries = new Vector<SQLQuery>();
	private Vector<ZOrderBy> orderByConditions;
	private String joinType;
	private ZExp onExp;
	
	public SQLUnion() { 
		this.unionQueries = new Vector<SQLQuery>();
	}

	public SQLUnion(Collection<? extends IQuery> queries) {
		this.unionQueries = new Vector<SQLQuery>();
		
		for(IQuery query : queries) {
			this.add(query);
		}
	}

	public void add(IQuery newQuery) {
		if(this.unionQueries == null) {
			this.unionQueries = new Vector<SQLQuery>();
		}
		
		if(newQuery != null) {
			if(newQuery instanceof SQLQuery) {
				this.unionQueries.add((SQLQuery) newQuery);	
			} else if(newQuery instanceof SQLUnion) {
				Collection<SQLQuery> queries = ((SQLUnion) newQuery).getUnionQueries();
				for(SQLQuery sqlQuery : queries) {
					this.unionQueries.add(sqlQuery);
				}
			}
		}
	}

	public Collection<SQLQuery> getUnionQueries() {
		return unionQueries;
	}

	@Override
	public String toString() {
		String result = null;
		String unionString = "\n" + Constants.SQL_KEYWORD_UNION + "\n" ;
		
		CollectionUtility collectionUtility = new CollectionUtility();
		if(this.unionQueries != null) {
			Collection<Object> unionQueriesCollection = new Vector<Object>();
			StringBuffer stringBuffer = new StringBuffer();
			for(IQuery sqlQuery : this.unionQueries) {
				unionQueriesCollection.add(sqlQuery);
				String sqlQueryString = sqlQuery.toString(); 
				stringBuffer.append(sqlQueryString);
				stringBuffer.append(unionString);
			}
			result = collectionUtility.mkString(unionQueriesCollection, 
					"\n" + Constants.SQL_KEYWORD_UNION + "\n", "", "\n");
		}
		
		if(this.orderByConditions != null && this.orderByConditions.size() > 0) {
			Collection<Object> orderByConditionsCollection = new Vector<Object>();
			for(ZOrderBy orderBy : this.orderByConditions) {
				orderByConditionsCollection.add(orderBy);
			}
			
			
			String orderByString = collectionUtility.mkString(orderByConditionsCollection, 
					", ", Constants.SQL_KEYWORD_ORDER_BY + " ", " ");
			result = result + orderByString;
		}
			
		return result;
	}

	public String generateAlias() {
		if(this.alias == null) {
			this.alias = Constants.VIEW_ALIAS + new Random().nextInt(10000);
		}
		return this.alias;
	}

	public ArrayList<String> getSelectItemAliases() {
		return this.unionQueries.iterator().next().getSelectItemAliases();
	}

	public void cleanupSelectItems() {
		if(this.unionQueries != null) {
			for(SQLQuery sqlQuery : this.unionQueries) {
				sqlQuery.cleanupSelectItems();
			}
		}
		
	}

	public void setOrderBy(Vector<ZOrderBy> orderByConditions) {
		this.orderByConditions = orderByConditions;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return this.alias;
	}

	public Vector<ZOrderBy> getOrderBy() {
		Vector<ZOrderBy> result = new Vector<ZOrderBy>();
		
		if(this.unionQueries != null && !this.unionQueries.isEmpty()) {
			result = this.unionQueries.iterator().next().getOrderBy();
		}
		return result;
	}

	public IQuery removeSubQuery(Collection<ZSelectItem> newSelectItems,
			ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions,
			String databaseType) throws Exception {
		SQLUnion result = new SQLUnion();
		Iterator<SQLQuery> it = this.unionQueries.iterator();
		while(it.hasNext()) {
			IQuery sqlQuery = it.next();
			IQuery resultAux;
			if(it.hasNext()) {
				resultAux = sqlQuery.removeSubQuery(
						newSelectItems, newWhereCondition, null, databaseType);				
			} else {
				resultAux = sqlQuery.removeSubQuery(
						newSelectItems, newWhereCondition, orderByConditions, databaseType);				
			}

			result.add(resultAux);
		}
		
		return result;
		
	}

	public Collection<ZSelectItem> getSelectItems() {
		return this.unionQueries.iterator().next().getSelectItems();
	}

	public void setSelectItems(Collection<ZSelectItem> newSelectItems) {
		for(SQLQuery query : this.unionQueries) {
			query.setSelectItems(newSelectItems);
		}		
	}

	public void addWhere(ZExp newWhere) {
		for(SQLQuery query : this.unionQueries) {
			query.addWhere(newWhere);
		}		
	}

	public void cleanupOrderBy() {
		for(SQLQuery query : this.unionQueries) {
			query.cleanupOrderBy();
		}
	}

	public IQuery removeSubQuery() throws Exception {
		SQLUnion result = new SQLUnion();
		Iterator<SQLQuery> it = this.unionQueries.iterator();
		while(it.hasNext()) {
			SQLQuery query = it.next();
			IQuery resultAux = query.removeSubQuery();
//			if(!it.hasNext()) {
//				resultAux.setOrderBy(this.orderByConditions);
//			}
			result.add(resultAux);
		}

		result.setOrderBy(this.orderByConditions);
		return result;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;		
	}

	public void setOnExp(ZExp onExp) {
		this.onExp = onExp;
	}

	public String getJoinType() {
		return this.joinType;
	}

	public ZExp getOnExp() {
		return this.onExp;
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


	public void setDistinct(boolean distinct) {
		// TODO Auto-generated method stub
		
	}

	public boolean getDistinct() {
		// TODO Auto-generated method stub
		return false;
	}

	public void pushProjectionsDown(Collection<ZSelectItem> pushedProjections) {
		for(SQLQuery sqlQuery : this.unionQueries) {
			Map<String, ZSelectItem> mapInnerAliasSelectItem = 
					sqlQuery.buildMapAliasSelectItemAux(this.getAlias());

			sqlQuery.pushProjectionsDown(pushedProjections
					, mapInnerAliasSelectItem);
		}
	}

//	public Map<String, ZSelectItem> buildMapAliasSelectItem() {
//		Map<String, ZSelectItem> mapAliasSelectItem = new LinkedHashMap<String, ZSelectItem>();
//		for(SQLQuery sqlQuery : this.unionQueries) {
//			Map<String, ZSelectItem> mapAliasSelectItemAux = 
//					sqlQuery.buildMapAliasSelectItemAux(this.getAlias());
//			mapAliasSelectItem.putAll(mapAliasSelectItemAux);
//		}
//		return mapAliasSelectItem;
//	}

	public void pushOrderByDown(Collection<ZOrderBy> pushedOrderByCollection) {
		Iterator<SQLQuery> it = this.unionQueries.iterator();
		while(it.hasNext()) {
			SQLQuery sqlQuery = it.next();
			
			if(!it.hasNext()) {
				Map<String, ZSelectItem> mapInnerAliasSelectItem = 
						sqlQuery.buildMapAliasSelectItemAux(this.getAlias());
				
				sqlQuery.pushOrderByDown(pushedOrderByCollection
						, mapInnerAliasSelectItem);				
			}
		}
	}

	public void pushFilterDown(ZExpression pushedFilter) {
		Iterator<SQLQuery> it = this.unionQueries.iterator();
		while(it.hasNext()) {
			SQLQuery sqlQuery = it.next();
			
			Map<String, ZSelectItem> mapInnerAliasSelectItem = 
					sqlQuery.buildMapAliasSelectItemAux(this.getAlias());
			
			sqlQuery.pushFilterDown(pushedFilter, mapInnerAliasSelectItem);
		}
	}

}
