package es.upm.fi.dia.oeg.obdi;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;



public class Utility {
	private static Logger logger = Logger.getLogger(Utility.class);

	public static ResultSet executeQuery(Connection conn, String query) throws SQLException {
//		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
//				ResultSet.CONCUR_READ_ONLY);

//		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
//		ResultSet.CONCUR_READ_ONLY);

		//st.setFetchSize(1000);
//		Statement st = conn.createStatement();
//		st.setFetchSize(1000);
		
		Statement stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
	              java.sql.ResultSet.CONCUR_READ_ONLY);
		
		try  {
			stmt.setFetchSize(Integer.MIN_VALUE);
		} catch(Exception e) {
			logger.debug("Can't set fetch size!");
		}
		
		//logger.info("Query = " + query);
		//logger.info("Please wait.................");
		
		try {
			ResultSet result = stmt.executeQuery(query);
			return result;
		} catch(SQLException e) {
			logger.error("Error executing query : " + query);
			logger.error("Error message = " + e.getMessage());
			throw e;
		}
		
		
		
	}
	
	public static Connection getLocalConnection(String username, String password, String driverString, String url, String requester) throws SQLException {
		Connection conn;
		
		try {
			Properties prop = new Properties();
			prop.put("ResultSetMetaDataOptions", "1");
			prop.put("user", username);
			prop.put("password", password);
			prop.put("autoReconnect", "true");
			Class.forName(driverString);
			logger.info("Opening database connection.");
			return DriverManager.getConnection(url, prop);

		} catch (Exception e) {
			//e.printStackTrace();
			throw new SQLException(e.getMessage(), e);
		}		
	}
	
	public static void closeConnection(Connection conn, String requester) {
		try {
			if(conn != null) {
				conn.close();
				logger.info("Closing db connection.");
			}
		} catch(Exception e) {
			logger.error("Error closing connection! Error message = " + e.getMessage());
		}
	}

	public static void closeRecordSet(ResultSet rs) {
		try {
			if(rs != null) {
				rs.close();
			}
		} catch(Exception e) {
			logger.error("Error closing result set! Error message = " + e.getMessage());
		}
	}

	public static void closeStatement(Statement stmt) {
		try {
			if(stmt != null) {
				stmt.close();
			}
		} catch(Exception e) {
			logger.error("Error closing statement! Error message = " + e.getMessage());
		}
	}
	
	public static String encodeURI(String uri) {
		String newURI = uri;
		try {
			String splitChar = "#";
			int splitPosition = uri.lastIndexOf(splitChar);
			if(splitPosition == -1) {
				splitChar = "/";
				splitPosition = uri.lastIndexOf(splitChar);
			}

			String leftPart = uri.substring(0, splitPosition);
			String rightPart = uri.substring(splitPosition+1, uri.length());

			String encodedURI = "";
			encodedURI = rightPart.replaceAll(" ", "_");
			encodedURI = URLEncoder.encode(encodedURI, "UTF-8");


			newURI = leftPart + splitChar + encodedURI;
		} catch(Exception e) {
			logger.error("Error encoding uri for uri = " + uri);
			//e.printStackTrace();
		}
		
		return newURI;
	}
	
	public static boolean inArray(String[] delegableOperations, String operationId) {
		boolean isDelegableOperation = false;
		
		for(int i=0 ; i<delegableOperations.length && !isDelegableOperation; i++) {
			if(delegableOperations[i].equalsIgnoreCase(operationId)) {
				isDelegableOperation = true;
			}
		}
		
		return isDelegableOperation;

	}
}
