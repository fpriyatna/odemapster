package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

public class SQLJoin implements IQuery {
	private Collection<ZSelectItem> selectItems;
	private SQLLogicalTable leftTable;
	private SQLLogicalTable rightTable;
	private String joinType;
	private ZExpression onExpression;
	private boolean distinct = false;
	private long slice = -1;
	private long offset = -1;
	
	public void setAlias(String alias) {
		// TODO Auto-generated method stub
		
	}
	public String getAlias() {
		// TODO Auto-generated method stub
		return null;
	}
	public String print(boolean withAlias) {
		// TODO Auto-generated method stub
		return null;
	}
	public String generateAlias() {
		// TODO Auto-generated method stub
		return null;
	}
	public Collection<ZSelectItem> getSelectItems() {
		// TODO Auto-generated method stub
		return null;
	}
	public ArrayList<String> getSelectItemAliases() {
		// TODO Auto-generated method stub
		return null;
	}
	public void cleanupSelectItems() {
		// TODO Auto-generated method stub
		
	}
	public void cleanupOrderBy() {
		// TODO Auto-generated method stub
		
	}
	public void setOrderBy(Vector<ZOrderBy> orderByConditions) {
		// TODO Auto-generated method stub
		
	}
	public Vector<ZOrderBy> getOrderBy() {
		// TODO Auto-generated method stub
		return null;
	}
	public IQuery removeSubQuery(Collection<ZSelectItem> newSelectItems,
			ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions,
			String databaseType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public void setSelectItems(Collection<ZSelectItem> newSelectItems) {
		// TODO Auto-generated method stub
		
	}
	public void addWhere(ZExp newWhere) {
		// TODO Auto-generated method stub
		
	}
	public IQuery removeSubQuery() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public String getDatabaseType() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setDatabaseType(String dbType) {
		// TODO Auto-generated method stub
		
	}
	public Collection<ZSelectItem> pushProjectionsDown(Collection<ZSelectItem> pushedProjections) {
		// TODO Auto-generated method stub
		return null;
	}
	public void pushFilterDown(ZExpression pushedFilter) {
		// TODO Auto-generated method stub
		
	}
	public SQLLogicalTable getLeftTable() {
		return leftTable;
	}
	public void setLeftTable(SQLLogicalTable leftTable) {
		this.leftTable = leftTable;
	}
	public SQLLogicalTable getRightTable() {
		return rightTable;
	}
	public void setRightTable(SQLLogicalTable rightTable) {
		this.rightTable = rightTable;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	public ZExpression getOnExpression() {
		return onExpression;
	}
	public void setOnExpression(ZExpression onExpression) {
		this.onExpression = onExpression;
	}
	@Override
	public String toString() {
		String result = "";
		
		String selectItemsString = SQLUtility.printSelectItems(this.getSelectItems(), this.distinct);
		selectItemsString += selectItemsString; 

		return result;
	}

	
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}
	public boolean getDistinct() {
		return this.distinct;
	}
	public void pushProjectionsDown(Collection<ZSelectItem> pushedProjections,
			Map<String, ZSelectItem> mapInnerAliasSelectItem) {
		// TODO Auto-generated method stub
		
	}
	public Map<String, ZSelectItem> buildMapAliasSelectItem() {
		// TODO Auto-generated method stub
		return null;
	}
	public void pushOrderByDown(Collection<ZOrderBy> pushedOrderByCollection) {
		// TODO Auto-generated method stub
		
	}
	public void setSlice(long slice) {
		// TODO Auto-generated method stub
		
	}
	public void setOffset(long offset) {
		// TODO Auto-generated method stub
		
	}
	public void addSelectItems(Collection<ZSelectItem> newSelectItems) {
		// TODO Auto-generated method stub
		
	}	

	
}
