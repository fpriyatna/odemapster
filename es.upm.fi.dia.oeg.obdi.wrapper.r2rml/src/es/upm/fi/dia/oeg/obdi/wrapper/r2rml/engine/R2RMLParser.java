package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractParser;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLParser extends AbstractParser {

	@Override
	public AbstractMappingDocument parse(Object mappingResource) throws Exception {
		String mappingDocumentPath = (String) mappingResource;
		R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentPath); 
		return md;
	}

}
