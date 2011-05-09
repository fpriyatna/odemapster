package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ODatabaseMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;

public class R2OMappingDocument implements IMappingDocument {
	private static Logger logger = Logger.getLogger(R2OMappingDocument.class);
	
	private String mappingDocumentID;
	private Collection<R2ODatabaseMapping> dbschemaDescs;
	private Collection<R2OConceptMapping> conceptmapDefs;
	private Collection<R2OPropertyMapping> propertymapDefs;
	
	
	public R2OMappingDocument(Collection<R2OConceptMapping> conceptmapDefs) {
		super();
		this.conceptmapDefs = conceptmapDefs;
	}

	public R2OMappingDocument() {
	}

	public void addConceptMapping(R2OConceptMapping cm) {
		if(this.conceptmapDefs == null) {
			this.conceptmapDefs = new ArrayList<R2OConceptMapping>();
		}
		
		this.conceptmapDefs.add(cm);
	}
	
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
		
		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappingsByConceptURI(domain);
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
	
	public Collection<String> getDistinctConceptMappingsNames() {
		Collection<String> result = new ArrayList<String>();
		
		for(R2OConceptMapping cm : conceptmapDefs) {
			String cmName = cm.getName();
			if(!result.contains(cmName)) {
				result.add(cmName);
			}
		}
		
		return result;
		
	}


	@Override
	public Collection<AbstractConceptMapping> getConceptMappingsByConceptURI(String conceptName) {
		Collection<AbstractConceptMapping> result = new ArrayList<AbstractConceptMapping>();
		
		for(AbstractConceptMapping conceptMapping : this.conceptmapDefs) {
			if(conceptMapping.getName().equals(conceptName)) {
				result.add(conceptMapping);
			}
		}
		return result;
	}

	public Collection<R2OConceptMapping> getR2OConceptMappings(String conceptName) {
		Collection<R2OConceptMapping> result = new ArrayList<R2OConceptMapping>();
		
		for(R2OConceptMapping conceptMapping : this.conceptmapDefs) {
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
		
		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappingsByConceptURI(domain);
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
		
		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappingsByConceptURI(domain);
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping; 
			result.addAll(r2oConceptMapping.getRelationMappings());
		}
		return result;
	}

	@Override
	public void parse(Element r2oElement) throws ParseException {
		//R2OMappingDocument result = new R2OMappingDocument();

		
		NodeList nlDBSchemas = r2oElement.getElementsByTagName(R2OConstants.DBSCHEMA_DESC_TAG);
		this.dbschemaDescs = new ArrayList<R2ODatabaseMapping>();
		for(int i=0; i<nlDBSchemas.getLength(); i++) {
			Element dbschemaDescElement = (Element) nlDBSchemas.item(i);
			
			R2ODatabaseMapping dbm = new R2ODatabaseMapping();
			dbm.parse(dbschemaDescElement);
			this.dbschemaDescs.add(dbm);
		}

		NodeList nlConceptMapDefs = r2oElement.getElementsByTagName(R2OConstants.CONCEPTMAP_DEF_TAG);
		this.conceptmapDefs = new ArrayList<R2OConceptMapping>();
		for(int i=0; i<nlConceptMapDefs.getLength(); i++) {
			Element conceptMappingElement = (Element) nlConceptMapDefs.item(i); 
			R2OConceptMapping conceptMapping = new R2OConceptMapping(conceptMappingElement);
			this.conceptmapDefs.add(conceptMapping);
		}

		this.propertymapDefs = new ArrayList<R2OPropertyMapping>();
		
		Collection<Element> attributeMappingsElements = XMLUtility.getChildElementsByTagName(r2oElement, R2OConstants.ATTRIBUTEMAP_DEF_TAG);
		if(attributeMappingsElements != null) {
			for(Element attributeMappingElement : attributeMappingsElements) {
				R2OAttributeMapping attributeMapping = new R2OAttributeMapping(attributeMappingElement);
				this.propertymapDefs.add(attributeMapping);
			}			
		}



	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.R2O_TAG + ">\n");

		if(this.dbschemaDescs != null) {
			for(R2ODatabaseMapping dbm : this.dbschemaDescs) {
				result.append(dbm.toString() + "\n");
			}			
		}

		if(this.conceptmapDefs != null) {
			for(R2OConceptMapping conceptMapping : this.conceptmapDefs) {
				result.append(conceptMapping.toString() + "\n\n\n");
			}			
		}

		if(this.propertymapDefs != null) {
			for(R2OPropertyMapping propertyMapping : this.propertymapDefs) {
				result.append(propertyMapping.toString() + "\n");
			}			
		}
		


		result.append("</" + R2OConstants.R2O_TAG + ">\n");
		
		return result.toString();
	}

	public Document toXMLDocument() throws Exception {
		Document result = XMLUtility.convertToXMLDocument(this.toString());
		return result;
	}

	@Override
	public R2OConceptMapping getConceptMappingsByMappingId(String mappingId) {
		for(R2OConceptMapping conceptMapping : this.conceptmapDefs) {
			if(conceptMapping.getId().equals(mappingId)) {
				return conceptMapping;
			}
		}
		
		return null;
	}

	
	@Override
	public Collection<AbstractPropertyMapping> getPropertyMappingsByPropertyURI(
			String propertyURI) {
		Collection<AbstractPropertyMapping> result = new ArrayList<AbstractPropertyMapping>();
		for(R2OConceptMapping conceptmapDef : this.conceptmapDefs) {
			Collection<R2OPropertyMapping> pms = conceptmapDef.getDescribedBy();
			for(R2OPropertyMapping pm : pms) {
				if(pm.getName().equals(propertyURI)) {
					result.add(pm);
				}
			}
		}
		return result;
	}
	
	public Collection<R2ORelationMapping> getRelationMappingsByPropertyURI(
			String propertyURI) {
		Collection<R2ORelationMapping> result = new ArrayList<R2ORelationMapping>();
		for(R2OConceptMapping conceptmapDef : this.conceptmapDefs) {
			Collection<R2OPropertyMapping> pms = conceptmapDef.getDescribedBy();
			for(R2OPropertyMapping pm : pms) {
				if(pm.getName().equals(propertyURI) && pm instanceof R2ORelationMapping) {
					result.add((R2ORelationMapping) pm);
				}
			}
		}
		return result;
	}
	
	public Collection<R2ORelationMapping> getR2ORelationMappings() {
		Collection<R2ORelationMapping> result = new ArrayList<R2ORelationMapping>();
		
		Collection<IRelationMapping> rms = this.getRelationMappings();
		for(IRelationMapping rm : rms) {
			result.add((R2ORelationMapping) rm);
		}
		return result;
	}
			
	public Collection<R2ORelationMapping> getRelationMappingsByRelationRange(
			String rangeType) {
		Collection<R2ORelationMapping> result = new ArrayList<R2ORelationMapping>();
		for(R2OConceptMapping conceptmapDef : this.conceptmapDefs) {
			Collection<R2ORelationMapping> rms = conceptmapDef.getRelationMappings();
			for(R2ORelationMapping rm : rms) {
				String rangeConceptID = rm.getToConcept();
				R2OConceptMapping rangeConceptMapping = this.getConceptMappingsByMappingId(rangeConceptID);
				if(rangeType.equals(rangeConceptMapping.getConceptName())) {
					result.add(rm);
				}
			}
		}
		return result;
	}

	@Override
	public String getMappingDocumentID() {
		if(this.mappingDocumentID != null) {
			return this.mappingDocumentID;
		} else {
			return "md" + this.hashCode() + "";
		}
			
	}
	
}
