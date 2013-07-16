package es.upm.fi.dia.oeg.obdi.core.sql;

import java.util.Collection;
import java.util.HashSet;

import Zql.ZExp;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.core.Constants;

public class SQLSelectItem extends ZSelectItem implements Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String dbType;
	private String schema;
	private String table;
	private String column;
	private String columnType;
	
	public static SQLSelectItem createSQLItem(String dbType) {
		SQLSelectItem selectItem = new SQLSelectItem();
		selectItem.dbType = dbType;
		return selectItem;
	}
	
	
	public SQLSelectItem() {
		super();
	}

	
	private SQLSelectItem(String dbType, String schema, String table,
			String column, String columnType) {
		super();
		this.dbType = dbType;
		this.schema = schema;
		this.table = table;
		this.column = column;
		this.columnType = columnType;
	}


	public static SQLSelectItem createSQLItem(String dbType, String inputColumnName, String tableAlias) {
		if(tableAlias != null && !tableAlias.equals("")) {
			inputColumnName = tableAlias + "." + inputColumnName;
		}
		
		SQLSelectItem selectItem = new SQLSelectItem(inputColumnName);
		selectItem.dbType = dbType;
		return selectItem;
	}

	public SQLSelectItem(String arg0) {
		super(arg0);
		
		String[] splitColumns = arg0.split("\\.");
		if(splitColumns.length == 1) {//nr
		} else if(splitColumns.length == 2) { //product.nr
			this.table = splitColumns[0];
		} else if(splitColumns.length == 3) { //benchmark.product.nr
			this.schema = splitColumns[0];
			this.table = splitColumns[1];
		} else if(splitColumns.length == 4) {//benchmark.dbo.product.nr			
			this.schema = splitColumns[0];
			this.table = splitColumns[1] + "." + splitColumns[2];
		}
		String[] splitColumnType = splitColumns[splitColumns.length - 1].split("::");
		this.column = splitColumnType[0];
		if(splitColumnType.length > 1) {
			this.columnType = splitColumnType[1];	
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
		
		if(this.dbType == null) {
			dbType = Constants.DATABASE_MYSQL;
		}
		
		this.isExpression();
		String enclosedCharacter = "\"";

		if(Constants.DATABASE_MONETDB.equalsIgnoreCase(dbType)) {
			enclosedCharacter = "\"";
		} else if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(dbType)) {
			enclosedCharacter = "\"";
		} else {
			enclosedCharacter = "\\`";
		}

		if(this.isExpression()) {
			result = this.getExpression().toString();
		} else {
			result = this.getFullyQualifiedName(enclosedCharacter);
		}

		if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(dbType) && this.columnType != null) {
			result += "::" + this.columnType; 
		}
		
		String alias = this.getAlias();
		if(alias != null && !alias.equals("")) {
			result += " AS " + alias;
		}
		
		return result;
	}


	@Override
	public String getColumn() {
		if(this.column.startsWith("\"") && this.column.endsWith("\"")) {
			return this.column.substring(1, this.column.length()-1);
		} else {
			return this.column;
		}
		
	}

	public String columnToString() {
		String result;
		
		if(Constants.DATABASE_MONETDB.equalsIgnoreCase(this.dbType)) {
			result = "\"" + this.getColumn() + "\"";
		} else {
			result = this.column;
		}
		
		return result;
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

	@Override
	public SQLSelectItem clone() throws CloneNotSupportedException {
		SQLSelectItem selectItem;
		if(this.isExpression()) {
			selectItem = new SQLSelectItem();
			selectItem.setExpression(this.getExpression());
		} else {
			String alias = this.getAlias();
			this.setAlias("");
			
			selectItem = new SQLSelectItem(this.dbType, this.schema, this.table, this.column, null);
			if(alias != null) {
				this.setAlias(alias);	
			}
		}
		
		if(this.dbType != null) {
			selectItem.setDbType(this.dbType);
		}
		
		return selectItem;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getColumnType() {
		return columnType;
	}

	public String getFullyQualifiedName(String enclosedCharacter) {
		String result = "";
		
		if(this.schema != null) {
			result += enclosedCharacter + this.schema + enclosedCharacter + ".";
		}
		if(this.table != null) {
			result += enclosedCharacter + this.table + enclosedCharacter + ".";
		}
		if(this.column != null) {
			result += enclosedCharacter + this.column + enclosedCharacter + ".";
		}
		
		//remove the last dot and space
		result = result.substring(0, result.length() - 1);

		return result;
	}
	
}
