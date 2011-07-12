package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.TranslatorUtility;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;

public class R2OJoinQuery {
	private static Logger logger = Logger.getLogger(R2OJoinQuery.class);
	
	private String joinType;
	//private ZExp joinSource;
	//private String joinSourceAlias;
	
	private R2OFromItem joinSource;
	
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
		
		
		result.append(" ON " + this.onExp);
		return result.toString();

	}

//	public void setJoinSourceAlias(String joinSourceAlias) {
//		this.joinSourceAlias = joinSourceAlias;
//	}
//	
	
}
