package es.upm.fi.dia.oeg.obdi.core.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.IParseable;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractRDB2RDFMapping.MappingType;


public abstract class AbstractMappingDocument implements IParseable {
	private Map<String, String> mappingDocumentPrefixMap;
	private String title;
	private String id;
	private String purpose;
	//private Connection conn;
	
	protected void setMappingDocumentPrefixMap(
			Map<String, String> mappingDocumentPrefixMap) {
		this.mappingDocumentPrefixMap = mappingDocumentPrefixMap;
	}
	
	public Map<String, String> getMappingDocumentPrefixMap() {
		return mappingDocumentPrefixMap;
	}

	protected Collection<AbstractConceptMapping> classMappings;
	
	public abstract void parse(Element xmlElement) throws ParseException;
	public abstract String getMappingDocumentID();
	
	public abstract List<String> getMappedConcepts();
	public abstract Set<AbstractConceptMapping> getConceptMappingsByConceptName(String conceptURI);

	
	public abstract List<String> getMappedProperties();
	
	public abstract List<String> getMappedAttributes();
	public abstract Collection<IAttributeMapping> getAttributeMappings();
	public abstract Collection<IAttributeMapping> getAttributeMappings(String domain, String range);
	
	public abstract List<String> getMappedRelations();
	
	public abstract Collection<IRelationMapping> getRelationMappings(String domain, String range);
	
	public abstract String getMappedConceptURI(String conceptMappingID);
	public abstract MappingType getPropertyMappingType(String propertyMappingID);

	public AbstractConceptMapping getConceptMappingByMappingId(String conceptMappingId) {
		for(AbstractConceptMapping conceptMapping : this.classMappings) {
			String abstractCMId = conceptMapping.getId(); 
			if(abstractCMId.equals(conceptMappingId)) {
				return conceptMapping;
			}
		}

		return null;
	}

	public Set<AbstractConceptMapping> getConceptMappingByPropertyUri(String propertyUri) {
		Set<AbstractConceptMapping> result = new HashSet<AbstractConceptMapping>();
		
		for(AbstractConceptMapping conceptMapping : this.classMappings) {
			Collection<AbstractPropertyMapping> pms = conceptMapping.getPropertyMappings(propertyUri);
			if(pms != null && pms.size() != 0) {
				result.add(conceptMapping);
			}
		}

		return result;
	}

	public Collection<AbstractConceptMapping> getConceptMappings() {
//		Collection<AbstractConceptMapping> result = new ArrayList<AbstractConceptMapping>();
//		if(this.classMappings != null) {
//			for(AbstractConceptMapping conceptmapDef : this.classMappings) {
//				result.add(conceptmapDef);
//			}
//		}
//		return result;
		return this.classMappings;
	}

	public Collection<IRelationMapping> getRelationMappingsByPropertyAndRange(
			String propertyURI, String rangeConceptName) {
		Collection<IRelationMapping> rmFromPropertyURI = this.getRelationMappingsByPropertyURI(propertyURI);
		Collection<IRelationMapping> rmFromRange = this.getRelationMappingsByRangeClassName(rangeConceptName);
		Collection<IRelationMapping> result = new HashSet<IRelationMapping>(rmFromPropertyURI);
		result.retainAll(rmFromRange);
		return result;
	}

	public Collection<String> getDistinctConceptMappingsNames() {
		Collection<String> result = new ArrayList<String>();

		if(this.classMappings != null) {
			for(AbstractConceptMapping cm : this.classMappings) {
				String cmName = cm.getName();
				if(!result.contains(cmName)) {
					result.add(cmName);
				}
			}
		}

		return result;
	}
	
	public void addConceptMapping(AbstractConceptMapping cm) {
		if(this.classMappings == null) {
			this.classMappings = new Vector<AbstractConceptMapping>();
		}

		this.classMappings.add(cm);
	}
	
	public Collection<AbstractPropertyMapping> getPropertyMappings() {
		Collection<AbstractPropertyMapping> result = new ArrayList<AbstractPropertyMapping>();
		for(AbstractConceptMapping classMapping : classMappings) {
			result.addAll(classMapping.getPropertyMappings());
		}
		return result;
	}
	
	public Collection<IPropertyMapping> getPropertyMappings(
			String domain, String range) {
		Collection<IPropertyMapping> result = new ArrayList<IPropertyMapping>();

		Collection<AbstractConceptMapping> conceptMappings = 
				this.getConceptMappingsByConceptName(domain);
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			result.addAll(conceptMapping.getPropertyMappings());
		}
		return result;
	}
	
