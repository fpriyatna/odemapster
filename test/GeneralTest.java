

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.engine.QueryEvaluator;

public class GeneralTest {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception {
		
		String input = "http://www.google.com/Hello Río";
		System.out.println(Utility.encodeURI(input));
		String inputString1 = "INSERT INTO TBLSTUDENT(CL1, CL2, CL3, CL4, CL5, CL6) VALUES (a, b, c, d, e, f);";
		String inputString2 = "INSERT INTO TBLSTUDENT(a, b, c, d, e ,f);";
		
		String[] inputString1Split = inputString1.split("\\s");
		System.out.println(inputString1Split.length);
		
		String[] inputString2Split = inputString2.split("\\s");
		System.out.println(inputString2Split.length);
		
	}

}
