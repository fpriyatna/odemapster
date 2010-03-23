package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.io.File;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.IMapping;
import es.upm.fi.dia.oeg.obdi.Parser;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;


public class ParserTest extends XMLTestCase {
	private Logger logger = Logger.getLogger(ParserTest.class);
	
	public void testMapping16() {
		this.testParse("mappings/Luis/luis16/EGM_Estero2.r2o.xml");
	}

	public void testTestcase15() {
		this.testParse("mappings/Luis/luis15/EGM_completa_URI.r2o.xml");
	}

	public void testTestcase14() {
		this.testParse("mappings/Luis/luis14/Hydrontology0.0.owl(200912041711)_2_URI.r2o");
	}

	public void testTestcase09() {
		this.testParse("mappings/Luis/luis09/Hydrontology0.0.owl(200912041816).r2o");
	}

	public void testTestcase08() {
		this.testParse("mappings/Luis/luis08/Hydrontology0.0.owl(200912151624).r2o");
	}

	public void testTestcase07() {
		this.testParse("mappings/Luis/luis07/PRUEBA_DMA25y12.r2o.xml");
	}

	public void testTestcase06() {
		this.testParse("mappings/Luis/luis06/Hydrontology0.0.owl(200912021815).r2o");
	}

	public void testTestcase05() {
		this.testParse("mappings/Luis/luis05/Hydrontology0.0.owl(200912082001)_regexp.r2o");
	}
	

	public void testTestcase04() {
		this.testParse("mappings/Luis/luis04/Hydrontology0.0.owl(200912082001)_regexp.r2o");
	}

	public void testTestcase03() {
		this.testParse("mappings/Luis/luis03/Hydrontology0.0.owl(200912082001).r2o");
	}

	public void testTestcase02() {
		this.testParse("mappings/Luis/luis02/Hydrontology0.0.owl(200912082001).r2o");
	}

	
	@Test
	private void testParse(String fileURI) {
		
		try {
			PropertyConfigurator.configure("log4j.properties");
			
			File file = new File(fileURI);
			String fileAbsolutePath = file.getAbsolutePath();


			//parse xml doc input
			Document document = XMLUtility.loadXMLFile(fileAbsolutePath);
			Element docEle = document.getDocumentElement();
			StringWriter writer = new StringWriter();
			XMLUtility.printXMLDocument(document, writer,false, true);
			String inputString = writer.toString();

			
			//parse r2o mapping
			Parser parser = new R2OParser(); 
			IMapping r2oMapping = parser.parse(fileAbsolutePath);
			String outputString = r2oMapping.toString();

			/*
			System.out.println(inputString);
			System.out.println();
			System.out.println(outputString);
			*/

			XMLUnit.setIgnoreComments(Boolean.TRUE);
			XMLUnit.setIgnoreWhitespace(Boolean.TRUE);

			
			Diff diff = new Diff(inputString, outputString);

			assertXMLEqual("not similar", diff, true);
			assertXMLIdentical("not identical", diff, true);
			/*
			if(!diff.similar()) {
				System.out.println("Not similar.");
				assertTrue(false);
				
			}

			if(!diff.identical()) {
				System.out.println("Not identical.");
				assertTrue(false);
			}

			
			System.out.println(diff.similar());
			System.out.println(diff.identical());
			
			//assertXMLEqual(writerString, resultString);
			*/
			
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		
	}

}
