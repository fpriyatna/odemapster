package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants.MappingType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLInvalidRefObjectMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLInvalidTriplesMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLJoinConditionException;

public class R2RMLMappingDocument extends AbstractMappingDocument implements R2RMLElement{
	private Collection<String> prefixes;
	private Map<String, R2RMLTriplesMap> triplesMaps;
	private Map<String, R2RMLObjectMap> objectMaps;
	public Map<String, R2RMLObjectMap> getObjectMaps() {
		return objectMaps;
	}

	private String mappingDocumentPath;
	

	public R2RMLMappingDocument(String mappingDocumentPath) throws R2RMLInvalidTriplesMapException, R2RMLInvalidRefObjectMapException, R2RMLJoinConditionException, R2RMLInvalidTermMapException {
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
			this.triplesMaps = new HashMap<String, R2RMLTriplesMap>();
			while(triplesMapResources.hasNext()) {
				Resource triplesMapResource = triplesMapResources.nextResource();
				String triplesMapKey = triplesMapResource.getNameSpace() + triplesMapResource.getLocalName();
				R2RMLTriplesMap tm = new R2RMLTriplesMap(triplesMapResource, this);
				this.triplesMaps.put(triplesMapKey, tm);
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
			this.triplesMaps = new HashMap<String, R2RMLTriplesMap>();
			while(triplesMapResources.hasNext()) {
				Resource triplesMapResource = triplesMapResources.nextResource();
				String triplesMapKey = triplesMapResource.getNameSpace() + triplesMapResource.getLocalName();
				R2RMLTriplesMap tm = new R2RMLTriplesMap(triplesMapResource, this);
				this.triplesMaps.put(triplesMapKey, tm);
			}

		}
		
		
		// write it to standard out
		//model.write(System.out);		
	}
	
	public String getMappingDocumentPath() {
		return mappingDocumentPath;
	}

	public Map<String, R2RMLTriplesMap> getTriplesMaps() {
		return triplesMaps;
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
	public Collection<AbstractConceptMapping> getConceptMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<AbstractConceptMapping> getConceptMappingsByName(
			String conceptURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConceptMapping getConceptMappingById(String triplesMapID) {
		return this.triplesMaps.get(triplesMapID);
	}

	@Override
	public List<String> getMappedProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IPropertyMapping> getPropertyMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<AbstractPropertyMapping> getPropertyMappingsByPropertyURI(
			String propertyURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IPropertyMapping> getPropertyMappings(String domain,
			String range) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractPropertyMapping getPropertyMappingByPropertyMappingID(
			String propertyMappingID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMappedAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IAttributeMapping> getAttributeMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IAttributeMapping> getAttributeMappings(String domain,
			String range) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IRelationMapping> getRelationMappings(String domain,
			String range) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMappedConceptURI(String conceptMappingID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMappedPropertyURI(String propertyMappingID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MappingType getMappingType(String propertyMappingID) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTriplesMaps(Map<String, R2RMLTriplesMap> triplesMaps) {
		this.triplesMaps = triplesMaps;
	}



}
