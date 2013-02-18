package es.upm.fi.dia.oeg.obdi.core.engine;

import java.util.List;

import es.upm.fi.dia.oeg.obdi.core.exception.ResultSetException;

public abstract class AbstractResultSet {
	private List<String> columnNames = null;

	public abstract boolean next() throws ResultSetException;
//	public Object getObject(int columnIndex) throws ResultSetException;
//	public Object getObject(String columnLabel) throws ResultSetException;
	public abstract String getString(int columnIndex) throws ResultSetException;
	public abstract String getString(String columnLabel) throws ResultSetException;

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}
	
	public String getColumnName(int columnIndex) {
		return this.columnNames.get(columnIndex);
	}
}