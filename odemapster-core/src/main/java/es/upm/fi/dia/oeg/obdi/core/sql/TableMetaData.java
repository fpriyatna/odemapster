package es.upm.fi.dia.oeg.obdi.core.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.Constants;

public class TableMetaData {
	private static Logger logger = Logger.getLogger(TableMetaData.class);

	private String tableName;
	private long tableRows;
	
	public TableMetaData(String tableName, long tableRows) {
		this.tableName = tableName;
		this.tableRows = tableRows;
	}
	
	public String getTableName() {
		return tableName;
	}
	public long getTableRows() {
		return tableRows;
	}
	
	public static Map<String, TableMetaData> buildTablesMetaData(
			Connection conn, String databaseName, String databaseType) {
		Map<String, TableMetaData> result = new HashMap<String, TableMetaData>();
		
		if(conn != null) {
			try {
				java.sql.Statement stmt = conn.createStatement();
				
				if(databaseType.equalsIgnoreCase(Constants.DATABASE_MYSQL)) {
					String query = "SELECT * FROM information_schema.tables WHERE TABLE_SCHEMA = 'bsbm100m'";
					ResultSet rs = stmt.executeQuery(query);
					while(rs.next()) {
						String tableName = rs.getString("TABLE_NAME");
						long tableRows = rs.getLong("TABLE_ROWS");
						TableMetaData tableMetaData = new TableMetaData(tableName, tableRows);
						result.put(tableMetaData.tableName, tableMetaData);
					}					
				}

			} catch(Exception e) {
				logger.error("Error while getting table meta data");
			}
		}
		
		return result;
	}
}	
