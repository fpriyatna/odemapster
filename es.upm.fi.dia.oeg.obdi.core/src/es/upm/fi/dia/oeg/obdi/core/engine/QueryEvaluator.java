package es.upm.fi.dia.oeg.obdi.core.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.Utility;

public class QueryEvaluator {
	
	public static ResultSet evaluateQuery(String query, Connection conn) throws SQLException {
		
		
		return DBUtility.executeQuery(conn, query);
	}
}
