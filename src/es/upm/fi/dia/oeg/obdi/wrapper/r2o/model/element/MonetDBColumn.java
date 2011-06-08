package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import Zql.ZConstant;
import Zql.ZSelectItem;


public class MonetDBColumn extends ZConstant {


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
