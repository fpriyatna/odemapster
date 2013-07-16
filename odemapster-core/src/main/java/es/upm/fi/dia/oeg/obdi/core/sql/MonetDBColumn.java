package es.upm.fi.dia.oeg.obdi.core.sql;

import Zql.ZConstant;
import Zql.ZSelectItem;


public class MonetDBColumn extends ZConstant {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MonetDBColumn(String arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		ZSelectItem selectItem = new ZSelectItem(this.getValue());
		
		String table = "\"" + selectItem.getTable() + "\"";
		String column = "\"" + selectItem.getColumn() + "\"";

		String result = table + "." + column;
		
		return result;

	}
	
}
