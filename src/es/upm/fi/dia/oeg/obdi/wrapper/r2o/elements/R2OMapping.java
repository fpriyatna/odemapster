package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.IMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class R2OMapping extends R2OElement implements IMapping {
	List<DatabaseMapping> dbschemaDescs;
	List<ConceptMapping> conceptmapDefs;
	
	
	@Override
	public R2OElement parse(Element r2oElement) throws R2OParserException {
		R2OMapping result = new R2OMapping();

		NodeList nlDBSchemas = r2oElement.getElementsByTagName(Constants.DBSCHEMA_DESC_TAG);
		result.dbschemaDescs = new ArrayList<DatabaseMapping>();
		for(int i=0; i<nlDBSchemas.getLength(); i++) {
			Element dbschemaDescElement = (Element) nlDBSchemas.item(i); 
			DatabaseMapping dbm = (DatabaseMapping) new DatabaseMapping().parse(dbschemaDescElement);
			result.dbschemaDescs.add(dbm);
		}

		NodeList nlConceptMapDefs = r2oElement.getElementsByTagName(Constants.CONCEPTMAP_DEF_TAG);
		result.conceptmapDefs = new ArrayList<ConceptMapping>();
		for(int i=0; i<nlConceptMapDefs.getLength(); i++) {
			Element conceptMappingElement = (Element) nlConceptMapDefs.item(i); 
			ConceptMapping conceptMapping = (ConceptMapping) new ConceptMapping().parse(conceptMappingElement);
			result.conceptmapDefs.add(conceptMapping);
		}

		return result;
	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + Constants.R2O_TAG + ">\n");

		for(DatabaseMapping dbm : this.dbschemaDescs) {
			result.append(dbm.toString() + "\n");
		}

		
		for(ConceptMapping conceptMapping : this.conceptmapDefs) {
			result.append(conceptMapping.toString() + "\n");
		}

		result.append("</" + Constants.R2O_TAG + ">\n");
		
		return result.toString();
	}


	@Override
	public List<String> getMappedConcepts() {
		List<String> result = new ArrayList<String>();
		for(ConceptMapping cmd : conceptmapDefs) {
			result.add(cmd.getName());
		}

		return result;
	}

	
}
