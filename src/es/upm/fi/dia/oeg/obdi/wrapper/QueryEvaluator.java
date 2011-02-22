package es.upm.fi.dia.oeg.obdi.wrapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import es.upm.fi.dia.oeg.obdi.Utility;

public class QueryEvaluator {
	
	public static ResultSet evaluateQuery(String query, Connection conn) throws SQLException {
		
		
		return Utility.executeQuery(conn, query);
	}
}
