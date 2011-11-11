package es.upm.fi.dia.oeg.obdi.core.materializer;

import java.io.Writer;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;


public class NTripleMaterializer extends AbstractMaterializer {
	private String currentSubject;
	private static Logger logger = Logger.getLogger(NTripleMaterializer.class);

	public NTripleMaterializer(Writer out) {
		super(out);
	}


	@Override
	public
	void materializeRDFTypeTriple(String subjectURI, String conceptName,
			boolean isBlankNodeSubject, String graph) {
		this.currentSubject = this.createSubject(isBlankNodeSubject, subjectURI);

		String triple = 
			Utility.createQuad(
					this.currentSubject
					, Utility.createURIref(RDF.type.toString())
					, Utility.createURIref(conceptName)
					, Utility.createURIref(graph)
					); 
		try {
			out.append(triple);
		} catch(Exception e) {
			logger.error("unable to serialize triple : " + triple + " because " + e.getMessage());
		}

	}

	@Override
	public void materializeDataPropertyTriple(String predicateName,
			Object propVal, String datatype,
			String lang, String graph) {
		String triplePredicate = Utility.createURIref(predicateName);

		String propValString = propVal.toString();

		if(AbstractRunner.configurationProperties != null) {
			if(AbstractRunner.configurationProperties.isLiteralRemoveStrangeChars()) {
				propValString = Utility.removeStrangeChars(propValString);
			}
		}
		
		String literalString = null;
		if(datatype == null) {
			if(lang == null) {
				literalString = Utility.createLiteral(propValString);
			} else {

				
				literalString = Utility.createLanguageLiteral(propValString, lang);
			}
		} else {
			literalString = Utility.createDataTypeLiteral(propValString, datatype);
		}

		String tripleString = Utility.createQuad(this.currentSubject, triplePredicate, literalString, Utility.createURIref(graph)); 
		try {
			out.append(tripleString);
		} catch(Exception e) {
			logger.error("unable to serialize triple : " + tripleString + " because " + e.getMessage());
		}
	}



	@Override
	public void materializeObjectPropertyTriple(String predicateName,
			String rangeURI, boolean isBlankNodeObject, String graph) {

		String objectString;
		if(isBlankNodeObject) {
			objectString = Utility.createBlankNode(rangeURI);
		} else {
			objectString = Utility.createURIref(rangeURI);
		}

		String triple =
			Utility.createQuad(
					this.currentSubject
					, Utility.createURIref(predicateName)
					, objectString, Utility.createURIref(graph)); 
		try {
			out.append(triple);
		} catch(Exception e) {
			logger.error("unable to serialize triple : " + triple);
		}

	}


	@Override
	public String createSubject(boolean isBlankNode, String subjectURI) {
		if(isBlankNode) {
			this.currentSubject = Utility.createBlankNode(subjectURI);
		} else {
			this.currentSubject = Utility.createURIref(subjectURI);
		}
		return this.currentSubject;
	}
	
	
}
