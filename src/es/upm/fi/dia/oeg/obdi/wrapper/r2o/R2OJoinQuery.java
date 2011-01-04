package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

public class R2OJoinQuery {
	private String joinType;
	private ZExp joinSource;
	private String joinSourceAlias;
	private ZExpression onExpression;
	
	public void setOnExpression(ZExpression onExpression) {
		this.onExpression = onExpression;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
	public void setJoinSource(ZExp joinSource) {
		this.joinSource = joinSource;
	}
	
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(this.joinType + " JOIN ");
		
		if(this.joinSourceAlias == null) {
			result.append(" " + this.joinSource);
		} else {
			if(this.joinSource instanceof ZConstant) {
				result.append(this.joinSource + " AS " + this.joinSourceAlias);
			} else {
				result.append("(" + this.joinSource + ") AS " + this.joinSourceAlias);
			}
			
		}
		
		
		result.append(" ON " + this.onExpression);
		return result.toString();

	}

	public void setJoinSourceAlias(String joinSourceAlias) {
		this.joinSourceAlias = joinSourceAlias;
	}
	
	
}
