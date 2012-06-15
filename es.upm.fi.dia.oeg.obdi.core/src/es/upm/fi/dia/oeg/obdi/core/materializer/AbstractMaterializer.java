package es.upm.fi.dia.oeg.obdi.core.materializer;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.obdi.core.Utility;
import es.upm.fi.dia.oeg.obdi.core.engine.Constants;

public abstract class AbstractMaterializer {
	private static Logger logger = Logger.getLogger(AbstractMaterializer.class);
	protected String outputFileName;
	protected Model model;
	protected String rdfLanguage;
	
	public abstract Object createSubject(boolean isBlankNode, String subjectURI);
	public abstract void materializeDataPropertyTriple(String predicateName, Object objectValue, String datatype, String lang, String graph);
	public abstract void materializeObjectPropertyTriple(String predicateName, String rangeURI, boolean isBlankNodeObject, String graph);
	public abstract void materializeRDFTypeTriple(String subjectURI, String conceptName, boolean isBlankNodeSubject, String graph);
	public abstract void materialize() throws IOException;
	
	public static AbstractMaterializer create(String rdfLanguage, String outputFileName, String jenaMode) throws IOException {
		if(rdfLanguage.equalsIgnoreCase(Constants.OUTPUT_FORMAT_NTRIPLE)) {
			Model model = Utility.createJenaModel(jenaMode);
			return new NTripleMaterializer(outputFileName, model);
		} else if(rdfLanguage.equalsIgnoreCase(Constants.OUTPUT_FORMAT_RDFXML)) {
			Model model = Utility.createJenaModel(jenaMode);
			return new RDFXMLMaterializer(outputFileName, model, rdfLanguage);
		} else if(rdfLanguage.equalsIgnoreCase(Constants.OUTPUT_FORMAT_NQUAD)) {
			Model model = Utility.createJenaModel(jenaMode);
			return new RDFXMLMaterializer(outputFileName, model, rdfLanguage);
		} else {
			Model model = Utility.createJenaModel(jenaMode);
			return new NTripleMaterializer(outputFileName, model);
		}
	}
	
	public void setModelPrefixMap(Map<String, String> prefixMap) {
		this.model.setNsPrefixes(prefixMap);
	}
}
