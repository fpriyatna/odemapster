package es.upm.fi.dia.oeg.obdi.core.materializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.log4j.Logger;

public abstract class AbstractMaterializer {
	private static Logger logger = Logger.getLogger(AbstractMaterializer.class);
	protected String outputFileName;
	
	

	public AbstractMaterializer() {
		super();
	}
	
	public abstract Object createSubject(boolean isBlankNode, String subjectURI);
	public abstract void materializeDataPropertyTriple(String predicateName, Object objectValue, String datatype, String lang, String graph);
	public abstract void materializeObjectPropertyTriple(String predicateName, String rangeURI, boolean isBlankNodeObject, String graph);
	public abstract void materializeRDFTypeTriple(String subjectURI, String conceptName, boolean isBlankNodeSubject, String graph);
	public abstract void materialize() throws IOException;
}
