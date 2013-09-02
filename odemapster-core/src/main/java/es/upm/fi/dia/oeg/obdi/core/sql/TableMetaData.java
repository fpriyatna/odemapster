package es.upm.fi.dia.oeg.obdi.core.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

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
				
				String query = null;
				String tableNameColumn = null;
				String tableRowsColumn = null;
				if(databaseType.equalsIgnoreCase(Constants.DATABASE_MYSQL)) {
					query = "SELECT * FROM information_schema.tables WHERE TABLE_SCHEMA = '" + databaseName + "'";
					tableNameColumn = "TABLE_NAME";
					tableRowsColumn = "TABLE_ROWS";
				} else if(databaseType.equalsIgnoreCase(Constants.DATABASE_POSTGRESQL)) {
					query = "SELECT * FROM pg_stat_user_tables ";
					tableNameColumn = "relname";
					tableRowsColumn = "seq_tup_read";					
				}

				if(query != null) {
					ResultSet rs = stmt.executeQuery(query);
					while(rs.next()) {
						String tableName = rs.getString(tableNameColumn);
						long tableRows = rs.getLong(tableRowsColumn);
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
