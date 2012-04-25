package es.upm.fi.dia.oeg.obdi.wrapper.r2rml;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

public class R2RMLDatatypeMapper {
	
	private static Map<Integer, String> mapDatatype = new HashMap<Integer, String>();
			
	static {
		//mapDatatype.put(new Integer(Types.VARCHAR), XSDDatatype.XSDstring.getURI());
		
		mapDatatype.put(new Integer(Types.INTEGER), XSDDatatype.XSDinteger.getURI());
		mapDatatype.put(new Integer(Types.FLOAT), XSDDatatype.XSDdouble.getURI());
		mapDatatype.put(new Integer(Types.REAL), XSDDatatype.XSDdouble.getURI());
		mapDatatype.put(new Integer(Types.BIGINT), XSDDatatype.XSDinteger.getURI());
		
		mapDatatype.put(new Integer(Types.DATE), XSDDatatype.XSDdate.getURI());
		
		mapDatatype.put(new Integer(Types.TIME), XSDDatatype.XSDtime.getURI());
		mapDatatype.put(new Integer(Types.TIMESTAMP), XSDDatatype.XSDdateTime.getURI());
		
		
	}
	
	public static String getMappedType(int sqlType) {
		return R2RMLDatatypeMapper.mapDatatype.get(new Integer(sqlType));
	}
}
