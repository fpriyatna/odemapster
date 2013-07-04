package es.upm.fi.dia.oeg.obdi.odemapster_core_querytranslator;

import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.oxford.comlab.requiem.rewriter.Clause;

import es.upm.fi.dia.oeg.newrqr.ISI2RQRLexer;
import es.upm.fi.dia.oeg.newrqr.ISI2RQRParser;

public class DatalogParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DatalogParser parser = new DatalogParser();
		Collection<Clause> datalogProgram = parser.readDatalogString("triple(?0, 'http://example.com/hasSalary', '?Salary') <- triple(?0, 'http://example.com/hasJob', 'Programmer') ^ triple(?0, 'http://example.com/hasTitle', 'Senior')");
		System.out.println("datalogProgram = " + datalogProgram);
	}


	
	public Collection<Clause> readDatalogString(String datalogString) {
		Collection<Clause> result = new ArrayList<Clause>();
		ISI2RQRLexer lexer = new ISI2RQRLexer(new ANTLRStringStream(datalogString));
		ISI2RQRParser parser = new ISI2RQRParser(new CommonTokenStream(lexer));
		try {
			result = parser.program();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}		
}
