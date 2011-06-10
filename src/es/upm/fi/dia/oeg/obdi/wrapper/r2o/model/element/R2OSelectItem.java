package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZSelectItem;

public class R2OSelectItem extends ZSelectItem {
	private String schema;
	private String table;
	private String column;

//	public R2OSelectItem() {
//		super();
//	}

	
	public R2OSelectItem(String arg0) {
		super(arg0);
		this.schema = super.getSchema();
		this.table = super.getTable();
		this.column = super.getColumn();
	}


	@Override
	public void setExpression(ZExp arg0) {
		super.setExpression(arg0);
		this.schema = super.getSchema();
		this.table = super.getTable();
		this.column = super.getColumn();		
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		R2OSelectItem other = (R2OSelectItem) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		if (table == null) {
			if (other.table != null)
				return false;
		} else if (!table.equals(other.table))
			return false;
		return true;
	}

	
	
	
}
