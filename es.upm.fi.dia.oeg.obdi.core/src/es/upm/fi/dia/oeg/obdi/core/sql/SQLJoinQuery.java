package es.upm.fi.dia.oeg.obdi.core.sql;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

public class SQLJoinQuery {
	private static Logger logger = Logger.getLogger(SQLJoinQuery.class);
	
	private String joinType;
	//private ZExp joinSource;
	//private String joinSourceAlias;
	
	private SQLFromItem joinSource;
	
	private ZExp onExp;
	
	public void setOnExpression(ZExp onExp) {
		if(onExp instanceof ZConstant ||
				onExp instanceof ZExpression) {
			this.onExp = onExp;
		} else {
			logger.error("Invalid join on expression");
		}
		
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
//	public void setJoinSource(ZExp joinSource) {
//		this.joinSource = joinSource;
//	}
	
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

//	public void setJoinSourceAlias(String joinSourceAlias) {
//		this.joinSourceAlias = joinSourceAlias;
//	}
//	
	
}
