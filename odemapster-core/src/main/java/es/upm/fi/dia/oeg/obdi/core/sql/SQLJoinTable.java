package es.upm.fi.dia.oeg.obdi.core.sql;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.Constants;
import Zql.ZExpression;
import Zql.ZFromItem;

public class SQLJoinTable extends ZFromItem {
	private static Logger logger = Logger.getLogger(SQLJoinTable.class);

	private SQLLogicalTable joinSource;
	private String joinType;
	private ZExpression onExpression;

	public SQLJoinTable(SQLLogicalTable joinSource,String joinType, ZExpression onExp) {
		super();
		this.joinType = joinType;
		this.joinSource = joinSource;
		this.onExpression = onExp;
	}

	public SQLJoinTable(SQLLogicalTable joinSource) {
		this(joinSource, null, null);
	}
	
	public void setOnExpression(ZExpression onExp) {
		this.onExpression = onExp;
	}

	public void addOnExpression(ZExpression onExp2) {
		this.onExpression = SQLUtility.combineExpressions(this.onExpression, onExp2, Constants.SQL_LOGICAL_OPERATOR_AND);
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
		if(this.joinType != null) {
			result.append(this.joinType + " JOIN ");	
		}
		result.append(this.joinSource);
		if(this.onExpression != null) {
			result.append(" ON " + this.onExpression);	
		}
		
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
