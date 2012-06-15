package es.upm.fi.dia.oeg.obdi.core.test;
import java.io.ByteArrayInputStream;

import Zql.ParseException;
import Zql.ZStatement;
import Zql.ZqlParser;


public class ZQLTest {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		String sql = "SELECT (col1 || col2) AS col3 FROM STUDENT;";
		ByteArrayInputStream bs = new ByteArrayInputStream(sql.getBytes());
		ZqlParser parser = new ZqlParser(bs);
		//parser.addCustomFunction("||", 2);
		ZStatement statement = parser.readStatement();
		System.out.println("statement = " + statement);

	}

}
