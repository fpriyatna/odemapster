package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.IMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractParser;


public class R2OParser extends AbstractParser {
	private Logger logger = Logger.getLogger(R2OParser.class);
	
	@Override
	public IMappingDocument parse(Object mappingResource) throws Exception {
		long startParsingR2OFile = System.currentTimeMillis();
		
		String fileAbsolutePath = (String) mappingResource;
		logger.info("Parsing r2o mapping file " + fileAbsolutePath);
		//parse the xml file
		Document document = XMLUtility.loadXMLFile(fileAbsolutePath);
		
		//get the root element
		Element r2oElement = document.getDocumentElement();
		
		R2OMappingDocument result = (R2OMappingDocument) new R2OMappingDocument().parse(r2oElement);
		
		long endParsingR2OFile = System.currentTimeMillis();
		long durationParsingR2OFile = (endParsingR2OFile-startParsingR2OFile) / 1000;
		logger.info("Parsing R2O file time was "+(durationParsingR2OFile)+" s.");

		return result;
	}
	
	public void testParseResult(String r2oFilePath, R2OMappingDocument r2oMapping) throws Exception {
		logger.debug("Validating parsed file....");
		XMLUnit.setIgnoreComments(Boolean.TRUE);
		XMLUnit.setIgnoreWhitespace(Boolean.TRUE);
		Document xmlDocument = XMLUtility.loadXMLFile(r2oFilePath);
		Document r2oDocument = r2oMapping.toXMLDocument();
		Diff diff = new Diff(xmlDocument, r2oDocument);
		
		
		
		if(!diff.similar()) {
			String errorMessage = "Parsed R2O mapping is not similar to the original file! " + diff.toString();
			logger.error(errorMessage);
			logger.debug("\n" + XMLUtility.printXMLDocument(xmlDocument, true, true));
			logger.debug("\n" + XMLUtility.printXMLDocument(r2oDocument, true, true));
			throw new Exception(errorMessage);
			//assertXMLEqual("not similar", diff, true);

		}

		if(!diff.identical()) {
			String errorMessage = "Parsed R2O mapping is not identical to the original file!" + diff.toString();
			logger.warn(errorMessage);
			logger.debug("\n" + XMLUtility.printXMLDocument(xmlDocument, true, true));
			logger.debug("\n" + XMLUtility.printXMLDocument(r2oDocument, true, true));
			//throw new Exception(errorMessage);			//assertXMLIdentical("not identical", diff, true);
		}
	}

}
