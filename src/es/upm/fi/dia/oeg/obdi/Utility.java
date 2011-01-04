package es.upm.fi.dia.oeg.obdi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.MonetDBColumn;

import Zql.ZAliasedName;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;



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

		logger.info("Executing query = \n" + query);

		try {
			long start = System.currentTimeMillis();
			ResultSet result = stmt.executeQuery(query);
			long end = System.currentTimeMillis();
			logger.info("SQL execution time was "+(end-start)+" ms.");

			return result;
		} catch(SQLException e) {
			e.printStackTrace();
			logger.error("Error executing query");
			logger.error("Error message = " + e.getMessage());
			throw e;
		}



	}

	public static Connection getLocalConnection(
			String username, String password, String driverString, String url, String requester) 
	throws SQLException {
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
			logger.info("Error opening database connection : " + e.getMessage());
			e.printStackTrace();

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

	public static String encodeURI(String uri)  {
		String result = uri;
		try {


			result = new URI(null, uri, null).toASCIIString();
			result = result.replaceAll(",", "%2C");
			result = result.replaceAll("'", "%27");
			//System.out.println("result = " + result);

			/*
			result = new URI(null, uri, null).toURL().toString();
			System.out.println("result = " + result);
			 */
		} catch(Exception e) {
			logger.error("Error encoding uri for uri = " + uri + " because of " + e.getMessage());
		}

		return result;
	}

	public static boolean inArray(String[] delegableOperations, String operationId) {
		boolean isDelegableOperation = false;

		for(int i=0 ; i<delegableOperations.length && !isDelegableOperation; i++) {
			if(delegableOperations[i].trim().equalsIgnoreCase(operationId.trim())) {
				isDelegableOperation = true;
			}
		}

		return isDelegableOperation;

	}

	public static ZExpression renameColumnsIfNotMatch(
			ZExpression zExpression, String tableName, String viewName) throws Exception 
			{
		String operator = zExpression.getOperator();
		ZExpression result = new ZExpression(operator);

		Collection<ZExp> operands = zExpression.getOperands();
		for(ZExp operand : operands) {
			if(operand instanceof ZConstant) {
				ZConstant newOperandConstant;

				ZConstant operandConstant = (ZConstant) operand;
				if(operandConstant.getType() == ZConstant.COLUMNNAME 
						&& !operandConstant.getValue().startsWith(tableName + ".")) {
					ZAliasedName oldColumnName = new ZAliasedName(
							operandConstant.getValue(), ZAliasedName.FORM_COLUMN);
					String newColumnName = viewName + "." + oldColumnName.getColumn();
					//newOperandConstant = new ZConstant(newColumnName, operandConstant.getType());
					newOperandConstant = Utility.constructDatabaseColumn(newColumnName);
				} else {
					newOperandConstant = operandConstant;					
				}
				result.addOperand(newOperandConstant);
			} else if(operand instanceof ZExpression) {
				ZExpression operandExpression  = (ZExpression) operand;
				result.addOperand(Utility.renameColumnsIfNotMatch(operandExpression, tableName, viewName));
			} else {
				throw new Exception("Unsupported columns renaming operation!");
			}
		}
		return result;
			}

	public static Collection<ZSelectItem> renameColumnsIfNotMatch(
			Collection<ZSelectItem> selectItems, String tableName, String alias) throws Exception 
			{
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();

		for(ZSelectItem selectItem :selectItems) {
			ZExp selectItemExp = selectItem.getExpression();
			if(selectItemExp instanceof ZExpression) {
				ZExpression newExpression = Utility.renameColumnsIfNotMatch((ZExpression) selectItemExp, tableName, alias);
				selectItem.setExpression(newExpression);
				result.add(selectItem);
			} else if(selectItemExp instanceof ZConstant) {
				if(tableName.equals(selectItem.getSchema() + "." + selectItem.getTable())) {
					result.add(selectItem);
				} else {
					result.add(new ZSelectItem(alias + "." + selectItem.getColumn()));
				}
			}
		}

		return result;
			}

	public static void main(String args[]) throws SQLException {
		String uri = "http://www.google.com/esp'a�a spain#lang=en,es";
		String uri2 = "http://geo.linkeddata.es/resource/�Qui�nes disfrutamos del parque?, Senda de educaci�n ambiental 1.2 o variante |";
		String newURI = Utility.encodeURI(uri2);
		System.out.println("newURI = " + newURI);

		Connection conn = Utility.getLocalConnection("bsbm1m", "bsbm1m"
				, "nl.cwi.monetdb.jdbc.MonetDriver", "jdbc:monetdb://localhost/demo", null);
		String bsbmQuery01 = "SELECT distinct nr, label"
			+ " FROM product p, producttypeproduct ptp"
			+ " WHERE p.nr = ptp.product AND ptp.\"productType\"=105"
			+ " AND p.propertyNum1 > 486"
			+ "	AND p.nr IN (SELECT distinct product FROM productfeatureproduct WHERE productFeature=815)"
			+ "	AND p.nr IN (SELECT distinct product FROM productfeatureproduct WHERE productFeature=814)"
			+ " ORDER BY label"
			+ " LIMIT 10";
		Utility.executeQuery(conn, bsbmQuery01);
	}

	public static ZConstant constructDatabaseColumn(String columnName) {
		String databaseType = R2ORunner.configurationProperties.getDatabaseType();
		if(databaseType == null) {
			databaseType = R2OConstants.DATABASE_MYSQL;
		}

		ZConstant zColumn;
		if(databaseType.equalsIgnoreCase(R2OConstants.DATABASE_MONETDB)) {
			zColumn = new MonetDBColumn(columnName, ZConstant.COLUMNNAME);
		} else {
			zColumn = new ZConstant(columnName, ZConstant.COLUMNNAME);
		}


		return zColumn;

	}

	public static String readFileAsString(String filePath) throws IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} finally {
			if (f != null) try { f.close(); } catch (IOException ignored) { }
		}
		return new String(buffer);
	}

	public static OntModel openOntoDescFromFile(String filePath) {
		logger.debug("opening from file " + filePath + "..");
		OntModel m = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_DL_MEM, null);
		try {
			File theOntoDoc = new File(filePath);
			FileInputStream fis = new FileInputStream(theOntoDoc);
			m.read(fis, null);
			return m;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return m;
		}
	}

	public static OntModel openOntoDescFromUrl(String url) {
		logger.debug("opening from url " + url + "..");
		OntModel m = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_DL_MEM, null);
		m.read(url);
		//m.write(System.out);
		return m;

	}

	public static String getCountryByURI(String sUri) {
		try {
			URI uri = new URI(sUri);
			String uriHost = uri.getHost();
			
			int sURILastDot = uriHost.lastIndexOf(".");
			String sCountryDomain = uriHost.substring(sURILastDot + 1);
			return sCountryDomain;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}


}

