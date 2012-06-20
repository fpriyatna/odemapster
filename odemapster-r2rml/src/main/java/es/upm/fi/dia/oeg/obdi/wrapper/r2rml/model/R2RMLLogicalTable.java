package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementVisitor;

public abstract class R2RMLLogicalTable implements R2RMLElement {
	private static Logger logger = Logger.getLogger(R2RMLLogicalTable.class);
	
	private String sqlVersionIdentifier;
	//public enum LogicalTableType {TABLE, SQLQUERY};
	LogicalTableType logicalTableType;
	private String alias;
	
	static R2RMLLogicalTable parse(Resource resource) {
		R2RMLLogicalTable logicalTable = null; 
		
		Statement tableNameStatement = resource.getProperty(R2RMLConstants.R2RML_TABLENAME_PROPERTY);
		if(tableNameStatement != null) {
			String tableName = tableNameStatement.getObject().toString();
			logicalTable = new R2RMLTable(tableName); 
		} else {
			Statement sqlQueryStatement = resource.getProperty(R2RMLConstants.R2RML_SQLQUERY_PROPERTY);
			if(sqlQueryStatement == null) {
				logger.error("Invalid logical table defined : " + resource);
			}
			String sqlQueryString = sqlQueryStatement.getObject().toString().trim();
			logicalTable = new R2RMLSQLQuery(sqlQueryString);
		}
		
		return logicalTable;
	}

	public abstract String getValue();

	@Override
	public String toString() {
		String result = "";
		if(this instanceof R2RMLTable) {
			result = "R2RMLTable";
		} else if(this instanceof R2RMLSQLQuery) {
			result = "R2RMLSQLQuery";
		}
		
		return result + ":" + this.getValue();
	}

	public LogicalTableType getLogicalTableType() {
		return logicalTableType;
	}
	
	public Object accept(R2RMLElementVisitor visitor) {
		Object result = visitor.visit(this);
		return result;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}	
	
}
