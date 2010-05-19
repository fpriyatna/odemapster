package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.IParseable;
import es.upm.fi.dia.oeg.obdi.wrapper.IPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ODatabaseMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OPropertyMapping;

public class R2OMappingDocument implements IMappingDocument {
	Collection<R2ODatabaseMapping> dbschemaDescs;
	Collection<R2OConceptMapping> conceptmapDefs;
	Collection<R2OPropertyMapping> propertymapDefs;
	
	
	@Override
	public Collection<IAttributeMapping> getAttributeMappings() {
		Collection<IAttributeMapping> result = new ArrayList<IAttributeMapping>();
		for(R2OConceptMapping conceptmapDef : conceptmapDefs) {
			result.addAll(conceptmapDef.getAttributeMappings());
		}
		return result;
	}
	
	@Override
	public Collection<IAttributeMapping> getAttributeMappings(
			String domain, String range) {
		Collection<IAttributeMapping> result = new ArrayList<IAttributeMapping>();
		
		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappings(domain);
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping; 
			result.addAll(r2oConceptMapping.getAttributeMappings());
		}
		return result;
		
	}


	@Override
	public Collection<AbstractConceptMapping> getConceptMappings() {
		Collection<AbstractConceptMapping> result = new ArrayList<AbstractConceptMapping>();
		for(R2OConceptMapping conceptmapDef : conceptmapDefs) {
			result.add(conceptmapDef);
		}
		return result;
	}


	@Override
	public Collection<AbstractConceptMapping> getConceptMappings(String conceptName) {
		Collection<AbstractConceptMapping> result = new ArrayList<AbstractConceptMapping>();
		
		for(AbstractConceptMapping conceptMapping : this.conceptmapDefs) {
			if(conceptMapping.getName().equals(conceptName)) {
				result.add(conceptMapping);
			}
		}
		return result;
	}


	@Override
	public List<String> getMappedAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMappedConcepts() {
		List<String> result = new ArrayList<String>();
		for(AbstractConceptMapping cmd : conceptmapDefs) {
			result.add(cmd.getName());
		}

		return result;
	}

	@Override
	public List<String> getMappedProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMappedRelations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IPropertyMapping> getPropertyMappings() {
		Collection<IPropertyMapping> result = new ArrayList<IPropertyMapping>();
		for(R2OConceptMapping conceptmapDef : conceptmapDefs) {
			result.addAll(conceptmapDef.getDescribedBy());
		}
		return result;
	}

	@Override
	public Collection<IPropertyMapping> getPropertyMappings(
			String domain, String range) {
		Collection<IPropertyMapping> result = new ArrayList<IPropertyMapping>();
		
		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappings(domain);
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping; 
			result.addAll(r2oConceptMapping.getPropertyMappings());
		}
		return result;
	}

	@Override
	public Collection<IRelationMapping> getRelationMappings() {
		Collection<IRelationMapping> result = new ArrayList<IRelationMapping>();
		for(R2OConceptMapping conceptmapDef : conceptmapDefs) {
			result.addAll(conceptmapDef.getRelationMappings());
		}
		return result;
	}

	@Override
	public Collection<IRelationMapping> getRelationMappings(
			String domain, String range) {
		Collection<IRelationMapping> result = new ArrayList<IRelationMapping>();
		
		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappings(domain);
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping; 
			result.addAll(r2oConceptMapping.getRelationMappings());
		}
		return result;
	}

	@Override
	public R2OMappingDocument parse(Element r2oElement) throws ParseException {
		R2OMappingDocument result = new R2OMappingDocument();

		NodeList nlDBSchemas = r2oElement.getElementsByTagName(R2OConstants.DBSCHEMA_DESC_TAG);
		result.dbschemaDescs = new ArrayList<R2ODatabaseMapping>();
		for(int i=0; i<nlDBSchemas.getLength(); i++) {
			Element dbschemaDescElement = (Element) nlDBSchemas.item(i); 
			R2ODatabaseMapping dbm = (R2ODatabaseMapping) new R2ODatabaseMapping().parse(dbschemaDescElement);
			result.dbschemaDescs.add(dbm);
		}

		NodeList nlConceptMapDefs = r2oElement.getElementsByTagName(R2OConstants.CONCEPTMAP_DEF_TAG);
		result.conceptmapDefs = new ArrayList<R2OConceptMapping>();
		for(int i=0; i<nlConceptMapDefs.getLength(); i++) {
			Element conceptMappingElement = (Element) nlConceptMapDefs.item(i); 
			R2OConceptMapping conceptMapping = new R2OConceptMapping().parse(conceptMappingElement);
			result.conceptmapDefs.add(conceptMapping);
		}

		result.propertymapDefs = new ArrayList<R2OPropertyMapping>();
		
		Collection<Element> attributeMappingsElements = XMLUtility.getChildElementsByTagName(r2oElement, R2OConstants.ATTRIBUTEMAP_DEF_TAG);
		for(Element attributeMappingElement : attributeMappingsElements) {
			R2OAttributeMapping attributeMapping = new R2OAttributeMapping().parse(attributeMappingElement);
			result.propertymapDefs.add(attributeMapping);
		}


		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.R2O_TAG + ">\n");

		for(R2ODatabaseMapping dbm : this.dbschemaDescs) {
			result.append(dbm.toString() + "\n");
		}

		for(R2OConceptMapping conceptMapping : this.conceptmapDefs) {
			result.append(conceptMapping.toString() + "\n");
		}

		for(R2OPropertyMapping propertyMapping : this.propertymapDefs) {
			result.append(propertyMapping.toString() + "\n");
		}

		result.append("</" + R2OConstants.R2O_TAG + ">\n");
		
		return result.toString();
	}

	public Document toXMLDocument() throws Exception {
		Document result = XMLUtility.convertToXMLDocument(this.toString());
		return result;
	}


	

	
}
