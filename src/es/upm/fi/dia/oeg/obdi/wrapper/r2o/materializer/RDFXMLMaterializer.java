package es.upm.fi.dia.oeg.obdi.wrapper.r2o.materializer;

import java.io.Writer;
import java.sql.ResultSet;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;


public class RDFXMLMaterializer extends AbstractMaterializer {
	private Model model;
	private Resource currentSubject;
	
	public RDFXMLMaterializer(Writer out, Model model) {
		super(out);
		this.model = model;
	}


	@Override
	public
	void materializeRDFTypeTriple(String subjectURI, String conceptName,
			boolean isBlankNodeSubject) {
		Resource subject;
		if(isBlankNodeSubject) {
			AnonId anonId = new AnonId(subjectURI);
			subject = model.createResource(anonId);		
		} else {
			subject = model.createResource(subjectURI);
		}
		this.currentSubject = subject;
		Resource object = model.getResource(conceptName);
		Statement statement = model.createStatement(subject, RDF.type, object);
		model.add(statement);
	}


	@Override
	public void materializeDataPropertyTriple(String predicateName,
			Object propVal, String datatype,
			String lang) {
		Literal literal;
		
		if(datatype == null) {
			if(lang == null) {
				literal =  model.createTypedLiteral(propVal);
			} else {
				literal =  model.createLiteral(propVal.toString(), lang);
			}
		} else {
			literal =  model.createTypedLiteral(propVal, datatype);
		}
		
		currentSubject.addProperty(model.createProperty(predicateName), literal);
		
	}

	@Override
	public void materializeObjectPropertyTriple(String predicateName,
			String rangeURI, boolean isBlankNodeObject) {
		Resource object;
		Property property = model.createProperty(predicateName);

		if(isBlankNodeObject) {
			AnonId anonId = new AnonId(rangeURI);
			object = model.createResource(anonId);
		} else {
			object = model.createResource(rangeURI);
		}
		
		Statement rdfstmtInstance = model.createStatement(currentSubject,property, object);
		model.add(rdfstmtInstance);		
	}





	
}
