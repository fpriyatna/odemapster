package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.Random;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;

import Zql.ZAliasedName;
import Zql.ZFromItem;
import Zql.ZSelectItem;

public class SQLFromItem extends ZFromItem implements SQLLogicalTable {
	public enum LogicalTableType {TABLE, SQLQUERY};
	
//	public static int FORM_TABLE = ZAliasedName.FORM_TABLE;
//	public static int FORM_QUERY = FORM_TABLE + ZAliasedName.FORM_COLUMN;  
//	private int form;
	
	//private String alias;
	private LogicalTableType form;
	
	public LogicalTableType getForm() {
		return form;
	}

	public SQLFromItem(String fullName, LogicalTableType form) {
		super(fullName);
		this.form = form;
	}
	
	public String generateAlias() {
		//return R2OConstants.VIEW_ALIAS + this.hashCode();
		if(super.getAlias() == null) {
			super.setAlias(R2OConstants.VIEW_ALIAS + new Random().nextInt(10000));
		}
		return super.getAlias();
	}

	@Override
	public String toString() {
		String result = "";
		
		String alias = this.getAlias();
		if(alias != null) {
			this.setAlias("");
			if(this.form == LogicalTableType.TABLE) {
				String tableName = super.toString();
				//tableName = R2RMLUtility.replaceNameWithSpaceChars(tableName);
				result = tableName + " " + alias;
			} else {
				result = "(" + super.toString() + ") " + alias;
			}
			
			this.setAlias(alias);
		} else {
			String tableName = super.toString();
			//tableName = R2RMLUtility.replaceNameWithSpaceChars(tableName);
			result = tableName;
		}		
		
		return result;
	}
	
	
	
}
