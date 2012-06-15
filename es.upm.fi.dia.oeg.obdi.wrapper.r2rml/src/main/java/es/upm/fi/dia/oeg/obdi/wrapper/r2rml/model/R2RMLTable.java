package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;

public class R2RMLTable extends R2RMLLogicalTable {
	private String tableName;

	public R2RMLTable(String tableName) {
		super();
		this.tableName = tableName;
		super.logicalTableType = LogicalTableType.TABLE;
	}

	@Override
	public String getValue() {
		return this.tableName;
	}
	
	
	
}
