package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import Zql.ZExpression;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

public interface IQuery extends SQLLogicalTable {
	public String generateAlias();

	public ArrayList<String> getSelectItemAliases();
	public void cleanupPrefixes();
	public SQLQuery setOrderBy(Vector<ZOrderBy> orderByConditions);
	public Vector<ZOrderBy> getOrderBy();
	public IQuery eliminateSubQuery(Collection<ZSelectItem> newSelectItems
			, ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions
			, String databaseType) throws Exception;
}
