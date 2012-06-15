package es.upm.fi.dia.oeg.obdi;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import test.r2rml.R2RMLTS;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

public class DatatypeMapper {
	private static Logger logger = Logger.getLogger(DatatypeMapper.class);
	private Map<String, String> mapXMLDatatype = new HashMap<String, String>();
	public Map<String, String> getMapXMLDatatype() {
		return mapXMLDatatype;
	}

	public Map<String, Integer> getMapDBDatatype() {
		return mapDBDatatype;
	}

	private Map<String, Integer> mapDBDatatype = new HashMap<String, Integer>();
	
	private final static Map<Integer, String> mapDatatype = new HashMap<Integer, String>();
	static {
		//mapDatatype.put(new Integer(Types.VARCHAR), XSDDatatype.XSDstring.getURI());
		mapDatatype.put(new Integer(Types.INTEGER), XSDDatatype.XSDinteger.getURI());
		mapDatatype.put(new Integer(Types.FLOAT), XSDDatatype.XSDdouble.getURI());
		mapDatatype.put(new Integer(Types.REAL), XSDDatatype.XSDdouble.getURI());
		mapDatatype.put(new Integer(Types.DATE), XSDDatatype.XSDdate.getURI());
		mapDatatype.put(new Integer(Types.TIME), XSDDatatype.XSDtime.getURI());
		mapDatatype.put(new Integer(Types.TIMESTAMP), XSDDatatype.XSDdateTime.getURI());
	}
	
	public static String getMappedType(int sqlType) {
		return DatatypeMapper.mapDatatype.get(new Integer(sqlType));
	}
	
	public void mapResultSetTypes(ResultSetMetaData rsmd) {
		try {
			int columnCount = rsmd.getColumnCount();
			for (int i=0; i<columnCount; i++) {
				String columnName = rsmd.getColumnName(i+1);
				int columnType= rsmd.getColumnType(i+1);
				
				//logger.info("rsmd.getColumnClassName(i+1) = " + rsmd.getColumnClassName(i+1));
				//logger.info("rsmd.getColumnTypeName(i+1) = " + rsmd.getColumnTypeName(i+1));
				
				String mappedDatatype = DatatypeMapper.getMappedType(columnType);
				mapXMLDatatype.put(columnName, mappedDatatype);
				mapDBDatatype.put(columnName, new Integer(columnType));
			}			
		} catch(SQLException e) {
			String errorMessage = "Error mapping data types, error message = " + e.getMessage();
			logger.error(errorMessage);
		}
	}
	
	
}