//	public Collection<AbstractPropertyMapping> getPropertyMappingsByPropertyURI(
//			String propertyURI) {
//		Collection<AbstractPropertyMapping> result = new ArrayList<AbstractPropertyMapping>();
//		for(AbstractConceptMapping conceptmapDef : this.classMappings) {
//			Collection<AbstractPropertyMapping> pms = conceptmapDef.getPropertyMappings();
//			for(AbstractPropertyMapping pm : pms) {
//				if(pm.getName().equals(propertyURI)) {
//					result.add(pm);
//				}
//			}
//		}
//		return result;
//	}
	
	public Collection<AbstractPropertyMapping> getPropertyMappingsByPropertyURI(
			String propertyURI) {
		Collection<AbstractPropertyMapping> result = new Vector<AbstractPropertyMapping>();
		if(this.classMappings != null && this.classMappings.size() > 0) {
			result = new Vector<AbstractPropertyMapping>();
			for(AbstractConceptMapping tripleMap : this.classMappings) {
				result.addAll(tripleMap.getPropertyMappings(propertyURI));
			}
		}
		return result;
	}
	
	public Collection<IRelationMapping> getRelationMappingsByPropertyURI(
			String propertyURI) {
		Collection<IRelationMapping> result = new ArrayList<IRelationMapping>();

		for(AbstractConceptMapping conceptmapDef : this.classMappings) {
			Collection<AbstractPropertyMapping> pms = conceptmapDef.getPropertyMappings();
			for(AbstractPropertyMapping pm : pms) {
				String predicateName = pm.getMappedPredicateName(); 
				if(predicateName.equals(propertyURI) && pm.isObjectPropertyMapping()) {
					result.add((IRelationMapping) pm);
				}
			}
		}
		return result;
	}
	
	public String getMappedPropertyURI(String propertyMappingID) {
		for(AbstractConceptMapping conceptmapDef : this.classMappings) {
			Collection<AbstractPropertyMapping> pms = conceptmapDef.getPropertyMappings();
			for(AbstractPropertyMapping pm : pms) {
				if(pm.getId().equals(propertyMappingID)) {
					return pm.getName();
				}
			}
		}
		return null;
	}
	
	public AbstractPropertyMapping getPropertyMappingByPropertyMappingID(
			String propertyMappingID) {
		for(AbstractConceptMapping conceptmapDef : this.classMappings) {
			Collection<AbstractPropertyMapping> pms = conceptmapDef.getPropertyMappings();
			for(AbstractPropertyMapping pm : pms) {
				if(pm.getId().equals(propertyMappingID)) {
					return pm;
				}
			}
		}
		return null;
	}
	
//	protected Collection<IRelationMapping> getRelationMappings() {
//		Collection<IRelationMapping> result = new Vector<IRelationMapping>();
//		Collection<AbstractPropertyMapping> propertyMappings = this.getPropertyMappings();
//		if(propertyMappings != null) {
//			for(AbstractPropertyMapping propertyMapping : propertyMappings) {
//				MappingType propertyMappingType = propertyMapping.getPropertyMappingType();
//				if(propertyMappingType == MappingType.RELATION) {
//					result.add((IRelationMapping) propertyMapping);
//				}
//			}
//		}
//		return result;
//	}
	
	protected Collection<IRelationMapping> getRelationMappings() {
		Collection<IRelationMapping> result = new Vector<IRelationMapping>();
		if(this.classMappings != null) {
			for(AbstractConceptMapping cm : this.classMappings) {
				result.addAll(cm.getRelationMappings());
			}
		}
		return result;
	}
	
	public Collection<IRelationMapping> getRelationMappingsByRangeClassName(
			String rangeClassName) {
		Collection<IRelationMapping> result = new ArrayList<IRelationMapping>();
		for(AbstractConceptMapping classMapping : this.classMappings) {
			Collection<IRelationMapping> rms = classMapping.getRelationMappings();
			for(IRelationMapping rm : rms) {
				String rangeConceptID = rm.getRangeClassMapping();
				if(rangeConceptID != null) {
					AbstractConceptMapping rangeCM = (AbstractConceptMapping) this.getConceptMappingByMappingId(rangeConceptID);
					if(rangeClassName.equals(rangeCM.getConceptName())) {
						result.add(rm);
					}
					
				}
			}
		}
		return result;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public Collection<AbstractConceptMapping> getClassMappings() {
		return classMappings;
	}

	public void setClassMappings(Collection<AbstractConceptMapping> classMappings) {
		this.classMappings = classMappings;
	}

//	public Connection getConn() {
//		return conn;
//	}
//
//	protected void setConn(Connection conn) {
//		this.conn = conn;
//	}
	
}
