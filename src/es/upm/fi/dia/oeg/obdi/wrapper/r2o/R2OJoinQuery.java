package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

public class R2OJoinQuery {
	private String joinType;
	//private ZExp joinSource;
	//private String joinSourceAlias;
	
	private R2OFromItem joinSource;
	
	private ZExpression onExpression;
	
	public void setOnExpression(ZExpression onExpression) {
		this.onExpression = onExpression;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	
//	public void setJoinSource(ZExp joinSource) {
//		this.joinSource = joinSource;
//	}
	
	public void setJoinSource(R2OFromItem fromItem) {
	this.joinSource = fromItem;
}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(this.joinType + " JOIN ");
		
		//if(this.joinSourceAlias == null) {
		if(this.joinSource.getAlias() == null) {
			result.append(" " + this.joinSource);
		} else {
			String databaseType = R2ORunner.configurationProperties.getDatabaseType();
			String joinSourceAlias = this.joinSource.getAlias();
			this.joinSource.setAlias("");
			
			//if(this.joinSource instanceof ZConstant) {
			if(this.joinSource.getForm() == R2OFromItem.FORM_TABLE) {
				result.append(this.joinSource + " AS " + joinSourceAlias);
			} else {
				result.append("(" + this.joinSource + ") AS " + joinSourceAlias);
			}
			this.joinSource.setAlias(joinSourceAlias);

			
		}
		
		
		result.append(" ON " + this.onExpression);
		return result.toString();

	}

//	public void setJoinSourceAlias(String joinSourceAlias) {
//		this.joinSourceAlias = joinSourceAlias;
//	}
//	
	
}
