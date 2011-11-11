package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import com.hp.hpl.jena.query.Query;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public abstract class AbstractQueryTranslator {
	public enum POS {sub, pre, obj}

	protected AbstractMappingDocument mappingDocument;
	protected AbstractAlphaGenerator alphaGenerator;
	protected AbstractBetaGenerator betaGenerator;
	protected NameGenerator nameGenerator;
	protected boolean optimizeTripleBlock = false;
	protected boolean subQueryElimination = false;
	
	


	public AbstractQueryTranslator(AbstractMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
	}

	public void setOptimizeTripleBlock(boolean optimizeTripleBlock) {
		this.optimizeTripleBlock = optimizeTripleBlock;
	}
	
	public void setSubQueryElimination(boolean subQueryElimination) {
		this.subQueryElimination = subQueryElimination;
	}

	public abstract SQLQuery translate(Query sparqlQuery) throws Exception;
	public abstract SQLQuery translate(String queryFilePath) throws Exception;


}
