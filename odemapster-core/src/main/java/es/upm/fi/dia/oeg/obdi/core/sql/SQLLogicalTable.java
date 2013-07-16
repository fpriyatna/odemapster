package es.upm.fi.dia.oeg.obdi.core.sql;

public interface SQLLogicalTable {

	public String generateAlias();
	public void setAlias(String alias);
	public String getAlias();
}
