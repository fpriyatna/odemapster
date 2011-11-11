

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractParser;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;

public class ParserExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		
		try {
			String r2oMappingDirectory = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
			String r2oMappingFile = r2oMappingDirectory + "NOMGEO_boris_r2o.xml";
			
			AbstractParser parser = new R2OParser(); 
			AbstractMappingDocument mappingDocument = parser.parse(r2oMappingFile);
			Collection<AbstractConceptMapping> mappedConcepts = mappingDocument.getConceptMappings();
			for(AbstractConceptMapping conceptMapping : mappedConcepts) {
				System.out.println("Mapped concept = " + conceptMapping.getName());
				
				Collection<IAttributeMapping> attributeMappings = 
					mappingDocument.getAttributeMappings(conceptMapping.getName(), null);
				for(IAttributeMapping attributeMapping : attributeMappings) {
					System.out.println("Mapped attribute = " + attributeMapping.getAttributeName());
				}

				Collection<IRelationMapping> relationMappings = 
					mappingDocument.getRelationMappings(conceptMapping.getName(), null);
				for(IRelationMapping relationMapping : relationMappings) {
					System.out.println("Mapped relation = " + relationMapping.getRelationName());
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}


	}

}
