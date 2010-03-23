package es.upm.fi.dia.oeg.obdi.wrapper.r2o.example;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.IMapping;
import es.upm.fi.dia.oeg.obdi.Parser;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;

public class ParserExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("log4j.properties");
			
			String absoluteFilePath = "/home/fpriyatna/Dropbox/workspace/es.upm.fi.dia.oeg.obdi/mappings/Luis/luis16/EGM_Estero2.r2o.xml";


			//parse r2o mapping
			Parser parser = new R2OParser(); 
			IMapping r2oMapping = parser.parse(absoluteFilePath);
			List<String> listOfMappedConcepts = r2oMapping.getMappedConcepts();
			System.out.println("listOfMappedConcepts = " + listOfMappedConcepts);
		} catch(Exception e) {
			e.printStackTrace();
		}


	}

}
