package es.upm.fi.dia.oeg.obdi.core.engine;

import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public interface IQueryTranslator {

	void setOptimizeTripleBlock(boolean optimizeTB);

	void setSubQueryElimination(boolean subQueryElimination);

	Object translate(Query query) throws Exception;

	void setMappingDocument(AbstractMappingDocument md);

	void setUnfolder(AbstractUnfolder unfolder);

	void setQueryFilePath(String queryFilePath);

	void setIgnoreRDFTypeStatement(boolean b);

	SQLQuery translateFromFile(String queryFilePath) throws Exception;

	void setSubqueryAsView(boolean b);

	SQLQuery translateFromFile() throws Exception;
	


}
