package es.upm.fi.dia.oeg.obdi.core.model;

import java.sql.Connection;
import java.util.Map;

import es.upm.fi.dia.oeg.obdi.core.sql.ColumnMetaData;
import es.upm.fi.dia.oeg.obdi.core.sql.TableMetaData;

public abstract class AbstractLogicalTable {
	protected Map<String, ColumnMetaData> columnsMetaData;
	protected TableMetaData tableMetaData;

	public abstract void buildMetaData(Connection conn) throws Exception;
	
	public TableMetaData getTableMetaData() {
		return this.tableMetaData;
	}

	public long getLogicalTableSize() {
		return this.tableMetaData.getTableRows();
	}

	public Map<String, ColumnMetaData> getColumnsMetaData() {
		return columnsMetaData;
	}
}
