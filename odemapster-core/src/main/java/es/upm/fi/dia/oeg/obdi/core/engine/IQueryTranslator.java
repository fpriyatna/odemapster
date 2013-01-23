package es.upm.fi.dia.oeg.obdi.core.engine;

import java.sql.Connection;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;

import es.upm.fi.dia.oeg.obdi.core.exception.QueryTranslatorException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public interface IQueryTranslator {
	Connection getConnection();
	
	void setOptimizer(IQueryTranslationOptimizer optimizer);

	Object translate(Query query) throws Exception;

	void setMappingDocument(AbstractMappingDocument md);

	void setUnfolder(AbstractUnfolder unfolder);

	void setQueryFilePath(String queryFilePath);

	void setIgnoreRDFTypeStatement(boolean b);

	SQLQuery translateFromQueryFile(String queryFilePath) throws Exception;

	IQueryTranslationOptimizer getOptimizer();

	SQLQuery translateFromPropertyFile() throws Exception;

	SQLQuery translateFromString(String queryString) throws Exception ;
	


}
