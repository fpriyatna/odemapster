package es.upm.fi.dia.oeg.obdi.core.sql;

import org.apache.log4j.Logger;

import Zql.ZExpression;

public class SQLJoinQuery {
	private static Logger logger = Logger.getLogger(SQLJoinQuery.class);

	private SQLLogicalTable joinSource;
	private String joinType;
	private ZExpression onExpression;

	public SQLJoinQuery(SQLLogicalTable joinSource,String joinType, ZExpression onExp) {
		super();
		this.joinType = joinType;
		this.joinSource = joinSource;
		this.onExpression = onExp;
	}

	public SQLJoinQuery(SQLLogicalTable joinSource) {
		this(joinSource, null, null);
	}
	
	public void setOnExpression(ZExpression onExp) {
		this.onExpression = onExp;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public void setJoinSource(SQLFromItem fromItem) {
		this.joinSource = fromItem;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(this.joinType + " JOIN ");
		result.append(" " + this.joinSource);
		result.append(" ON " + this.onExpression);
		return result.toString();
	}

	public SQLLogicalTable getJoinSource() {
		return joinSource;
	}

	public String getJoinType() {
		return joinType;
	}

	public ZExpression getOnExpression() {
		return onExpression;
	}
}
