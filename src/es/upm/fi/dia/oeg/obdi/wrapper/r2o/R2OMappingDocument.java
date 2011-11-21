package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractRDB2RDFMapping.MappingType;
import es.upm.fi.dia.oeg.obdi.core.model.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ODatabaseMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;

public class R2OMappingDocument extends AbstractMappingDocument {
	private static Logger logger = Logger.getLogger(R2OMappingDocument.class);

	private String mappingDocumentID;
	private Collection<R2ODatabaseMapping> dbschemaDescs;
	private Collection<R2OPropertyMapping> propertymapDefs;


	public R2OMappingDocument(Collection<AbstractConceptMapping> conceptmapDefs) {
		super();
		this.classMappings = conceptmapDefs;
	}

	public R2OMappingDocument() {
	}


	@Override
	public Collection<IAttributeMapping> getAttributeMappings() {
		Collection<IAttributeMapping> result = new ArrayList<IAttributeMapping>();
		if(this.classMappings != null) {
			for(AbstractConceptMapping conceptmapDef : this.classMappings) {
				result.addAll(((R2OConceptMapping)conceptmapDef).getAttributeMappings());
			}
		}
		return result;
	}

	@Override
	public Collection<IAttributeMapping> getAttributeMappings(
			String domain, String range) {
		Collection<IAttributeMapping> result = new ArrayList<IAttributeMapping>();

		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappingsByConceptName(domain);
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping; 
			result.addAll(r2oConceptMapping.getAttributeMappings());
		}
		return result;

	}

	@Override
	public Collection<AbstractConceptMapping> getConceptMappingsByConceptName(String conceptName) {
		Collection<AbstractConceptMapping> result = new ArrayList<AbstractConceptMapping>();

		if(this.classMappings != null) {
			for(AbstractConceptMapping conceptMapping : this.classMappings) {
				if(conceptMapping.getName().equals(conceptName)) {
					result.add(conceptMapping);
				}
			}
		}
		return result;
	}

	public Collection<AbstractConceptMapping> getR2OConceptMappingsByName(String conceptName) {
		Collection<AbstractConceptMapping> result = new ArrayList<AbstractConceptMapping>();

		for(AbstractConceptMapping conceptMapping : this.classMappings) {
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
		for(AbstractConceptMapping cmd : classMappings) {
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
	public Collection<IRelationMapping> getRelationMappings() {
		Collection<IRelationMapping> result = new ArrayList<IRelationMapping>();
		for(AbstractConceptMapping conceptmapDef : classMappings) {
			result.addAll(((R2OConceptMapping)conceptmapDef).getRelationMappings());
		}
		return result;
	}

	@Override
	public Collection<IRelationMapping> getRelationMappings(
			String domain, String range) {
		Collection<IRelationMapping> result = new ArrayList<IRelationMapping>();

		Collection<AbstractConceptMapping> conceptMappings = this.getConceptMappingsByConceptName(domain);
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
		this.classMappings = new ArrayList<AbstractConceptMapping>();
		for(int i=0; i<nlConceptMapDefs.getLength(); i++) {
			Element conceptMappingElement = (Element) nlConceptMapDefs.item(i); 
			R2OConceptMapping conceptMapping = new R2OConceptMapping(conceptMappingElement);
			this.classMappings.add(conceptMapping);
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

		if(this.classMappings != null) {
			for(AbstractConceptMapping conceptMapping : this.classMappings) {
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











	public Collection<R2ORelationMapping> getR2ORelationMappings() {
		Collection<R2ORelationMapping> result = new ArrayList<R2ORelationMapping>();

		Collection<IRelationMapping> rms = this.getRelationMappings();
		for(IRelationMapping rm : rms) {
			result.add((R2ORelationMapping) rm);
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

	@Override
	public String getMappedConceptURI(String conceptMappingID) {
		for(AbstractConceptMapping conceptMapping : this.classMappings) {
			if(conceptMapping.getId().equals(conceptMappingID)) {
				return conceptMapping.getName();

			}
		}
		return null;
	}



	@Override
	public MappingType getPropertyMappingType(String propertyMappingID) {
		AbstractPropertyMapping pm = this.getPropertyMappingByPropertyMappingID(propertyMappingID);
		if(pm instanceof R2OAttributeMapping) {
			return MappingType.ATTRIBUTE;
		} else if (pm instanceof R2ORelationMapping) {
			return MappingType.RELATION;
		}
		return null;
	}




}
