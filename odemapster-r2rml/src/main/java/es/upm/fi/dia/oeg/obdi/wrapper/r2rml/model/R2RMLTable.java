package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;

public class R2RMLTable extends R2RMLLogicalTable {
	private String tableName;

	public R2RMLTable(String tableName) throws Exception {
		super();
		if(tableName == null || tableName.equals("")) {
			throw new Exception("Empty table name specified!");
		}
		this.tableName = tableName;
		super.logicalTableType = LogicalTableType.TABLE_NAME;
	}

	@Override
	public String getValue() {
		return this.tableName;
	}
	
	
	
}
