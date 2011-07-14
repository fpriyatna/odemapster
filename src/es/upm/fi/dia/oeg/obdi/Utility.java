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

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.tdb.TDBFactory;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.MonetDBColumn;

import Zql.ZAliasedName;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;



public class Utility {
	private static Logger logger = Logger.getLogger(Utility.class);

	public static Model createJenaModel(String jenaMode) {
		Model model = null;

		if(jenaMode == null) {
			//logger.warn("Unspecified jena mode, memory based will be used!");
			model = Utility.createJenaMemoryModel();
		} else {
			if(jenaMode.equalsIgnoreCase(R2OConstants.JENA_MODE_TYPE_HSQL)) {
				//logger.debug("jena mode = idb hsqldb");
				model = Utility.createJenaHSQLDBModel();
			} else if(jenaMode.equalsIgnoreCase(R2OConstants.JENA_MODE_TYPE_TDB)) {
				//logger.debug("jena mode = tdb");
				model = Utility.createJenaTDBModel();
			} else if (jenaMode.equalsIgnoreCase(R2OConstants.JENA_MODE_TYPE_MEMORY)){
				//logger.debug("jena mode = memory");
				model = Utility.createJenaMemoryModel();
			} else {
				//logger.warn("invalid mode of jena type, memory mode will be used.");
				model = Utility.createJenaMemoryModel();
			}				
		}		

		return model;
	}

	private static Model createJenaMemoryModel() {
		return ModelFactory.createDefaultModel();
	}

