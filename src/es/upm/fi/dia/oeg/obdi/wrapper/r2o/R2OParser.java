package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.IMapping;
import es.upm.fi.dia.oeg.obdi.Parser;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements.R2OMapping;


public class R2OParser extends Parser {
	private Logger logger = Logger.getLogger(R2OParser.class);
	
	@Override
	public IMapping parse(Object mappingResource) throws Exception {
		String fileAbsolutePath = (String) mappingResource;
		logger.info("Parsing mapping file " + fileAbsolutePath);
		//parse the xml file
		Document document = XMLUtility.loadXMLFile(fileAbsolutePath);
		
		//get the root element
		Element r2oElement = document.getDocumentElement();
		
		R2OMapping result = (R2OMapping) new R2OMapping().parse(r2oElement);
		
		return result;
	}

}
