package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.util.Collection;

import org.apache.log4j.PropertyConfigurator;

import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractParser;
import es.upm.fi.dia.oeg.obdi.wrapper.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;


public class ParserTest {
	public static void main(String args[]) {
		PropertyConfigurator.configure("log4j.properties");
		
		try {
			String r2oMappingDirectory = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
			String r2oMappingFile = r2oMappingDirectory + "NOMGEO_boris_r2o.xml";
			
			AbstractParser parser = new R2OParser(); 
			IMappingDocument mappingDocument = parser.parse(r2oMappingFile);
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