	private static Model createJenaHSQLDBModel() {
		try {
			String className = "org.hsqldb.jdbcDriver";       // path of driver class
			Class.forName(className);                        // Load the Driver
			String DB_URL =    "jdbc:hsqldb:file:testdb4";   // URL of database 
			String DB_USER =   "sa";                          // database user id
			String DB_PASSWD = "";                            // database password
			String DB =        "HSQL";                        // database type

			// Create database connection
			IDBConnection conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
			ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;

			// create or open the default model
			Model model = maker.createDefaultModel();

			// Close the database connection
			conn.close();

			return model;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Model createJenaTDBModel() {
		String jenaDatabaseName = System.currentTimeMillis() + ""; 
		String tdbDatabaseFolder = "tdb-database";
		File folder = new File(tdbDatabaseFolder);
		if(!folder.exists()) {
			folder.mkdir();
		}

		String tdbFileBase = tdbDatabaseFolder + "/" + jenaDatabaseName;
		logger.info("TDB filebase = " + tdbFileBase);
		return TDBFactory.createModel(tdbFileBase) ;

	}

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
			String username, String databaseName, String password, String driverString, String url, String requester) 
	throws SQLException {
		Connection conn;

		try {
			Properties prop = new Properties();
			prop.put("ResultSetMetaDataOptions", "1");
			prop.put("user", username);
			prop.put("database", databaseName);
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


	public static boolean inArray(String[] delegableOperations, String operationId) {
		boolean isDelegableOperation = false;

		for(int i=0 ; i<delegableOperations.length && !isDelegableOperation; i++) {
			if(delegableOperations[i].trim().equalsIgnoreCase(operationId.trim())) {
				isDelegableOperation = true;
			}
		}

		return isDelegableOperation;

	}

	public static ZExpression renameColumns(
			ZExpression zExpression, String tableName, String viewName, boolean matchCondition) throws Exception 
			{
		String operator = zExpression.getOperator();
		ZExpression result = new ZExpression(operator);

		Collection<ZExp> operands = zExpression.getOperands();
		for(ZExp operand : operands) {
			if(operand instanceof ZConstant) {
				ZConstant newOperandConstant;

				ZConstant operandConstant = (ZConstant) operand;

				if(operandConstant.getType() == ZConstant.COLUMNNAME) {
					String operandConstantValue = operandConstant.getValue();
					operandConstantValue = operandConstantValue.replaceAll("\'", "");
					if(operandConstantValue.startsWith(tableName + ".") == matchCondition) {
						//be careful here when passing sql server column names that have 4 elements 
						//(db.schema.table.column)
						ZAliasedName oldColumnName = new ZAliasedName(
								operandConstantValue, ZAliasedName.FORM_COLUMN);
						String newColumnName = viewName + "." + oldColumnName.getColumn();
						//newOperandConstant = new ZConstant(newColumnName, operandConstant.getType());
						newOperandConstant = Utility.constructDatabaseColumn(newColumnName);						
					} else {
						newOperandConstant = operandConstant;
					}
				} else {
					newOperandConstant = operandConstant;					
				}
				result.addOperand(newOperandConstant);
			} else if(operand instanceof ZExpression) {
				ZExpression operandExpression  = (ZExpression) operand;
				result.addOperand(Utility.renameColumns(operandExpression, tableName, viewName, matchCondition));
			} else {
				throw new Exception("Unsupported columns renaming operation!");
			}
		}
		return result;
			}

	public static Collection<ZSelectItem> renameColumns(
			Collection<ZSelectItem> selectItems, String tableName, String alias, boolean matches) throws Exception 
			{
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();

		for(ZSelectItem selectItem :selectItems) {
			boolean isExpression = selectItem.isExpression();
			
			
			if(isExpression) {
				ZExp selectItemExp = selectItem.getExpression();
				ZExpression newExpression = Utility.renameColumns((ZExpression) selectItemExp, tableName, alias, matches);
				selectItem.setExpression(newExpression);
				result.add(selectItem);
			} else {
				String selectItemSchema = selectItem.getSchema() ;
				String selectItemTable = selectItem.getTable();
				
				if(tableName.equals(selectItemSchema + "." + selectItemTable) == matches) {
					ZSelectItem newSelectItem = new ZSelectItem(alias + "." + selectItem.getColumn());
					if(selectItem.getAlias() != null) {
						newSelectItem.setAlias(selectItem.getAlias());
					}
					result.add(newSelectItem);
				} else {
					result.add(selectItem);
					
				}
			}
		}

		return result;
			}



	public static ZConstant constructDatabaseColumn(String columnName) {
		String databaseType = R2ORunner.configurationProperties.getDatabaseType();
		if(databaseType == null) {
			databaseType = R2OConstants.DATABASE_MYSQL;
		}

		ZConstant zColumn;
		if(databaseType.equalsIgnoreCase(R2OConstants.DATABASE_MONETDB)) {
			zColumn = new MonetDBColumn(columnName, ZConstant.COLUMNNAME);
		} else if(databaseType.equalsIgnoreCase(R2OConstants.DATABASE_MYSQL)) {
			//			zColumn = new ZConstant("\'" + columnName + "\'", ZConstant.COLUMNNAME);
			zColumn = new ZConstant(columnName, ZConstant.COLUMNNAME);
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


	public static String encodeLiteral(String originalLiteral) {
		String result = originalLiteral;
		try {
			if(result != null) {
				result = result.replaceAll("\"", "\\\\\"");
				result = result.replaceAll("\'", "\\\\\'");
				result = result.replaceAll("\\\\n", " ");
				result = result.replaceAll("\\\\r", " ");
				result = result.replaceAll("_{2,}+", "_");
			}
		} catch(Exception e) {
			logger.error("Error encoding literal for literal = " + originalLiteral + " because of " + e.getMessage());
		}

		return result;
	}

	private static String preEncoding(String uri) {
		uri = uri.replaceAll("\\(", "_");
		uri = uri.replaceAll("\\)", "_");
		uri = uri.replaceAll("\\[", "_");
		uri = uri.replaceAll("\\]", "_");
		//			uri = uri.replaceAll("\\.", "_");
		uri = uri.replaceAll("\n", " ");
		uri = uri.replaceAll("\\n", " ");
		uri = uri.replaceAll("\t", " ");
		uri = uri.replaceAll("\\t", " ");
		//			uri = uri.replaceAll("\"", "_");

		uri = uri.replaceAll("\\\\", "%5C");
		uri = uri.replaceAll("\\b\\s{2,}\\b", " ");

		return uri;

	}

	private static String postEncoding(String uri) {
		uri = uri.replaceAll(",", "%2C");
		uri = uri.replaceAll("&", "%26");
		uri = uri.replaceAll("'", "%27");
		uri = uri.replaceAll(" ", "%20");
		uri = uri.replaceAll("_{2,}+", "_");

		uri = uri.replaceAll("%23", "#");
		uri = uri.replaceAll("&", "and");

		return uri;
	}

	public static String encodeURI(String originalURI)  throws Exception {
		String uri = originalURI;
		try {
			uri = Utility.preEncoding(uri);

			//	uri = new URI(uri).toASCIIString();
			uri = new URI(null, uri, null).toASCIIString();

			uri = Utility.postEncoding(uri);
		} catch(Exception e) {
			logger.error("Error encoding uri for uri = " + originalURI + " because of " + e.getMessage());
			throw e;
		}

		return uri;
	}

	public static void main(String args[]) throws Exception {

		String str1 = "[\nab,c";
		System.out.println("str1Encoded = " + Utility.encodeURI(str1));

		String str2 = "_";
		System.out.println("str2Encoded = " + Utility.encodeURI(str2));

		String str3 = "\\";
		System.out.println("str3Encoded = " + Utility.encodeURI(str3));

		String str4 = "Ã�ndi%ce";
		System.out.println("str4Encoded = " + Utility.encodeURI(str4));

		String str5 = "	?";
		System.out.println("str5Encoded = " + Utility.encodeURI(str5));

		String str6 = ",";
		System.out.println("str6Encoded = " + Utility.encodeURI(str6));

		String str7 = "'";
		System.out.println("str7Encoded = " + Utility.encodeURI(str7));

		String str8 = "abc def";
		System.out.println("str8Encoded = " + Utility.encodeURI(str8));

		String str9 = "\"";
		System.out.println("str9Encoded = " + Utility.encodeURI(str9));

		String uri1 = "http://edu.linkeddata.es/UPM/resource/Actividad/Manual    de la calidad del Laboratorio de Ensayos QuÃ�micos Industriales , ((LEQIM)), Rev.10";
		System.out.println("uri1Encoded = " + Utility.encodeURI(uri1));

		String uri2 = "http://edu.linkeddata.es/UPM/resource/LineaInvestigaci%C3%B3n/31656_AnÃ¡lisis del Sector de las TIC\\s";
		System.out.println("uri2Encoded = " + Utility.encodeURI(uri2));

		String uri3 = "http://edu.linkeddata.es/UPM/resource/OtroParticipante/ Gallardo	_Fernando";
		System.out.println("uri3Encoded = " + Utility.encodeURI(uri3));

		String uri4 = "http://www.google.com/espaÃ±a spain#lang=en,es";
		System.out.println("uri4Encoded = " + Utility.encodeURI(uri4));

		String uri5 = "http://geo.linkeddata.es/HospitalesMadrid#HospitÃ¡l110051";
		System.out.println("uri5Encoded = " + Utility.encodeURI(uri5));

		String uri6 = "http://edu.linkeddata.es/UPM/resource/Fecha/31/12/2004";
		System.out.println("uri6Encoded = " + Utility.encodeURI(uri6));

		String uri7 = "http://edu.linkeddata.es/UPM/resource/OtroParticipante/LABORATORIO \"SALVADOR VELAYOS\"_INSTITUTO DE MAGNETISMO APLICADO";
		System.out.println("uri7Encoded = " + Utility.encodeURI(uri7));

		String uri8 = "http://edu_linkeddata_es/UPM/resource/Actividad/10013_ANÃ�LISIS%20E%20INVESTIGACIÃ“N%20DE%20ACELERACIÃ“N%20DE\n%20VALORACIÃ“N%20FINANCIERA%20MEDIANTE%20PLATAFORMAS%20RECONFIGURABLES";
		System.out.println("uri8Encoded = " + Utility.encodeURI(uri8));

		String literal1 = "Say \\r \"Hello World\"";
		System.out.println("literal1 = " + literal1);
		System.out.println("literal1Encoded = " + Utility.encodeLiteral(literal1));

		String literal2 = "Say \\n \'Hello World\'";
		System.out.println("literal2 = " + literal2);
		System.out.println("literal2Encoded = " + Utility.encodeLiteral(literal2));

		String literal3 = "Soledad_____Hurtado";
		System.out.println("literal3Encoded = " + Utility.encodeLiteral(literal3));

		String str10 = "A & D ARQUITECTURA Y DECORACIÓN 2000";
		System.out.println("str10Encoded = " + Utility.encodeURI(str10));

		/*
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
		 */
	}


	//Creates a triple
	public static String createTriple(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append(subject);
		result.append(" ");
		result.append(predicate);
		result.append(" ");
		result.append(object);
		result.append(" .\n");


		return result.toString();
	}

	//Create Literal
	public static String createLiteral(String value)
	{
		value = Utility.encodeLiteral(value);
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"");
		return result.toString();
	}

	//Create typed literal
	public static String createDataTypeLiteral(String value, String datatypeURI)
	{
		value = Utility.encodeLiteral(value);
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"^^");
		result.append(datatypeURI);
		return result.toString();
	}

	//Create language tagged literal
	public static String createLanguageLiteral(String text, String languageCode)
	{
		text = Utility.encodeLiteral(text);
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(text);
		result.append("\"@");
		result.append(languageCode);
		return result.toString();
	}

	//Create URIREF from namespace and element
	public static String createURIref(String namespace, String element)
	{
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(namespace);
		result.append(element);
		result.append(">");
		return result.toString();
	}

	//Create URIREF from URI
	public static String createURIref(String uri)
	{
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(uri);
		result.append(">");
		return result.toString();
	}

	//Create blank node from id
	public static String createBlankNode(String id)
	{
		StringBuffer result = new StringBuffer();
		result.append("_:");
		result.append(id);
		return result.toString();
	}
	
	//TODO improve this
	public static boolean isIRI(String id) {
		if(id != null && id.startsWith("http://")) {
			return true;
		} else {
			return false;
		}
	}

}
