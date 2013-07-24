package es.upm.fi.dia.oeg.obdi.core.sql;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

public class SQLJoinQuery {
	private static Logger logger = Logger.getLogger(SQLJoinQuery.class);

	private String joinType;
	private SQLLogicalTable joinSource;
	private ZExpression onExp;

	public void setOnExpression(ZExpression onExp) {
		this.onExp = onExp;
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
		result.append(" ON " + this.onExp);
		return result.toString();
	}
}
