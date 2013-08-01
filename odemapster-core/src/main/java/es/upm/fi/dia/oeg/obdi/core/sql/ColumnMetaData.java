package es.upm.fi.dia.oeg.obdi.core.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.Constants;

public class ColumnMetaData {
	private static Logger logger = Logger.getLogger(ColumnMetaData.class);
	private String tableName;
	private String columnName;
	private String dataType;
	private boolean isNullable = true;
	
	public ColumnMetaData(String tableName, String columnName, String dataType, boolean isNullable) {
		super();
		this.tableName = tableName;
		this.columnName = columnName;
		this.dataType = dataType;
		this.isNullable = isNullable;
	}
	
	public static Map<String, Map<String, ColumnMetaData>> buildColumnsMetaData(
			Connection conn, String databaseName, String databaseType) {
		Map<String, Map<String, ColumnMetaData>> result = new HashMap<String, Map<String, ColumnMetaData>>();
		
		if(conn != null) {
			try {
				java.sql.Statement stmt = conn.createStatement();
				
				if(databaseType.equalsIgnoreCase(Constants.DATABASE_MYSQL)) {
					String query = "SELECT * FROM information_schema.columns WHERE TABLE_SCHEMA = '" + databaseName + "'";
					ResultSet rs = stmt.executeQuery(query);
					while(rs.next()) {
						String tableName = rs.getString("TABLE_NAME");
						Map<String, ColumnMetaData> mapTableColumnMetaData = result.get(tableName);
						if(mapTableColumnMetaData == null) {
							mapTableColumnMetaData = new HashMap<String, ColumnMetaData>();
							result.put(tableName, mapTableColumnMetaData);
						}
								
						String columnName = rs.getString("COLUMN_NAME");
						ColumnMetaData columnMetaData = mapTableColumnMetaData.get(columnName);
						if(columnMetaData == null) {
							String dataType = rs.getString("DATA_TYPE");
							String isNullableString = rs.getString("IS_NULLABLE");
							boolean isNullable = true;
							if(isNullableString != null) {
								if(isNullableString.equalsIgnoreCase("NO") || isNullableString.equalsIgnoreCase("FALSE")) {
									isNullable = false;
								}
							}
							
							columnMetaData = new ColumnMetaData(
									tableName, columnName, dataType, isNullable);
							mapTableColumnMetaData.put(columnName, columnMetaData);
						}
					}					
				}
			} catch(Exception e) {
				logger.error("Error while getting table meta data");
			}
		}
		
		return result;
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getDataType() {
		return dataType;
	}

	@Override
	public String toString() {
		return "ColumnMetaData [tableName=" + tableName + ", columnName="
				+ columnName + ", dataType=" + dataType + ", isNullable="
				+ isNullable + "]";
	}

	public boolean isNullable() {
		return isNullable;
	}



	
}
