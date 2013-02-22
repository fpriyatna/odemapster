package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.apache.log4j.Logger;

import scala.reflect.generic.Trees.This;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementVisitor;

public abstract class R2RMLLogicalTable implements R2RMLElement {
	private static Logger logger = Logger.getLogger(R2RMLLogicalTable.class);

	private String sqlVersionIdentifier;
	//public enum LogicalTableType {TABLE, SQLQUERY};
	LogicalTableType logicalTableType;
	private String alias;
	private R2RMLTriplesMap owner;
	private ResultSetMetaData rsmd;
	//private ConfigurationProperties configurationProperties;
	
	R2RMLLogicalTable(R2RMLTriplesMap owner) {}

	public ResultSetMetaData buildResultSetMetaData(Connection conn) {
		ResultSetMetaData result = null;
		try {
			if(conn != null) {
				java.sql.Statement stmt = conn.createStatement();
				String query = null;
				
				if(this instanceof R2RMLTable) {
					R2RMLTable r2rmlTable = (R2RMLTable) this;
					String tableName = r2rmlTable.getValue();
					query = "SELECT * FROM " + tableName + " WHERE 1=0";
				} else if (this instanceof R2RMLSQLQuery){
					R2RMLSQLQuery r2rmlSQLQuery = (R2RMLSQLQuery) this;
					query = r2rmlSQLQuery.getValue();
				}

				if(query != null) {
					ResultSet rs = stmt.executeQuery(query);
					result = rs.getMetaData();					
				}
			}
		} catch(Exception e) {
			logger.error("Error while producing ResultSetMetaData for Logical Table of Triples Map " + owner);
		}
		
		return result;
	}
	
	static R2RMLLogicalTable parse(Resource resource, R2RMLTriplesMap owner) throws Exception {

		R2RMLLogicalTable logicalTable = null; 

		Statement tableNameStatement = resource.getProperty(
				R2RMLConstants.R2RML_TABLENAME_PROPERTY);
		if(tableNameStatement != null) {
			String tableName = tableNameStatement.getObject().toString();
			logicalTable = new R2RMLTable(tableName, owner);
			
//			try {
//				Connection conn = this.configurationProperties.getConn();
//				if(conn != null) {
//					java.sql.Statement stmt = conn.createStatement();
//					String query = "SELECT * FROM " + tableName + " WHERE 1=0";
//					ResultSet rs = stmt.executeQuery(query);
//					logicalTable.rsmd = rs.getMetaData();
//				}
//			} catch(Exception e) {
//				logger.error("Error while producing ResultSetMetaData for Logical Table of Triples Map " + owner);
//			}
		} else {
			Statement sqlQueryStatement = resource.getProperty(
					R2RMLConstants.R2RML_SQLQUERY_PROPERTY);
			if(sqlQueryStatement == null) {
				logger.error("Invalid logical table defined : " + resource);
			}
			String sqlQueryString = sqlQueryStatement.getObject().toString().trim();
			logicalTable = new R2RMLSQLQuery(sqlQueryString, owner);
			
//			try {
//				Connection conn = AbstractRunner.configurationProperties.getConn();
//				if(conn != null) {
//					java.sql.Statement stmt = conn.createStatement();
//					ResultSet rs = stmt.executeQuery(sqlQueryString);
//					logicalTable.rsmd = rs.getMetaData();
//				}
//			} catch(Exception e) {
//				logger.error("Error while producing ResultSetMetaData for Logical Table of Triples Map " + owner);
//			}
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

	public ResultSetMetaData getRsmd() {
		return rsmd;
	}

	public void setRsmd(ResultSetMetaData rsmd) {
		this.rsmd = rsmd;
	}



}
