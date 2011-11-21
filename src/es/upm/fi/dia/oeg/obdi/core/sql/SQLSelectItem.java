package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.Collection;
import java.util.HashSet;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLUtility;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZSelectItem;

public class SQLSelectItem extends ZSelectItem {
	private String schema;
	private String table;
	private String column;
	
	public SQLSelectItem() {
		super();
	}

	
	public SQLSelectItem(String arg0) {
		super(arg0);
		
		
		
		String[] splitColumns = arg0.split("\\.");
		if(splitColumns.length == 1) {//nr
			String columnName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[0]);
			this.column = splitColumns[0];
		} else if(splitColumns.length == 2) { //product.nr
			String tableName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[0]);			
			this.table = splitColumns[0];
			String columnName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[1]);
			this.column = splitColumns[1];
		} else if(splitColumns.length == 3) { //benchmark.product.nr
			String schemaName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[0]);
			this.schema = splitColumns[0];
			String tableName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[1]);
			this.table = splitColumns[1];
			String columnName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[2]);
			this.column = splitColumns[2];
		} else if(splitColumns.length == 4) {//benchmark.dbo.product.nr			
			String schemaName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[0]);
			this.schema = splitColumns[0];

			String tableName1 = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[1]);
			String tableName2 = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[2]);
			this.table = splitColumns[1] + "." + splitColumns[2];
			
			String columnName = R2RMLUtility.replaceNameWithSpaceChars(splitColumns[3]);
			this.column = splitColumns[3];

		}
		
		
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
		SQLSelectItem other = (SQLSelectItem) obj;
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
		String databaseType;
		try {
			databaseType = AbstractRunner.configurationProperties.getDatabaseType();
		} catch(Exception e) {
			databaseType = Constants.DATABASE_MYSQL;
		}		
		
		if(databaseType == null) {
			databaseType = Constants.DATABASE_MYSQL;
		}
		
		boolean isExpression = this.isExpression();
		if(Constants.DATABASE_MONETDB.equalsIgnoreCase(databaseType)) {
			
			
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
			if(this.isExpression()) {
				result = this.getExpression().toString(); 
			} else {
				result = "";
				if(this.schema != null) {
					result += R2RMLUtility.replaceNameWithSpaceChars(this.schema) + ".";
				}
				if(this.table != null) {
					result += R2RMLUtility.replaceNameWithSpaceChars(this.table) + ".";
				}
				if(this.column != null) {
					result += R2RMLUtility.replaceNameWithSpaceChars(this.column) + ".";
				}
				//remove the last dot and space
				result = result.substring(0, result.length() - 1);
				
			}


			//result = super.toString();
		}
		
		String alias = this.getAlias();
		if(alias != null && !alias.equals("")) {
			result += " AS " + alias;
		}
		
		return result;
		
	}


	@Override
	public String getColumn() {
		return this.column;
	}


	@Override
	public String getSchema() {
		return this.schema;
	}


	@Override
	public String getTable() {
		return this.table;
	}

	
	public static void main(String args[]) {
		Collection<SQLSelectItem> selectItems = new HashSet<SQLSelectItem>();
		SQLSelectItem selectItem1 = new SQLSelectItem("benchmark.product.nr");
		selectItems.add(selectItem1);
		SQLSelectItem selectItem2 = new SQLSelectItem("benchmark.product.label");
		selectItems.add(selectItem2);
		SQLSelectItem selectItem3 = new SQLSelectItem("benchmark.product.nr");
		selectItem3.setAlias("");
		selectItems.add(selectItem3);
		SQLSelectItem selectItem4 = new SQLSelectItem("product.label");
		selectItems.add(selectItem4);
		
		System.out.println("selectItems = " + selectItems);
		
	}
	
	
	
}
