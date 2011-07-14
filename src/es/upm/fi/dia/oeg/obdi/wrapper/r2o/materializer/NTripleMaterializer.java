package es.upm.fi.dia.oeg.obdi.wrapper.r2o.materializer;

import java.io.Writer;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;


public class NTripleMaterializer extends AbstractMaterializer {
	private String currentSubject;
	private static Logger logger = Logger.getLogger(NTripleMaterializer.class);

	public NTripleMaterializer(Writer out) {
		super(out);
	}


	@Override
	public
	void materializeRDFTypeTriple(String subjectURI, String conceptName,
			boolean isBlankNodeSubject) {
		String subjectString;
		if(isBlankNodeSubject) {
			subjectString = Utility.createBlankNode(subjectURI);
		} else {
			subjectString = Utility.createURIref(subjectURI);
		}
		this.currentSubject = subjectString;

		String triple = 
			Utility.createTriple(
					subjectString
					, Utility.createURIref(RDF.type.toString())
					, Utility.createURIref(conceptName)); 
		try {
			out.append(triple);
		} catch(Exception e) {
			logger.error("unable to serialize triple : " + triple);
		}

	}

	@Override
	public void materializeDataPropertyTriple(String predicateName,
			Object propVal, String datatype,
			String lang) {
		String triplePredicate = Utility.createURIref(predicateName);

		String literalString = null;
		if(datatype == null) {
			if(lang == null) {
				literalString = Utility.createLiteral(propVal.toString());
			} else {
				literalString = Utility.createLanguageLiteral(propVal.toString(), lang);
			}
		} else {
			literalString = Utility.createDataTypeLiteral(propVal.toString(), datatype);
		}

		String tripleString = Utility.createTriple(this.currentSubject, triplePredicate, literalString); 
		try {
			out.append(tripleString);
		} catch(Exception e) {
			logger.error("unable to serialize triple : " + tripleString);
		}
	}



	@Override
	public void materializeObjectPropertyTriple(String predicateName,
			String rangeURI, boolean isBlankNodeObject) {

		String objectString;
		if(isBlankNodeObject) {
			objectString = Utility.createBlankNode(rangeURI);
		} else {
			objectString = Utility.createURIref(rangeURI);
		}

		String triple =
			Utility.createTriple(
					this.currentSubject
					, Utility.createURIref(predicateName)
					, objectString); 
		try {
			out.append(triple);
		} catch(Exception e) {
			logger.error("unable to serialize triple : " + triple);
		}

	}
}
