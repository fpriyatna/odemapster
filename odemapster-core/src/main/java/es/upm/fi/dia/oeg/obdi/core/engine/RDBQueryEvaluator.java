package es.upm.fi.dia.oeg.obdi.core.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.DBUtility;

public class RDBQueryEvaluator extends AbstractQueryEvaluator {
	private Connection conn;
	private int timeout;
	
	public static ResultSet evaluateQuery(String query, Connection conn, int timeout) throws SQLException {
		return DBUtility.executeQuery(conn, query, timeout);
	}
	
	@Override
	public AbstractResultSet evaluateQuery(String query) 
			throws Exception {
		ResultSet rs = DBUtility.executeQuery(this.conn, query, this.timeout);
		AbstractResultSet abstractResultSet = new RDBResultSet(rs);
		return abstractResultSet;
	}
	
	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	protected void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	
}
