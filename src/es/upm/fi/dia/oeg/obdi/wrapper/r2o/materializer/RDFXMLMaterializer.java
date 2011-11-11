package es.upm.fi.dia.oeg.obdi.core.materializer;

import java.io.Writer;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;


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
			boolean isBlankNodeSubject, String graph) {
		this.currentSubject = (Resource) this.createSubject(isBlankNodeSubject, subjectURI);
		Resource object = model.getResource(conceptName);
		Statement statement = model.createStatement(this.currentSubject, RDF.type, object);
		model.add(statement);
	}


	@Override
	public void materializeDataPropertyTriple(String predicateName,
			Object propVal, String datatype,
			String lang, String graph) {
		Literal literal;
		
		if(datatype == null) {
			String propValString = propVal.toString();
			if(AbstractRunner.configurationProperties.isLiteralRemoveStrangeChars()) {
				propValString = Utility.removeStrangeChars(propValString);
			}
			
			
			if(lang == null) {
				literal =  model.createTypedLiteral(propValString);
			} else {
		
				literal =  model.createLiteral(propValString, lang);
			}
		} else {
			literal =  model.createTypedLiteral(propVal, datatype);
		}
		
		currentSubject.addProperty(model.createProperty(predicateName), literal);
		
	}

	@Override
	public void materializeObjectPropertyTriple(String predicateName,
			String rangeURI, boolean isBlankNodeObject, String graph) {
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


	@Override
	public Resource createSubject(boolean isBlankNode, String subjectURI) {
		if(isBlankNode) {
			AnonId anonId = new AnonId(subjectURI);
			this.currentSubject = model.createResource(anonId);		
		} else {
			this.currentSubject = model.createResource(subjectURI);
		}
		return this.currentSubject;
	}





	
}
