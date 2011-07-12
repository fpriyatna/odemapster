package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZSelectItem;

public class R2OSelectItem extends ZSelectItem {
	private String schema;
	private String table;
	private String column;
	
	public R2OSelectItem() {
		super();
	}

	
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
		return super.toString().hashCode();
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


	@Override
	public String toString() {
		String result;
		String thisInString = super.toString();
		String databaseType = R2ORunner.configurationProperties.getDatabaseType();
		if(databaseType == null) {
			databaseType = R2OConstants.DATABASE_MYSQL;
		}
		
		if(R2OConstants.DATABASE_MONETDB.equalsIgnoreCase(databaseType)) {
			boolean isExpression = this.isExpression();
			
			if(this.isExpression()) {
				result = this.getExpression().toString();
			} else {
				if(this.getTable() == null) {
					result = this.getColumn();
				} else {
					String table = "\"" + this.getTable() + "\"";
					String column = "\"" + this.getColumn() + "\"";
					result = table + "." + column;
				}
				//result = super.toString();
				
			}

		} else {
			result = super.toString();
		}
		
		return result;
		
	}

	
	
	
}
