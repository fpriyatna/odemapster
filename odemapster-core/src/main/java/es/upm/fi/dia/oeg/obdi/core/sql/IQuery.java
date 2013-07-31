package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

public interface IQuery extends SQLLogicalTable {
	public String generateAlias();
	public Collection<ZSelectItem> getSelectItems();
	public ArrayList<String> getSelectItemAliases();
	public void cleanupSelectItems();
	public void cleanupOrderBy();
	public void setOrderBy(Vector<ZOrderBy> orderByConditions);
	public Vector<ZOrderBy> getOrderBy();
	public IQuery removeSubQuery(Collection<ZSelectItem> newSelectItems
			, ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions
			, String databaseType) throws Exception;
	public void setSelectItems(Collection<ZSelectItem> newSelectItems);
	public void addWhere(ZExp newWhere);
	public IQuery removeSubQuery() throws Exception;
	public String getDatabaseType();
	public void setDatabaseType(String dbType);
	public void setDistinct(boolean distinct);
	public boolean getDistinct();
	
	//public Map<String, ZSelectItem> buildMapAliasSelectItem();
	public void pushProjectionsDown(Collection<ZSelectItem> pushedProjections);
	public void pushFilterDown(ZExpression pushedFilter);
	public void pushOrderByDown(Collection<ZOrderBy> pushedOrderByCollection);
	
}
