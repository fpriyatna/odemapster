package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.Random;

import Zql.ZAliasedName;
import Zql.ZFromItem;
import Zql.ZSelectItem;

public class R2OFromItem extends ZFromItem {
	public static int FORM_TABLE = ZAliasedName.FORM_TABLE;
	private static int FORM_COLUMN = ZSelectItem.FORM_COLUMN;
	public static int FORM_QUERY = FORM_TABLE + ZAliasedName.FORM_COLUMN;  
	private String alias;
	
	private int form;
	
	public int getForm() {
		return form;
	}

	public R2OFromItem(String fullName, int form) {
		super(fullName);
		this.form = form;
	}
	
	public String generateAlias() {
		//return R2OConstants.VIEW_ALIAS + this.hashCode();
		if(this.alias == null) {
			this.alias = R2OConstants.VIEW_ALIAS + new Random().nextInt(10000); 
		}
		return this.alias;
	}
	
	
}
