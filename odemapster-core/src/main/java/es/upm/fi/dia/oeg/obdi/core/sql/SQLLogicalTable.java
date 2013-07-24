package es.upm.fi.dia.oeg.obdi.core.sql;

import Zql.ZExp;

public interface SQLLogicalTable {

	public String generateAlias();
	public void setAlias(String alias);
	public String getAlias();
	
	public void setJoinType(String joinType);
	public String getJoinType();
	
	public void setOnExp(ZExp onExp);
	public ZExp getOnExp();
	
	public String print(boolean withAlias);
	
}
