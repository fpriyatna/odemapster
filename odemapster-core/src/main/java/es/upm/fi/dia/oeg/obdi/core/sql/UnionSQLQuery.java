package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import Zql.ZExpression;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

import es.upm.fi.dia.oeg.obdi.core.Constants;

public class UnionSQLQuery implements IQuery {
	String alias;
	private Collection<SQLQuery> unionQueries = new Vector<SQLQuery>();

	public UnionSQLQuery() { 
		this.unionQueries = new Vector<SQLQuery>();
	}

	public UnionSQLQuery(Collection<? extends IQuery> queries) {
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
			} else if(newQuery instanceof UnionSQLQuery) {
				Collection<SQLQuery> queries = ((UnionSQLQuery) newQuery).getUnionQueries();
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
		
		String unionSQL = "\nUNION\n";
		if(this.unionQueries != null) {
			StringBuffer stringBuffer = new StringBuffer();
			for(IQuery sqlQuery : this.unionQueries) {
				String sqlQueryString = sqlQuery.toString(); 
				stringBuffer.append(sqlQueryString);
				stringBuffer.append(unionSQL); 
			}
			result = stringBuffer.toString();
			result = result.substring(0, result.length() - unionSQL.length());
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

	public void cleanupPrefixes() {
		if(this.unionQueries != null) {
			for(SQLQuery sqlQuery : this.unionQueries) {
				sqlQuery.cleanupPrefixes();
			}
		}
		
	}

	public SQLQuery setOrderBy(Vector<ZOrderBy> orderByConditions) {
		SQLQuery result;
		
		if(this.unionQueries == null || this.unionQueries.isEmpty()) {
			result = null;
		} else if(this.unionQueries.size() == 1) {
			result = this.unionQueries.iterator().next();
			result.setOrderBy(orderByConditions);
		} else {
			SQLQuery firstQuery = this.unionQueries.iterator().next();
			Vector<ZSelectItem> oldSelectItems = firstQuery.getSelect();
			Vector<ZSelectItem> newSelectItems = new Vector<ZSelectItem>();
			for(ZSelectItem oldSelectItem : oldSelectItems) {
				SQLSelectItem newSelectItem = new SQLSelectItem(oldSelectItem.getAlias());
				newSelectItems.add(newSelectItem);
			}
			SQLQuery newQuery = new SQLQuery();
			newQuery.setSelectItems(newSelectItems);
			this.generateAlias();
			newQuery.setLogicalTable(this);
			newQuery.setOrderBy(orderByConditions);
			result = newQuery;
		}
		
		return result;
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

	public IQuery eliminateSubQuery(Collection<ZSelectItem> newSelectItems,
			ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions,
			String databaseType) throws Exception {
		UnionSQLQuery result = new UnionSQLQuery();
		
		for(SQLQuery sqlQuery : this.unionQueries) {
			SQLQuery resultAux = sqlQuery.eliminateSubQuery(newSelectItems, newWhereCondition, orderByConditions, databaseType);
			result.add(resultAux);
		}
		
		return result;
		
	}


}
