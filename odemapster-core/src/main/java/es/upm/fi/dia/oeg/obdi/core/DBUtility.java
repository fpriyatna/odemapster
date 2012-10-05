package es.upm.fi.dia.oeg.obdi.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.engine.Constants;

public class DBUtility {
	private static Logger logger = Logger.getLogger(DBUtility.class);
	
	public static boolean execute(Connection conn, String query) throws SQLException {
		Statement stmt = conn.createStatement();
		
		try  {
			stmt.setQueryTimeout(60);
			stmt.setFetchSize(Integer.MIN_VALUE);
		} catch(Exception e) {
			logger.debug("Can't set fetch size!");
		}

		logger.debug("Executing query = \n" + query);

		try {
			long start = System.currentTimeMillis();
			boolean result = stmt.execute(query);
			long end = System.currentTimeMillis();
			logger.debug("View creation/deletion time was "+(end-start)+" ms.");

			return result;
		} catch(SQLException e) {
			e.printStackTrace();
			logger.error("Error executing query, error message = "+ e.getMessage());
			throw e;
		}

	}
	
	public static ResultSet executeQuery(Connection conn, String query, int timeout) throws SQLException {
		//		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		//				ResultSet.CONCUR_READ_ONLY);

		//		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
		//		ResultSet.CONCUR_READ_ONLY);

		//st.setFetchSize(1000);
		//		Statement st = conn.createStatement();
		//		st.setFetchSize(1000);

//		Statement stmt = conn.createStatement(
//				java.sql.ResultSet.TYPE_FORWARD_ONLY,
//				java.sql.ResultSet.CONCUR_READ_ONLY);
		Statement stmt = conn.createStatement();
				
		try  {
			if(timeout > 0) {
				stmt.setQueryTimeout(timeout);
			}
			//stmt.setFetchSize(Integer.MIN_VALUE);
		} catch(Exception e) {
			logger.warn("Exception occur : " + e.getMessage());
		}

		logger.debug("Executing query = \n" + query);

		try {
			long start = System.currentTimeMillis();
			ResultSet result = stmt.executeQuery(query);
			long end = System.currentTimeMillis();
			logger.info("SQL execution time was "+(end-start)+" ms.");

			return result;
		} catch(SQLException e) {
			//e.printStackTrace();
			logger.error("Error executing query, error message = "+ e.getMessage());
			throw e;
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
	
    public static int getRowCount(ResultSet set) throws SQLException  
    {  
       int rowCount;  
       int currentRow = set.getRow();            // Get current row  
       rowCount = set.last() ? set.getRow() : 0; // Determine number of rows  
       if (currentRow == 0)                      // If there was no current row  
          set.beforeFirst();                     // We want next() to go to first row  
       else                                      // If there WAS a current row  
          set.absolute(currentRow);              // Restore it  
       return rowCount;  
    }  
}
