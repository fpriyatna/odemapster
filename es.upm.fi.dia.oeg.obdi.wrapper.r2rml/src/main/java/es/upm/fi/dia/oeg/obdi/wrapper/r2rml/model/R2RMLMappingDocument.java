package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractRDB2RDFMapping.MappingType;
import es.upm.fi.dia.oeg.obdi.core.model.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLInvalidRefObjectMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLInvalidTriplesMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLJoinConditionException;

public class R2RMLMappingDocument extends AbstractMappingDocument implements R2RMLElement{
	private static Logger logger = Logger.getLogger(R2RMLMappingDocument.class);
	
	
	private Collection<String> prefixes;
	//private Collection<AbstractConceptMapping> triplesMaps;
	private String mappingDocumentPath;
	

	public R2RMLMappingDocument(String mappingDocumentPath) 
			throws R2RMLInvalidTriplesMapException, R2RMLInvalidRefObjectMapException, R2RMLJoinConditionException, R2RMLInvalidTermMapException {
		super();
		this.mappingDocumentPath = mappingDocumentPath;
		String inputFileName = this.getMappingDocumentPath();
		
		 Model model = ModelFactory.createDefaultModel();
		 // use the FileManager to find the input file
		 InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + inputFileName + " not found");
		}
		
		// read the Turtle file
		model.read(in, null, "TURTLE");
		super.setMappingDocumentPrefixMap(model.getNsPrefixMap());

//		//parsing ObjectMap resources
//		ResIterator objectMapResources = model.listResourcesWithProperty(RDF.type, R2RMLConstants.R2RML_OBJECTSMAP_CLASS);
//		if(objectMapResources != null) {
//			this.objectMaps = new HashMap<String, R2RMLObjectMap>();
//			while(objectMapResources.hasNext()) {
//				Resource objectMapResource = objectMapResources.nextResource();
//				String objectMapKey = objectMapResource.getNameSpace() + objectMapResource.getLocalName();
//				R2RMLObjectMap om = new R2RMLObjectMap(objectMapResource);
//				this.objectMaps.put(objectMapKey, om);
//			}
//		}

		//parsing TriplesMap resources
		ResIterator triplesMapResources = model.listResourcesWithProperty(RDF.type, R2RMLConstants.R2RML_TRIPLESMAP_CLASS);
		if(triplesMapResources != null) {
			this.classMappings = new Vector<AbstractConceptMapping>();
			while(triplesMapResources.hasNext()) {
				Resource triplesMapResource = triplesMapResources.nextResource();
				String triplesMapKey = triplesMapResource.getNameSpace() + triplesMapResource.getLocalName();
				R2RMLTriplesMap tm = new R2RMLTriplesMap(triplesMapResource, this);
				tm.setId(triplesMapKey);
				this.classMappings.add(tm);
			}
		}
		
		
		// write it to standard out
		//model.write(System.out);		
	}

	@Override
	public Object accept(R2RMLElementVisitor visitor) throws Exception {
		Object result = visitor.visit(this);
		return result;
	}

	public void parse() throws R2RMLInvalidTriplesMapException, R2RMLInvalidRefObjectMapException, R2RMLJoinConditionException, R2RMLInvalidTermMapException {
		String inputFileName = this.getMappingDocumentPath();
		
		 Model model = ModelFactory.createDefaultModel();
		 // use the FileManager to find the input file
		 InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + inputFileName + " not found");
		}
		
		// read the Turtle file
		model.read(in, null, "TURTLE");

		ResIterator triplesMapResources = model.listResourcesWithProperty(RDF.type, R2RMLConstants.R2RML_TRIPLESMAP_CLASS);
		if(triplesMapResources != null) {
			this.classMappings = new Vector<AbstractConceptMapping>();
			while(triplesMapResources.hasNext()) {
				Resource triplesMapResource = triplesMapResources.nextResource();
				String triplesMapKey = triplesMapResource.getNameSpace() + triplesMapResource.getLocalName();
				R2RMLTriplesMap tm = new R2RMLTriplesMap(triplesMapResource, this);
				this.classMappings.add(tm);
			}

		}
		
		
		// write it to standard out
		//model.write(System.out);		
	}
	
	public String getMappingDocumentPath() {
		return mappingDocumentPath;
	}

	public Collection<AbstractConceptMapping> getTriplesMaps() {
		return classMappings;
	}

	@Override
	public void parse(Element xmlElement) throws ParseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMappingDocumentID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMappedConcepts() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Collection<AbstractConceptMapping> getConceptMappingsByConceptName(
			String conceptURI) {
		Collection<AbstractConceptMapping> result = null;
		
		if(this.classMappings != null && this.classMappings.size() > 0) {
			result = new Vector<AbstractConceptMapping>();
			for(AbstractConceptMapping triplesMap : this.classMappings) {
				R2RMLSubjectMap subjectMap = ((R2RMLTriplesMap)triplesMap).getSubjectMap();
				Collection<String> classURIs = subjectMap.getClassURIs();
				if(classURIs != null && classURIs.contains(conceptURI)) {
					result.add(triplesMap);
				}
			}
		}
		return result;
	}



	@Override
	public List<String> getMappedProperties() {
		logger.warn("TODO: Implement getMappedProperties()");
		// TODO Auto-generated method stub
		return null;
	}









	@Override
	public List<String> getMappedAttributes() {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getMappedAttributes()");
		return null;
	}

	@Override
	public Collection<IAttributeMapping> getAttributeMappings() {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getAttributeMappings()");
		return null;
	}

	@Override
	public Collection<IAttributeMapping> getAttributeMappings(String domain,String range) {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getAttributeMappings(String domain,String range)");
		return null;
	}

	@Override
	public List<String> getMappedRelations() {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getMappedRelations()");
		return null;
	}

	@Override
	public Collection<IRelationMapping> getRelationMappings() {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getRelationMappings()");
		return null;
	}

	@Override
	public Collection<IRelationMapping> getRelationMappings(String domain,String range) {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getRelationMappings(String domain,String range)");
		return null;
	}

	@Override
	public String getMappedConceptURI(String conceptMappingID) {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getMappedConceptURI(String conceptMappingID)");
		return null;
	}



	@Override
	public MappingType getPropertyMappingType(String propertyMappingID) {
		// TODO Auto-generated method stub
		logger.warn("TODO: Implement getMappingType(String propertyMappingID)");
		return null;
	}

	public void setTriplesMaps(Collection<AbstractConceptMapping> triplesMaps) {
		this.classMappings = triplesMaps;
	}









}
