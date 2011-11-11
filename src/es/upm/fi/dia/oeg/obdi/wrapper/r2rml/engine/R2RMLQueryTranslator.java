package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.R2OQueryTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLQueryTranslator extends AbstractQueryTranslator {
	private static Logger logger = Logger.getLogger(R2RMLQueryTranslator.class);
	
	public R2RMLQueryTranslator(AbstractMappingDocument mappingDocument) {
		super(mappingDocument);
	}

	@Override
	public SQLQuery translate(String queryFilePath) throws Exception {
		//process SPARQL file
		logger.info("Parsing query file : " + queryFilePath);
		Query query = QueryFactory.read(queryFilePath);
		logger.info("query = " + query);
		return this.translate(query);
	}

	@Override
	public SQLQuery translate(Query sparqlQuery) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
